# E-Commerce Microservices Application

This repository hosts a microservices-based e-commerce application, with core functionalities like User Management, Inventory Management, and Order Processing. The application is built using Java Spring Boot and is designed for scalability and future enhancements, such as Payment Integration and Shipment Tracking.

## Project Overview

This Java Spring Boot-based microservices e-commerce system manages the essential e-commerce functionality, including:

- **User Service:** Handles authentication, registration, and user profile management.
- **Inventory Service:** Manages product inventory, including adding, updating, and fetching product details.
- **Order Service:** Manages the creation and tracking of customer orders.

The application is architected for scalability with microservices, and future services such as Payment and Shipment will be implemented to make it enterprise-ready.

## Technologies Used

- **Backend:** Java Spring Boot, Spring Cloud
- **Database:** MySQL, MongoDB
- **Authentication:** JWT (JSON Web Tokens)
- **API Communication:** RESTful APIs
- **Service Discovery:** Spring Cloud Netflix Eureka
- **Database Communication:** Spring Data JPA, MongoDB Repository
- **Load Balancing:** Spring Cloud Netflix Ribbon (planned for future scalability)
- **Containerization:** Docker (planned)
- **Deployment:** AWS (planned)
- **Version Control:** Git & GitHub

## Microservices Architecture

The application follows a microservices architecture. Each service is designed to handle a specific domain of the e-commerce platform.

### 1. User Service

- **Functionality:** Manages user registration, login, authentication (using JWT), and user profiles.
- **Database:** MySQL (user data)
- **Dependencies:**
  - Spring Boot: Application framework
  - Spring Security: For securing APIs and managing authentication
  - JWT: JSON Web Tokens for secure stateless authentication
  - Spring Data JPA: For database communication with MySQL
- **Key Files:**
  - `user-service/src/main/java/com/example/userservice/controller/UserController.java`: Handles authentication, registration, and profile management routes.
  - `user-service/src/main/java/com/example/userservice/service/UserService.java`: Contains the business logic for user operations.

### 2. Inventory Service

- **Functionality:** Manages product inventory, including adding new products, updating stock, and retrieving product details.
- **Database:** MongoDB (product data)
- **Dependencies:**
  - Spring Boot
  - Spring Data MongoDB: For database communication with MongoDB
  - Spring Cloud Netflix Eureka: For service discovery
- **Key Files:**
  - `inventory-service/src/main/java/com/example/inventoryservice/controller/InventoryController.java`: Exposes product-related routes.
  - `inventory-service/src/main/java/com/example/inventoryservice/service/InventoryService.java`: Contains business logic for managing product inventory.

### 3. Order Service

- **Functionality:** Manages order creation, tracking, and status updates.
- **Database:** MySQL (order data)
- **Dependencies:**
  - Spring Boot
  - Spring Data JPA
  - Feign Client: For inter-service communication with the Inventory and User Services
  - Ribbon (Planned): Load balancing future feature
- **Key Files:**
  - `order-service/src/main/java/com/example/orderservice/controller/OrderController.java`: Handles order-related routes.
  - `order-service/src/main/java/com/example/orderservice/service/OrderService.java`: Implements the business logic for order creation and tracking.

## Setup & Installation

### Pre-requisites

- Java 17 (or latest stable version)
- Maven (for project building)
- MySQL (or set up MySQL using Docker)
- MongoDB (for inventory service)
- Git (for version control)

### Installation Steps

1. Clone the repository:

    ```bash
    git clone https://github.com/yourusername/ecommerce-microservices.git
    cd ecommerce-microservices
    ```

2. Set up the database:

   - **MySQL:** Ensure you have MySQL installed and running.
   - Create databases for each microservice:

    ```sql
    CREATE DATABASE userdb;
    CREATE DATABASE orderdb;
    ```

   - **MongoDB:** Make sure MongoDB is installed and running locally (or use MongoDB Atlas).
   - Update `.properties` files for each service with your own database credentials.

    Example for User Service (`application.properties`):

    ```properties
    spring.datasource.url=jdbc:mysql://localhost:3306/userdb
    spring.datasource.username=root
    spring.datasource.password=yourpassword
    spring.jpa.hibernate.ddl-auto=update
    jwt.secret=yourjwtsecret
    ```

3. Install dependencies using Maven for each microservice:

    ```bash
    cd user-service
    mvn clean install
    ```

   Repeat for each service (User, Inventory, and Order).

4. Run the services:

   Start each service on separate ports:
   - User Service: `http://localhost:8081`
   - Inventory Service: `http://localhost:8082`
   - Order Service: `http://localhost:8083`

    Example:

    ```bash
    cd user-service
    mvn spring-boot:run
    ```

5. Service Discovery (Eureka): Ensure the Eureka Server is running to register all services.

## Running the Application

Each microservice runs on its own port. Use Postman or a similar API client to interact with the endpoints. Make sure the MySQL and MongoDB databases are running before you start the services.

## API Endpoints

### User Service (Port: 8081)

- `POST /api/auth/register`: Register a new user.
- `POST /api/auth/login`: Login and generate JWT.

### Inventory Service (Port: 8082)

- `POST /api/inventory/add`: Add a new product to inventory.
- `GET /api/inventory/{id}`: Retrieve product details.
- `PATCH /api/inventory/update/{id}`: Update stock levels for a product.

### Order Service (Port: 8083)

- `POST /api/orders/create`: Create a new order.
- `GET /api/orders/{id}`: Get details of a specific order.
- `PATCH /api/orders/update/{id}`: Update order status.

### Dockerization (Coming Soon)
Containerization using Docker will allow easy deployment and scalability. Each service will be containerized and connected via Docker networks.

### Deployment to AWS (Coming Soon)
Deployment to AWS will use services like EC2, ECS, or EKS, ensuring high availability and scalability for the application.

### Future Enhancements
- Payment Service: Integrating with payment gateways like Stripe or PayPal.
- Shipment Service: Adding shipment tracking and delivery status.
- Load Balancing: Implementing load balancing and autoscaling with Spring Cloud Ribbon and Kubernetes.
- Monitoring: Adding services for performance monitoring using tools like Prometheus and Grafana.

### Contributing
Contributions are welcome! 

### License
This project is licensed under the MIT License.
