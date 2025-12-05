# Grocery Store API

A RESTful API for managing products in a grocery store, built with Spring Boot and Groovy, with Redis caching support.

## 🚀 Tech Stack

- **Framework**: Spring Boot 3.3.4
- **Language**: Groovy 4.0.23
- **Build Tool**: Gradle
- **Database**: MySQL 8.0+
- **Cache**: Redis
- **API Documentation**: SpringDoc OpenAPI 2.3.0 (Swagger UI)
- **Validation**: Jakarta Bean Validation

## 📋 Prerequisites

- Java 17 or higher
- MySQL 8.0 or higher
- Redis 7.0 or higher (or Docker)
- Gradle 8.0 or higher

## 🐳 Running Redis with Docker

To run Redis using Docker:

```bash
docker run -d --name redis-local -p 6379:6379 redis:latest
```

To connect to the Redis CLI:

```bash
docker exec -it redis-local redis-cli
```

## 🔍 Viewing Redis Data

You can use any Redis client to view the cached data. Here are some options:

1. **Command Line**:
   ```bash
   # Connect to Redis CLI
   docker exec -it redis-local redis-cli
   
   # List all keys
   KEYS *
   
   # Get value for a specific key
   GET product:1
   ```

2. **RedisInsight** (GUI Client):
   - Download from: https://redis.com/redis-enterprise/redis-insight/
   - Connect to `localhost:6379`

## 🛠️ Configuration

1. Create a MySQL database named `grocery_store`
2. Update the database configuration in `src/main/resources/application.properties` if needed:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3307/grocery_store
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

## 🚀 Running the Application

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd grocery-store
   ```

2. **Build the application**
   ```bash
   ./gradlew clean build
   ```

3. **Run the application**
   ```bash
   ./gradlew bootRun
   ```

   The application will start on port 8089.

## 🌐 API Documentation

Once the application is running, you can access the following:

- **Swagger UI**: [http://localhost:8089/swagger-ui/index.html](http://localhost:8089/swagger-ui/index.html)
- **OpenAPI JSON**: [http://localhost:8089/v3/api-docs](http://localhost:8089/v3/api-docs)

### Available Endpoints

- `GET /products` - Get all products
- `GET /products/{id}` - Get a product by ID
- `POST /products` - Create a new product
- `PUT /products/{id}` - Update a product
- `DELETE /products/{id}` - Delete a product


##To go to home page
Hit this url: 
http://localhost:8089/

#For customer facing
http://localhost:8089/store.html