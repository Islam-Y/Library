package DAOTest;


import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
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

import com.library.model.Author;
import com.library.model.Book;
import com.library.model.Publisher;
import com.library.repository.AuthorDAO;
import com.library.repository.BookDAO;
import com.library.repository.PublisherDAO;

@Testcontainers
class BookDAOTest {

    @Container
    private static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15")
                    .withDatabaseName("test")
                    .withUsername("test")
                    .withPassword("test")
                    .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*\\n", 2))
                    .waitingFor(Wait.forListeningPort())
                    .withStartupTimeout(Duration.ofSeconds(60));

    private BookDAO bookDAO;
    private AuthorDAO authorDAO;
    private PublisherDAO publisherDAO;
    private DataSource dataSource;

    @BeforeAll
    static void setup() {
        postgres.start();

        System.out.println("JDBC URL: " + postgres.getJdbcUrl());
        System.out.println("Username: " + postgres.getUsername());
        System.out.println("Password: " + postgres.getPassword());

        Flyway flyway = Flyway.configure()
                .cleanDisabled(false)
                .dataSource(
                        postgres.getJdbcUrl(),
                        postgres.getUsername(),
                        postgres.getPassword()
                )
                .schemas("public")
                .locations("filesystem:src/main/resources/db/migration") // Абсолютный путь
                .baselineOnMigrate(true)
                .load();

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
        this.bookDAO = new BookDAO(dataSource);
        this.authorDAO = new AuthorDAO(dataSource);
        this.publisherDAO = new PublisherDAO(dataSource);
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            conn.prepareStatement("DELETE FROM book_author").executeUpdate();
            conn.prepareStatement("DELETE FROM books").executeUpdate();
            conn.prepareStatement("DELETE FROM authors").executeUpdate();
            conn.prepareStatement("DELETE FROM publishers").executeUpdate();
        }
    }

    @Test
    void shouldCreateAndRetrieveBook() throws SQLException {
        Book book = new Book();
        book.setTitle("1984");
        book.setPublishedDate(LocalDate.now().toString());
        book.setGenre("Антиутопия");

        bookDAO.create(book);

        Optional<Book> found = bookDAO.getById(book.getId());
        assertThat(found).isPresent();

        Book retrievedBook = found.get();
        assertThat(retrievedBook.getTitle()).isEqualTo("1984");
        assertThat(retrievedBook.getGenre()).isEqualTo("Антиутопия");
    }

    @Test
    void shouldUpdateBook() throws SQLException {
        Book book = new Book();
        book.setTitle("Старик и море");
        bookDAO.create(book);

        book.setTitle("Старик и море (обновлённое)");
        book.setGenre("Роман");
        bookDAO.update(book);

        Optional<Book> updated = bookDAO.getById(book.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getTitle()).isEqualTo("Старик и море (обновлённое)");
    }

    @Test
    void shouldDeleteBook() throws SQLException {
        Book book = new Book();
        book.setTitle("Мастер и Маргарита");
        bookDAO.create(book);

        bookDAO.delete(book.getId());

        assertThat(bookDAO.getById(book.getId())).isEmpty();
    }

    @Test
    void shouldHandleAuthorRelations() throws SQLException {
        Author author = new Author();
        author.setName("Джордж");
        author.setSurname("Оруэлл");
        authorDAO.create(author);

        Book book = new Book();
        book.setTitle("1984");
        book.setAuthors(new HashSet<>(Collections.singleton(author)));
        bookDAO.create(book);

        Book retrieved = bookDAO.getById(book.getId()).orElseThrow();
        assertThat(retrieved.getAuthors())
                .hasSize(1)
                .extracting(Author::getSurname)
                .containsExactly("Оруэлл");
    }

    @Test
    void shouldHandlePublisherRelation() throws SQLException {
        Publisher publisher = new Publisher();
        publisher.setName("Эксмо");
        publisherDAO.create(publisher);

        Book book = new Book();
        book.setTitle("Преступление и наказание");
        book.setPublisher(publisher);
        bookDAO.create(book);

        Book retrieved = bookDAO.getById(book.getId()).orElseThrow();
        assertThat(retrieved.getPublisher())
                .isNotNull()
                .extracting(Publisher::getName)
                .isEqualTo("Эксмо");
    }
}