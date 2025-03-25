package com.library.repository;

import com.library.model.Author;
import com.library.model.Book;
import com.library.model.Publisher;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import javax.sql.DataSource;

public class BookDAO {
    private final DataSource dataSource;

    public BookDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Optional<Book> getById(int id) throws SQLException {
        String sql = """
        SELECT b.id, b.title, b.published_date, b.genre, 
               p.id AS publisher_id, p.name AS publisher_name
        FROM books b
        LEFT JOIN publishers p ON b.publisher_id = p.id
        WHERE b.id = ?
        """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Book book = mapRowToBook(rs);
                    book.setAuthors(getAuthorsForBook(id)); // Загрузка авторов
                    return Optional.of(book);
                }
                return Optional.empty();
            }
        }
    }

    private Book mapRowToBook(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getInt("id"));
        book.setTitle(rs.getString("title"));
        Date publishedDate = rs.getDate("published_date");
        book.setPublishedDate(publishedDate != null ? publishedDate.toString() : null);
        book.setGenre(rs.getString("genre"));

        // Загрузка издателя
        int publisherId = rs.getInt("publisher_id");
        if (!rs.wasNull()) {
            Publisher publisher = new Publisher();
            publisher.setId(publisherId);
            publisher.setName(rs.getString("publisher_name"));
            book.setPublisher(publisher);
        }

        return book;
    }

    public List<Book> getAll() throws SQLException {
        String sql = """
        SELECT b.id, b.title, b.published_date, b.genre, 
               p.id AS publisher_id, p.name AS publisher_name
        FROM books b
        LEFT JOIN publishers p ON b.publisher_id = p.id
        """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            List<Book> books = new ArrayList<>();
            while (rs.next()) {
                Book book = mapRowToBook(rs);
                book.setAuthors(getAuthorsForBook(book.getId()));
                books.add(book);
            }
            return books;
        }
    }

    public void create(Book book) throws SQLException {
        String sql = "INSERT INTO books (title, published_date, publisher_id, genre) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setBookParameters(stmt, book);
            stmt.executeUpdate();
            setIdFromGeneratedKeys(stmt, book);
        }
        updateBookAuthors(book);
    }

    public void update(Book book) throws SQLException {
        String sql = "UPDATE books SET title = ?, published_date = ?, publisher_id = ?, genre = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setBookParameters(stmt, book);
            stmt.setInt(5, book.getId());
            stmt.executeUpdate();
        }
        updateBookAuthors(book);
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM books WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private void setBookParameters(PreparedStatement stmt, Book book) throws SQLException {
        stmt.setString(1, book.getTitle());
        stmt.setDate(2, book.getPublishedDate() != null
                ? Date.valueOf(book.getPublishedDate())
                : null
        );
        stmt.setObject(3, book.getPublisher() != null
                ? book.getPublisher().getId()
                : null, Types.INTEGER
        );
        stmt.setString(4, book.getGenre());
    }

    private void setIdFromGeneratedKeys(PreparedStatement stmt, Book book) throws SQLException {
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                book.setId(generatedKeys.getInt(1));
            }
        }
    }

    private void updateBookAuthors(Book book) throws SQLException {
        if (book.getId() < 0) return;

        removeAllAuthorsFromBook(book.getId());
        if (book.getAuthors() != null && !book.getAuthors().isEmpty()) {
            addAuthorsToBook(book.getId(), book.getAuthors());
        }
    }

    private Set<Author> getAuthorsForBook(int bookId) throws SQLException {
        String sql = """
                SELECT a.id, a.name, a.surname, a.country 
                FROM authors a 
                JOIN book_author ba ON a.id = ba.author_id 
                WHERE ba.book_id = ?
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                Set<Author> authors = new HashSet<>();
                while (rs.next()) {
                    authors.add(mapRowToAuthor(rs));
                }
                return authors;
            }
        }
    }

    private Author mapRowToAuthor(ResultSet rs) throws SQLException {
        Author author = new Author();
        author.setId(rs.getInt("id"));
        author.setName(rs.getString("name"));
        author.setSurname(rs.getString("surname"));
        author.setCountry(rs.getString("country"));
        return author;
    }

    private void addAuthorsToBook(int bookId, Set<Author> authors) throws SQLException {
        String sql = "INSERT INTO book_author (book_id, author_id) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookId);

            for (Author author : authors) {

                stmt.setInt(2, author.getId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void removeAllAuthorsFromBook(int bookId) throws SQLException {
        String sql = "DELETE FROM book_author WHERE book_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookId);
            stmt.executeUpdate();
        }
    }
    // endregion
}
