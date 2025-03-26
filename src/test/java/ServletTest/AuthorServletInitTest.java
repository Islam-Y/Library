package ServletTest;

import static org.assertj.core.api.Assertions.assertThat;

import com.library.service.AuthorService;
import com.library.service.Fabric;
import com.library.servlet.AuthorServlet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

@ExtendWith(MockitoExtension.class)
class AuthorServletInitTest {

    @Mock
    private AuthorService mockAuthorService;

    @BeforeEach
    void setUp() throws Exception {
        Field authorServiceField = Fabric.class.getDeclaredField("authorService");
        authorServiceField.setAccessible(true);
        authorServiceField.set(null, mockAuthorService);
    }

    @Test
    void init_SetsUpServiceProperly() throws Exception {
        AuthorServlet servlet = new AuthorServlet();
        servlet.init();

        Field serviceField = AuthorServlet.class.getDeclaredField("authorService");
        serviceField.setAccessible(true);
        AuthorService service = (AuthorService) serviceField.get(servlet);

        assertThat(service).isSameAs(mockAuthorService).isNotNull();
    }

    @AfterEach
    void tearDown() throws Exception {
        Field authorServiceField = Fabric.class.getDeclaredField("authorService");
        authorServiceField.setAccessible(true);
        authorServiceField.set(null, null);
    }
}