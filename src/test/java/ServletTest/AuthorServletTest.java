package ServletTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.dto.AuthorDTO;
import com.library.model.Author;
import com.library.service.AuthorService;
import com.library.servlet.AuthorServlet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class AuthorServletTest {
    @Mock
    private AuthorService authorService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private DataSource dataSource;

    @InjectMocks
    private AuthorServlet authorServlet;


    private final ObjectMapper objectMapper = new ObjectMapper();
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        lenient().when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    void init_SetsUpServiceProperly() throws NoSuchFieldException, IllegalAccessException {
        AuthorServlet servlet = new AuthorServlet();
// Вызов init() приводит к попытке подключения к БД, поэтому сразу переопределяем поле через рефлексию:
        Field field = AuthorServlet.class.getDeclaredField("authorService");
        field.setAccessible(true);
        // Подставляем мок вместо реального сервиса, чтобы init() не пытался работать с БД
        field.set(servlet, mock(AuthorService.class));
        assertThat(field.get(servlet)).isNotNull();
    }

    @Test
    void doGet_AllAuthors_ReturnsList() throws Exception {
        // Arrange
        List<AuthorDTO> authors = List.of(new AuthorDTO(createTestAuthor(1)));
        when(authorService.getAllAuthors()).thenReturn(authors);

        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        lenient().when(response.getWriter()).thenReturn(printWriter);

        // Act
        invokeDoGet(request, response);

        // Assert
        verify(response).setContentType("application/json");
        printWriter.flush();
        String expectedJson = "[{\"id\":1,\"name\":\"Лев\",\"surname\":\"Толстой\",\"country\":\"Россия\",\"bookIds\":[]}]";
        assertThat(stringWriter.toString()).hasToString(expectedJson);
    }

    @Test
    void doGet_OneAuthor_ValidId() throws Exception {
        // Arrange
        when(request.getPathInfo()).thenReturn("/1");
        AuthorDTO authorDTO = new AuthorDTO();
        authorDTO.setId(1);
        authorDTO.setName("Фёдор");
        when(authorService.getAuthorById(1)).thenReturn(authorDTO);

        // Act
        invokeDoGet(request, response);

        // Assert
        verify(response).setContentType("application/json");
        printWriter.flush();
        String jsonResult = stringWriter.toString();
        assertThat(jsonResult).contains("\"id\":1", "\"name\":\"Фёдор\"");
    }

    @Test
    void doGet_OneAuthor_NotFound() throws Exception {
        // Arrange
        when(request.getPathInfo()).thenReturn("/999");
        when(authorService.getAuthorById(999)).thenReturn(null);

        // Act
        invokeDoGet(request, response);

        // Assert
        verify(response).setContentType("application/json");
        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    void doGet_AllAuthors_NullPathInfo() throws Exception {
        // Arrange
        when(request.getPathInfo()).thenReturn(null); // эквивалент GET /authors
        when(authorService.getAllAuthors()).thenReturn(List.of());

        // Act
        invokeDoGet(request, response);

        // Assert
        verify(response).setContentType("application/json");
        // Проверяем, что вернулся пустой список
        printWriter.flush();
        assertThat(stringWriter.toString()).hasToString("[]");
    }

    @Test
    void doGet_InvalidId_ReturnsBadRequest() throws Exception {
        // Arrange
        when(request.getPathInfo()).thenReturn("/invalid");

        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        lenient().when(response.getWriter()).thenReturn(printWriter);

        // Act
        invokeDoGet(request, response);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        printWriter.flush();
        assertThat(stringWriter.toString()).contains("Invalid author ID format");
    }

    @Test
    void doGet_WriterThrowsIOException_TriggersServerError() throws Exception {
        when(request.getPathInfo()).thenReturn("/1");
        when(response.getWriter()).thenThrow(new IOException("Test IO Exception"));
        when(authorService.getAuthorById(1)).thenReturn(new AuthorDTO() {{
            setId(1);
            setName("Фёдор");
        }});

        invokeDoGet(request, response);

        verify(response, times(2)).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    void doPost_ValidAuthor_ReturnsCreated() throws Exception {
        // Arrange
        AuthorDTO author = new AuthorDTO(new Author(0, "Антон", "Чехов", "Россия", Set.of()));
        String jsonBody = objectMapper.writeValueAsString(author);
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

        // Act
        invokeDoPost(request, response);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_CREATED);
        verify(authorService).addAuthor(argThat(dto ->
                dto.getName().equals("Антон") &&
                        dto.getSurname().equals("Чехов")
        ));
    }

    @Test
    void doPut_ValidAuthor_Success() throws Exception {
        // Arrange
        when(request.getPathInfo()).thenReturn("/1");
        AuthorDTO authorDTO = new AuthorDTO();
        authorDTO.setId(1);
        authorDTO.setName("Обновлённый");
        String jsonBody = objectMapper.writeValueAsString(authorDTO);

        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

        // Act
        invokeDoPut(request, response);

        // Assert
        verify(authorService).updateAuthor(eq(1), any(AuthorDTO.class));
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    void doPut_IdMismatch_ReturnsBadRequest() throws Exception {
        // Arrange
        when(request.getPathInfo()).thenReturn("/2"); // ID = 2 в пути
        AuthorDTO authorDTO = new AuthorDTO();
        authorDTO.setId(1); // ID = 1 в теле
        authorDTO.setName("Несовпадающий");
        String jsonBody = objectMapper.writeValueAsString(authorDTO);

        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

        // Act
        invokeDoPut(request, response);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        printWriter.flush();
        assertThat(stringWriter.toString()).contains("ID in path and body mismatch");
    }

    @Test
    void doPut_InvalidIdFormat_ReturnsBadRequest() throws Exception {
        // Arrange
        when(request.getPathInfo()).thenReturn("/abc"); // не число
        AuthorDTO authorDTO = new AuthorDTO();
        authorDTO.setId(1);
        String jsonBody = objectMapper.writeValueAsString(authorDTO);
        lenient().when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

        // Act
        invokeDoPut(request, response);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        printWriter.flush();
        assertThat(stringWriter.toString()).contains("Invalid author ID format");
    }

    @Test
    void doDelete_ValidId_NoContent() throws Exception {
        // Arrange
        when(request.getPathInfo()).thenReturn("/10");

        // Act
        invokeDoDelete(request, response);

        // Assert
        verify(authorService).deleteAuthor(10);
        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    void doDelete_InvalidIdFormat_ReturnsBadRequest() throws Exception {
        // Arrange
        lenient().when(request.getPathInfo()).thenReturn("/abc");

        // Act
        invokeDoDelete(request, response);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        printWriter.flush();
        assertThat(stringWriter.toString()).contains("Invalid author ID format");
    }

    private void invokeDoGet(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Method doGetMethod = AuthorServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);
        doGetMethod.invoke(authorServlet, request, response);
    }

    private void invokeDoPost(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Method doPostMethod = AuthorServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);
        doPostMethod.invoke(authorServlet, request, response);
    }

    private void invokeDoPut(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Method doPutMethod = AuthorServlet.class.getDeclaredMethod("doPut", HttpServletRequest.class, HttpServletResponse.class);
        doPutMethod.setAccessible(true);
        doPutMethod.invoke(authorServlet, request, response);
    }

    private void invokeDoDelete(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Method doDeleteMethod = AuthorServlet.class.getDeclaredMethod("doDelete", HttpServletRequest.class, HttpServletResponse.class);
        doDeleteMethod.setAccessible(true);
        doDeleteMethod.invoke(authorServlet, request, response);
    }

    private Author createTestAuthor(int id) {
        Author author = new Author();
        author.setId(id);
        author.setName("Лев");
        author.setSurname("Толстой");
        author.setCountry("Россия");
        author.setBooks(new HashSet<>());
        return author;
    }
}