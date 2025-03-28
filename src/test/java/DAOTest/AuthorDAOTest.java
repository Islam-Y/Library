package DAOTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.*;

import javax.sql.DataSource;

import com.library.model.Publisher;
import com.library.repository.PublisherDAO;
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
import com.library.repository.AuthorDAO;
import com.library.repository.BookDAO;

@Testcontainers
class AuthorDAOTest {

    @Container
    private static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:14")
                    .withDatabaseName("test")
                    .withUsername("test")
                    .withPassword("test")

                    .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*\\n", 2))
                    .waitingFor(Wait.forListeningPort())
                    .withStartupTimeout(Duration.ofSeconds(60));

    private AuthorDAO authorDAO;
    private BookDAO bookDAO;
    private static DataSource dataSource;

    @BeforeAll
    static void setup() {
        postgres.start();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(postgres.getJdbcUrl());
        config.setUsername(postgres.getUsername());
        config.setPassword(postgres.getPassword());
        config.setDriverClassName(postgres.getDriverClassName());
        config.setMaximumPoolSize(2);
        config.setConnectionTimeout(3000);

        dataSource = new HikariDataSource(config);

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas("public")
                .locations("filesystem:src/main/resources/db/migration")
                .baselineOnMigrate(true)
                .load();

        flyway.repair();
        flyway.migrate();
    }

    @BeforeEach
    void init() {
        this.authorDAO = AuthorDAO.forTests(dataSource);
        this.bookDAO = BookDAO.forTests(dataSource);
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            conn.prepareStatement("DELETE FROM book_author").executeUpdate();
            conn.prepareStatement("DELETE FROM books").executeUpdate();
            conn.prepareStatement("DELETE FROM authors").executeUpdate();
        }
    }

    @Test
    void shouldCreateAndRetrieveAuthor() throws SQLException {
        // Создаем автора
        Author author = new Author();
        author.setName("Фёдор");
        author.setSurname("Достоевский");
        author.setCountry("Россия");

        // Сохраняем автора в БД
        authorDAO.create(author);

        // Получаем автора из БД
        Optional<Author> found = authorDAO.getById(author.getId());
        assertThat(found).isPresent();

        // Проверяем поля автора
        Author retrievedAuthor = found.get();
        assertThat(retrievedAuthor.getName()).isEqualTo("Фёдор");
        assertThat(retrievedAuthor.getSurname()).isEqualTo("Достоевский");
        assertThat(retrievedAuthor.getCountry()).isEqualTo("Россия");
    }

    @Test
    void shouldUpdateAuthor() throws SQLException {
        // Создаем автора
        Author author = new Author();
        author.setName("Лев");
        author.setSurname("Толстой");
        author.setCountry("Россия");
        authorDAO.create(author);

        // Обновляем поля автора
        author.setName("Лев Николаевич");
        author.setCountry("Российская Империя");
        authorDAO.update(author);

        // Получаем обновленного автора
        Optional<Author> updated = authorDAO.getById(author.getId());
        assertThat(updated).isPresent();

        // Проверяем обновленные поля
        Author retrievedAuthor = updated.get();
        assertThat(retrievedAuthor.getName()).isEqualTo("Лев Николаевич");
        assertThat(retrievedAuthor.getCountry()).isEqualTo("Российская Империя");
    }

    @Test
    void shouldDeleteAuthor() throws SQLException {
        // Создаем автора
        Author author = new Author();
        author.setName("Антон");
        author.setSurname("Чехов");
        author.setCountry("Россия");
        authorDAO.create(author);

        // Удаляем автора
        authorDAO.delete(author.getId());

        // Проверяем, что автор удален
        Optional<Author> deleted = authorDAO.getById(author.getId());
        assertThat(deleted).isEmpty();
    }

    @Test
    void shouldAddBooksToAuthor() throws SQLException {
        Publisher publisher = new Publisher();
        publisher.setName("Эксмо");
        PublisherDAO publisherDAO = PublisherDAO.forTests(dataSource);
        publisherDAO.create(publisher);

        Author author = new Author();
        author.setName("Федор");
        author.setSurname("Достоевский");
        authorDAO.create(author);

        Book book1 = new Book();
        book1.setTitle("Преступление и наказание");
        book1.setPublisher(publisher);
        bookDAO.create(book1);

        Book book2 = new Book();
        book2.setTitle("Идиот");
        book2.setPublisher(publisher);
        bookDAO.create(book2);

        author.setBooks(new HashSet<>(Arrays.asList(book1, book2)));
        authorDAO.update(author);

        Author retrieved = authorDAO.getById(author.getId()).orElseThrow();
        assertThat(retrieved.getBooks())
                .hasSize(2)
                .extracting(Book::getTitle)
                .containsExactlyInAnyOrder("Преступление и наказание", "Идиот");
    }

    @Test
    void shouldRemoveBooksFromAuthor() throws SQLException {
        Publisher publisher = new Publisher();
        publisher.setName("АСТ");
        PublisherDAO publisherDAO = PublisherDAO.forTests(dataSource);
        publisherDAO.create(publisher);

        Author author = new Author();
        author.setName("Иван");
        author.setSurname("Тургенев");
        author.setCountry("Россия");
        authorDAO.create(author);

        Book book = new Book();
        book.setTitle("Отцы и дети");
        book.setPublisher(publisher);
        bookDAO.create(book);

        Set<Book> books = new HashSet<>();
        books.add(book);
        author.setBooks(books);
        authorDAO.update(author);

        author.setBooks(new HashSet<>(Collections.singleton(book)));
        authorDAO.update(author);

        // Удаляем книгу у автора
        author.setBooks(new HashSet<>());
        authorDAO.update(author);

        // Получаем автора
        Optional<Author> found = authorDAO.getById(author.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getBooks()).isEmpty();
    }

    @Test
    void shouldNotFindNonExistentAuthor() throws SQLException {
        Optional<Author> found = authorDAO.getById(-1);
        assertThat(found).isEmpty();
    }
}