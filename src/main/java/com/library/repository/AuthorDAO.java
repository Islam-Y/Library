package com.library.repository;

import com.library.model.Author;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class AuthorDAO {
    private final DataSource dataSource;

    public AuthorDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Author getById(int id) throws SQLException {
        String sql = "SELECT id, name, surname, country FROM authors WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Author author = new Author();
                    author.setId(rs.getInt("id"));
                    author.setName(rs.getString("name"));
                    author.setSurname(rs.getString("surname"));
                    author.setCountry(rs.getString("country"));
                    // Если необходимо, можно отдельно загрузить книги автора через BookDAO
                    return author;
                }
            }
        }
        return null;
    }

    public List<Author> getAll() throws SQLException {
        List<Author> authors = new ArrayList<>();
        String sql = "SELECT id, name, surname, country FROM authors";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Author author = new Author();
                author.setId(rs.getInt("id"));
                author.setName(rs.getString("name"));
                author.setSurname(rs.getString("surname"));
                author.setCountry(rs.getString("country"));
                authors.add(author);
            }
        }
        return authors;
    }

    public void create(Author author) throws SQLException {
        String sql = "INSERT INTO authors (name, surname, country) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, author.getName());
            stmt.setString(2, author.getSurname());
            stmt.setString(3, author.getCountry());
            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    author.setId(generatedKeys.getInt(1));
                }
            }
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
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM authors WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
