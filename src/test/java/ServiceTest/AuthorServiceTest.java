package ServiceTest;

import com.library.dto.AuthorDTO;
import com.library.exception.AuthorServiceException;
import com.library.mapper.AuthorMapper;
import com.library.model.Author;
import com.library.repository.AuthorDAO;
import com.library.service.AuthorService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuthorServiceTest {

    @Mock
    private AuthorDAO authorDAO;
    @Mock
    private AuthorMapper authorMapper;

    private AuthorService authorService;

    private Author testAuthor;
    private AuthorDTO testAuthorDTO;

    @Before
    public void setUp() {
        authorService = AuthorService.forTest(authorDAO, authorMapper);

        testAuthor = new Author(1, "Лев", "Толстой", "Россия", new HashSet<>());
        testAuthorDTO = new AuthorDTO();
        testAuthorDTO.setId(1);
        testAuthorDTO.setName("Лев");
        testAuthorDTO.setSurname("Толстой");
        testAuthorDTO.setCountry("Россия");

        when(authorMapper.toDTO(any(Author.class))).thenReturn(testAuthorDTO);
    }

    @Test
    public void getAllAuthors_Success() throws SQLException {
        when(authorDAO.getAll()).thenReturn(Collections.singletonList(testAuthor));

        List<AuthorDTO> result = authorService.getAllAuthors();

        assertEquals(1, result.size());
        assertEquals("Лев", result.get(0).getName());
    }

    @Test(expected = AuthorServiceException.class)
    public void getAllAuthors_Exception() throws SQLException {
        when(authorDAO.getAll()).thenThrow(new SQLException("DB error"));
        authorService.getAllAuthors();
    }

    @Test
    public void getAuthorById_Success() throws SQLException {
        when(authorDAO.getById(1)).thenReturn(Optional.of(testAuthor));

        AuthorDTO result = authorService.getAuthorById(1);

        assertEquals(1, result.getId());
        assertEquals("Лев", result.getName());
    }

    @Test
    public void addAuthor_Success() throws SQLException {
        AuthorDTO inputDTO = new AuthorDTO();
        inputDTO.setName("Новый Автор");

        Author expectedAuthor = new Author();
        expectedAuthor.setName("Новый Автор");

        // 2. Настройка маппера
        when(authorMapper.toModel(inputDTO)).thenReturn(expectedAuthor);

        // 3. Вызов метода
        authorService.addAuthor(inputDTO);

        // 4. Проверки
        ArgumentCaptor<Author> captor = ArgumentCaptor.forClass(Author.class);
        verify(authorDAO).create(captor.capture());

        Author savedAuthor = captor.getValue();
        assertEquals("Новый Автор", savedAuthor.getName());
    }

    @Test
    public void updateAuthor_Success() throws SQLException {
        // 1. Подготовка данных
        Author existingAuthor = new Author(1, "Старое имя", "Старая фамилия", "Старая страна", new HashSet<>());
        when(authorDAO.getById(1)).thenReturn(Optional.of(existingAuthor));
        lenient().when(authorMapper.toModel(any(AuthorDTO.class))).thenReturn(testAuthor);

        // 2. Вызов метода
        authorService.updateAuthor(1, testAuthorDTO);

        // 3. Проверка вызовов DAO
        verify(authorDAO).update(testAuthor);
        verify(authorDAO).updateBookAuthors(testAuthor);

        // 4. Проверка обновленных полей
        assertEquals("Лев", testAuthor.getName());
        assertEquals("Толстой", testAuthor.getSurname());
        assertEquals("Россия", testAuthor.getCountry());
    }

    @Test
    public void updateAuthor_UpdateBookAuthorsCalled() throws SQLException {
        // 1. Подготовка
        Author existingAuthor = new Author(1, "Старое имя", "Старая фамилия", "Старая страна", new HashSet<>());
        when(authorDAO.getById(1)).thenReturn(Optional.of(existingAuthor));
        lenient().when(authorMapper.toModel(any(AuthorDTO.class))).thenReturn(testAuthor);

        // 2. Вызов
        authorService.updateAuthor(1, testAuthorDTO);

        // 3. Проверка
        verify(authorDAO, times(1)).updateBookAuthors(testAuthor);

    }

    @Test(expected = AuthorServiceException.class)
    public void updateAuthor_NotFound() throws SQLException {
        when(authorDAO.getById(1)).thenReturn(Optional.empty());
        authorService.updateAuthor(1, new AuthorDTO());
    }

    @Test(expected = AuthorServiceException.class)
    public void updateAuthor_SQLExceptionOnGet() throws SQLException {
        when(authorDAO.getById(1)).thenThrow(new SQLException("DB error"));
        authorService.updateAuthor(1, testAuthorDTO);
    }

    @Test(expected = AuthorServiceException.class)
    public void updateAuthor_SQLExceptionOnUpdate() throws SQLException {
        when(authorDAO.getById(1)).thenReturn(Optional.of(testAuthor));
        doThrow(new SQLException()).when(authorDAO).update(any(Author.class));

        authorService.updateAuthor(1, new AuthorDTO());
    }

    @Test
    public void deleteAuthor_Success() throws SQLException {
        authorService.deleteAuthor(1);
        verify(authorDAO).delete(1);
    }

    @Test(expected = AuthorServiceException.class)
    public void deleteAuthor_SQLException() throws SQLException {
        doThrow(new SQLException()).when(authorDAO).delete(1);
        authorService.deleteAuthor(1);
    }

    @Test(expected = AuthorServiceException.class)
    public void getAuthorById_NotFound() throws SQLException {
        when(authorDAO.getById(anyInt())).thenReturn(Optional.empty());
        authorService.getAuthorById(1);
    }

    @Test(expected = AuthorServiceException.class)
    public void addAuthor_SQLException() throws SQLException {
        // 1. Настройка маппера
        Author mockAuthor = new Author();
        when(authorMapper.toModel(any(AuthorDTO.class))).thenReturn(mockAuthor);
        // 2. Настройка DAO на выброс исключения
        doThrow(new SQLException("Database error")).when(authorDAO).create(mockAuthor);

        // 3. Вызов тестируемого метода
        authorService.addAuthor(new AuthorDTO());
    }
}
