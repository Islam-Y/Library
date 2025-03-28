package DataSourceProvider;

import com.library.exception.ConfigurationFileNotFoundException;
import com.library.exception.ConfigurationLoadException;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import com.library.config.DataSourceProvider;

@ExtendWith(MockitoExtension.class)
class DataSourceProviderUnitTest {

    @BeforeEach
    void resetDataSource() throws Exception {
        setStaticField(DataSourceProvider.class, "dataSource", null);
    }

    @Test
    void shouldLoadConfigurationFromFile() throws Exception {
        System.clearProperty("testing");
        String testConfig = """
                db.url=jdbc:postgresql://test:5432/db
                db.user=user
                db.password=pass
                db.driver=org.postgresql.Driver
                db.pool.size=5
                db.initializationFailTimeout=0
                """;
        ClassLoader originalLoader = DataSourceProvider.class.getClassLoader();
        ClassLoader mockLoader = new ClassLoader(originalLoader) {
            @Override
            public InputStream getResourceAsStream(String name) {
                if (name.equals(DataSourceProvider.getPropertiesFileName())) {
                    return new ByteArrayInputStream(testConfig.getBytes());
                }
                return super.getResourceAsStream(name);
            }
        };
        setStaticField(DataSourceProvider.class, "classLoader", mockLoader);

        HikariDataSource ds = (HikariDataSource) DataSourceProvider.getDataSource();
        assertThat(ds.getJdbcUrl()).isEqualTo("jdbc:postgresql://test:5432/db");
        assertThat(ds.getMaximumPoolSize()).isEqualTo(5);

        setStaticField(DataSourceProvider.class, "classLoader", originalLoader);
    }

    @Test
    void shouldThrowWhenConfigFileMissing() throws Exception {
        System.clearProperty("testing");
        ClassLoader mockLoader = mock(ClassLoader.class);
        when(mockLoader.getResourceAsStream(any())).thenReturn(null);
        setStaticField(DataSourceProvider.class, "classLoader", mockLoader);

        assertThatThrownBy(DataSourceProvider::getDataSource)
                .isInstanceOf(ConfigurationFileNotFoundException.class);
    }

    @Test
    void shouldHandleIOExceptions() throws Exception {
        System.clearProperty("testing");
        InputStream brokenStream = mock(InputStream.class);
        when(brokenStream.read(any())).thenThrow(new IOException("Test"));
        ClassLoader mockLoader = mock(ClassLoader.class);
        when(mockLoader.getResourceAsStream(any())).thenReturn(brokenStream);
        setStaticField(DataSourceProvider.class, "classLoader", mockLoader);

        assertThatThrownBy(DataSourceProvider::getDataSource)
                .isInstanceOf(ConfigurationLoadException.class)
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void shouldUseDefaultPoolSettings() throws Exception {
        System.clearProperty("testing");
        String testConfig = """
                db.url=jdbc:postgresql://test:5432/db
                db.user=user
                db.password=pass
                db.driver=org.postgresql.Driver
                db.pool.size=10
                db.pool.minIdle=2
                db.initializationFailTimeout=0
                """;
        ClassLoader originalLoader = DataSourceProvider.class.getClassLoader();
        ClassLoader mockLoader = new ClassLoader(originalLoader) {
            @Override
            public InputStream getResourceAsStream(String name) {
                if (name.equals(DataSourceProvider.getPropertiesFileName())) {
                    return new ByteArrayInputStream(testConfig.getBytes());
                }
                return super.getResourceAsStream(name);
            }
        };
        setStaticField(DataSourceProvider.class, "classLoader", mockLoader);

        HikariDataSource ds = (HikariDataSource) DataSourceProvider.getDataSource();
        assertThat(ds.getMaximumPoolSize()).isEqualTo(10);
        assertThat(ds.getMinimumIdle()).isEqualTo(2);

        setStaticField(DataSourceProvider.class, "classLoader", originalLoader);
    }

    private static void setStaticField(Class<?> clazz, String fieldName, Object value) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, value);
    }
}
