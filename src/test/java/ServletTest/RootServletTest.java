package ServletTest;

import com.library.servlet.RootServlet;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class RootServletTest {

    @Test
    public void testDoGetWritesHelloFromRoot() {
        try {
            RootServlet servlet = new RootServlet();
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = mock(HttpServletResponse.class);

            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            when(response.getWriter()).thenReturn(writer);

            // Используем рефлексию для вызова protected метода doGet
            Method doGetMethod = RootServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
            doGetMethod.setAccessible(true);
            doGetMethod.invoke(servlet, request, response);

            writer.flush();
            String output = stringWriter.toString();
            assertTrue("Ответ должен содержать 'Hello from ROOT!'", output.contains("Hello from ROOT!"));
        } catch (Exception e) {
            fail("Исключение при вызове doGet: " + e.getMessage());
        }
    }
}
