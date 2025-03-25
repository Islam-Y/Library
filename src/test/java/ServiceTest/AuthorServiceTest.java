package ServiceTest;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.library.dto.AuthorDTO;
import com.library.exception.AuthorServiceException;
import com.library.mapper.AuthorMapper;
import com.library.model.Author;
import com.library.model.Book;
import com.library.repository.AuthorDAO;
import com.library.service.AuthorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {

    @Mock
    private AuthorDAO authorDAO;

    @Mock
    private AuthorMapper authorMapper;

    @InjectMocks
    private AuthorService authorService;

    @Test
    void getAllAuthors_ShouldReturnList() throws SQLException {
        // Arrange
        Author author = new Author(1, "Лев", "Толстой", "Россия", new HashSet<>());
        AuthorDTO authorDTO = new AuthorDTO(author);
        when(authorDAO.getAll()).thenReturn(List.of(author));
        when(authorMapper.toDTO(author)).thenReturn(authorDTO);

        // Act
        List<AuthorDTO> result = authorService.getAllAuthors();

        // Assert
        assertThat(result)
                .hasSize(1)
                .first()
                .extracting(AuthorDTO::getSurname)
                .isEqualTo("Толстой");
        verify(authorMapper).toDTO(author);
    }

    @Test
    void getAllAuthors_WhenDAOThrowsException_ShouldThrow() throws SQLException {
        when(authorDAO.getAll()).thenThrow(new SQLException("DB error"));
        assertThatThrownBy(() -> authorService.getAllAuthors())
                .isInstanceOf(AuthorServiceException.class)
                .hasMessageContaining("Ошибка при получении списка авторов");
    }

    @Test
    void addAuthor_WhenDAOThrowsException_ShouldThrow() throws SQLException {
        AuthorDTO dto = new AuthorDTO(new Author(0, "Иван", "Тургенев", "Россия", Set.of()));
        doThrow(new SQLException("DB error")).when(authorDAO).create(any());
        assertThatThrownBy(() -> authorService.addAuthor(dto))
                .isInstanceOf(AuthorServiceException.class)
                .hasMessageContaining("Ошибка при добавлении автора");
    }

    @Test
    void getAuthorById_WhenExists_ShouldReturnDTO() throws SQLException {
        // Arrange
        Author author = new Author(1, "Фёдор", "Достоевский", "Россия", new HashSet<>());
        AuthorDTO authorDTO = new AuthorDTO(author);

        when(authorDAO.getById(1)).thenReturn(Optional.of(author));
        when(authorMapper.toDTO(author)).thenReturn(authorDTO); // Настройка маппера

        // Act
        AuthorDTO result = authorService.getAuthorById(1);

        // Assert
        assertThat(result)
                .extracting(AuthorDTO::getName, AuthorDTO::getCountry)
                .containsExactly("Фёдор", "Россия");
        verify(authorMapper).toDTO(author);
    }

    @Test
    void getAuthorById_WhenNotExists_ShouldThrow() throws SQLException {
        // Arrange
        when(authorDAO.getById(anyInt())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authorService.getAuthorById(1))
                .isInstanceOf(AuthorServiceException.class)
                .hasMessageContaining("Автор не найден");
    }

    @Test
    void addAuthor_ShouldMapAndSave() throws SQLException {
        // Arrange
        Author author = new Author(0, "Антон", "Чехов", "Россия", new HashSet<>());
        AuthorDTO dto = new AuthorDTO(author);

        when(authorMapper.toModel(dto)).thenReturn(author);

        // Act
        authorService.addAuthor(dto);

        // Assert
        verify(authorMapper).toModel(dto);
        verify(authorDAO).create(refEq(author, "id", "books"));
    }

    @Test
    void getAuthorById_WhenDAOThrowsException_ShouldThrow() throws SQLException {
        when(authorDAO.getById(1)).thenThrow(new SQLException("DB error"));
        assertThatThrownBy(() -> authorService.getAuthorById(1))
                .isInstanceOf(AuthorServiceException.class)
                .hasMessageContaining("Ошибка при получении автора с ID 1");
    }

    @Test
    void updateAuthor_WhenDAOThrowsException_ShouldThrow() throws SQLException {
        AuthorDTO dto = new AuthorDTO(new Author(1, "Новое", "Имя", "Страна", Set.of()));
        when(authorDAO.getById(1)).thenReturn(Optional.of(new Author()));
        doThrow(new SQLException("DB error")).when(authorDAO).update(any());
        assertThatThrownBy(() -> authorService.updateAuthor(1, dto))
                .isInstanceOf(AuthorServiceException.class)
                .hasMessageContaining("Ошибка при обновлении автора с ID 1");
    }

    @Test
    void deleteAuthor_WhenDAOThrowsException_ShouldThrow() throws SQLException {
        doThrow(new SQLException("DB error")).when(authorDAO).delete(1);
        assertThatThrownBy(() -> authorService.deleteAuthor(1))
                .isInstanceOf(AuthorServiceException.class)
                .hasMessageContaining("Ошибка при удалении автора с ID 1");
    }

    @Test
    void updateAuthor_WhenExists_ShouldUpdate() throws SQLException {
        // Arrange
        Set<Book> books = new HashSet<>();
        Author existing = new Author(1, "Старое", "Имя", "Страна", books);
        Author updatedAuthor = new Author(1, "Новое", "Имя", "Страна", books);
        AuthorDTO update = new AuthorDTO(updatedAuthor);

        when(authorDAO.getById(1)).thenReturn(Optional.of(existing));

        // Act
        authorService.updateAuthor(1, update);

        // Assert
        verify(authorDAO).update(argThat(author ->
                author.getName().equals("Новое") &&
                        author.getSurname().equals("Имя")
        ));
    }

    @Test
    void deleteAuthor_ShouldCallDAO() throws SQLException {
        // Act
        authorService.deleteAuthor(1);

        // Assert
        verify(authorDAO).delete(1);
    }
}
