package com.library.repository;

import com.library.model.Author;
import com.library.model.Book;
import com.library.model.Publisher;

import java.sql.*;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;

public class BookDAO {
    private final DataSource dataSource;

    public BookDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Book getById(int id) throws SQLException {
        String sql = "SELECT id, title, published_date, publisher_id, genre FROM books WHERE id = ?";
        Book book = null;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()){
                if (rs.next()){
                    book = new Book();
                    book.setId(rs.getInt("id"));
                    book.setTitle(rs.getString("title"));
                    Date publishedDate = rs.getDate("published_date");
                    book.setPublishedDate(publishedDate != null ? publishedDate.toString() : null);
                    book.setGenre(rs.getString("genre"));

                    int publisherId = rs.getInt("publisher_id");
                    if (publisherId > 0) {
                        Publisher publisher = new Publisher();
                        publisher.setId(publisherId);
                        book.setPublisher(publisher);
                    }
                }
            }
        }
        // Загружаем авторов книги
        if (book != null) {
            book.setAuthors(getAuthorsForBook(book.getId()));
        }
        return book;
    }

    public List<Book> getAll() throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT id, title, published_date, publisher_id, genre FROM books";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()){

            while (rs.next()){
                Book book = new Book();
                book.setId(rs.getInt("id"));
                book.setTitle(rs.getString("title"));
                Date publishedDate = rs.getDate("published_date");
                book.setPublishedDate(publishedDate != null ? publishedDate.toString() : null);
                book.setGenre(rs.getString("genre"));

                int publisherId = rs.getInt("publisher_id");
                if (publisherId > 0) {
                    Publisher publisher = new Publisher();
                    publisher.setId(publisherId);
                    book.setPublisher(publisher);
                }
                // Загружаем список авторов для каждой книги
                book.setAuthors(getAuthorsForBook(book.getId()));
                books.add(book);
            }
        }
        return books;
    }

    public void create(Book book) throws SQLException {
        String sql = "INSERT INTO books (title, published_date, publisher_id, genre) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){

            stmt.setString(1, book.getTitle());
            // Преобразуем дату (если она задана) в java.sql.Date
            if (book.getPublishedDate() != null) {
                stmt.setDate(2, Date.valueOf(book.getPublishedDate()));
            } else {
                stmt.setNull(2, Types.DATE);
            }
            if (book.getPublisher() != null) {
                stmt.setInt(3, book.getPublisher().getId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.setString(4, book.getGenre());
            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()){
                if (generatedKeys.next()){
                    book.setId(generatedKeys.getInt(1));
                }
            }
        }
        // Обновляем связь many-to-many: добавляем записи в таблицу book_author
        if (book.getAuthors() != null && !book.getAuthors().isEmpty()){
            addAuthorsToBook(book.getId(), book.getAuthors());
        }
    }

    public void update(Book book) throws SQLException {
        String sql = "UPDATE books SET title = ?, published_date = ?, publisher_id = ?, genre = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setString(1, book.getTitle());
            if (book.getPublishedDate() != null) {
                stmt.setDate(2, Date.valueOf(book.getPublishedDate()));
            } else {
                stmt.setNull(2, Types.DATE);
            }
            if (book.getPublisher() != null) {
                stmt.setInt(3, book.getPublisher().getId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.setString(4, book.getGenre());
            stmt.setInt(5, book.getId());
            stmt.executeUpdate();
        }
        // Обновляем связи в таблице book_author:
        // Удаляем старые связи и добавляем новые
        removeAllAuthorsFromBook(book.getId());
        if (book.getAuthors() != null && !book.getAuthors().isEmpty()){
            addAuthorsToBook(book.getId(), book.getAuthors());
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM books WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // Вспомогательные методы для работы с таблицей-связкой book_author

    private void addAuthorsToBook(int bookId, Set<Author> authors) throws SQLException {
        String sql = "INSERT INTO book_author (book_id, author_id) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (Author author : authors) {
                stmt.setInt(1, bookId);
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

    private Set<Author> getAuthorsForBook(int bookId) throws SQLException {
        Set<Author> authors = new HashSet<>();
        String sql = "SELECT a.id, a.name, a.surname, a.country FROM authors a " +
                "JOIN book_author ba ON a.id = ba.author_id " +
                "WHERE ba.book_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookId);
            try (ResultSet rs = stmt.executeQuery()){
                while (rs.next()){
                    Author author = new Author();
                    author.setId(rs.getInt("id"));
                    author.setName(rs.getString("name"));
                    author.setSurname(rs.getString("surname"));
                    author.setCountry(rs.getString("country"));
                    authors.add(author);
                }
            }
        }
        return authors;
    }
}
