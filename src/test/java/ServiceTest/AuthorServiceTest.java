package ServiceTest;

import com.library.dto.AuthorDTO;
import com.library.exception.AuthorServiceException;
import com.library.mapper.AuthorMapper;
import com.library.model.Author;
import com.library.model.Book;
import com.library.repository.AuthorDAO;
import com.library.service.AuthorService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

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
        inputDTO.setBookIds(Collections.emptySet());

        Author expectedAuthor = new Author();
        expectedAuthor.setName("Новый Автор");

        when(authorMapper.toModel(inputDTO)).thenReturn(expectedAuthor);

        authorService.addAuthor(inputDTO);

        ArgumentCaptor<Author> captor = ArgumentCaptor.forClass(Author.class);
        verify(authorDAO).create(captor.capture());

        assertEquals("Новый Автор", captor.getValue().getName());
    }

    @Test
    public void updateAuthor_Success() throws SQLException {
        Author existingAuthor = new Author(1, "Старое имя", "Старая фамилия", "Старая страна", new HashSet<>());
        when(authorDAO.getById(1)).thenReturn(Optional.of(existingAuthor));

        AuthorDTO updateDTO = new AuthorDTO();
        updateDTO.setName("Лев");
        updateDTO.setSurname("Толстой");
        updateDTO.setCountry("Россия");
        updateDTO.setBookIds(Set.of(1, 2));

        lenient().when(authorMapper.toModel(updateDTO)).thenReturn(testAuthor);

        authorService.updateAuthor(1, updateDTO);

        verify(authorDAO).update(testAuthor);
        assertEquals("Лев", testAuthor.getName());
    }

    @Test
    public void updateAuthor_UpdateBookAuthorsCalled() throws SQLException {
        Set<Integer> expectedBookIds = Set.of(10, 20);
        AuthorDTO updateDTO = new AuthorDTO();
        updateDTO.setId(1);
        updateDTO.setName("Новое имя");
        updateDTO.setSurname("Новая фамилия");
        updateDTO.setCountry("Новая страна");
        updateDTO.setBookIds(expectedBookIds);

        Author existingAuthor = new Author(1, "Старое имя", "Старая фамилия", "Старая страна", new HashSet<>());
        when(authorDAO.getById(1)).thenReturn(Optional.of(existingAuthor));

        authorService.updateAuthor(1, updateDTO);

        assertEquals("Новое имя", existingAuthor.getName());
        assertEquals("Новая фамилия", existingAuthor.getSurname());
        assertEquals("Новая страна", existingAuthor.getCountry());

        Set<Integer> actualBookIds = existingAuthor.getBooks().stream()
                .map(Book::getId)
                .collect(Collectors.toSet());
        assertEquals(expectedBookIds, actualBookIds);

        verify(authorDAO).update(existingAuthor);
        verify(authorDAO).updateBooksOfAuthor(existingAuthor);
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

    @Test(expected = IllegalArgumentException.class)
    public void addAuthor_SQLException() {
        AuthorDTO invalidDTO = new AuthorDTO();
        authorService.addAuthor(invalidDTO);
    }
}
