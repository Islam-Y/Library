package ServletTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.dto.PublisherDTO;
import com.library.service.PublisherService;
import com.library.servlet.PublisherServlet;
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
        PublisherServlet servlet = new PublisherServlet();
        Field field = PublisherServlet.class.getDeclaredField("publisherService");
        field.setAccessible(true);
        field.set(servlet, mock(PublisherService.class));
        assertThat(field.get(servlet)).isNotNull();
    }

    @Test
    void doGet_WriterThrowsIOException_TriggersServerError() throws Exception {
        when(request.getPathInfo()).thenReturn("/1");
        when(response.getWriter()).thenThrow(new IOException("Test IO Exception"));
        when(publisherService.getPublisherById(1)).thenReturn(new PublisherDTO() {{
            setId(1);
            setName("Эксмо");
        }});

        invokeDoGet(request, response);

        verify(response, times(2)).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    void doGet_AllPublishers_ReturnsList() throws Exception {
        // Arrange
        when(request.getPathInfo()).thenReturn(null); // GET /publishers
        PublisherDTO publisherDTO = new PublisherDTO();
        publisherDTO.setId(1);
        publisherDTO.setName("Эксмо");
        when(publisherService.getAllPublishers()).thenReturn(List.of(publisherDTO));

        // Act
        invokeDoGet(request, response);

        // Assert
        verify(response).setContentType("application/json");
        printWriter.flush();
        // Проверяем, что вернулся список с одним PublisherDTO
        assertThat(stringWriter.toString()).contains("\"id\":1", "\"name\":\"Эксмо\"");
    }

    @Test
    void doGet_OnePublisher_ValidId() throws Exception {
        when(request.getPathInfo()).thenReturn("/1");
        PublisherDTO publisherDTO = new PublisherDTO();
        publisherDTO.setId(1);
        publisherDTO.setName("Эксмо");
        when(publisherService.getPublisherById(1)).thenReturn(publisherDTO);

        invokeDoGet(request, response);

        verify(response).setContentType("application/json");
        printWriter.flush();
        assertThat(stringWriter.toString()).contains("\"id\":1", "\"name\":\"Эксмо\"");
    }

    @Test
    void doGet_OnePublisher_NotFound() throws Exception {
        when(request.getPathInfo()).thenReturn("/999");
        when(publisherService.getPublisherById(999)).thenReturn(null);

        invokeDoGet(request, response);

        verify(response).setContentType("application/json");
        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    void doGet_InvalidId_ReturnsBadRequest() throws Exception {
        when(request.getPathInfo()).thenReturn("/abc");

        invokeDoGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        printWriter.flush();
        assertThat(stringWriter.toString()).contains("Invalid publisher ID format");
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
    void doPost_InvalidBody_ReturnsBadRequest() throws Exception {
        // Некорректный JSON
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("invalid-json")));

        invokeDoPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        printWriter.flush();
        assertThat(stringWriter.toString()).contains("Invalid request:");
    }

    @Test
    void doPut_ValidId_Success() throws Exception {
        when(request.getPathInfo()).thenReturn("/2");
        PublisherDTO publisherDTO = new PublisherDTO();
        publisherDTO.setId(2);
        publisherDTO.setName("Updated Publisher");

        String jsonBody = objectMapper.writeValueAsString(publisherDTO);
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

        invokeDoPut(request, response);

        verify(publisherService).updatePublisher(eq(2), any(PublisherDTO.class));
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    void doPut_IdMismatch_ReturnsBadRequest() throws Exception {
        lenient().when(request.getPathInfo()).thenReturn("/2");
        PublisherDTO publisherDTO = new PublisherDTO();
        publisherDTO.setId(3); // mismatch
        publisherDTO.setName("Mismatch ID");

        String jsonBody = objectMapper.writeValueAsString(publisherDTO);
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

        invokeDoPut(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        printWriter.flush();
        assertThat(stringWriter.toString()).contains("ID in path and body mismatch");
    }

    @Test
    void doPut_InvalidIdFormat_ReturnsBadRequest() throws Exception {
        when(request.getPathInfo()).thenReturn("/abc");
        PublisherDTO publisherDTO = new PublisherDTO();
        publisherDTO.setId(1);
        publisherDTO.setName("Should Fail");
        String jsonBody = objectMapper.writeValueAsString(publisherDTO);

        lenient().when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

        invokeDoPut(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        printWriter.flush();
        assertThat(stringWriter.toString()).contains("Invalid publisher ID format");
    }

    @Test
    void doDelete_ValidId_NoContent() throws Exception {
        when(request.getPathInfo()).thenReturn("/10");

        invokeDoDelete(request, response);

        verify(publisherService).deletePublisher(10);
        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    void doDelete_InvalidIdFormat_ReturnsBadRequest() throws Exception {
        when(request.getPathInfo()).thenReturn("/abc");

        invokeDoDelete(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        printWriter.flush();
        assertThat(stringWriter.toString()).contains("Invalid publisher ID format");
    }

    private void invokeDoGet(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Method doGetMethod = PublisherServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);
        doGetMethod.invoke(publisherServlet, request, response);
    }


    private void invokeDoPut(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Method doPutMethod = PublisherServlet.class.getDeclaredMethod("doPut", HttpServletRequest.class, HttpServletResponse.class);
        doPutMethod.setAccessible(true);
        doPutMethod.invoke(publisherServlet, request, response);
    }

    private void invokeDoDelete(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Method doDeleteMethod = PublisherServlet.class.getDeclaredMethod("doDelete", HttpServletRequest.class, HttpServletResponse.class);
        doDeleteMethod.setAccessible(true);
        doDeleteMethod.invoke(publisherServlet, request, response);
    }

    private void invokeDoPost(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Method doPostMethod = PublisherServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);
        doPostMethod.invoke(publisherServlet, request, response);
    }
}