package com.library.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class BookResponseDTO {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private Integer publicationYear;
    private Boolean available;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
