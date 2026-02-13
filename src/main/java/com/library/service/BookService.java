package com.library.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.library.dto.BookRequestDTO;
import com.library.dto.BookResponseDTO;
import com.library.entity.Book;
import com.library.exception.BookNotFoundException;
import com.library.mapper.BookMapper;
import com.library.repository.BookRepository;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    
    public BookService(BookRepository bookRepository, BookMapper bookMapper) {
        this.bookMapper = bookMapper;
        this.bookRepository = bookRepository;
    }

    public  List<BookResponseDTO> getAllBooks() {
         List<Book> books = bookRepository.findAll();
         return bookMapper.toBookResponseDTOList(books);
    }
    public BookResponseDTO getBookById(Long id) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));
        return bookMapper.toBookResponseDTO(book);
    }

    public List<BookResponseDTO> getAvailableBooks() {
        List<Book> books = bookRepository.findByAvailableTrue();
        return bookMapper.toBookResponseDTOList(books);
    }

    public BookResponseDTO createBook(BookRequestDTO bookRequestDTO){
        if(bookRepository.existsByIsbn(bookRequestDTO.getIsbn())){
            throw new DataIntegrityViolationException("ISBN already exists");
        }
        Book book = bookMapper.toBook(bookRequestDTO);
        Book createdBook = bookRepository.save(book);
        return bookMapper.toBookResponseDTO(createdBook);
    }

    public BookResponseDTO updateBook(Long id, BookRequestDTO bookRequestDTO){
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));
        
        // Check if ISBN is being changed and if the new ISBN already exists for another book
        if (!book.getIsbn().equals(bookRequestDTO.getIsbn()) && 
            bookRepository.existsByIsbn(bookRequestDTO.getIsbn())) {
            throw new DataIntegrityViolationException("ISBN already exists");
        }
       
        book.setTitle(bookRequestDTO.getTitle());
        book.setAuthor(bookRequestDTO.getAuthor());
        book.setIsbn(bookRequestDTO.getIsbn());
        book.setPublicationYear(bookRequestDTO.getPublicationYear());
        Book updatedBook = bookRepository.save(book);
        return bookMapper.toBookResponseDTO(updatedBook);
    }

    public String deleteBook(Long id){
         bookRepository.deleteById(id);
         return "Book deleted successfully";
    }

    public List<BookResponseDTO> getBooksByAuthor(String AuthorName){
        List<Book> books = bookRepository.findByAuthorIgnoreCase(AuthorName);
        return bookMapper.toBookResponseDTOList(books);
    }
}