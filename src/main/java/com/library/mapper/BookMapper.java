package com.library.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.library.dto.BookRequestDTO;
import com.library.dto.BookResponseDTO;
import com.library.entity.Book;

@Component
public class BookMapper {

    public BookResponseDTO toBookResponseDTO(Book book){
        BookResponseDTO bookResponseDTO = new BookResponseDTO();
        bookResponseDTO.setId(book.getId());
        bookResponseDTO.setTitle(book.getTitle());
        bookResponseDTO.setAuthor(book.getAuthor());
        bookResponseDTO.setIsbn(book.getIsbn());
        bookResponseDTO.setPublicationYear(book.getPublicationYear());
        bookResponseDTO.setAvailable(book.getAvailable());
        bookResponseDTO.setCreatedAt(book.getCreatedAt());
        bookResponseDTO.setUpdatedAt(book.getUpdatedAt());
        return bookResponseDTO;
    }

    public Book toBook(BookRequestDTO bookRequestDTO){
        Book book = new Book();
        book.setTitle(bookRequestDTO.getTitle());
        book.setAuthor(bookRequestDTO.getAuthor());
        book.setIsbn(bookRequestDTO.getIsbn());
        book.setPublicationYear(bookRequestDTO.getPublicationYear());
        return book;
    }

    public List<BookResponseDTO> toBookResponseDTOList(List<Book> books){
        return books.stream().map(this::toBookResponseDTO).collect(Collectors.toList());
    }
}

