package DataSourceProvider;

import com.library.exception.ConfigurationFileNotFoundException;
import com.library.exception.ConfigurationLoadException;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.library.config.DataSourceProvider;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Testcontainers
@ExtendWith(MockitoExtension.class)
class DataSourceProviderTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test_db")
            .withUsername("test_user")
            .withPassword("test_pass")
            .withInitScript("init.sql");

    @BeforeAll
    static void setup() {
        postgres.withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("POSTGRES")));

        System.setProperty("testing", "true");
        System.setProperty("DB_URL", postgres.getJdbcUrl());
        System.setProperty("DB_USER", postgres.getUsername());
        System.setProperty("DB_PASS", postgres.getPassword());
        System.setProperty("DB_DRIVER", "org.postgresql.Driver");

        // Принудительная переинициализация
        try {
            setStaticField(DataSourceProvider.class, "dataSource", null);
            setStaticField(DataSourceProvider.class, "classLoader", DataSourceProvider.class.getClassLoader());
        } catch (Exception e) {
            throw new RuntimeException("Init failed", e);
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        HikariDataSource ds = (HikariDataSource) getStaticField(DataSourceProvider.class, "dataSource");
        if (ds != null && !ds.isClosed()) {
            ds.close();
        }
        setStaticField(DataSourceProvider.class, "dataSource", null);
        setStaticField(DataSourceProvider.class, "classLoader", DataSourceProvider.class.getClassLoader());
    }

    @Test
    void shouldProvideWorkingDataSource() throws SQLException {
        DataSource dataSource = DataSourceProvider.getDataSource();

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            // Проверка создания временной таблицы
            statement.execute("CREATE TEMPORARY TABLE test_temp (id INT)");

            // Основной тест
            statement.execute("""
            CREATE TABLE IF NOT EXISTS test (
                id SERIAL PRIMARY KEY,
                created_at TIMESTAMP DEFAULT NOW()
            )""");

            assertThat(statement.executeUpdate("INSERT INTO test DEFAULT VALUES"))
                    .isEqualTo(1);
        }
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

    private static Object getStaticField(Class<?> clazz, String fieldName) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(null);
    }
}
