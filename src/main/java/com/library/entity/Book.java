package com.library.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.library.validation.YearMax;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*Create a `Book` entity with the following fields:
- `id` (Long, auto-generated)
- `title` (String, required, max 200 characters)
- `author` (String, required, max 100 characters)
- `isbn` (String, required, unique, exactly 13 characters)
- `publicationYear` (Integer, required, between 1000 and current year)
- `available` (Boolean, defaults to true)
- `createdAt` (LocalDateTime, auto-set on creation)
- `updatedAt` (LocalDateTime, auto-updated on modification) */

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be less than 200 characters")
    private String title;

    @NotBlank(message = "Author is required")
    @Size(max = 100, message = "Author must be less than 100 characters")
    private String author;

    @NotBlank(message = "ISBN is required")
    @Column(unique = true, nullable = false)
    @Size(min = 13, max = 13, message = "ISBN must be exactly 13 characters")
    @Pattern(regexp = "^\\d{13}$", message = "ISBN must be exactly 13 digits")
    private String isbn;

    @NotNull(message = "Publication year is required")
    @Min(value = 1000, message = "Publication year must be greater than 1000")
    @YearMax(message = "Publication year must be less than or equal to the current year")
    private Integer publicationYear;

    @Column(columnDefinition = "boolean default true")
    @Builder.Default
    private Boolean available = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate(){
        if(this.available == null){
            this.available = true;
        }
        
    }

  
}