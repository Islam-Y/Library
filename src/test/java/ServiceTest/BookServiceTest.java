package ServiceTest;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.library.dto.BookDTO;
import com.library.exception.BookServiceException;
import com.library.mapper.BookMapper;
import com.library.model.Author;
import com.library.model.Book;
import com.library.model.Publisher;
import com.library.repository.AuthorDAO;
import com.library.repository.BookDAO;
import com.library.repository.PublisherDAO;
import com.library.service.BookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookDAO bookDAO;

    @Mock
    private AuthorDAO authorDAO;

    @Mock
    private PublisherDAO publisherDAO;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookService bookService;

    @Test
    void getAllBooks_ShouldReturnList() throws SQLException {
        // Arrange
        Book book = createTestBook(1);
        BookDTO bookDTO = new BookDTO(book);

        when(bookDAO.getAll()).thenReturn(List.of(book));
        when(bookMapper.toDTO(book)).thenReturn(bookDTO); // Настройка маппера

        // Act
        List<BookDTO> result = bookService.getAllBooks();

        // Assert
        assertThat(result)
                .hasSize(1)
                .first()
                .extracting(BookDTO::getTitle)
                .isEqualTo("1984");
        verify(bookMapper).toDTO(book); // Проверка вызова маппера
    }

    @Test
    void getBookById_WhenExists_ShouldReturnDTO() throws SQLException {
        // Arrange
        Book book = createTestBook(1);
        BookDTO bookDTO = new BookDTO(book);

        when(bookDAO.getById(1)).thenReturn(Optional.of(book));
        when(bookMapper.toDTO(book)).thenReturn(bookDTO); // Настройка маппера

        // Act
        BookDTO result = bookService.getBookById(1);

        // Assert
        assertThat(result)
                .extracting(
                        BookDTO::getTitle,
                        BookDTO::getGenre,
                        dto -> dto.getAuthorIds().size()
                )
                .containsExactly("1984", "Антиутопия", 1);
        verify(bookMapper).toDTO(book);
    }

    @Test
    void getBookById_WhenNotExists_ShouldThrow() throws SQLException {
        // Arrange
            when(bookDAO.getById(anyInt())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> bookService.getBookById(1))
                .isInstanceOf(BookServiceException.class)
                .hasMessageContaining("Книга не найдена");
    }

    @Test
    void addBook_ShouldMapAndSave() throws SQLException {
        // Arrange
        Book book = createTestBook(0);
        BookDTO bookDTO = new BookDTO(book);

        when(bookMapper.toModel(bookDTO)).thenReturn(book); // Настройка преобразования DTO -> Model

        // Act
        bookService.addBook(bookDTO);

        // Assert
        verify(bookMapper).toModel(bookDTO);
        verify(bookDAO).create(refEq(book, "id"));
    }

    @Test
    void updateBook_WhenExists_ShouldUpdate() throws SQLException {
        // Arrange
        Book existing = createTestBook(1);
        Book updated = createTestBook(1);
        updated.setTitle("Новое название");
        BookDTO updatedDTO = new BookDTO(updated);

        when(bookDAO.getById(1)).thenReturn(Optional.of(existing));

        // Act
        bookService.updateBook(1, updatedDTO);

        // Assert
        verify(bookDAO).update(argThat(b ->
                b.getTitle().equals("Новое название")
        ));
    }

    @Test
    void deleteBook_ShouldCallDAO() throws SQLException {
        // Act
        bookService.deleteBook(1);

        // Assert
        verify(bookDAO).delete(1);
    }

    private Book createTestBook(int id) {
        Book book = new Book();
        book.setId(id);
        book.setTitle("1984");
        book.setPublishedDate(LocalDate.now().toString());
        book.setGenre("Антиутопия");
        book.setPublisher(new Publisher(1, "Издатель", new ArrayList<>()));
        book.setAuthors(new HashSet<>(Set.of(new Author(1, "Джордж", "Оруэлл", "Англия", new HashSet<>()))));
        return book;
    }
}