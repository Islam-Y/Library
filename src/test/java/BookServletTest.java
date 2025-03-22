import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.config.DataSourceProvider;
import com.library.dto.BookDTO;
import com.library.model.Author;
import com.library.model.Book;
import com.library.model.Publisher;
import com.library.service.BookService;
import com.library.servlet.BookServlet;
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
import java.util.ArrayList;
import java.util.HashSet;
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
    void doGet_AllBooks_ReturnsList() throws Exception {
        // Arrange
        List<BookDTO> books = List.of(new BookDTO(createTestBook(1)));
        when(bookService.getAllBooks()).thenReturn(books);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        // Act
        invokeDoGet(request, response);

        // Assert
        verify(response).setContentType("application/json");
        writer.flush();
        String expectedJson = "[{\"id\":1,\"title\":\"1984\",\"publishedDate\":\"2023-01-01\",\"genre\":\"Антиутопия\",\"publisherId\":1,\"authorIds\":[]}]";
        assertThat(stringWriter.toString()).isEqualTo(expectedJson);
    }

    @Test
    void doPost_ValidBook_ReturnsCreated() throws Exception {
        BookDTO book = new BookDTO();
        Author author = new Author();
        Set<Integer> authorIds = new HashSet<>();
        authorIds.add(author.getId());

        book.setTitle("1984");
        book.setPublishedDate("2023-01-01");
        book.setGenre("Антиутопия");
        book.setPublisherId(1);
        book.setAuthorIds(authorIds);

        String jsonBody = objectMapper.writeValueAsString(book);
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

        invokeDoPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_CREATED);
    }


    @Test
    void doGet_InvalidId_ReturnsBadRequest() throws Exception {
        // Arrange
        when(request.getPathInfo()).thenReturn("/invalid");

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        // Act
        invokeDoGet(request, response);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        writer.flush();
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

    private Book createTestBook(int id) {
        Book book = new Book();
        book.setId(id);
        book.setTitle("1984");
        book.setPublishedDate("2023-01-01");
        book.setGenre("Антиутопия");
        book.setPublisher(new Publisher(1, "Эксмо", new ArrayList<>()));
        return book;
    }
}