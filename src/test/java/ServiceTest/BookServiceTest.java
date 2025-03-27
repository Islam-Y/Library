package ServiceTest;

import com.library.dto.BookDTO;
import com.library.exception.BookServiceException;
import com.library.mapper.BookMapper;
import com.library.model.Book;
import com.library.repository.BookDAO;
import com.library.service.BookService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BookServiceTest {

    @Mock
    private BookDAO bookDAO;
    @Mock
    private BookMapper bookMapper;

    private BookService bookService;

    private Book testBook;
    private BookDTO testBookDTO;

    @Before
    public void setUp() {
        // Предполагается, что добавлен метод forTest в BookService для тестирования
        bookService = BookService.forTest(bookDAO, bookMapper);

        testBook = new Book();
        testBook.setId(1);
        testBook.setTitle("Test Book");
        testBook.setPublishedDate(new String());

        testBookDTO = new BookDTO();
        testBookDTO.setId(1);
        testBookDTO.setTitle("Test Book");
        testBookDTO.setPublishedDate(testBook.getPublishedDate());

        when(bookMapper.toDTO(any(Book.class))).thenReturn(testBookDTO);
    }

    @Test
    public void getAllBooks_Success() throws SQLException {
        when(bookDAO.getAll()).thenReturn(Collections.singletonList(testBook));

        List<BookDTO> result = bookService.getAllBooks();

        assertEquals(1, result.size());
        assertEquals("Test Book", result.get(0).getTitle());
    }

    @Test(expected = BookServiceException.class)
    public void getAllBooks_Exception() throws SQLException {
        when(bookDAO.getAll()).thenThrow(new SQLException("DB error"));
        bookService.getAllBooks();
    }

    @Test
    public void getBookById_Success() throws SQLException {
        when(bookDAO.getById(1)).thenReturn(Optional.of(testBook));

        BookDTO result = bookService.getBookById(1);

        assertEquals(1, result.getId());
        assertEquals("Test Book", result.getTitle());
    }

    @Test(expected = BookServiceException.class)
    public void getBookById_NotFound() throws SQLException {
        when(bookDAO.getById(anyInt())).thenReturn(Optional.empty());
        bookService.getBookById(1);
    }

    @Test
    public void addBook_Success() throws SQLException {
        BookDTO inputDTO = new BookDTO();
        inputDTO.setTitle("New Book");

        Book expectedBook = new Book();
        expectedBook.setTitle("New Book");

        when(bookMapper.toModel(inputDTO)).thenReturn(expectedBook);

        bookService.addBook(inputDTO);

        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookDAO).create(captor.capture());

        Book savedBook = captor.getValue();
        assertEquals("New Book", savedBook.getTitle());
    }

    @Test(expected = BookServiceException.class)
    public void addBook_SQLException() throws SQLException {
        BookDTO inputDTO = new BookDTO();
        inputDTO.setTitle("New Book");

        Book expectedBook = new Book();
        expectedBook.setTitle("New Book");

        when(bookMapper.toModel(inputDTO)).thenReturn(expectedBook);
        doThrow(new SQLException("DB error")).when(bookDAO).create(expectedBook);

        bookService.addBook(inputDTO);
    }

    @Test
    public void updateBook_Success() throws SQLException {
        Book existingBook = new Book();
        existingBook.setId(1);
        existingBook.setTitle("Old Title");
        existingBook.setPublishedDate(new String());

        when(bookDAO.getById(1)).thenReturn(Optional.of(existingBook));

        BookDTO updateDTO = new BookDTO();
        updateDTO.setTitle("Updated Title");
        updateDTO.setPublishedDate(new String());

        bookService.updateBook(1, updateDTO);

        verify(bookDAO).update(existingBook);
        assertEquals("Updated Title", existingBook.getTitle());
    }

    @Test(expected = BookServiceException.class)
    public void updateBook_NotFound() throws SQLException {
        when(bookDAO.getById(1)).thenReturn(Optional.empty());
        bookService.updateBook(1, new BookDTO());
    }

    @Test(expected = BookServiceException.class)
    public void updateBook_SQLException() throws SQLException {
        Book existingBook = new Book();
        existingBook.setId(1);
        existingBook.setTitle("Old Title");
        existingBook.setPublishedDate(new String());

        when(bookDAO.getById(1)).thenReturn(Optional.of(existingBook));
        doThrow(new SQLException()).when(bookDAO).update(existingBook);

        BookDTO updateDTO = new BookDTO();
        updateDTO.setTitle("Updated Title");
        updateDTO.setPublishedDate(new String());

        bookService.updateBook(1, updateDTO);
    }

    @Test
    public void deleteBook_Success() throws SQLException {
        bookService.deleteBook(1);
        verify(bookDAO).delete(1);
    }

    @Test(expected = BookServiceException.class)
    public void deleteBook_SQLException() throws SQLException {
        doThrow(new SQLException()).when(bookDAO).delete(1);
        bookService.deleteBook(1);
    }
}

