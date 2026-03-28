#!/bin/bash

# Grocery Store Application - Docker Management Script
# This script helps manage the Docker containers for the Grocery Store application

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored messages
print_message() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Check if Docker is installed
check_docker() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed. Please install Docker first."
        exit 1
    fi

    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose is not installed. Please install Docker Compose first."
        exit 1
    fi

    print_message "Docker and Docker Compose are installed."
}

# Create .env file if it doesn't exist
setup_env() {
    if [ ! -f .env ]; then
        print_warning ".env file not found. Creating from .env.example..."
        cp .env.example .env
        print_message ".env file created. Please review and update it with your settings."
    else
        print_message ".env file already exists."
    fi
}

# Start the application
start() {
    print_message "Starting Grocery Store application..."
    setup_env
    docker-compose up -d
    print_message "Application started successfully!"
    print_message "Access the application at: http://localhost:8089"
    print_message "Swagger UI: http://localhost:8089/swagger-ui.html"
}

# Stop the application
stop() {
    print_message "Stopping Grocery Store application..."
    docker-compose down
    print_message "Application stopped successfully!"
}

# Restart the application
restart() {
    print_message "Restarting Grocery Store application..."
    docker-compose restart
    print_message "Application restarted successfully!"
}

# View logs
logs() {
    local service=${1:-app}
    print_message "Showing logs for service: $service"
    docker-compose logs -f "$service"
}

# Build the application
build() {
    print_message "Building Grocery Store application..."
    docker-compose build --no-cache
    print_message "Application built successfully!"
}

# Clean up (remove containers, volumes, and images)
clean() {
    print_warning "This will remove all containers, volumes, and images. Are you sure? (y/N)"
    read -r response
    if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
        print_message "Cleaning up..."
        docker-compose down -v --rmi all
        print_message "Cleanup completed!"
    else
        print_message "Cleanup cancelled."
    fi
}

# Check status of services
status() {
    print_message "Checking service status..."
    docker-compose ps
}

# Database backup
backup_db() {
    local backup_file="backup_$(date +%Y%m%d_%H%M%S).sql"
    print_message "Creating database backup: $backup_file"
    docker-compose exec -T mysql mysqldump -uroot -ppassword123 grocery_store > "$backup_file"
    print_message "Database backup created: $backup_file"
}

# Database restore
restore_db() {
    local backup_file=$1
    if [ -z "$backup_file" ]; then
        print_error "Please specify a backup file: ./docker.sh restore_db <backup_file>"
        exit 1
    fi

    if [ ! -f "$backup_file" ]; then
        print_error "Backup file not found: $backup_file"
        exit 1
    fi

    print_message "Restoring database from: $backup_file"
    docker-compose exec -T mysql mysql -uroot -ppassword123 grocery_store < "$backup_file"
    print_message "Database restored successfully!"
}

# Show help
show_help() {
    echo "Grocery Store Application - Docker Management"
    echo ""
    echo "Usage: ./docker.sh [command]"
    echo ""
    echo "Commands:"
    echo "  start         Start the application and all services"
    echo "  stop          Stop all services"
    echo "  restart       Restart all services"
    echo "  build         Build the application image"
    echo "  logs [svc]    Show logs (default: app, options: app, mysql, redis)"
    echo "  status        Show status of all services"
    echo "  clean         Remove all containers, volumes, and images"
    echo "  backup_db     Create a database backup"
    echo "  restore_db    Restore database from backup file"
    echo "  help          Show this help message"
    echo ""
    echo "Examples:"
    echo "  ./docker.sh start"
    echo "  ./docker.sh logs app"
    echo "  ./docker.sh backup_db"
    echo "  ./docker.sh restore_db backup_20260226_120000.sql"
}

# Main script logic
main() {
    check_docker

    case "${1:-help}" in
        start)
            start
            ;;
        stop)
            stop
            ;;
        restart)
            restart
            ;;
        logs)
            logs "$2"
            ;;
        build)
            build
            ;;
        clean)
            clean
            ;;
        status)
            status
            ;;
        backup_db)
            backup_db
            ;;
        restore_db)
            restore_db "$2"
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            print_error "Unknown command: $1"
            show_help
            exit 1
            ;;
    esac
}

# Run the script
main "$@"

