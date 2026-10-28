# Grocery Store - Docker Setup

This application has been dockerized for easy deployment and development.

## Prerequisites

- Docker (version 20.10 or higher)
- Docker Compose (version 2.0 or higher)

## Quick Start

### 1. Build and Start All Services

```bash
# Start all services (MySQL, Redis, and the application)
docker-compose up -d

# View logs
docker-compose logs -f

# View logs for a specific service
docker-compose logs -f app
```

### 2. Stop Services

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (WARNING: This will delete your database data)
docker-compose down -v
```

## Services

The Docker setup includes the following services:

### MySQL Database
- **Port**: 3307 (host) → 3306 (container)
- **Database**: grocery_store
- **Username**: root
- **Password**: password123

### Redis Cache
- **Port**: 6379 (host) → 6379 (container)

### Spring Boot Application
- **Port**: 8089 (host) → 8089 (container)
- **Access**: http://localhost:8089

## Useful Commands

### Application Management

```bash
# Rebuild the application after code changes
docker-compose build app

# Restart only the application
docker-compose restart app

# View application logs
docker-compose logs -f app

# Execute a command in the running container
docker-compose exec app sh
```

### Database Management

```bash
# Access MySQL CLI
docker-compose exec mysql mysql -uroot -ppassword123 grocery_store

# Create a database backup
docker-compose exec mysql mysqldump -uroot -ppassword123 grocery_store > backup.sql

# Restore from backup
docker-compose exec -T mysql mysql -uroot -ppassword123 grocery_store < backup.sql
```

### Redis Management

```bash
# Access Redis CLI
docker-compose exec redis redis-cli

# View Redis info
docker-compose exec redis redis-cli INFO
```

## Environment Variables

You can customize the configuration by creating a `.env` file:

```bash
# Copy the example environment file
cp .env.example .env

# Edit the .env file with your preferred settings
nano .env
```

## Volumes

The setup uses Docker volumes for persistent data:

- `mysql_data`: MySQL database files
- `upload_data`: Application uploaded files

To inspect volumes:
```bash
docker volume ls
docker volume inspect grocery-store_mysql_data
```

## Development Workflow

### Live Development with Auto-Reload

For development, you can mount the source code and use Spring Boot DevTools:

```bash
# Edit docker-compose.yml to add volume mount under app service:
# volumes:
#   - ./src:/app/src
#   - upload_data:/app/uploads

# Then rebuild and restart
docker-compose up -d --build
```

### Running Tests

```bash
# Run tests inside the container
docker-compose exec app sh -c "gradle test"

# Or build without tests
docker-compose build --build-arg SKIP_TESTS=true
```

## Troubleshooting

### Port Already in Use

If ports 8089, 3307, or 6379 are already in use:

```bash
# Find the process using the port (example for port 8089)
lsof -i :8089

# Kill the process
kill -9 <PID>

# Or change the port in docker-compose.yml
```

### Database Connection Issues

```bash
# Check if MySQL is healthy
docker-compose ps

# View MySQL logs
docker-compose logs mysql

# Ensure the database is created
docker-compose exec mysql mysql -uroot -ppassword123 -e "SHOW DATABASES;"
```

### Application Won't Start

```bash
# Check application logs
docker-compose logs app

# Verify all dependencies are healthy
docker-compose ps

# Rebuild from scratch
docker-compose down -v
docker-compose up -d --build
```

## Production Deployment

There is no production Compose file yet: `docker-compose.yml` is for local development. Before deploying, create a production setup that covers the points below.


1. **Update environment variables** in `.env` with secure credentials
2. **Use secrets management** instead of plain text passwords
3. **Configure proper networking** and firewall rules
4. **Set up monitoring** and log aggregation
5. **Configure backups** for MySQL volume
6. **Use reverse proxy** (nginx/traefik) for SSL/TLS

Example production-ready changes:

```yaml
# Use environment variables for sensitive data
environment:
  SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}

# Add restart policy
restart: always

# Resource limits
deploy:
  resources:
    limits:
      cpus: '2'
      memory: 1G
    reservations:
      cpus: '1'
      memory: 512M
```

## Accessing the Application

Once all services are running:

- **Application**: http://localhost:8089
- **Swagger UI**: http://localhost:8089/swagger-ui.html
- **API Docs**: http://localhost:8089/api-docs
- **Health Check**: http://localhost:8089/actuator/health

## Cleaning Up

```bash
# Stop and remove containers, networks
docker-compose down

# Remove volumes as well (WARNING: deletes data)
docker-compose down -v

# Remove all related images
docker-compose down --rmi all
```

## Network Architecture

```
┌─────────────────────────────────────────────┐
│              Docker Network                  │
│                                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐ │
│  │  MySQL   │  │  Redis   │  │   App    │ │
│  │  :3306   │  │  :6379   │  │  :8089   │ │
│  └──────────┘  └──────────┘  └──────────┘ │
│       │              │              │       │
└───────┼──────────────┼──────────────┼───────┘
        │              │              │
     :3307          :6379          :8089
        │              │              │
     [Host Machine]                   
```

## Support

For issues or questions:
1. Check the logs: `docker-compose logs -f`
2. Verify service health: `docker-compose ps`
3. Review the troubleshooting section above

