package com.library.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.library.dto.BookRequestDTO;
import com.library.dto.BookResponseDTO;
import com.library.entity.Book;
import com.library.exception.BookNotFoundException;
import com.library.mapper.BookMapper;
import com.library.repository.BookRepository;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;
    
    @InjectMocks
    private BookService bookService;

    @Test
    void testGetAllBooks() {
        // Arrange
        List<Book> books = new ArrayList<>();
        books.add(Book.builder().id(1L).title("Book 1").author("Author 1")
                .isbn("1234567890123").publicationYear(2020).available(true).build());
        books.add(Book.builder().id(2L).title("Book 2").author("Author 2")
                .isbn("1234567890124").publicationYear(2021).available(true).build());
        books.add(Book.builder().id(3L).title("Book 3").author("Author 3")
                .isbn("1234567890125").publicationYear(2022).available(true).build());
        when(bookRepository.findAll()).thenReturn(books);

        List<BookResponseDTO> expectedBooks = new ArrayList<>();
        BookResponseDTO dto1 = new BookResponseDTO();
        dto1.setId(1L);
        dto1.setTitle("Book 1");
        dto1.setAuthor("Author 1");
        dto1.setIsbn("1234567890123");
        dto1.setPublicationYear(2020);
        dto1.setAvailable(true);
        expectedBooks.add(dto1);
        
        BookResponseDTO dto2 = new BookResponseDTO();
        dto2.setId(2L);
        dto2.setTitle("Book 2");
        dto2.setAuthor("Author 2");
        dto2.setIsbn("1234567890124");
        dto2.setPublicationYear(2021);
        dto2.setAvailable(true);
        expectedBooks.add(dto2);
        
        BookResponseDTO dto3 = new BookResponseDTO();
        dto3.setId(3L);
        dto3.setTitle("Book 3");
        dto3.setAuthor("Author 3");
        dto3.setIsbn("1234567890125");
        dto3.setPublicationYear(2022);
        dto3.setAvailable(true);
        expectedBooks.add(dto3);
        
        when(bookMapper.toBookResponseDTOList(books)).thenReturn(expectedBooks);

        // Act
        List<BookResponseDTO> result = bookService.getAllBooks();

        // Assert
        assertEquals(expectedBooks, result);
    }

    @Test
    void testGetBookById() {
        // Arrange
        Long id = 1L;
        Book book = Book.builder().id(1L).title("Book 1").author("Author 1")
                .isbn("1234567890123").publicationYear(2020).available(true).build();
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        BookResponseDTO responseDTO = new BookResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setTitle("Book 1");
        responseDTO.setAuthor("Author 1");
        responseDTO.setIsbn("1234567890123");
        responseDTO.setPublicationYear(2020);
        responseDTO.setAvailable(true);
        when(bookMapper.toBookResponseDTO(book)).thenReturn(responseDTO);

        // Act
        BookResponseDTO result = bookService.getBookById(id);

        // Assert
        assertEquals(responseDTO, result);
    }

    @Test
    void testCreateBook() {
        // Arrange
        BookRequestDTO bookRequestDTO = new BookRequestDTO();
        bookRequestDTO.setTitle("Book 1");
        bookRequestDTO.setAuthor("Author 1");
        bookRequestDTO.setIsbn("1234567890123");
        bookRequestDTO.setPublicationYear(2020);
        
        Book book = Book.builder().id(1L).title("Book 1").author("Author 1")
                .isbn("1234567890123").publicationYear(2020).available(true).build();
        
        BookResponseDTO responseDTO = new BookResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setTitle("Book 1");
        responseDTO.setAuthor("Author 1");
        responseDTO.setIsbn("1234567890123");
        responseDTO.setPublicationYear(2020);
        responseDTO.setAvailable(true);
        
        when(bookRepository.existsByIsbn("1234567890123")).thenReturn(false);
        when(bookMapper.toBook(bookRequestDTO)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toBookResponseDTO(book)).thenReturn(responseDTO);

        // Act
        BookResponseDTO result = bookService.createBook(bookRequestDTO);

        // Assert
        assertEquals(responseDTO, result);
        verify(bookRepository, times(1)).existsByIsbn("1234567890123");
        verify(bookRepository, times(1)).save(book);
    }

    @Test
    void testUpdateBook() {
        // Arrange
        Long id = 1L;
        Book book = Book.builder().id(1L).title("Book 1").author("Author 1")
                .isbn("1234567890123").publicationYear(2020).available(true).build();
        
        BookRequestDTO bookRequestDTO = new BookRequestDTO();
        bookRequestDTO.setTitle("Updated Book");
        bookRequestDTO.setAuthor("Updated Author");
        bookRequestDTO.setIsbn("1234567890123");
        bookRequestDTO.setPublicationYear(2020);
        
        BookResponseDTO responseDTO = new BookResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setTitle("Updated Book");
        responseDTO.setAuthor("Updated Author");
        responseDTO.setIsbn("1234567890123");
        responseDTO.setPublicationYear(2020);
        responseDTO.setAvailable(true);
        
        when(bookRepository.findById(id)).thenReturn(Optional.of(book));
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toBookResponseDTO(book)).thenReturn(responseDTO);

        // Act
        BookResponseDTO result = bookService.updateBook(id, bookRequestDTO);

        // Assert
        assertEquals(responseDTO, result);
        verify(bookRepository, times(1)).findById(id);
        verify(bookRepository, times(1)).save(book);
    }

    @Test
    void testDeleteBook() {
        // Arrange
        Long id = 1L;
        doNothing().when(bookRepository).deleteById(id);

        // Act
        String result = bookService.deleteBook(id);

        // Assert
        assertEquals("Book deleted successfully", result);
        verify(bookRepository, times(1)).deleteById(id);
    }

    @Test
    void testGetBooksByAuthor() {
        // Arrange
        String author = "Author 1";
        List<Book> books = new ArrayList<>();
        books.add(Book.builder().id(1L).title("Book 1").author("Author 1")
                .isbn("1234567890123").publicationYear(2020).available(true).build());
        books.add(Book.builder().id(2L).title("Book 2").author("Author 1")
                .isbn("1234567890124").publicationYear(2021).available(true).build());
        
        List<BookResponseDTO> expectedDTOs = new ArrayList<>();
        BookResponseDTO dto1 = new BookResponseDTO();
        dto1.setId(1L);
        dto1.setTitle("Book 1");
        dto1.setAuthor("Author 1");
        dto1.setIsbn("1234567890123");
        dto1.setPublicationYear(2020);
        dto1.setAvailable(true);
        expectedDTOs.add(dto1);
        
        BookResponseDTO dto2 = new BookResponseDTO();
        dto2.setId(2L);
        dto2.setTitle("Book 2");
        dto2.setAuthor("Author 1");
        dto2.setIsbn("1234567890124");
        dto2.setPublicationYear(2021);
        dto2.setAvailable(true);
        expectedDTOs.add(dto2);
        
        when(bookRepository.findByAuthorIgnoreCase(author)).thenReturn(books);
        when(bookMapper.toBookResponseDTOList(books)).thenReturn(expectedDTOs);
        
        // Act
        List<BookResponseDTO> result = bookService.getBooksByAuthor(author);
        
        // Assert
        assertEquals(expectedDTOs, result);
        verify(bookRepository, times(1)).findByAuthorIgnoreCase(author);
    }

    @Test
    void testGetAvailableBooks() {
        // Arrange
        List<Book> books = new ArrayList<>();
        books.add(Book.builder().id(1L).title("Book 1").author("Author 1")
                .isbn("1234567890123").publicationYear(2020).available(true).build());
        books.add(Book.builder().id(2L).title("Book 2").author("Author 1")
                .isbn("1234567890124").publicationYear(2021).available(true).build());
      
        List<BookResponseDTO> expectedDTOs = new ArrayList<>();
        BookResponseDTO dto1 = new BookResponseDTO();
        dto1.setId(1L);
        dto1.setTitle("Book 1");
        dto1.setAuthor("Author 1");
        dto1.setIsbn("1234567890123");
        dto1.setPublicationYear(2020);
        dto1.setAvailable(true);
        expectedDTOs.add(dto1);
        
        BookResponseDTO dto2 = new BookResponseDTO();
        dto2.setId(2L);
        dto2.setTitle("Book 2");
        dto2.setAuthor("Author 1");
        dto2.setIsbn("1234567890124");
        dto2.setPublicationYear(2021);
        dto2.setAvailable(true);
        expectedDTOs.add(dto2);
        
        when(bookRepository.findByAvailableTrue()).thenReturn(books);
        when(bookMapper.toBookResponseDTOList(books)).thenReturn(expectedDTOs);

        // Act
        List<BookResponseDTO> result = bookService.getAvailableBooks();

        // Assert
        assertEquals(expectedDTOs, result);
    }

    @Test
    void testGetBookById_ThrowsBookNotFoundException() {
        // Arrange
        Long bookId = 1L;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());
        
        // Act & Assert
        BookNotFoundException exception = assertThrows(
            BookNotFoundException.class,
            () -> bookService.getBookById(bookId)
        );
        
        assertEquals("Book not found with id: 1", exception.getMessage());
    }

    @Test
    void testCreateBook_ThrowsDataIntegrityViolationException() {
        // Arrange
        BookRequestDTO bookRequestDTO = new BookRequestDTO();
        bookRequestDTO.setTitle("Book 1");
        bookRequestDTO.setAuthor("Author 1");
        bookRequestDTO.setIsbn("1234567890123");
        bookRequestDTO.setPublicationYear(2020);
        
        when(bookRepository.existsByIsbn("1234567890123")).thenReturn(true);
        
        // Act & Assert
        assertThrows(
            DataIntegrityViolationException.class,
            () -> bookService.createBook(bookRequestDTO)
        );
    }
}

