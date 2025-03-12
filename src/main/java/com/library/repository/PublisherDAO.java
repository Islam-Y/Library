package com.library.repository;

import com.library.model.Book;
import com.library.model.Publisher;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class PublisherDAO {
    private final DataSource dataSource;

    public PublisherDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Publisher getById(int id) throws SQLException {
        String sql = "SELECT id, name FROM publishers WHERE id = ?";
        Publisher publisher = null;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()){
                if (rs.next()){
                    publisher = new Publisher();
                    publisher.setId(rs.getInt("id"));
                    publisher.setName(rs.getString("name"));
                    // Можно дополнительно загрузить список книг, опубликованных этим издателем
                    publisher.setBooks(getBooksForPublisher(rs.getInt("id")));
                }
            }
        }
        return publisher;
    }

    public List<Publisher> getAll() throws SQLException {
        List<Publisher> publishers = new ArrayList<>();
        String sql = "SELECT id, name FROM publishers";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()){

            while (rs.next()){
                Publisher publisher = new Publisher();
                publisher.setId(rs.getInt("id"));
                publisher.setName(rs.getString("name"));
                publisher.setBooks(getBooksForPublisher(rs.getInt("id")));
                publishers.add(publisher);
            }
        }
        return publishers;
    }

    public void create(Publisher publisher) throws SQLException {
        String sql = "INSERT INTO publishers (name) VALUES (?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){

            stmt.setString(1, publisher.getName());
            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()){
                if (generatedKeys.next()){
                    publisher.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public void update(Publisher publisher) throws SQLException {
        String sql = "UPDATE publishers SET name = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setString(1, publisher.getName());
            stmt.setInt(2, publisher.getId());
            stmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM publishers WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // Метод для загрузки книг данного издателя
    private List<Book> getBooksForPublisher(int publisherId) throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT id, title, published_date, genre FROM books WHERE publisher_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setInt(1, publisherId);
            try (ResultSet rs = stmt.executeQuery()){
                while (rs.next()){
                    Book book = new Book();
                    book.setId(rs.getInt("id"));
                    book.setTitle(rs.getString("title"));
                    Date publishedDate = rs.getDate("published_date");
                    book.setPublishedDate(publishedDate != null ? publishedDate.toString() : null);
                    book.setGenre(rs.getString("genre"));
                    // Издатель уже известен, поэтому не заполняем его снова
                    books.add(book);
                }
            }
        }
        return books;
    }
}
