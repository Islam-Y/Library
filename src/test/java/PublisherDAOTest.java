import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Optional;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.library.model.Book;
import com.library.model.Publisher;
import com.library.repository.BookDAO;
import com.library.repository.PublisherDAO;

@Testcontainers
class PublisherDAOTest {

    @Container
    private static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15")
                    .withDatabaseName("test")
                    .withUsername("test")
                    .withPassword("test")
                    .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*\\n", 2))
                    .waitingFor(Wait.forListeningPort())
                    .withStartupTimeout(Duration.ofSeconds(60));

    private PublisherDAO publisherDAO;
    private BookDAO bookDAO;
    private DataSource dataSource;

    @BeforeAll
    static void setup() {
        postgres.start();

        // 2. Диагностический вывод параметров подключения
        System.out.println("JDBC URL: " + postgres.getJdbcUrl());
        System.out.println("Username: " + postgres.getUsername());
        System.out.println("Password: " + postgres.getPassword());

        // 3. Настройка Flyway с явным указанием схемы
        Flyway flyway = Flyway.configure()
                .dataSource(
                        postgres.getJdbcUrl(),
                        postgres.getUsername(),
                        postgres.getPassword()
                )
                .schemas("public") // Явное указание схемы
                .locations("filesystem:src/main/resources/db/migration") // Абсолютный путь
                .load();

        // 4. Принудительная очистка и миграция
        flyway.clean();
        flyway.migrate();

        System.setProperty("testing", "true");
        System.setProperty("DB_URL", postgres.getJdbcUrl());
        System.setProperty("DB_USER", postgres.getUsername());
        System.setProperty("DB_PASS", postgres.getPassword());
    }

    @BeforeEach
    void init() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(postgres.getJdbcUrl());
        config.setUsername(postgres.getUsername());
        config.setPassword(postgres.getPassword());
        config.setDriverClassName(postgres.getDriverClassName());
        config.setMaximumPoolSize(2);
        config.setConnectionTimeout(3000);

        this.dataSource = new HikariDataSource(config);
        this.publisherDAO = new PublisherDAO(dataSource);
        this.bookDAO = new BookDAO(dataSource);
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            conn.prepareStatement("DELETE FROM books").executeUpdate();
            conn.prepareStatement("DELETE FROM publishers").executeUpdate();
        }
    }

    @Test
    void shouldCreateAndRetrievePublisher() throws SQLException {
        Publisher publisher = new Publisher();
        publisher.setName("АСТ");
        publisherDAO.create(publisher);

        Optional<Publisher> found = publisherDAO.getById(publisher.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("АСТ");
    }

    @Test
    void shouldUpdatePublisher() throws SQLException {
        Publisher publisher = new Publisher();
        publisher.setName("Росмэн");
        publisherDAO.create(publisher);

        publisher.setName("Росмэн (обновлённое)");
        publisherDAO.update(publisher);

        Optional<Publisher> updated = publisherDAO.getById(publisher.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getName()).isEqualTo("Росмэн (обновлённое)");
    }

    @Test
    void shouldDeletePublisher() throws SQLException {
        Publisher publisher = new Publisher();
        publisher.setName("Дрофа");
        publisherDAO.create(publisher);

        publisherDAO.delete(publisher.getId());

        assertThat(publisherDAO.getById(publisher.getId())).isEmpty();
    }

    @Test
    void shouldHandleBookRelations() throws SQLException {
        Publisher publisher = new Publisher();
        publisher.setName("Азбука");
        publisherDAO.create(publisher);

        Book book = new Book();
        book.setTitle("Игра престолов");
        book.setPublisher(publisher);
        bookDAO.create(book);

        Publisher retrieved = publisherDAO.getById(publisher.getId()).orElseThrow();
        assertThat(retrieved.getBooks())
                .hasSize(1)
                .extracting(Book::getTitle)
                .containsExactly("Игра престолов");
    }

    @Test
    void shouldNotDeleteBooksOnPublisherDelete() throws SQLException {
        Publisher publisher = new Publisher();
        publisher.setName("Фламинго");
        publisherDAO.create(publisher);

        Book book = new Book();
        book.setTitle("Маленький принц");
        book.setPublisher(publisher);
        bookDAO.create(book);

        publisherDAO.delete(publisher.getId());

        Optional<Book> foundBook = bookDAO.getById(book.getId());
        assertThat(foundBook).isPresent();
        assertThat(foundBook.get().getPublisher()).isNull();
    }
}