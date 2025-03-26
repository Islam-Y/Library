package com.library.repository;

import com.library.config.DataSourceProvider;
import com.library.model.Book;
import com.library.model.Publisher;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

public class PublisherDAO {
    private final DataSource dataSource;

    public PublisherDAO() {
        this.dataSource = DataSourceProvider.getDataSource();
    }

    public Optional<Publisher> getById(int id) throws SQLException {
        String sql = "SELECT id, name FROM publishers WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Publisher publisher = mapRowToPublisher(rs);
                    publisher.setBooks(getBooksForPublisher(id));
                    return Optional.of(publisher);
                }
                return Optional.empty();
            }
        }
    }

    public List<Publisher> getAll() throws SQLException {
        String sql = "SELECT id, name FROM publishers";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            List<Publisher> publishers = new ArrayList<>();
            while (rs.next()) {
                Publisher publisher = mapRowToPublisher(rs);
                publisher.setBooks(getBooksForPublisher(publisher.getId()));
                publishers.add(publisher);
            }
            return publishers;
        }
    }

    public void create(Publisher publisher) throws SQLException {
        String sql = "INSERT INTO publishers (name) VALUES (?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, publisher.getName());
            stmt.executeUpdate();
            setIdFromGeneratedKeys(stmt, publisher);
        }
    }

    public void update(Publisher publisher) throws SQLException {
        String sql = "UPDATE publishers SET name = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, publisher.getName());
            stmt.setInt(2, publisher.getId());
            stmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        // Обнуляем publisher_id у связанных книг
        String updateSql = "UPDATE books SET publisher_id = NULL WHERE publisher_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateSql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }

        String sql = "DELETE FROM publishers WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // region Helper Methods
    private Publisher mapRowToPublisher(ResultSet rs) throws SQLException {
        Publisher publisher = new Publisher();
        publisher.setId(rs.getInt("id"));
        publisher.setName(rs.getString("name"));
        return publisher;
    }

    private List<Book> getBooksForPublisher(int publisherId) throws SQLException {
        String sql = "SELECT id, title, published_date, genre FROM books WHERE publisher_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, publisherId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Book> books = new ArrayList<>();
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
        book.setPublishedDate(Optional.ofNullable(publishedDate).map(Date::toString).orElse(null));
        book.setGenre(rs.getString("genre"));
        return book;
    }

    private void setIdFromGeneratedKeys(PreparedStatement stmt, Publisher publisher) throws SQLException {
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                publisher.setId(generatedKeys.getInt(1));
            }
        }
    }
    // endregion
}
