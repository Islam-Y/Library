package ServletTest;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.library.config.DataSourceProvider;
import com.library.service.AuthorService;
import com.library.servlet.AuthorServlet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.lang.reflect.Field;

@ExtendWith(MockitoExtension.class)
class AuthorServletInitTest {

    @Test
    void init_SetsUpServiceProperly() throws Exception {
        DataSource dummyDataSource = mock(DataSource.class);
        try (MockedStatic<DataSourceProvider> dsProviderMock = Mockito.mockStatic(DataSourceProvider.class)) {
            dsProviderMock.when(DataSourceProvider::getDataSource)
                    .thenReturn(dummyDataSource);

            AuthorServlet servlet = new AuthorServlet();
            servlet.init();

            Field field = AuthorServlet.class.getDeclaredField("authorService");
            field.setAccessible(true);
            AuthorService service = (AuthorService) field.get(servlet);
            assertThat(service).isNotNull();
        }
    }
}
