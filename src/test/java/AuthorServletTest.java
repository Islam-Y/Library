import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.dto.AuthorDTO;
import com.library.model.Author;
import com.library.service.AuthorService;
import com.library.servlet.AuthorServlet;
import com.library.config.DataSourceProvider;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
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

    @BeforeEach
    void setUp() throws Exception {

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        lenient().when(response.getWriter()).thenReturn(printWriter);
    }

//    @AfterEach
//    void tearDown() throws SQLException {
//        try (Connection conn = dataSource.getConnection()) {
//            conn.prepareStatement("DELETE FROM book_author").executeUpdate();
//            conn.prepareStatement("DELETE FROM books").executeUpdate();
//            conn.prepareStatement("DELETE FROM authors").executeUpdate();
//            conn.prepareStatement("DELETE FROM publishers").executeUpdate();
//        }
//    }

    @Test
    void doGet_AllAuthors_ReturnsList() throws Exception {
        // Arrange
        List<AuthorDTO> authors = List.of(new AuthorDTO(createTestAuthor(1)));
        when(authorService.getAllAuthors()).thenReturn(authors);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        // Act
        invokeDoGet(request, response);

        // Assert
        verify(response).setContentType("application/json");
        writer.flush();
        String expectedJson = "[{\"id\":1,\"name\":\"Лев\",\"surname\":\"Толстой\",\"country\":\"Россия\",\"bookIds\":[]}]";
        assertThat(stringWriter.toString()).isEqualTo(expectedJson);
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
    void doGet_InvalidId_ReturnsBadRequest() throws Exception {
        // Arrange
        when(request.getPathInfo()).thenReturn("/invalid");

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        lenient().when(response.getWriter()).thenReturn(writer);

        // Act
        invokeDoGet(request, response);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        writer.flush();
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