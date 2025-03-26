package ServletTest;

import static org.assertj.core.api.Assertions.assertThat;

import com.library.service.BookService;
import com.library.service.Fabric;
import com.library.servlet.BookServlet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

@ExtendWith(MockitoExtension.class)
class BookServletInitTest {

    @Mock
    private BookService mockBookService;

    @BeforeEach
    void setUp() throws Exception {
        Field bookServiceField = Fabric.class.getDeclaredField("bookService");
        bookServiceField.setAccessible(true);
        bookServiceField.set(null, mockBookService);
    }

    @Test
    void init_SetsUpServiceProperly() throws Exception {
        BookServlet servlet = new BookServlet();
        servlet.init();

        Field serviceField = BookServlet.class.getDeclaredField("bookService");
        serviceField.setAccessible(true);
        BookService service = (BookService) serviceField.get(servlet);

        assertThat(service).isSameAs(mockBookService).isNotNull();
    }

    @AfterEach
    void tearDown() throws Exception {
        Field bookServiceField = Fabric.class.getDeclaredField("bookService");
        bookServiceField.setAccessible(true);
        bookServiceField.set(null, null);
    }
}