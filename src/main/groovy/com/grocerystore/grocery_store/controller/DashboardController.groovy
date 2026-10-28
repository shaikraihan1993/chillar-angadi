package com.grocerystore.grocery_store.controller

import com.grocerystore.grocery_store.model.Order
import com.grocerystore.grocery_store.model.enums.OrderStatus
import com.grocerystore.grocery_store.repository.OrderRepository
import com.grocerystore.grocery_store.repository.ProductRepository
import com.grocerystore.grocery_store.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Admin dashboard numbers. Everything is computed from real orders, products and users.
 * Cancelled orders never count towards revenue or sales.
 */
@RestController
@RequestMapping("/api/v1/dashboard")
class DashboardController {

    static final ZoneId STORE_ZONE = ZoneId.of('Asia/Kolkata')
    static final int LOW_STOCK = 15

    private final OrderRepository orderRepository
    private final ProductRepository productRepository
    private final UserRepository userRepository

    DashboardController(OrderRepository orderRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository
        this.productRepository = productRepository
        this.userRepository = userRepository
    }

    @GetMapping("/stats")
    @Transactional(readOnly = true)
    ResponseEntity<Map<String, Object>> getStats() {
        def orders = orderRepository.findAll()
        def products = productRepository.findAll()
        def now = ZonedDateTime.now(STORE_ZONE)
        def monthStart = now.toLocalDate().withDayOfMonth(1).atStartOfDay(STORE_ZONE).toInstant()
        def lastMonthStart = now.toLocalDate().withDayOfMonth(1).minusMonths(1).atStartOfDay(STORE_ZONE).toInstant()
        long weekAgo = now.minusDays(7).toInstant().toEpochMilli()

        ResponseEntity.ok([
            totalOrders     : orders.size(),
            pendingOrders   : orders.count { it.status == OrderStatus.PENDING },
            ordersThisMonth : orders.count { it.orderTime >= monthStart },
            ordersLastMonth : orders.count { it.orderTime >= lastMonthStart && it.orderTime < monthStart },
            revenue         : revenueOf(orders),
            totalProducts   : products.size(),
            outOfStock      : products.count { (it.quantity ?: 0) <= 0 },
            lowStock        : products.count { (it.quantity ?: 0) > 0 && it.quantity <= LOW_STOCK },
            totalUsers      : userRepository.count(),
            newUsersThisWeek: userRepository.findAll().count { (it.createdAt ?: 0L) >= weekAgo }
        ] as Map<String, Object>)
    }

    /**
     * @param range today | week | month | year | all
     */
    @GetMapping("/analytics")
    @Transactional(readOnly = true)
    ResponseEntity<?> analytics(@RequestParam(defaultValue = 'month') String range) {
        if (!(range in ['today', 'week', 'month', 'year', 'all'])) {
            return ResponseEntity.badRequest().body([message: 'range must be today, week, month, year or all'])
        }
        def now = ZonedDateTime.now(STORE_ZONE)
        List<Order> allOrders = orderRepository.findAll()
        def products = productRepository.findAll()
        Map productsById = products.collectEntries { [(it.id): it] }

        // Current window and the window just before it (for "vs previous period")
        Instant end = now.toInstant()
        Instant start
        Instant prevStart = null
        switch (range) {
            case 'today':
                start = now.toLocalDate().atStartOfDay(STORE_ZONE).toInstant()
                prevStart = start.minus(1, ChronoUnit.DAYS)
                break
            case 'week':
                start = now.minusDays(6).toLocalDate().atStartOfDay(STORE_ZONE).toInstant()
                prevStart = start.minus(7, ChronoUnit.DAYS)
                break
            case 'month':
                start = now.minusDays(29).toLocalDate().atStartOfDay(STORE_ZONE).toInstant()
                prevStart = start.minus(30, ChronoUnit.DAYS)
                break
            case 'year':
                start = now.minusMonths(11).toLocalDate().withDayOfMonth(1).atStartOfDay(STORE_ZONE).toInstant()
                prevStart = start.atZone(STORE_ZONE).minusMonths(12).toInstant()
                break
            default:
                start = allOrders ? allOrders*.orderTime.min() : end
        }
        List<Order> inRange = allOrders.findAll { it.orderTime >= start && it.orderTime <= end }
        List<Order> inPrev = prevStart ? allOrders.findAll { it.orderTime >= prevStart && it.orderTime < start } : []
        List<Order> counted = inRange.findAll { it.status != OrderStatus.CANCELLED }

        // Customers: the account when there is one, otherwise the email/phone used at checkout
        Map firstOrderByCustomer = allOrders.groupBy { customerKey(it) }.collectEntries { k, v -> [(k): v*.orderTime.min()] }
        def newCustomers = inRange.collect { customerKey(it) }.unique().count { firstOrderByCustomer[it] >= start }
        def newCustomersPrev = prevStart
            ? inPrev.collect { customerKey(it) }.unique().count { firstOrderByCustomer[it] >= prevStart && firstOrderByCustomer[it] < start }
            : null

        // Sales by category and top products come from the order lines
        def lines = counted.collectMany { o -> o.items.collect { [order: o, item: it] } }
        def byCategory = lines.groupBy { productsById[it.item.productId]?.category?.displayName ?: 'Other' }
            .collect { cat, ls -> [category: cat, revenue: ls.sum { it.item.lineTotal ?: 0 }, units: ls.sum { it.item.quantity ?: 0 }] }
            .sort { -(it.revenue as BigDecimal) }
        def topProducts = lines.groupBy { it.item.productId }
            .collect { pid, ls ->
                def p = productsById[pid]
                [productId: pid, name: p?.name ?: ls[0].item.productName, category: p?.category?.displayName ?: 'Other',
                 imageId  : p?.image?.id, price: p?.price ?: ls[0].item.unitPrice,
                 units    : ls.sum { it.item.quantity ?: 0 }, revenue: ls.sum { it.item.lineTotal ?: 0 }]
            }
            .sort { -(it.units as int) }
            .take(5)

        def recent = inRange.sort(false) { -it.orderTime.toEpochMilli() }.take(6).collect {
            [id: it.id, customerName: it.customerName, orderTime: it.orderTime, totalAmount: it.totalAmount, status: it.status]
        }

        BigDecimal revenue = revenueOf(inRange)
        ResponseEntity.ok([
            range           : range,
            from            : start,
            to              : end,
            revenue         : revenue,
            revenuePrev     : prevStart ? revenueOf(inPrev) : null,
            orders          : inRange.size(),
            ordersPrev      : prevStart ? inPrev.size() : null,
            newCustomers    : newCustomers,
            newCustomersPrev: newCustomersPrev,
            averageOrder    : counted ? revenue.divide(counted.size() as BigDecimal, 2, RoundingMode.HALF_UP) : 0,
            productsInStock : products.count { (it.quantity ?: 0) > 0 },
            lowStock        : products.count { (it.quantity ?: 0) > 0 && it.quantity <= LOW_STOCK },
            outOfStock      : products.count { (it.quantity ?: 0) <= 0 },
            series          : buildSeries(range, start, end, counted),
            byCategory      : byCategory,
            topProducts     : topProducts,
            recentOrders    : recent,
            statusBreakdown : inRange.countBy { it.status?.name() ?: 'UNKNOWN' }
        ])
    }

    /** Every account with how much it has ordered (orders linked to the account only). */
    @GetMapping("/users")
    @Transactional(readOnly = true)
    ResponseEntity<?> users() {
        Map ordersByUser = orderRepository.findAll().findAll { userIdOf(it) }.groupBy { userIdOf(it) }
        def users = userRepository.findAll().sort { -(it.createdAt ?: 0L) }.collect { u ->
            List<Order> mine = ordersByUser[u.id] ?: []
            [
                id         : u.id,
                username   : u.username,
                email      : u.email,
                firstName  : u.firstName,
                lastName   : u.lastName,
                phoneNumber: u.phoneNumber,
                address    : u.address,
                role       : u.role?.name?.name() ?: 'ROLE_CUSTOMER',
                memberSince: u.createdAt,
                orders     : mine.size(),
                spent      : revenueOf(mine),
                lastOrder  : mine ? mine*.orderTime.max() : null
            ]
        }
        ResponseEntity.ok([users    : users, total: users.size(),
                           admins   : users.count { it.role == 'ROLE_ADMIN' },
                           customers: users.count { it.role != 'ROLE_ADMIN' }])
    }

    private static BigDecimal revenueOf(Collection<Order> orders) {
        (orders.findAll { it.status != OrderStatus.CANCELLED }.sum { it.totalAmount ?: 0 } ?: 0) as BigDecimal
    }

    private static Long userIdOf(Order order) {
        try {
            Long id = order.user?.id   // reads the proxy id without loading the user
            id && id > 0 ? id : null
        } catch (Exception ignored) {
            null
        }
    }

    private static String customerKey(Order order) {
        Long uid = userIdOf(order)
        uid ? "user:${uid}".toString() : (order.email?.trim()?.toLowerCase() ?: order.phoneNumber?.trim() ?: "order:${order.id}".toString())
    }

    /** Revenue and order count per hour (today), day (week/month) or month (year/all). */
    private static List<Map> buildSeries(String range, Instant start, Instant end, List<Order> orders) {
        def zStart = start.atZone(STORE_ZONE)
        def zEnd = end.atZone(STORE_ZONE)
        if (range == 'today') {
            Map grouped = orders.groupBy { it.orderTime.atZone(STORE_ZONE).hour }
            return (0..zEnd.hour).collect { h -> point(String.format('%02d:00', h), grouped[h] ?: []) }
        }
        List<Map> out = []
        if (range in ['week', 'month']) {
            def fmt = DateTimeFormatter.ofPattern(range == 'week' ? 'EEE d' : 'd MMM')
            Map grouped = orders.groupBy { it.orderTime.atZone(STORE_ZONE).toLocalDate() }
            LocalDate d = zStart.toLocalDate()
            while (!d.isAfter(zEnd.toLocalDate())) { out << point(d.format(fmt), grouped[d] ?: []); d = d.plusDays(1) }
            return out
        }
        def fmt = DateTimeFormatter.ofPattern('MMM yy')
        Map grouped = orders.groupBy { it.orderTime.atZone(STORE_ZONE).toLocalDate().withDayOfMonth(1) }
        LocalDate m = zStart.toLocalDate().withDayOfMonth(1)
        while (!m.isAfter(zEnd.toLocalDate())) { out << point(m.format(fmt), grouped[m] ?: []); m = m.plusMonths(1) }
        out
    }

    private static Map point(String label, List<Order> orders) {
        [label: label, revenue: revenueOf(orders), orders: orders.size()]
    }
}
