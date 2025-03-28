package DataSourceProvider;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.mockito.junit.jupiter.MockitoExtension;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import static org.assertj.core.api.Assertions.assertThat;
import com.library.config.DataSourceProvider;
import java.lang.reflect.Field;

@Testcontainers
@ExtendWith(MockitoExtension.class)
class DataSourceProviderIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceProviderIntegrationTest.class);

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test_db")
            .withUsername("test_user")
            .withPassword("test_pass")
            .withInitScript("init.sql");

    @BeforeAll
    static void setup() throws Exception {
        postgres.withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("POSTGRES")));

        System.setProperty("testing", "true");
        System.setProperty("db.url", postgres.getJdbcUrl());
        System.setProperty("db.user", postgres.getUsername());
        System.setProperty("db.password", postgres.getPassword());
        System.setProperty("db.driver", "org.postgresql.Driver");

        resetDataSource();
    }

    @AfterEach
    void tearDown() throws Exception {
        HikariDataSource ds = (HikariDataSource) getStaticField(DataSourceProvider.class, "dataSource");
        if (ds != null && !ds.isClosed()) {
            ds.close();
        }
    }

    @Test
    void shouldProvideWorkingDataSource() throws SQLException {
        DataSource dataSource = DataSourceProvider.getDataSource();

        logger.debug("Проверка соединения с базой данных...");
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            logger.debug("Создание временной таблицы...");
            statement.execute("CREATE TEMPORARY TABLE test_temp (id INT)");

            logger.debug("Создание основной таблицы...");
            statement.execute("""
            CREATE TABLE IF NOT EXISTS test (
                id SERIAL PRIMARY KEY,
                created_at TIMESTAMP DEFAULT NOW()
            )""");

            int inserted = statement.executeUpdate("INSERT INTO test DEFAULT VALUES");
            logger.debug("Вставлена строк: {}", inserted);

            assertThat(inserted).isEqualTo(1);
        }
    }

    private static void resetDataSource() throws Exception {
        setStaticField(DataSourceProvider.class, "dataSource", null);
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
