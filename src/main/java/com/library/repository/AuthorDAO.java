package com.library.repository;

import com.library.model.Author;
import com.library.model.Book;

import java.sql.*;
import java.util.*;
import javax.sql.DataSource;

public class AuthorDAO {
    private final DataSource dataSource;

    public AuthorDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Optional<Author> getById(int id) throws SQLException {
        String sql = "SELECT id, name, surname, country FROM authors WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Author author = mapRowToAuthor(rs);
                    // Загружаем книги автора
                    author.setBooks(getBooksByAuthorId(conn, id));
                    return Optional.of(author);
                }
                return Optional.empty();
            }
        }
    }

    private Set<Book> getBooksByAuthorId(Connection conn, int authorId) throws SQLException {
        String sql = "SELECT b.id, b.title FROM books b " +
                "INNER JOIN book_author ba ON b.id = ba.book_id " +
                "WHERE ba.author_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, authorId);
            try (ResultSet rs = stmt.executeQuery()) {
                Set<Book> books = new HashSet<>();
                while (rs.next()) {
                    Book book = new Book();
                    book.setId(rs.getInt("id"));
                    book.setTitle(rs.getString("title"));
                    books.add(book);
                }
                return books;
            }
        }
    }

    public List<Author> getAll() throws SQLException {
        String sql = "SELECT id, name, surname, country FROM authors";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            List<Author> authors = new ArrayList<>();
            while (rs.next()) {
                authors.add(mapRowToAuthor(rs));
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
    }

    public void update(Author author) throws SQLException {
        String sql = "UPDATE authors SET name = ?, surname = ?, country = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, author.getName());
            stmt.setString(2, author.getSurname());
            stmt.setString(3, author.getCountry());
            stmt.setInt(4, author.getId());
            stmt.executeUpdate();
        }

        // Обновление связей с книгами
        updateBookAuthors(author);
    }

    private void updateBookAuthors(Author author) throws SQLException {
        // Удаляем старые связи
        String deleteSql = "DELETE FROM book_author WHERE author_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
            stmt.setInt(1, author.getId());
            stmt.executeUpdate();
        }

        // Добавляем новые связи
        String insertSql = "INSERT INTO book_author (author_id, book_id) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            for (Book book : author.getBooks()) {
                stmt.setInt(1, author.getId());
                stmt.setInt(2, book.getId());
                stmt.addBatch();
            }
            stmt.executeBatch();
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
    // endregion
}
