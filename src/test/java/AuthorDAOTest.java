import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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
import com.library.repository.AuthorDAO;
import com.library.repository.BookDAO;

@Testcontainers
class AuthorDAOTest {

    @Container
    private static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15")
                    .withDatabaseName("test")
                    .withUsername("test")
                    .withPassword("test")
                    .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*\\n", 2))
                    .waitingFor(Wait.forListeningPort())
                    .withStartupTimeout(Duration.ofSeconds(60));

    private AuthorDAO authorDAO;
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
        // Настройка HikariCP
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(postgres.getJdbcUrl());
        config.setUsername(postgres.getUsername());
        config.setPassword(postgres.getPassword());
        config.setDriverClassName(postgres.getDriverClassName());
        config.setMaximumPoolSize(2);
        config.setConnectionTimeout(3000);

        // Создаем DataSource
        this.dataSource = new HikariDataSource(config);
        this.authorDAO = new AuthorDAO(dataSource);
        this.bookDAO = new BookDAO(dataSource); // Инициализация BookDAO для тестирования связей
    }

    @AfterEach
    void tearDown() throws SQLException {
        // Очищаем данные после каждого теста
        try (Connection conn = dataSource.getConnection()) {
            conn.prepareStatement("DELETE FROM book_author").executeUpdate(); // Очистка связей
            conn.prepareStatement("DELETE FROM books").executeUpdate(); // Очистка книг
            conn.prepareStatement("DELETE FROM authors").executeUpdate(); // Очистка авторов
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
        // Создаем автора
        Author author = new Author();
        author.setName("Николай");
        author.setSurname("Гоголь");
        author.setCountry("Россия");
        authorDAO.create(author);

        // Создаем книги
        Book book1 = new Book();
        book1.setTitle("Мёртвые души");
        bookDAO.create(book1);

        Book book2 = new Book();
        book2.setTitle("Ревизор");
        bookDAO.create(book2);

        // Добавляем книги к автору
        Set<Book> books = new HashSet<>();
        books.add(book1);
        books.add(book2);
        author.setBooks(books);
        authorDAO.update(author);

        // Получаем автора с книгами
        Optional<Author> found = authorDAO.getById(author.getId());
        assertThat(found).isPresent();

        // Проверяем книги автора
        Author retrievedAuthor = found.get();
        assertThat(retrievedAuthor.getBooks())
                .hasSize(2)
                .extracting(Book::getTitle)
                .containsExactlyInAnyOrder("Мёртвые души", "Ревизор");
    }

    @Test
    void shouldRemoveBooksFromAuthor() throws SQLException {
        // Создаем автора
        Author author = new Author();
        author.setName("Иван");
        author.setSurname("Тургенев");
        author.setCountry("Россия");
        authorDAO.create(author);

        // Создаем книгу
        Book book = new Book();
        book.setTitle("Отцы и дети");
        bookDAO.create(book);

        // Добавляем книгу к автору
        Set<Book> books = new HashSet<>();
        books.add(book);
        author.setBooks(books);
        authorDAO.update(author);

        // Удаляем книгу у автора
        author.setBooks(new HashSet<>());
        authorDAO.update(author);

        // Получаем автора
        Optional<Author> found = authorDAO.getById(author.getId());
        assertThat(found).isPresent();

        // Проверяем, что у автора нет книг
        Author retrievedAuthor = found.get();
        assertThat(retrievedAuthor.getBooks()).isEmpty();
    }

    @Test
    void shouldNotFindNonExistentAuthor() throws SQLException {
        Optional<Author> found = authorDAO.getById(-1);
        assertThat(found).isEmpty();
    }
}