package com.library.controller;

import java.util.List;

import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.library.dto.BookRequestDTO;
import com.library.dto.BookResponseDTO;

import com.library.service.BookService;

import jakarta.validation.Valid;




@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {

        this.bookService = bookService;
    }

    @GetMapping
    public ResponseEntity<List<BookResponseDTO>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDTO> getBookById(@PathVariable Long id) {
        BookResponseDTO book = bookService.getBookById(id);
        return ResponseEntity.ok(book);
    }

    @GetMapping("/search")
    public ResponseEntity<List<BookResponseDTO>> getBooksByAuthor(@RequestParam String author){
        List<BookResponseDTO> books = bookService.getBooksByAuthor(author);
        return ResponseEntity.status(HttpStatus.OK).body(books);
    }

    @GetMapping("/available")
    public ResponseEntity<List<BookResponseDTO>> getAvailableBooks() {
        List<BookResponseDTO> books = bookService.getAvailableBooks();
        return ResponseEntity.status(HttpStatus.OK).body(books);
    }

    @PostMapping
    public ResponseEntity<BookResponseDTO> createBook(@RequestBody BookRequestDTO bookRequestDTO) {
        BookResponseDTO createdBook = bookService.createBook(bookRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponseDTO> updateBook( @PathVariable Long id,@Valid @RequestBody BookRequestDTO bookRequestDTO) {
        BookResponseDTO updatedBook = bookService.updateBook(id, bookRequestDTO);
        return ResponseEntity.ok(updatedBook);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    
}

