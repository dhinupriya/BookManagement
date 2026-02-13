package com.library.repository;

import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.library.entity.Book;

@Repository
public interface BookRepository  extends JpaRepository<Book, Long>{
    // Add a custom query method to find books by author name (case-insensitive)

    List<Book> findByAuthorIgnoreCase(String author);

    List<Book> findByAvailableTrue();

    boolean existsByIsbn(String isbn);
}
