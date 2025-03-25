package ServletTest;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.library.config.DataSourceProvider;
import com.library.service.PublisherService;
import com.library.servlet.PublisherServlet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.lang.reflect.Field;

@ExtendWith(MockitoExtension.class)
class PublisherServletInitTest {

    @Test
    void init_SetsUpServiceProperly() throws Exception {
        DataSource dummyDataSource = mock(DataSource.class);

        try (MockedStatic<DataSourceProvider> dsProviderMock = Mockito.mockStatic(DataSourceProvider.class)) {
            dsProviderMock.when(DataSourceProvider::getDataSource).thenReturn(dummyDataSource);

            PublisherServlet servlet = new PublisherServlet();
            servlet.init();

            Field field = PublisherServlet.class.getDeclaredField("publisherService");
            field.setAccessible(true);
            PublisherService service = (PublisherService) field.get(servlet);
            assertThat(service).isNotNull();
        }
    }
}

