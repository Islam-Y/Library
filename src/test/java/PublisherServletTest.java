import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.config.DataSourceProvider;
import com.library.dto.PublisherDTO;
import com.library.model.Publisher;
import com.library.service.PublisherService;
import com.library.servlet.PublisherServlet;
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
import java.util.List;

@ExtendWith(MockitoExtension.class)
class PublisherServletTest {

    @Mock
    private PublisherService publisherService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private DataSource dataSource;

    @InjectMocks
    private PublisherServlet publisherServlet;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        // Инициализация сервлета
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
    void doGet_AllPublishers_ReturnsList() throws Exception {
        // Arrange
        List<PublisherDTO> publishers = List.of(new PublisherDTO(createTestPublisher(1)));
        when(publisherService.getAllPublishers()).thenReturn(publishers);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        // Act
        invokeDoGet(request, response);

        // Assert
        verify(response).setContentType("application/json");
        writer.flush();
        String expectedJson = "[{\"id\":1,\"name\":\"Эксмо\",\"bookIds\":[]}]";
        assertThat(stringWriter.toString()).isEqualTo(expectedJson);
    }

    @Test
    void doPost_ValidPublisher_ReturnsCreated() throws Exception {
        PublisherDTO publisherDTO = new PublisherDTO();
        publisherDTO.setName("O'Reilly");

        String jsonBody = objectMapper.writeValueAsString(publisherDTO);
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

        invokeDoPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_CREATED);
        verify(publisherService).addPublisher(any(PublisherDTO.class));
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
        assertThat(stringWriter.toString()).contains("Invalid publisher ID format");
    }

    private void invokeDoGet(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Method doGetMethod = PublisherServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);
        doGetMethod.invoke(publisherServlet, request, response);
    }

    private void invokeDoPost(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Method doPostMethod = PublisherServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);
        doPostMethod.invoke(publisherServlet, request, response);
    }

    private Publisher createTestPublisher(int id) {
        Publisher publisher = new Publisher();
        publisher.setId(id);
        publisher.setName("Эксмо");
        publisher.setBooks(new ArrayList<>());
        return publisher;
    }
}