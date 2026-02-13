package com.library.dto;


import com.library.validation.YearMax;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BookRequestDTO {
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be less than 200 characters")
    private String title;
    @NotBlank(message = "Author is required")
    @Size(max = 100, message = "Author must be less than 100 characters")
    private String author;
    @NotNull(message = "ISBN is required")
    @Pattern(regexp = "^\\d{13}$", message = "ISBN must be exactly 13 digits")
    private String isbn;
    @NotNull(message = "Publication year is required")
    @Min(value = 1000, message = "Publication year must be greater than 1000")
    @YearMax( message = "Publication year must not exceed the current year")
    private Integer publicationYear;

}
