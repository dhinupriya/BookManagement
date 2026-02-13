# Library Management System - Spring Boot Assessment

## Overview
This is a Spring Boot assessment project. Your task is to implement a complete REST API for a Library Management System.

## Prerequisites
- JDK 17 or higher
- Maven 3.6+ (or use Maven wrapper)
- IDE of your choice (IntelliJ IDEA, Eclipse, VS Code)

## Project Structure
```
src/
├── main/
│   ├── java/com/library/
│   │   ├── LibraryManagementApplication.java (main class - already created)
│   │   ├── entity/          (create Book entity here)
│   │   ├── repository/      (create BookRepository here)
│   │   ├── service/         (create BookService here)
│   │   ├── controller/      (create BookController here)
│   │   ├── dto/             (create DTOs here)
│   │   └── exception/       (create custom exceptions and handlers here)
│   └── resources/
│       └── application.properties (already configured)
└── test/
    └── java/com/library/    (create your tests here)
```

## Getting Started

### 1. Build the Project
```bash
mvn clean install
```

### 2. Run the Application
```bash
mvn spring-boot:run
```

Or run the `LibraryManagementApplication` class directly from your IDE.

### 3. Access H2 Console
Once the application is running, you can access the H2 database console at:
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:librarydb`
- Username: `sa`
- Password: (leave empty)

## What You Need to Implement

Refer to `ASSESSMENT.md` for detailed requirements. In summary, you need to:

1. ✅ Create the `Book` entity with all required fields
2. ✅ Create the `BookRepository` with custom query methods
3. ✅ Create the `BookService` with all business logic
4. ✅ Create the `BookController` with all REST endpoints
5. ✅ Implement custom exception handling
6. ✅ Add validation to entities
7. ✅ Create response DTOs

## Testing Your API

Once implemented, you can test the API using:

### Using cURL:
```bash
# Get all books
curl http://localhost:8080/api/books

# Get book by ID
curl http://localhost:8080/api/books/1

# Create a book
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{
    "title": "SQL in Action",
    "author": "Craig Walls",
    "isbn": "9781617298546",
    "publicationYear": 2021
  }'

# Update a book
curl -X PUT http://localhost:8080/api/books/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated Title",
    "author": "Author Name",
    "isbn": "9781617292545",
    "publicationYear": 2022
  }'

# Delete a book
curl -X DELETE http://localhost:8080/api/books/1

# Search by author
curl http://localhost:8080/api/books/search?author=Craig

# Get available books
curl http://localhost:8080/api/books/available
```

### Using Postman:
Import the API endpoints into Postman and test each endpoint.

## Tips

1. **Start Small**: Begin with the entity, then repository, then service, then controller
2. **Test Frequently**: Test each component as you build it
3. **Handle Errors**: Make sure to handle all edge cases and provide meaningful error messages
4. **Follow Best Practices**: Use proper REST conventions, meaningful variable names, and clean code principles

## Good Luck! 🚀

Feel free to explore Spring Boot documentation if you need help:
- https://spring.io/guides
- https://docs.spring.io/spring-boot/docs/current/reference/html/

