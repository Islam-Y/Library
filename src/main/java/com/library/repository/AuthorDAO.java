package com.library.repository;

import com.library.config.DataSourceProvider;
import com.library.model.Author;
import com.library.model.Book;
import com.library.model.Publisher;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import javax.sql.DataSource;

public class AuthorDAO {
    private DataSource dataSource;

    public AuthorDAO() {
        this.dataSource = DataSourceProvider.getDataSource();
    }

    private AuthorDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static AuthorDAO forTests(DataSource dataSource) {
        return new AuthorDAO(dataSource);
    }

    public Optional<Author> getById(int id) throws SQLException {
        String sql = "SELECT id, name, surname, country FROM authors WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Author author = mapRowToAuthor(rs);
                    author.setBooks(getBooksForAuthor(id));
                    return Optional.of(author);
                }
                return Optional.empty();
            }
        }
    }

    private Set<Book> getBooksForAuthor(int authorId) throws SQLException {
        String sql = """
                SELECT b.id, b.title, b.published_date, b.genre,\s
                       p.id AS publisher_id, p.name AS publisher_name
                FROM books b
                LEFT JOIN publishers p ON b.publisher_id = p.id
                INNER JOIN book_author ba ON b.id = ba.book_id
                WHERE ba.author_id = ?;
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, authorId);
            try (ResultSet rs = stmt.executeQuery()) {
                Set<Book> books = new HashSet<>();
                while (rs.next()) {
                    books.add(mapRowToBook(rs));
                }
                return books;
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

        int publisherId = rs.getInt("publisher_id");
        if (!rs.wasNull()) {
            Publisher publisher = new Publisher();
            publisher.setId(publisherId);
            publisher.setName(rs.getString("publisher_name"));
            book.setPublisher(publisher);
        }

        return book;
    }

    public List<Author> getAll() throws SQLException {
        String sql = "SELECT id, name, surname, country FROM authors";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            List<Author> authors = new ArrayList<>();
            while (rs.next()) {
                Author author = mapRowToAuthor(rs);
                author.setBooks(getBooksForAuthor(author.getId()));
                authors.add(author);
            }
            return authors;
        }
    }

    public void create(Author author) throws SQLException {
        String sql = "INSERT INTO authors (name, surname, country) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setAuthorParameters(stmt, author);
            stmt.executeUpdate();
            setIdFromGeneratedKeys(stmt, author);
        }
        updateBooksOfAuthor(author);
    }

    public void update(Author author) throws SQLException {
        String sql = "UPDATE authors SET name = ?, surname = ?, country = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setAuthorParameters(stmt, author);
            stmt.setInt(4, author.getId());
            stmt.executeUpdate();
        }

        updateBooksOfAuthor(author);
    }

    public void updateBooksOfAuthor(Author author) throws SQLException {
        if (author.getId() < 0) return;

        removeAllBooksFromAuthor(author.getId());

        if (author.getBooks() != null && !author.getBooks().isEmpty()) {
            addBooksToAuthor(author.getId(), author.getBooks());
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM authors WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // region Helper Methods
    private Author mapRowToAuthor(ResultSet rs) throws SQLException {
        Author author = new Author();
        author.setId(rs.getInt("id"));
        author.setName(rs.getString("name"));
        author.setSurname(rs.getString("surname"));
        author.setCountry(rs.getString("country"));
        return author;
    }

    private void setAuthorParameters(PreparedStatement stmt, Author author) throws SQLException {
        stmt.setString(1, author.getName());
        stmt.setString(2, author.getSurname());
        stmt.setString(3, author.getCountry());
    }

    private void setIdFromGeneratedKeys(PreparedStatement stmt, Author author) throws SQLException {
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                author.setId(generatedKeys.getInt(1));
            }
        }
    }

    private void addBooksToAuthor(int authorId, Set<Book> books) throws SQLException {
        String sql = "INSERT INTO book_author (author_id, book_id) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, authorId);

            for (Book book : books) {
                stmt.setInt(2, book.getId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void removeAllBooksFromAuthor(int authorId) throws SQLException {
        String sql = "DELETE FROM book_author WHERE author_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, authorId);
            stmt.executeUpdate();
        }
    }
    // endregion
}
