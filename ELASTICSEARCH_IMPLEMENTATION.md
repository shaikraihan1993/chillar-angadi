# Elasticsearch Implementation Guide

This guide covers implementing Elasticsearch for the Grocery Store application to enable fast, full-text search across products, orders, and other entities.

---

## Table of Contents

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Setup Elasticsearch](#setup-elasticsearch)
4. [Add Dependencies](#add-dependencies)
5. [Configuration](#configuration)
6. [Implementation Steps](#implementation-steps)
7. [Index Mappings](#index-mappings)
8. [Service Layer Changes](#service-layer-changes)
9. [Controller Changes](#controller-changes)
10. [Data Synchronization](#data-synchronization)
11. [Testing](#testing)
12. [Production Considerations](#production-considerations)

---

## Overview

### Why Elasticsearch?

- **Fast full-text search** - Search products by name, description, brand with relevance scoring
- **Autocomplete** - Suggest products as users type
- **Faceted search** - Filter by category, brand, price range with counts
- **Fuzzy matching** - Handle typos and misspellings
- **Scalability** - Handle millions of products efficiently

### Use Cases for Grocery Store

- Product search with autocomplete
- Filter products by multiple criteria
- Search orders by customer name, phone, address
- Analytics and aggregations (best sellers, sales by category)

---

## Prerequisites

- Java 17+
- Docker (recommended for local development)
- Elasticsearch 8.x
- Spring Boot 3.x

---

## Setup Elasticsearch

### Option 1: Docker (Recommended)

Add to `docker-compose.yml`:

```yaml
version: '3.8'

services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    container_name: grocery-elasticsearch
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    ports:
      - "9200:9200"
      - "9300:9300"
    volumes:
      - elasticsearch-data:/usr/share/elasticsearch/data
    networks:
      - grocery-network

  kibana:
    image: docker.elastic.co/kibana/kibana:8.11.0
    container_name: grocery-kibana
    environment:
      - ELASTICSEARCH_HOSTS=http://elasticsearch:9200
    ports:
      - "5601:5601"
    depends_on:
      - elasticsearch
    networks:
      - grocery-network

volumes:
  elasticsearch-data:

networks:
  grocery-network:
    driver: bridge
```

Start with:
```bash
docker-compose up -d elasticsearch kibana
```

### Option 2: Local Installation

Download from: https://www.elastic.co/downloads/elasticsearch

```bash
# macOS with Homebrew
brew tap elastic/tap
brew install elastic/tap/elasticsearch-full

# Start Elasticsearch
elasticsearch
```

### Verify Installation

```bash
curl http://localhost:9200
```

Expected response:
```json
{
  "name": "node-1",
  "cluster_name": "elasticsearch",
  "version": {
    "number": "8.11.0"
  }
}
```

---

## Add Dependencies

Add to `build.gradle`:

```groovy
dependencies {
    // Spring Data Elasticsearch
    implementation 'org.springframework.boot:spring-boot-starter-data-elasticsearch'

    // Elasticsearch Java Client (for advanced queries)
    implementation 'co.elastic.clients:elasticsearch-java:8.11.0'

    // Jackson for JSON processing (required by ES client)
    implementation 'com.fasterxml.jackson.core:jackson-databind'
}
```

---

## Configuration

### application.properties

```properties
# Elasticsearch Configuration
spring.elasticsearch.uris=http://localhost:9200
spring.elasticsearch.connection-timeout=5s
spring.elasticsearch.socket-timeout=30s

# Optional: Authentication (for production)
# spring.elasticsearch.username=elastic
# spring.elasticsearch.password=changeme

# Index prefix (useful for multi-tenant or environments)
app.elasticsearch.index-prefix=grocery_store

# Auto-create indices on startup
app.elasticsearch.auto-create-index=true

# Batch size for reindexing
app.elasticsearch.batch-size=100
```

### application-docker.properties

```properties
spring.elasticsearch.uris=http://elasticsearch:9200
```

---

## Implementation Steps

### Step 1: Create Elasticsearch Configuration

Create `src/main/groovy/com/grocerystore/grocery_store/config/ElasticsearchConfig.groovy`:

```groovy
package com.grocerystore.grocery_store.config

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.ElasticsearchTransport
import co.elastic.clients.transport.rest_client.RestClientTransport
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.grocerystore.grocery_store.repository.elasticsearch")
class ElasticsearchConfig {

    @Value('${spring.elasticsearch.uris:http://localhost:9200}')
    String elasticsearchUri

    @Bean
    RestClient restClient() {
        def uri = URI.create(elasticsearchUri)
        RestClient.builder(new HttpHost(uri.host, uri.port, uri.scheme)).build()
    }

    @Bean
    ElasticsearchTransport elasticsearchTransport(RestClient restClient) {
        new RestClientTransport(restClient, new JacksonJsonpMapper())
    }

    @Bean
    ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        new ElasticsearchClient(transport)
    }
}
```

### Step 2: Create Elasticsearch Document Models

Create `src/main/groovy/com/grocerystore/grocery_store/document/ProductDocument.groovy`:

```groovy
package com.grocerystore.grocery_store.document

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.*

@Document(indexName = "#{@environment.getProperty('app.elasticsearch.index-prefix', 'grocery_store')}_products")
@Setting(settingPath = "elasticsearch/product-settings.json")
@Mapping(mappingPath = "elasticsearch/product-mapping.json")
class ProductDocument {

    @Id
    String id

    @Field(type = FieldType.Text, analyzer = "autocomplete", searchAnalyzer = "standard")
    String name

    @Field(type = FieldType.Text, analyzer = "standard")
    String description

    @Field(type = FieldType.Keyword)
    String brand

    @Field(type = FieldType.Keyword)
    String manufacturer

    @Field(type = FieldType.Keyword)
    String category

    @Field(type = FieldType.Keyword)
    String subCategory

    @Field(type = FieldType.Double)
    BigDecimal price

    @Field(type = FieldType.Integer)
    Integer quantity

    @Field(type = FieldType.Keyword)
    String unit

    @Field(type = FieldType.Boolean)
    Boolean isActive

    @Field(type = FieldType.Long)
    Long imageId

    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    Long createdAt

    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    Long updatedAt

    // Completion field for autocomplete suggestions
    @CompletionField(analyzer = "simple", searchAnalyzer = "simple", maxInputLength = 100)
    Completion suggest

    static ProductDocument fromProduct(product) {
        new ProductDocument(
            id: product.id?.toString(),
            name: product.name,
            description: product.description,
            brand: product.brand,
            manufacturer: product.manufacturer,
            category: product.category?.name(),
            subCategory: product.subCategory?.name(),
            price: product.price,
            quantity: product.quantity,
            unit: product.unit?.name(),
            isActive: product.isActive,
            imageId: product.imageId,
            createdAt: System.currentTimeMillis(),
            updatedAt: System.currentTimeMillis(),
            suggest: new Completion([product.name, product.brand].findAll { it })
        )
    }
}

class Completion {
    List<String> input

    Completion(List<String> input) {
        this.input = input
    }
}
```

Create `src/main/groovy/com/grocerystore/grocery_store/document/OrderDocument.groovy`:

```groovy
package com.grocerystore.grocery_store.document

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.*

@Document(indexName = "#{@environment.getProperty('app.elasticsearch.index-prefix', 'grocery_store')}_orders")
class OrderDocument {

    @Id
    String id

    @Field(type = FieldType.Text, analyzer = "standard")
    String customerName

    @Field(type = FieldType.Keyword)
    String phoneNumber

    @Field(type = FieldType.Text, analyzer = "standard")
    String address

    @Field(type = FieldType.Keyword)
    String paymentMethod

    @Field(type = FieldType.Keyword)
    String status

    @Field(type = FieldType.Double)
    BigDecimal totalAmount

    @Field(type = FieldType.Text)
    String notes

    @Field(type = FieldType.Nested)
    List<OrderItemDocument> items

    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    Long orderTime

    static OrderDocument fromOrder(order) {
        new OrderDocument(
            id: order.id?.toString(),
            customerName: order.customerName,
            phoneNumber: order.phoneNumber,
            address: order.address,
            paymentMethod: order.paymentMethod,
            status: order.status,
            totalAmount: order.totalAmount,
            notes: order.notes,
            orderTime: order.orderTime?.time,
            items: order.items?.collect { item ->
                new OrderItemDocument(
                    productId: item.productId,
                    productName: item.productName,
                    unitPrice: item.unitPrice,
                    quantity: item.quantity,
                    lineTotal: item.lineTotal
                )
            }
        )
    }
}

class OrderItemDocument {
    Long productId
    String productName
    BigDecimal unitPrice
    Integer quantity
    BigDecimal lineTotal
}
```

### Step 3: Create Index Settings and Mappings

Create `src/main/resources/elasticsearch/product-settings.json`:

```json
{
  "index": {
    "number_of_shards": 1,
    "number_of_replicas": 0,
    "max_ngram_diff": 10
  },
  "analysis": {
    "analyzer": {
      "autocomplete": {
        "type": "custom",
        "tokenizer": "autocomplete_tokenizer",
        "filter": ["lowercase", "asciifolding"]
      },
      "autocomplete_search": {
        "type": "custom",
        "tokenizer": "standard",
        "filter": ["lowercase", "asciifolding"]
      }
    },
    "tokenizer": {
      "autocomplete_tokenizer": {
        "type": "edge_ngram",
        "min_gram": 2,
        "max_gram": 10,
        "token_chars": ["letter", "digit"]
      }
    }
  }
}
```

Create `src/main/resources/elasticsearch/product-mapping.json`:

```json
{
  "properties": {
    "name": {
      "type": "text",
      "analyzer": "autocomplete",
      "search_analyzer": "autocomplete_search",
      "fields": {
        "keyword": {
          "type": "keyword",
          "ignore_above": 256
        }
      }
    },
    "description": {
      "type": "text",
      "analyzer": "standard"
    },
    "brand": {
      "type": "keyword"
    },
    "manufacturer": {
      "type": "keyword"
    },
    "category": {
      "type": "keyword"
    },
    "subCategory": {
      "type": "keyword"
    },
    "price": {
      "type": "scaled_float",
      "scaling_factor": 100
    },
    "quantity": {
      "type": "integer"
    },
    "unit": {
      "type": "keyword"
    },
    "isActive": {
      "type": "boolean"
    },
    "imageId": {
      "type": "long"
    },
    "suggest": {
      "type": "completion",
      "analyzer": "simple",
      "preserve_separators": true,
      "preserve_position_increments": true,
      "max_input_length": 50
    }
  }
}
```

### Step 4: Create Elasticsearch Repositories

Create `src/main/groovy/com/grocerystore/grocery_store/repository/elasticsearch/ProductSearchRepository.groovy`:

```groovy
package com.grocerystore.grocery_store.repository.elasticsearch

import com.grocerystore.grocery_store.document.ProductDocument
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.annotations.Query
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, String> {

    // Find by name (uses autocomplete analyzer)
    Page<ProductDocument> findByNameContaining(String name, Pageable pageable)

    // Find by category
    Page<ProductDocument> findByCategory(String category, Pageable pageable)

    // Find by brand
    Page<ProductDocument> findByBrand(String brand, Pageable pageable)

    // Find in stock products
    Page<ProductDocument> findByQuantityGreaterThan(Integer quantity, Pageable pageable)

    // Find by category and in stock
    Page<ProductDocument> findByCategoryAndQuantityGreaterThan(String category, Integer quantity, Pageable pageable)

    // Custom query: Full-text search across multiple fields
    @Query('''
    {
      "bool": {
        "should": [
          { "match": { "name": { "query": "?0", "boost": 3 } } },
          { "match": { "description": { "query": "?0", "boost": 1 } } },
          { "match": { "brand": { "query": "?0", "boost": 2 } } }
        ],
        "minimum_should_match": 1
      }
    }
    ''')
    Page<ProductDocument> searchByKeyword(String keyword, Pageable pageable)

    // Find by multiple categories
    Page<ProductDocument> findByCategoryIn(List<String> categories, Pageable pageable)

    // Find by price range
    Page<ProductDocument> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable)
}
```

Create `src/main/groovy/com/grocerystore/grocery_store/repository/elasticsearch/OrderSearchRepository.groovy`:

```groovy
package com.grocerystore.grocery_store.repository.elasticsearch

import com.grocerystore.grocery_store.document.OrderDocument
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface OrderSearchRepository extends ElasticsearchRepository<OrderDocument, String> {

    Page<OrderDocument> findByCustomerNameContaining(String customerName, Pageable pageable)

    Page<OrderDocument> findByPhoneNumber(String phoneNumber, Pageable pageable)

    Page<OrderDocument> findByStatus(String status, Pageable pageable)

    Page<OrderDocument> findByOrderTimeBetween(Long startTime, Long endTime, Pageable pageable)
}
```

### Step 5: Create Search Service

Create `src/main/groovy/com/grocerystore/grocery_store/service/ProductSearchService.groovy`:

```groovy
package com.grocerystore.grocery_store.service

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.query_dsl.*
import co.elastic.clients.elasticsearch.core.SearchRequest
import co.elastic.clients.elasticsearch.core.search.CompletionSuggestOption
import com.grocerystore.grocery_store.document.ProductDocument
import com.grocerystore.grocery_store.model.Product
import com.grocerystore.grocery_store.repository.ProductRepository
import com.grocerystore.grocery_store.repository.elasticsearch.ProductSearchRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.core.query.NativeQuery
import org.springframework.data.elasticsearch.core.query.Query
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductSearchService {

    private final ProductSearchRepository productSearchRepository
    private final ProductRepository productRepository
    private final ElasticsearchClient elasticsearchClient
    private final ElasticsearchOperations elasticsearchOperations

    @Value('${app.elasticsearch.index-prefix:grocery_store}')
    String indexPrefix

    ProductSearchService(
        ProductSearchRepository productSearchRepository,
        ProductRepository productRepository,
        ElasticsearchClient elasticsearchClient,
        ElasticsearchOperations elasticsearchOperations
    ) {
        this.productSearchRepository = productSearchRepository
        this.productRepository = productRepository
        this.elasticsearchClient = elasticsearchClient
        this.elasticsearchOperations = elasticsearchOperations
    }

    /**
     * Search products with filters
     */
    Page<ProductDocument> search(
        String keyword,
        List<String> categories,
        List<String> brands,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean inStockOnly,
        Pageable pageable
    ) {
        def boolQuery = new BoolQuery.Builder()

        // Keyword search across multiple fields
        if (keyword) {
            boolQuery.must(Query.of { q ->
                q.multiMatch { mm ->
                    mm.query(keyword)
                        .fields("name^3", "description", "brand^2")
                        .fuzziness("AUTO")
                        .type(TextQueryType.BestFields)
                }
            })
        }

        // Category filter
        if (categories && !categories.isEmpty()) {
            boolQuery.filter(Query.of { q ->
                q.terms { t ->
                    t.field("category")
                        .terms { tv -> tv.value(categories.collect { TermsQueryField.of { f -> f.stringValue(it) } }) }
                }
            })
        }

        // Brand filter
        if (brands && !brands.isEmpty()) {
            boolQuery.filter(Query.of { q ->
                q.terms { t ->
                    t.field("brand")
                        .terms { tv -> tv.value(brands.collect { TermsQueryField.of { f -> f.stringValue(it) } }) }
                }
            })
        }

        // Price range filter
        if (minPrice != null || maxPrice != null) {
            boolQuery.filter(Query.of { q ->
                q.range { r ->
                    def rangeBuilder = r.field("price")
                    if (minPrice != null) rangeBuilder.gte(minPrice.toString())
                    if (maxPrice != null) rangeBuilder.lte(maxPrice.toString())
                    rangeBuilder
                }
            })
        }

        // In stock filter
        if (inStockOnly) {
            boolQuery.filter(Query.of { q ->
                q.range { r ->
                    r.field("quantity").gt("0")
                }
            })
        }

        // Active products only
        boolQuery.filter(Query.of { q ->
            q.term { t ->
                t.field("isActive").value(true)
            }
        })

        def query = NativeQuery.builder()
            .withQuery(Query.of { q -> q.bool(boolQuery.build()) })
            .withPageable(pageable)
            .build()

        SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(query, ProductDocument)

        def products = searchHits.searchHits.collect { it.content }
        new PageImpl<>(products, pageable, searchHits.totalHits)
    }

    /**
     * Autocomplete suggestions for product names
     */
    List<String> autocomplete(String prefix, int maxSuggestions = 10) {
        def response = elasticsearchClient.search({ s ->
            s.index("${indexPrefix}_products")
                .suggest { sg ->
                    sg.suggesters("product-suggest") { sug ->
                        sug.prefix(prefix)
                            .completion { c ->
                                c.field("suggest")
                                    .size(maxSuggestions)
                                    .skipDuplicates(true)
                                    .fuzzy { f ->
                                        f.fuzziness("AUTO")
                                    }
                            }
                    }
                }
        }, ProductDocument)

        response.suggest()
            .get("product-suggest")
            ?.first()
            ?.completion()
            ?.options()
            ?.collect { it.text() } ?: []
    }

    /**
     * Get facets (aggregations) for filters
     */
    Map<String, Map<String, Long>> getFacets(String keyword = null) {
        def boolQuery = new BoolQuery.Builder()
            .filter(Query.of { q -> q.term { t -> t.field("isActive").value(true) } })

        if (keyword) {
            boolQuery.must(Query.of { q ->
                q.multiMatch { mm ->
                    mm.query(keyword).fields("name", "description", "brand")
                }
            })
        }

        def response = elasticsearchClient.search({ s ->
            s.index("${indexPrefix}_products")
                .size(0)
                .query(Query.of { q -> q.bool(boolQuery.build()) })
                .aggregations("categories") { a ->
                    a.terms { t -> t.field("category").size(20) }
                }
                .aggregations("brands") { a ->
                    a.terms { t -> t.field("brand").size(50) }
                }
                .aggregations("priceRanges") { a ->
                    a.range { r ->
                        r.field("price")
                            .ranges(
                                { rng -> rng.key("Under \$10").to("10") },
                                { rng -> rng.key("\$10 - \$25").from("10").to("25") },
                                { rng -> rng.key("\$25 - \$50").from("25").to("50") },
                                { rng -> rng.key("\$50 - \$100").from("50").to("100") },
                                { rng -> rng.key("Over \$100").from("100") }
                            )
                    }
                }
        }, ProductDocument)

        def facets = [:]

        // Categories
        facets.categories = response.aggregations()
            .get("categories")
            .sterms()
            .buckets()
            .array()
            .collectEntries { [(it.key().stringValue()): it.docCount()] }

        // Brands
        facets.brands = response.aggregations()
            .get("brands")
            .sterms()
            .buckets()
            .array()
            .collectEntries { [(it.key().stringValue()): it.docCount()] }

        // Price ranges
        facets.priceRanges = response.aggregations()
            .get("priceRanges")
            .range()
            .buckets()
            .array()
            .collectEntries { [(it.key()): it.docCount()] }

        facets
    }

    /**
     * Index a single product
     */
    void indexProduct(Product product) {
        def document = ProductDocument.fromProduct(product)
        productSearchRepository.save(document)
    }

    /**
     * Delete a product from the index
     */
    void deleteProduct(Long productId) {
        productSearchRepository.deleteById(productId.toString())
    }

    /**
     * Reindex all products from the database
     */
    @Transactional(readOnly = true)
    void reindexAll() {
        // Delete existing index
        productSearchRepository.deleteAll()

        // Batch index all products
        def products = productRepository.findAll()
        def documents = products.collect { ProductDocument.fromProduct(it) }
        productSearchRepository.saveAll(documents)
    }
}
```

### Step 6: Create Search Controller

Create `src/main/groovy/com/grocerystore/grocery_store/controller/SearchController.groovy`:

```groovy
package com.grocerystore.grocery_store.controller

import com.grocerystore.grocery_store.service.ProductSearchService
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/search")
class SearchController {

    private final ProductSearchService productSearchService

    SearchController(ProductSearchService productSearchService) {
        this.productSearchService = productSearchService
    }

    /**
     * Search products with filters
     */
    @GetMapping("/products")
    ResponseEntity<?> searchProducts(
        @RequestParam(required = false) String q,
        @RequestParam(required = false) List<String> categories,
        @RequestParam(required = false) List<String> brands,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice,
        @RequestParam(required = false, defaultValue = "false") Boolean inStockOnly,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        def pageable = PageRequest.of(page, size)
        def results = productSearchService.search(
            q, categories, brands, minPrice, maxPrice, inStockOnly, pageable
        )

        ResponseEntity.ok([
            products: results.content,
            totalItems: results.totalElements,
            totalPages: results.totalPages,
            currentPage: results.number
        ])
    }

    /**
     * Autocomplete suggestions
     */
    @GetMapping("/autocomplete")
    ResponseEntity<?> autocomplete(
        @RequestParam String q,
        @RequestParam(defaultValue = "10") int limit
    ) {
        def suggestions = productSearchService.autocomplete(q, limit)
        ResponseEntity.ok([suggestions: suggestions])
    }

    /**
     * Get filter facets
     */
    @GetMapping("/facets")
    ResponseEntity<?> getFacets(@RequestParam(required = false) String q) {
        def facets = productSearchService.getFacets(q)
        ResponseEntity.ok(facets)
    }

    /**
     * Reindex all products (admin only)
     */
    @PostMapping("/reindex")
    ResponseEntity<?> reindexProducts() {
        productSearchService.reindexAll()
        ResponseEntity.ok([message: "Reindexing started"])
    }
}
```

---

## Data Synchronization

### Option 1: Event-Based Synchronization (Recommended)

Modify `ProductService.groovy` to index products on CRUD operations:

```groovy
package com.grocerystore.grocery_store.service

import com.grocerystore.grocery_store.model.Product
import com.grocerystore.grocery_store.repository.ProductRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService {

    private final ProductRepository productRepository
    private final ProductSearchService productSearchService

    ProductService(ProductRepository productRepository, ProductSearchService productSearchService) {
        this.productRepository = productRepository
        this.productSearchService = productSearchService
    }

    @Transactional
    Product create(Product product) {
        def saved = productRepository.save(product)
        // Index in Elasticsearch
        productSearchService.indexProduct(saved)
        saved
    }

    @Transactional
    Product save(Product product) {
        def saved = productRepository.save(product)
        // Update in Elasticsearch
        productSearchService.indexProduct(saved)
        saved
    }

    @Transactional
    void delete(Long id) {
        productRepository.deleteById(id)
        // Remove from Elasticsearch
        productSearchService.deleteProduct(id)
    }
}
```

### Option 2: Scheduled Synchronization

Create `src/main/groovy/com/grocerystore/grocery_store/scheduler/ElasticsearchSyncScheduler.groovy`:

```groovy
package com.grocerystore.grocery_store.scheduler

import com.grocerystore.grocery_store.service.ProductSearchService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ElasticsearchSyncScheduler {

    private final ProductSearchService productSearchService

    ElasticsearchSyncScheduler(ProductSearchService productSearchService) {
        this.productSearchService = productSearchService
    }

    // Reindex every night at 2 AM
    @Scheduled(cron = "0 0 2 * * ?")
    void reindexProducts() {
        productSearchService.reindexAll()
    }
}
```

---

## Testing

### Unit Test for Search Service

Create `src/test/groovy/com/grocerystore/grocery_store/service/ProductSearchServiceTest.groovy`:

```groovy
package com.grocerystore.grocery_store.service

import com.grocerystore.grocery_store.document.ProductDocument
import com.grocerystore.grocery_store.repository.elasticsearch.ProductSearchRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.elasticsearch.ElasticsearchContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import spock.lang.Specification

@SpringBootTest
@Testcontainers
class ProductSearchServiceTest extends Specification {

    @Container
    static ElasticsearchContainer elasticsearch = new ElasticsearchContainer(
        "docker.elastic.co/elasticsearch/elasticsearch:8.11.0"
    ).withEnv("xpack.security.enabled", "false")

    @DynamicPropertySource
    static void elasticsearchProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris", elasticsearch::getHttpHostAddress)
    }

    @Autowired
    ProductSearchService productSearchService

    @Autowired
    ProductSearchRepository productSearchRepository

    def setup() {
        productSearchRepository.deleteAll()
    }

    def "should search products by keyword"() {
        given:
        def product = new ProductDocument(
            id: "1",
            name: "Organic Bananas",
            description: "Fresh organic bananas",
            brand: "Nature's Best",
            category: "FRUITS_AND_VEGETABLES",
            price: 2.99,
            quantity: 100,
            isActive: true
        )
        productSearchRepository.save(product)

        // Wait for indexing
        Thread.sleep(1000)

        when:
        def results = productSearchService.search(
            "banana", null, null, null, null, false, PageRequest.of(0, 10)
        )

        then:
        results.totalElements == 1
        results.content[0].name == "Organic Bananas"
    }

    def "should filter products by category"() {
        given:
        productSearchRepository.saveAll([
            new ProductDocument(id: "1", name: "Apple", category: "FRUITS_AND_VEGETABLES", isActive: true),
            new ProductDocument(id: "2", name: "Milk", category: "DAIRY_AND_EGGS", isActive: true)
        ])
        Thread.sleep(1000)

        when:
        def results = productSearchService.search(
            null, ["FRUITS_AND_VEGETABLES"], null, null, null, false, PageRequest.of(0, 10)
        )

        then:
        results.totalElements == 1
        results.content[0].name == "Apple"
    }
}
```

### Manual Testing with cURL

```bash
# Search products
curl "http://localhost:8089/api/v1/search/products?q=banana"

# Search with filters
curl "http://localhost:8089/api/v1/search/products?q=organic&categories=FRUITS_AND_VEGETABLES&inStockOnly=true"

# Autocomplete
curl "http://localhost:8089/api/v1/search/autocomplete?q=ban"

# Get facets
curl "http://localhost:8089/api/v1/search/facets"

# Reindex all products
curl -X POST "http://localhost:8089/api/v1/search/reindex"
```

---

## Production Considerations

### 1. Cluster Configuration

For production, use a multi-node cluster:

```yaml
# docker-compose.production.yml
elasticsearch:
  environment:
    - cluster.name=grocery-cluster
    - discovery.seed_hosts=es01,es02,es03
    - cluster.initial_master_nodes=es01,es02,es03
    - xpack.security.enabled=true
```

### 2. Index Lifecycle Management

```json
PUT _ilm/policy/products_policy
{
  "policy": {
    "phases": {
      "hot": {
        "actions": {
          "rollover": {
            "max_size": "50GB",
            "max_age": "30d"
          }
        }
      },
      "delete": {
        "min_age": "90d",
        "actions": {
          "delete": {}
        }
      }
    }
  }
}
```

### 3. Security

- Enable TLS/SSL
- Use API keys or basic authentication
- Implement role-based access control

### 4. Monitoring

- Use Kibana for monitoring
- Set up alerts for cluster health
- Monitor indexing latency and search performance

### 5. Backup

```bash
# Register snapshot repository
PUT _snapshot/grocery_backup
{
  "type": "fs",
  "settings": {
    "location": "/mount/backups/grocery"
  }
}

# Create snapshot
PUT _snapshot/grocery_backup/snapshot_1
```

---

## Summary of Files to Create/Modify

### New Files

| File | Purpose |
|------|---------|
| `config/ElasticsearchConfig.groovy` | ES client configuration |
| `document/ProductDocument.groovy` | Product search document |
| `document/OrderDocument.groovy` | Order search document |
| `repository/elasticsearch/ProductSearchRepository.groovy` | Product ES repository |
| `repository/elasticsearch/OrderSearchRepository.groovy` | Order ES repository |
| `service/ProductSearchService.groovy` | Search service with business logic |
| `controller/SearchController.groovy` | REST API for search |
| `scheduler/ElasticsearchSyncScheduler.groovy` | Scheduled reindexing |
| `resources/elasticsearch/product-settings.json` | Index settings |
| `resources/elasticsearch/product-mapping.json` | Index mappings |

### Modified Files

| File | Changes |
|------|---------|
| `build.gradle` | Add Elasticsearch dependencies |
| `application.properties` | Add ES configuration |
| `docker-compose.yml` | Add ES and Kibana services |
| `ProductService.groovy` | Add indexing on CRUD operations |

---

## Quick Start Commands

```bash
# 1. Start Elasticsearch
docker-compose up -d elasticsearch kibana

# 2. Verify Elasticsearch is running
curl http://localhost:9200

# 3. Start the application
./gradlew bootRun

# 4. Reindex all products
curl -X POST http://localhost:8089/api/v1/search/reindex

# 5. Test search
curl "http://localhost:8089/api/v1/search/products?q=apple"

# 6. Access Kibana
open http://localhost:5601
```
