# Spring Boot Technical Interview - Questions & Answers

## Question 1: Entity & JPA Basics

### Q: In your `Book` entity, you're using `@CreationTimestamp` and `@UpdateTimestamp`. Can you explain:
1. How do these annotations work internally?
2. What's the difference between using these annotations vs manually setting `createdAt` and `updatedAt` in a constructor?

### A:

**1. How `@CreationTimestamp` and `@UpdateTimestamp` work internally:**

- **`@CreationTimestamp`**: This Hibernate annotation automatically sets the field value to the current timestamp **only once** when the entity is first persisted to the database. It works at the JPA lifecycle callback level using `@PrePersist`.
  
- **`@UpdateTimestamp`**: This Hibernate annotation automatically updates the field value to the current timestamp **every time** the entity is updated (on `UPDATE` operations). It works at the JPA lifecycle callback level using `@PreUpdate`.

Both annotations use Hibernate's event listeners to inject the current timestamp automatically, so you don't need to manually manage these fields.

**2. Difference between annotations vs manual setting:**

| Aspect | Using Annotations | Manual in Constructor |
|--------|------------------|----------------------|
| **Automatic Updates** | `@UpdateTimestamp` automatically updates on every update | Manual setting doesn't auto-update on subsequent updates |
| **JPA Lifecycle** | Works with JPA lifecycle callbacks (`@PrePersist`, `@PreUpdate`) | Only sets value when constructor is called |
| **Repository Updates** | Works when using `save()`, `saveAll()`, etc. | May not work with direct repository updates |
| **Consistency** | Guaranteed to be set by Hibernate | Can be forgotten or overwritten |
| **Maintenance** | Less code, declarative | More code, imperative |

**Example Problem with Manual Setting:**
```java
// If you only set in constructor
Book book = bookRepository.findById(1L).orElseThrow();
book.setTitle("New Title");
bookRepository.save(book); // updatedAt won't be updated!
```

---

## Question 2: Lombok Understanding

### Q: You're using `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, and `@Builder` from Lombok.
1. If you have both `@AllArgsConstructor` and `@NoArgsConstructor`, and then write a custom constructor like you did, what problems might arise?
2. Can you use `@Builder` with `@AllArgsConstructor`? What happens if you don't have `@AllArgsConstructor`?

### A:

**1. Problems with custom constructor alongside Lombok constructors:**

- **Compilation Error**: Java doesn't allow multiple constructors with the same signature. If you write a custom constructor that matches `@AllArgsConstructor`, it will conflict.
  
- **Builder Issues**: `@Builder` by default uses all fields. If you have a custom constructor, the builder might not work correctly because it expects the `@AllArgsConstructor`.

- **JPA Requirements**: JPA requires a no-args constructor. `@NoArgsConstructor` provides this, but if your custom constructor doesn't call `super()`, you might break inheritance.

- **Field Initialization Order**: Custom constructors might not initialize all fields, causing `NullPointerException` if Lombok-generated methods are called.

**Solution**: Remove the custom constructor and use `@Builder` or add `@Builder` with `toBuilder = true` if you need to create objects with specific initializations.

**2. `@Builder` with `@AllArgsConstructor`:**

- **Yes, you CAN use them together**, but:
  - `@Builder` will use `@AllArgsConstructor` to construct the object
  - This is actually the recommended pattern: `@AllArgsConstructor` + `@Builder`
  
- **If you DON'T have `@AllArgsConstructor`:**
  - `@Builder` will generate its own private constructor internally
  - You'll need `@NoArgsConstructor` for JPA, but then you need `@AllArgsConstructor` for the builder
  - **Best Practice**: Use all three: `@NoArgsConstructor`, `@AllArgsConstructor`, and `@Builder`

**Recommended Pattern:**
```java
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Book {
    // fields
}
```

---

## Question 3: Validation

### Q: In your entity, you have validation annotations like `@NotBlank`, `@Size`, `@Pattern`, and a custom `@YearMax`.
1. When does Bean Validation execute in Spring Boot? Before or after the request hits the controller?
2. How would you make validation errors return a structured JSON response instead of the default error format?
3. What's the difference between `@NotNull`, `@NotEmpty`, and `@NotBlank`?

### A:

**1. When Bean Validation executes:**

Bean Validation executes **AFTER** the request hits the controller but **BEFORE** the controller method body executes. Specifically:

- HTTP request arrives → Spring MVC
- Request deserialization (JSON to Java object)
- **Validation happens here** (if `@Valid` or `@Validated` is used)
- Controller method executes (only if validation passes)
- Response serialization

To trigger validation, you must annotate the controller parameter with `@Valid` or `@Validated`:
```java
@PostMapping("/books")
public ResponseEntity<Book> createBook(@Valid @RequestBody Book book) {
    // Validation happens before this line executes
}
```

**2. Structured JSON response for validation errors:**

You need a `@ControllerAdvice` with `@ExceptionHandler` for `MethodArgumentNotValidException`:

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message("Validation failed")
            .errors(errors)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
}

// ErrorResponse DTO
@Data
@Builder
public class ErrorResponse {
    private int status;
    private String message;
    private Map<String, String> errors;
    private LocalDateTime timestamp;
}
```

**3. Difference between `@NotNull`, `@NotEmpty`, and `@NotBlank`:**

| Annotation | What it checks | Example - Valid | Example - Invalid |
|-----------|---------------|----------------|-------------------|
| `@NotNull` | Object is not null | `"hello"`, `""`, `" "` | `null` |
| `@NotEmpty` | Not null AND not empty | `"hello"`, `[1,2]` | `null`, `""`, `[]` |
| `@NotBlank` | Not null, not empty, and has at least one non-whitespace character | `"hello"` | `null`, `""`, `"   "` |

**Code Example:**
```java
@NotNull  // null is not allowed, but "" and " " are allowed
private String name1;

@NotEmpty  // null and "" are not allowed, but " " is allowed
private String name2;

@NotBlank  // null, "", and "   " are all not allowed
private String name3;
```

**For Strings**: Use `@NotBlank` most of the time (most common case)
**For Collections**: Use `@NotEmpty`
**For Objects**: Use `@NotNull`

---

## Question 4: Database & JPA

### Q:
1. Your `isbn` field needs to be unique. You can use `@Column(unique = true)`, but what happens if you try to insert a duplicate ISBN? What exception will be thrown?
2. Explain the difference between `@GeneratedValue(strategy = GenerationType.IDENTITY)` and `GenerationType.AUTO`. When would you use each?
3. What's the difference between `spring.jpa.hibernate.ddl-auto=update` and `validate`? Which is better for production?

### A:

**1. Exception for duplicate ISBN:**

When you try to insert a duplicate ISBN with `@Column(unique = true)`, Hibernate/Spring will throw:
- **`DataIntegrityViolationException`** (Spring's wrapper exception)
- The underlying cause is **`ConstraintViolationException`** from JPA or **`SQLIntegrityConstraintViolationException`** from JDBC

**How to handle it:**
```java
try {
    bookRepository.save(book);
} catch (DataIntegrityViolationException e) {
    if (e.getCause() instanceof ConstraintViolationException) {
        throw new DuplicateIsbnException("ISBN already exists: " + book.getIsbn());
    }
}
```

**2. `GenerationType.IDENTITY` vs `GenerationType.AUTO`:**

| Strategy | How it works | Database Support | Best for |
|---------|-------------|-----------------|----------|
| **IDENTITY** | Uses database auto-increment (AUTO_INCREMENT in MySQL, IDENTITY in SQL Server, SERIAL in PostgreSQL) | MySQL, SQL Server, PostgreSQL, H2 | Most common, reliable, database-native |
| **AUTO** | Hibernate chooses the best strategy based on the database dialect | All databases | When you want portability, less control |
| **SEQUENCE** | Uses a database sequence | Oracle, PostgreSQL | When you need sequences |
| **TABLE** | Uses a separate table to generate IDs | All databases | Legacy databases, rarely used |

**When to use each:**

- **IDENTITY**: Use for MySQL, PostgreSQL, SQL Server, H2. Most efficient and reliable.
  ```java
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  ```
  
- **AUTO**: Use when you want database portability and don't care about the specific strategy.
  ```java
  @GeneratedValue(strategy = GenerationType.AUTO)  // Hibernate decides
  ```

**Best Practice**: Use `IDENTITY` for most cases (most databases support it).

**3. `ddl-auto=update` vs `validate`:**

| Value | Behavior | Production Safe? | Use Case |
|------|---------|-----------------|----------|
| **create** | Drops and recreates schema on startup | ❌ NO - Data loss! | Development, testing |
| **create-drop** | Creates on startup, drops on shutdown | ❌ NO - Data loss! | Testing only |
| **update** | Updates schema to match entities (adds columns/tables, doesn't drop) | ⚠️ RISKY - Can cause unexpected changes | Development |
| **validate** | Validates schema matches entities, throws error if mismatch | ✅ YES - Safe | Production |
| **none** | No schema management | ✅ YES - Safe | Production with migrations |

**For Production:**
```properties
# Use validate or none
spring.jpa.hibernate.ddl-auto=validate
```

**For Development:**
```properties
# Can use update for convenience
spring.jpa.hibernate.ddl-auto=update
```

**Best Practice**: 
- **Production**: Use `validate` or `none` + Flyway/Liquibase for migrations
- **Development**: Use `update` for convenience
- **Never use `create` or `create-drop` in production!**

---

## Question 5: Service Layer Design

### Q: For the `BookService` you need to create:
1. Should the service layer catch and handle exceptions, or let them propagate to the controller? Explain your approach.
2. If a book with a duplicate ISBN is being created, where should you check for this — in the service layer, or rely on the database constraint? Why?
3. How would you implement the `updateBook` method? Should it update all fields, or only the non-null fields provided?

### A:

**1. Exception handling in service layer:**

**General Principle**: Let exceptions propagate to the controller, but **transform** low-level exceptions into business-level exceptions.

**Approach:**
```java
@Service
public class BookService {
    
    @Autowired
    private BookRepository bookRepository;
    
    public Book getBookById(Long id) {
        // Don't catch - let it propagate, but wrap in business exception
        return bookRepository.findById(id)
            .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));
    }
    
    public Book createBook(Book book) {
        // Transform low-level exceptions to business exceptions
        try {
            // Check business rules first
            if (bookRepository.existsByIsbn(book.getIsbn())) {
                throw new DuplicateIsbnException("ISBN already exists: " + book.getIsbn());
            }
            return bookRepository.save(book);
        } catch (DataIntegrityViolationException e) {
            // Fallback: catch database constraint violations
            throw new DuplicateIsbnException("ISBN already exists: " + book.getIsbn());
        }
    }
}
```

**Why this approach:**
- Service layer throws **business exceptions** (`BookNotFoundException`, `DuplicateIsbnException`)
- Controller/`@ControllerAdvice` handles these and converts to HTTP responses
- Separation of concerns: Service = business logic, Controller = HTTP concerns

**2. Where to check for duplicate ISBN:**

**Best Practice: Check in BOTH places, but prefer service layer check:**

**Service Layer Check (Primary):**
```java
public Book createBook(Book book) {
    // Explicit business rule check - better error message
    if (bookRepository.existsByIsbn(book.getIsbn())) {
        throw new DuplicateIsbnException("ISBN already exists: " + book.getIsbn());
    }
    return bookRepository.save(book);
}
```

**Database Constraint (Fallback/Safety):**
```java
@Column(unique = true)
private String isbn;
```

**Why both:**
- **Service layer check**: Faster (one query), better error messages, clearer business intent
- **Database constraint**: Guaranteed data integrity, prevents race conditions, final safety net

**Race Condition Example:**
```java
// Thread 1 and Thread 2 both check ISBN at the same time
// Both pass the service check, both try to save
// Database constraint prevents the duplicate - this is why we need both!
```

**3. Implementing `updateBook` method:**

**Two approaches:**

**Approach 1: Update all fields (PUT-style)**
```java
public Book updateBook(Long id, Book updatedBook) {
    Book existingBook = getBookById(id); // throws if not found
    
    existingBook.setTitle(updatedBook.getTitle());
    existingBook.setAuthor(updatedBook.getAuthor());
    existingBook.setIsbn(updatedBook.getIsbn());
    existingBook.setPublicationYear(updatedBook.getPublicationYear());
    existingBook.setAvailable(updatedBook.getAvailable());
    // createdAt and updatedAt are handled by @UpdateTimestamp
    
    return bookRepository.save(existingBook);
}
```

**Approach 2: Partial update (PATCH-style, only non-null fields)**
```java
public Book updateBook(Long id, Book updatedBook) {
    Book existingBook = getBookById(id);
    
    if (updatedBook.getTitle() != null) {
        existingBook.setTitle(updatedBook.getTitle());
    }
    if (updatedBook.getAuthor() != null) {
        existingBook.setAuthor(updatedBook.getAuthor());
    }
    if (updatedBook.getIsbn() != null) {
        // Check if new ISBN is unique (if changed)
        if (!existingBook.getIsbn().equals(updatedBook.getIsbn()) 
            && bookRepository.existsByIsbn(updatedBook.getIsbn())) {
            throw new DuplicateIsbnException("ISBN already exists");
        }
        existingBook.setIsbn(updatedBook.getIsbn());
    }
    if (updatedBook.getPublicationYear() != null) {
        existingBook.setPublicationYear(updatedBook.getPublicationYear());
    }
    if (updatedBook.getAvailable() != null) {
        existingBook.setAvailable(updatedBook.getAvailable());
    }
    
    return bookRepository.save(existingBook);
}
```

**Best Practice:**
- Use **PUT** for full updates (all fields required)
- Use **PATCH** for partial updates (only provided fields)
- For assessment: Partial update is more flexible and user-friendly

**Better Approach - Use DTO for updates:**
```java
@Data
public class BookUpdateDTO {
    private String title;
    private String author;
    private String isbn;
    private Integer publicationYear;
    private Boolean available;
}

public Book updateBook(Long id, BookUpdateDTO updateDTO) {
    Book existingBook = getBookById(id);
    // Copy non-null fields from DTO to entity
    // ...
}
```

---

## Question 6: REST API Design

### Q:
1. What HTTP status codes would you return for these scenarios:
   - Successful book creation
   - Book not found
   - Duplicate ISBN violation
   - Validation errors
   - Successful deletion
2. For the `GET /api/books/{id}` endpoint, should it return `200 OK` or `404 Not Found` when the book doesn't exist? Justify your answer.
3. What's the difference between `PUT` and `PATCH`? Which one should you use for updating a book?

### A:

**1. HTTP Status Codes:**

| Scenario | Status Code | Description |
|---------|------------|-------------|
| **Successful book creation** | `201 Created` | Resource was created, include `Location` header with URI |
| **Book not found** | `404 Not Found` | Resource doesn't exist |
| **Duplicate ISBN violation** | `409 Conflict` | Resource conflict (duplicate unique constraint) |
| **Validation errors** | `400 Bad Request` | Client error - invalid input data |
| **Successful deletion** | `204 No Content` | Success, no response body needed |
| **Successful update** | `200 OK` | Standard success with response body |
| **Server error** | `500 Internal Server Error` | Unexpected server error |

**Example Implementation:**
```java
@PostMapping("/books")
public ResponseEntity<BookResponseDTO> createBook(@Valid @RequestBody Book book) {
    Book savedBook = bookService.createBook(book);
    return ResponseEntity.status(HttpStatus.CREATED)
        .header("Location", "/api/books/" + savedBook.getId())
        .body(bookMapper.toDTO(savedBook));
}

@GetMapping("/books/{id}")
public ResponseEntity<BookResponseDTO> getBookById(@PathVariable Long id) {
    Book book = bookService.getBookById(id);
    return ResponseEntity.ok(bookMapper.toDTO(book));
}

@DeleteMapping("/books/{id}")
public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
    bookService.deleteBook(id);
    return ResponseEntity.noContent().build(); // 204
}
```

**2. `GET /api/books/{id}` - 200 vs 404:**

**Answer: Return `404 Not Found`** when book doesn't exist.

**Justification:**
- **REST Principle**: HTTP status codes should reflect the actual state. If the resource doesn't exist, it's a "Not Found" condition.
- **Semantic Correctness**: `200 OK` means "request succeeded and returned data". `404` means "resource not found".
- **Client Behavior**: Clients can differentiate between "success with data" vs "resource missing".
- **API Consistency**: Follows REST best practices and HTTP specifications.

**Implementation:**
```java
@GetMapping("/books/{id}")
public ResponseEntity<BookResponseDTO> getBookById(@PathVariable Long id) {
    // Service throws BookNotFoundException if not found
    Book book = bookService.getBookById(id); // throws 404 in exception handler
    return ResponseEntity.ok(bookMapper.toDTO(book));
}

// In GlobalExceptionHandler:
@ExceptionHandler(BookNotFoundException.class)
@ResponseStatus(HttpStatus.NOT_FOUND)
public ResponseEntity<ErrorResponse> handleBookNotFound(BookNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ErrorResponse.builder()
            .status(404)
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build());
}
```

**3. `PUT` vs `PATCH`:**

| Aspect | PUT | PATCH |
|--------|-----|-------|
| **Idempotency** | ✅ Idempotent | ⚠️ May or may not be idempotent |
| **Full vs Partial** | Full resource replacement | Partial resource update |
| **Fields Required** | All fields must be provided | Only changed fields |
| **Missing Fields** | Set to null/default | Leave unchanged |
| **Use Case** | Complete resource update | Incremental update |
| **Request Body** | Full resource representation | Only changed fields |

**PUT Example:**
```java
@PutMapping("/books/{id}")
public ResponseEntity<BookResponseDTO> updateBook(
        @PathVariable Long id, 
        @Valid @RequestBody Book book) {
    // All fields must be provided, replaces entire resource
    Book updatedBook = bookService.updateBook(id, book);
    return ResponseEntity.ok(bookMapper.toDTO(updatedBook));
}
```

**PATCH Example:**
```java
@PatchMapping("/books/{id}")
public ResponseEntity<BookResponseDTO> partialUpdateBook(
        @PathVariable Long id,
        @RequestBody BookUpdateDTO updateDTO) {
    // Only provided fields are updated
    Book updatedBook = bookService.partialUpdateBook(id, updateDTO);
    return ResponseEntity.ok(bookMapper.toDTO(updatedBook));
}
```

**Which to use for updating a book?**

**For the assessment**: Use **PUT** for simplicity (full update)
**For real-world**: Use **PATCH** for flexibility (partial update)

**Recommendation**: Implement both:
- `PUT /api/books/{id}` - Full update (all fields required)
- `PATCH /api/books/{id}` - Partial update (only provided fields)

---

## Question 7: Exception Handling

### Q:
1. What's the difference between `@ControllerAdvice` and `@ExceptionHandler`? When would you use each?
2. How would you structure a custom exception class for `BookNotFoundException`? What fields should it contain?
3. If you have multiple exception handlers, how does Spring decide which one to use?

### A:

**1. `@ControllerAdvice` vs `@ExceptionHandler`:**

| Annotation | Scope | Use Case |
|-----------|-------|----------|
| **`@ExceptionHandler`** | Single controller class | Handle exceptions for specific controller |
| **`@ControllerAdvice`** | Global - all controllers | Handle exceptions across entire application |

**Example: `@ExceptionHandler` (Local):**
```java
@Controller
public class BookController {
    
    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBookNotFound(BookNotFoundException ex) {
        // Only handles exceptions from this controller
        return ResponseEntity.status(404).body(...);
    }
}
```

**Example: `@ControllerAdvice` (Global):**
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBookNotFound(BookNotFoundException ex) {
        // Handles exceptions from ALL controllers
        return ResponseEntity.status(404).body(...);
    }
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateIsbn(DataIntegrityViolationException ex) {
        // Global handler for duplicate ISBNs
        return ResponseEntity.status(409).body(...);
    }
}
```

**Best Practice**: Use `@ControllerAdvice` for global exception handling (what you need for the assessment).

**2. Custom Exception Structure:**

**Basic Custom Exception:**
```java
public class BookNotFoundException extends RuntimeException {
    
    public BookNotFoundException(String message) {
        super(message);
    }
    
    public BookNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

**Enhanced Custom Exception (Better):**
```java
public class BookNotFoundException extends RuntimeException {
    
    private Long bookId;
    private String isbn;
    private LocalDateTime timestamp;
    
    public BookNotFoundException(Long bookId) {
        super("Book not found with id: " + bookId);
        this.bookId = bookId;
        this.timestamp = LocalDateTime.now();
    }
    
    public BookNotFoundException(String isbn) {
        super("Book not found with ISBN: " + isbn);
        this.isbn = isbn;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters
    public Long getBookId() { return bookId; }
    public String getIsbn() { return isbn; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
```

**Base Exception Class (Best Practice):**
```java
public abstract class LibraryException extends RuntimeException {
    private LocalDateTime timestamp;
    private String errorCode;
    
    public LibraryException(String message) {
        super(message);
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters
}

public class BookNotFoundException extends LibraryException {
    public BookNotFoundException(Long id) {
        super("Book not found with id: " + id);
    }
}
```

**3. Exception Handler Resolution Order:**

Spring resolves exception handlers using this priority:

1. **Most specific exception type wins**
   ```java
   @ExceptionHandler(BookNotFoundException.class)  // More specific
   @ExceptionHandler(RuntimeException.class)       // Less specific
   // BookNotFoundException will match first
   ```

2. **Method-level `@ExceptionHandler` beats `@ControllerAdvice`**
   ```java
   // If BookController has @ExceptionHandler, it takes precedence
   // over @ControllerAdvice in GlobalExceptionHandler
   ```

3. **Multiple `@ControllerAdvice` classes**: Order with `@Order` annotation
   ```java
   @ControllerAdvice
   @Order(1)  // Higher priority
   public class SpecificExceptionHandler { }
   
   @ControllerAdvice
   @Order(2)  // Lower priority
   public class GeneralExceptionHandler { }
   ```

4. **Parameter matching**: Handler with matching exception type is chosen
   ```java
   @ExceptionHandler(BookNotFoundException.class)  // Matches BookNotFoundException
   public ResponseEntity<?> handle(BookNotFoundException ex) { }
   
   @ExceptionHandler(Exception.class)  // Fallback
   public ResponseEntity<?> handle(Exception ex) { }
   ```

**Example Resolution:**
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    // 1. Most specific - catches BookNotFoundException
    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<?> handleBookNotFound(BookNotFoundException ex) {
        return ResponseEntity.status(404).body(...);
    }
    
    // 2. Less specific - catches all validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(400).body(...);
    }
    
    // 3. Generic fallback - catches all other exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {
        return ResponseEntity.status(500).body(...);
    }
}
```

---

## Question 8: Spring Boot Concepts

### Q:
1. What is the purpose of `@SpringBootApplication` annotation? What three annotations does it combine?
2. Explain the difference between `@Autowired`, constructor injection, and `@RequiredArgsConstructor` from Lombok. Which is preferred and why?
3. What happens during Spring Boot application startup? Walk me through the startup process.

### A:

**1. `@SpringBootApplication` annotation:**

`@SpringBootApplication` is a **meta-annotation** that combines three annotations:

1. **`@SpringBootConfiguration`** - Indicates this is a Spring Boot configuration class (extends `@Configuration`)
2. **`@EnableAutoConfiguration`** - Enables Spring Boot's auto-configuration magic
3. **`@ComponentScan`** - Scans for Spring components (`@Component`, `@Service`, `@Repository`, `@Controller`) in the package and sub-packages

**Equivalent to:**
```java
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.library")
public class LibraryManagementApplication {
    // ...
}
```

**Benefits:**
- **Convention over configuration**: Less boilerplate
- **Auto-configuration**: Automatically configures beans based on classpath
- **Component scanning**: Automatically discovers Spring beans

**2. Dependency Injection Methods:**

| Method | Example | Pros | Cons | Preferred? |
|--------|---------|------|------|------------|
| **Field Injection (`@Autowired`)** | `@Autowired private BookRepository repo;` | Simple, concise | Hard to test, not immutable | ❌ Not preferred |
| **Constructor Injection** | Constructor with params | Testable, immutable, required dependencies clear | More verbose | ✅ Preferred |
| **`@RequiredArgsConstructor`** | Lombok generates constructor | Best of both worlds | Requires Lombok | ✅✅ Most preferred |

**Field Injection (Not Recommended):**
```java
@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository;  // Can't be final, hard to test
}
```

**Constructor Injection (Good):**
```java
@Service
public class BookService {
    private final BookRepository bookRepository;
    
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;  // Can be final, testable
    }
}
```

**`@RequiredArgsConstructor` (Best):**
```java
@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;  // Lombok generates constructor
}
```

**Why `@RequiredArgsConstructor` is preferred:**
- ✅ Immutable (fields are `final`)
- ✅ Testable (can inject mocks in constructor)
- ✅ Less boilerplate (Lombok generates constructor)
- ✅ Clear dependencies (all `final` fields are required)
- ✅ Compile-time safety (missing dependency = compilation error)

**3. Spring Boot Application Startup Process:**

**Step-by-step startup:**

1. **JVM starts** → `main()` method is called
   ```java
   SpringApplication.run(LibraryManagementApplication.class, args);
   ```

2. **SpringApplication initialization:**
   - Reads `application.properties/yml`
   - Determines application type (Web, Reactive, etc.)
   - Creates `ApplicationContext`

3. **Application Context Creation:**
   - **Bean Factory** initialization
   - **Bean Definition** scanning (finds `@Component`, `@Service`, etc.)
   - Registers all bean definitions

4. **Bean Instantiation:**
   - Creates beans in dependency order
   - Resolves dependencies (dependency injection)
   - Calls `@PostConstruct` methods
   - Applies `@Autowired` annotations

5. **Auto-Configuration:**
   - Spring Boot scans classpath
   - Finds auto-configuration classes (e.g., `DataSourceAutoConfiguration`)
   - Configures beans based on available dependencies
   - Example: If H2 is in classpath → auto-configures H2 database

6. **Web Server Initialization** (if web app):
   - Embedded Tomcat/Jetty starts
   - DispatcherServlet is registered
   - Controllers are mapped

7. **Application Ready:**
   - `CommandLineRunner` and `ApplicationRunner` beans execute
   - Application is ready to accept requests

**Detailed Flow:**
```
1. main() called
2. SpringApplication.run()
3. Create ApplicationContext
4. Scan for @Component, @Service, @Repository, @Controller
5. Load bean definitions
6. Resolve dependencies
7. Create bean instances (in order)
8. Inject dependencies
9. Call @PostConstruct methods
10. Execute auto-configuration
11. Start embedded web server (if web app)
12. Register DispatcherServlet
13. Map @RequestMapping handlers
14. Execute ApplicationRunner/CommandLineRunner
15. Application ready! 🚀
```

**Example with Logging:**
```
2024-01-01 10:00:00.000  INFO --- Starting LibraryManagementApplication
2024-01-01 10:00:01.000  INFO --- No active profile set, falling back to default profiles: default
2024-01-01 10:00:02.000  INFO --- HikariPool-1 - Starting...
2024-01-01 10:00:03.000  INFO --- HikariPool-1 - Start completed.
2024-01-01 10:00:04.000  INFO --- HHH000227: Running hbm2ddl schema export
2024-01-01 10:00:05.000  INFO --- Started LibraryManagementApplication in 5.234 seconds
```

---

## Question 9: Testing

### Q:
1. How would you test your `BookService`? What mocking framework would you use, and why?
2. What's the difference between `@SpringBootTest` and `@WebMvcTest`? When would you use each?
3. How would you test an endpoint that requires a database? Would you use a real database or mock it?

### A:

**1. Testing `BookService`:**

**Mocking Framework: Mockito** (most common, comes with Spring Boot Test)

**Why Mockito:**
- ✅ Default with Spring Boot Test
- ✅ Simple and intuitive API
- ✅ Well-documented
- ✅ Integrates well with JUnit 5

**Test Example:**
```java
@ExtendWith(MockitoExtension.class)  // JUnit 5
class BookServiceTest {
    
    @Mock
    private BookRepository bookRepository;  // Mock the repository
    
    @InjectMocks
    private BookService bookService;  // Inject mocks into service
    
    @Test
    void testGetBookById_Success() {
        // Arrange
        Long id = 1L;
        Book book = Book.builder()
            .id(id)
            .title("Test Book")
            .author("Test Author")
            .isbn("1234567890123")
            .publicationYear(2023)
            .build();
        
        when(bookRepository.findById(id)).thenReturn(Optional.of(book));
        
        // Act
        Book result = bookService.getBookById(id);
        
        // Assert
        assertNotNull(result);
        assertEquals("Test Book", result.getTitle());
        verify(bookRepository).findById(id);
    }
    
    @Test
    void testGetBookById_NotFound() {
        // Arrange
        Long id = 999L;
        when(bookRepository.findById(id)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(BookNotFoundException.class, () -> {
            bookService.getBookById(id);
        });
    }
    
    @Test
    void testCreateBook_Success() {
        // Arrange
        Book book = Book.builder()
            .title("New Book")
            .isbn("1234567890123")
            .build();
        
        Book savedBook = book.toBuilder().id(1L).build();
        
        when(bookRepository.existsByIsbn(book.getIsbn())).thenReturn(false);
        when(bookRepository.save(book)).thenReturn(savedBook);
        
        // Act
        Book result = bookService.createBook(book);
        
        // Assert
        assertNotNull(result.getId());
        assertEquals("New Book", result.getTitle());
        verify(bookRepository).existsByIsbn(book.getIsbn());
        verify(bookRepository).save(book);
    }
}
```

**2. `@SpringBootTest` vs `@WebMvcTest`:**

| Annotation | What it loads | Use Case | Speed |
|-----------|--------------|----------|-------|
| **`@SpringBootTest`** | Full application context (all beans, full auto-configuration) | Integration tests, full stack tests | Slower |
| **`@WebMvcTest`** | Only web layer (controllers, filters, exception handlers), mocks services/repositories | Unit tests for controllers | Faster |

**`@SpringBootTest` Example (Integration Test):**
```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookServiceIntegrationTest {
    
    @Autowired
    private BookService bookService;
    
    @Autowired
    private BookRepository bookRepository;
    
    @Test
    void testCreateBook_Integration() {
        // Uses real database (H2 in-memory or test database)
        Book book = Book.builder()
            .title("Integration Test Book")
            .isbn("9999999999999")
            .build();
        
        Book saved = bookService.createBook(book);
        
        assertNotNull(saved.getId());
        assertTrue(bookRepository.existsById(saved.getId()));
    }
}
```

**`@WebMvcTest` Example (Controller Unit Test):**
```java
@WebMvcTest(BookController.class)
class BookControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean  // Mock the service (not loaded)
    private BookService bookService;
    
    @Test
    void testGetBookById_Success() throws Exception {
        Book book = Book.builder().id(1L).title("Test").build();
        when(bookService.getBookById(1L)).thenReturn(book);
        
        mockMvc.perform(get("/api/books/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Test"));
    }
}
```

**When to use each:**
- **`@WebMvcTest`**: Test controller layer in isolation (fast, focused)
- **`@SpringBootTest`**: Test full application integration (slower, comprehensive)

**3. Testing with Database:**

**Options:**

**Option 1: In-Memory Database (H2) - Recommended for Tests**
```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class BookRepositoryTest {
    
    @Autowired
    private BookRepository bookRepository;
    
    @Test
    void testSaveAndFind() {
        Book book = new Book(...);
        Book saved = bookRepository.save(book);
        
        Optional<Book> found = bookRepository.findById(saved.getId());
        assertTrue(found.isPresent());
    }
}
```

**Option 2: Test Containers (Real Database in Docker)**
```java
@Testcontainers
@SpringBootTest
class BookRepositoryTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");
    
    @Test
    void testWithRealPostgreSQL() {
        // Uses real PostgreSQL in Docker container
    }
}
```

**Option 3: Mock Repository (No Database)**
```java
@ExtendWith(MockitoExtension.class)
class BookServiceTest {
    @Mock
    private BookRepository bookRepository;  // Mocked, no database
    
    @Test
    void testWithoutDatabase() {
        // Fast, but doesn't test database interactions
    }
}
```

**Best Practice:**
- **Unit Tests**: Mock repository (no database) - fast
- **Integration Tests**: Use H2 in-memory database - good balance
- **E2E Tests**: Use TestContainers with real database - most realistic

**For Assessment:**
Use H2 in-memory database - simple and sufficient:
```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookServiceIntegrationTest {
    // Uses H2 from application.properties for tests
}
```

---

## Question 10: Best Practices

### Q:
1. Should DTOs (Data Transfer Objects) be used for request/response in your REST API? Why or why not?
2. How would you implement pagination for the `getAllBooks()` method? What parameters would you accept?
3. What security considerations should you keep in mind for this API? What would you add before deploying to production?

### A:

**1. DTOs for Request/Response:**

**Yes, DTOs should be used!** Here's why:

**Problems with exposing Entity directly:**
- ❌ Exposes internal database structure
- ❌ Security risk (may expose sensitive fields)
- ❌ Tight coupling between API and database schema
- ❌ Can't control what fields are serialized
- ❌ Validation conflicts (entity validation vs API validation)

**Benefits of DTOs:**
- ✅ Decouples API contract from database schema
- ✅ Control over what data is exposed
- ✅ Different validation rules for API vs entity
- ✅ Versioning API without changing entities
- ✅ Better performance (only serialize needed fields)

**Example:**
```java
// Entity (internal)
@Entity
public class Book {
    private Long id;
    private String title;
    private LocalDateTime createdAt;  // Do we want to expose this?
}

// Request DTO (what client sends)
@Data
public class BookRequestDTO {
    @NotBlank
    @Size(max = 200)
    private String title;
    
    @NotBlank
    private String author;
    
    // No id, createdAt - client doesn't provide these
}

// Response DTO (what client receives)
@Data
public class BookResponseDTO {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private Integer publicationYear;
    private Boolean available;
    private LocalDateTime createdAt;  // Can include or exclude
    private LocalDateTime updatedAt;
}
```

**Implementation:**
```java
@PostMapping("/books")
public ResponseEntity<BookResponseDTO> createBook(
        @Valid @RequestBody BookRequestDTO requestDTO) {
    Book book = bookMapper.toEntity(requestDTO);
    Book saved = bookService.createBook(book);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(bookMapper.toDTO(saved));
}
```

**2. Pagination Implementation:**

**Using Spring Data JPA Pagination:**

**Repository:**
```java
public interface BookRepository extends JpaRepository<Book, Long> {
    // Spring Data provides findAll(Pageable pageable) automatically
}
```

**Service:**
```java
public Page<BookResponseDTO> getAllBooks(int page, int size, String sortBy, String sortDir) {
    Sort sort = sortDir.equalsIgnoreCase("asc") 
        ? Sort.by(sortBy).ascending() 
        : Sort.by(sortBy).descending();
    
    Pageable pageable = PageRequest.of(page, size, sort);
    Page<Book> books = bookRepository.findAll(pageable);
    
    return books.map(bookMapper::toDTO);
}
```

**Controller:**
```java
@GetMapping("/books")
public ResponseEntity<Page<BookResponseDTO>> getAllBooks(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") String sortDir) {
    
    Page<BookResponseDTO> books = bookService.getAllBooks(page, size, sortBy, sortDir);
    return ResponseEntity.ok(books);
}
```

**Request Example:**
```
GET /api/books?page=0&size=10&sortBy=title&sortDir=asc
```

**Response Format:**
```json
{
  "content": [
    {"id": 1, "title": "Book 1", ...},
    {"id": 2, "title": "Book 2", ...}
  ],
  "pageable": {...},
  "totalElements": 100,
  "totalPages": 10,
  "size": 10,
  "number": 0,
  "first": true,
  "last": false
}
```

**Alternative: Custom Pagination Response:**
```java
@Data
@Builder
public class PaginatedResponse<T> {
    private List<T> data;
    private int currentPage;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
}
```

**3. Security Considerations for Production:**

**Must-Have Security Features:**

1. **Authentication & Authorization**
   ```java
   // Add Spring Security
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-security</artifactId>
   </dependency>
   ```
   - JWT tokens or OAuth2
   - Role-based access control (RBAC)
   - Secure endpoints (some may be public, some require auth)

2. **Input Validation & Sanitization**
   - ✅ Already have Bean Validation (`@Valid`)
   - Add SQL injection prevention (JPA handles this, but be careful with native queries)
   - XSS prevention (escape output in responses)

3. **HTTPS/SSL**
   - Enable HTTPS in production
   - Use TLS 1.3
   - Redirect HTTP to HTTPS

4. **Rate Limiting**
   ```java
   // Use Spring Cloud Gateway or implement custom rate limiting
   @RateLimiter(name = "bookApi")
   public ResponseEntity<?> createBook(...) { }
   ```

5. **CORS Configuration**
   ```java
   @Configuration
   public class CorsConfig {
       @Bean
       public WebMvcConfigurer corsConfigurer() {
           return new WebMvcConfigurer() {
               @Override
               public void addCorsMappings(CorsRegistry registry) {
                   registry.addMapping("/api/**")
                       .allowedOrigins("https://yourdomain.com")
                       .allowedMethods("GET", "POST", "PUT", "DELETE")
                       .allowCredentials(true);
               }
           };
       }
   }
   ```

6. **Error Handling**
   - Don't expose stack traces in production
   - Generic error messages for 500 errors
   - Log detailed errors server-side

7. **Sensitive Data Protection**
   - Don't log sensitive data (passwords, tokens)
   - Encrypt sensitive fields in database
   - Use environment variables for secrets

8. **Database Security**
   - Use connection pooling with proper limits
   - Use parameterized queries (JPA does this)
   - Regular backups
   - Database user with minimal privileges

9. **API Documentation**
   ```java
   // Add Swagger/OpenAPI
   <dependency>
       <groupId>org.springdoc</groupId>
       <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
   </dependency>
   ```

10. **Monitoring & Logging**
    - Add Actuator for health checks
    - Structured logging (JSON format)
    - Monitor API performance
    - Set up alerts for errors

**Security Configuration Example:**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()  // If using stateless JWT
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/books/**").authenticated()
                .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().permitAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtDecoder(jwtDecoder()))
            )
            .headers(headers -> headers
                .frameOptions().sameOrigin()  // For H2 console
            );
        return http.build();
    }
}
```

**Production Checklist:**
- [ ] Authentication enabled
- [ ] HTTPS configured
- [ ] Rate limiting implemented
- [ ] CORS properly configured
- [ ] Input validation on all endpoints
- [ ] Error handling doesn't expose sensitive info
- [ ] Database credentials in environment variables
- [ ] Logging configured (not logging sensitive data)
- [ ] Health checks enabled (Actuator)
- [ ] API documentation (Swagger)
- [ ] Monitoring and alerts set up

---

## Summary

These questions cover the essential Spring Boot concepts you'll encounter in interviews and while building your assessment. Key takeaways:

1. **Entity Design**: Use JPA annotations correctly, understand Hibernate lifecycle
2. **Lombok**: Know what it generates and when to use it
3. **Validation**: Understand when and how validation executes
4. **Service Layer**: Handle exceptions properly, transform low-level to business exceptions
5. **REST API**: Use proper HTTP status codes and methods
6. **Testing**: Understand different types of tests and when to use each
7. **Security**: Always consider security in production applications

Good luck with your assessment! 🚀

