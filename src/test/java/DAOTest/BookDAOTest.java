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
            new PostgreSQLContainer<>("postgres:14")
                    .withDatabaseName("test")
                    .withUsername("test")
                    .withPassword("test")
                    .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*\\n", 2))
                    .withStartupTimeout(Duration.ofSeconds(60));

    private static DataSource dataSource;
    private BookDAO bookDAO;
    private AuthorDAO authorDAO;
    private PublisherDAO publisherDAO;

    @BeforeAll
    static void setup() {
        postgres.start();

        // Настройка HikariCP
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(postgres.getJdbcUrl());
        config.setUsername(postgres.getUsername());
        config.setPassword(postgres.getPassword());
        config.setDriverClassName(postgres.getDriverClassName());
        config.setMaximumPoolSize(2);
        config.setConnectionTimeout(3000);

        dataSource = new HikariDataSource(config);

        // Настройка Flyway без clean()
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas("public")
                .locations("filesystem:src/main/resources/db/migration")
                .baselineOnMigrate(true)
                .load();

        flyway.migrate();
    }

    @BeforeEach
    void init() {
        this.bookDAO = BookDAO.forTests(dataSource);
        this.authorDAO = AuthorDAO.forTests(dataSource);
        this.publisherDAO = PublisherDAO.forTests(dataSource);
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