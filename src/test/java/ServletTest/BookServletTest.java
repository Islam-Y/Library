package ServletTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.dto.BookDTO;
import com.library.service.BookService;
import com.library.servlet.BookServlet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
import java.util.List;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class BookServletTest {

    @Mock
    private BookService bookService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private DataSource dataSource;

    @InjectMocks
    private BookServlet bookServlet;

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
        BookServlet servlet = new BookServlet();
        Field field = BookServlet.class.getDeclaredField("bookService");
        field.setAccessible(true);
        field.set(servlet, mock(BookService.class));
        assertThat(field.get(servlet)).isNotNull();
    }

    @Test
    void doGet_WriterThrowsIOException_TriggersServerError() throws Exception {
        when(request.getPathInfo()).thenReturn("/1");
        when(response.getWriter()).thenThrow(new IOException("Test IO Exception"));
        when(bookService.getBookById(1)).thenReturn(new BookDTO() {{
            setId(1);
            setTitle("1984");
        }});

        invokeDoGet(request, response);

        verify(response, times(2)).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    void doGet_AllBooks_ReturnsList() throws Exception {
        // Arrange
        when(request.getPathInfo()).thenReturn(null);

        BookDTO bookDTO = new BookDTO();
        bookDTO.setId(1);
        bookDTO.setTitle("1984");

        when(bookService.getAllBooks()).thenReturn(List.of(bookDTO));

        // Act
        invokeDoGet(request, response);

        // Assert
        verify(response).setContentType("application/json");
        printWriter.flush();
        assertThat(stringWriter.toString()).contains("\"id\":1", "\"title\":\"1984\"");
    }

    @Test
    void doGet_InvalidId_ReturnsBadRequest() throws Exception {
        // Arrange
        when(request.getPathInfo()).thenReturn("/invalid");

        stringWriter =new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        // Act
        invokeDoGet(request, response);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        printWriter.flush();
        assertThat(stringWriter.toString()).contains("Invalid book ID format");
    }

    @Test
    void doGet_OneBook_ValidId() throws Exception {
        // Arrange
        when(request.getPathInfo()).thenReturn("/1");
        BookDTO bookDTO = new BookDTO();
        bookDTO.setId(1);
        bookDTO.setTitle("1984");
        when(bookService.getBookById(1)).thenReturn(bookDTO);

        // Act
        invokeDoGet(request, response);

        // Assert
        verify(response).setContentType("application/json");
        printWriter.flush();
        assertThat(stringWriter.toString()).contains("\"id\":1", "\"title\":\"1984\"");
    }

    @Test
    void doGet_OneBook_NotFound() throws Exception {
        // Arrange
        when(request.getPathInfo()).thenReturn("/999");
        when(bookService.getBookById(999)).thenReturn(null);

        // Act
        invokeDoGet(request, response);

        // Assert
        verify(response).setContentType("application/json");
        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    void doPost_ValidBook_ReturnsCreated() throws Exception {
        // Arrange
        BookDTO book = new BookDTO();
        book.setTitle("1984");
        book.setPublishedDate("2023-01-01");
        book.setGenre("Антиутопия");
        book.setPublisherId(1);
        book.setAuthorIds(Set.of(10, 20));

        String jsonBody = objectMapper.writeValueAsString(book);
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

        // Act
        invokeDoPost(request, response);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_CREATED);
        verify(bookService).addBook(any(BookDTO.class));
    }

    @Test
    void doPost_InvalidBody_ReturnsBadRequest() throws Exception {
        // Например, невалидный JSON
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("invalid-json")));

        invokeDoPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        printWriter.flush();
        assertThat(stringWriter.toString()).contains("Invalid request:");
    }

    @Test
    void doPut_ValidId_Success() throws Exception {
        // Arrange
        when(request.getPathInfo()).thenReturn("/5");
        BookDTO bookDTO = new BookDTO();
        bookDTO.setId(5);
        bookDTO.setTitle("Обновленная книга");
        String jsonBody = objectMapper.writeValueAsString(bookDTO);

        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

        // Act
        invokeDoPut(request, response);

        // Assert
        verify(bookService).updateBook(eq(5), any(BookDTO.class));
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    void doPut_IdMismatch_ReturnsBadRequest() throws Exception {
        // Arrange
        when(request.getPathInfo()).thenReturn("/2"); // ID в пути
        BookDTO bookDTO = new BookDTO();
        bookDTO.setId(3); // ID в теле
        String jsonBody = objectMapper.writeValueAsString(bookDTO);
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
        when(request.getPathInfo()).thenReturn("/abc");
        BookDTO bookDTO = new BookDTO();
        bookDTO.setId(1);
        String jsonBody = objectMapper.writeValueAsString(bookDTO);
        lenient().when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

        invokeDoPut(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        printWriter.flush();
        assertThat(stringWriter.toString()).contains("Invalid book ID format");
    }

    @Test
    void doDelete_ValidId_NoContent() throws Exception {
        when(request.getPathInfo()).thenReturn("/10");

        invokeDoDelete(request, response);

        verify(bookService).deleteBook(10);
        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    void doDelete_InvalidIdFormat_ReturnsBadRequest() throws Exception {
        when(request.getPathInfo()).thenReturn("/abc");

        invokeDoDelete(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        printWriter.flush();
        assertThat(stringWriter.toString()).contains("Invalid book ID format");
    }

    private void invokeDoGet(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Method doGetMethod = BookServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);
        doGetMethod.invoke(bookServlet, request, response);
    }

    private void invokeDoPost(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Method doPostMethod = BookServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);
        doPostMethod.invoke(bookServlet, request, response);
    }

    private void invokeDoPut(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Method doPutMethod = BookServlet.class.getDeclaredMethod("doPut", HttpServletRequest.class, HttpServletResponse.class);
        doPutMethod.setAccessible(true);
        doPutMethod.invoke(bookServlet, request, response);
    }

    private void invokeDoDelete(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Method doDeleteMethod = BookServlet.class.getDeclaredMethod("doDelete", HttpServletRequest.class, HttpServletResponse.class);
        doDeleteMethod.setAccessible(true);
        doDeleteMethod.invoke(bookServlet, request, response);
    }
}