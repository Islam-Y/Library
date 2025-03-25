package com.library.service;

import com.library.exception.BookServiceException;
import com.library.repository.BookDAO;
import com.library.dto.BookDTO;
import com.library.model.Book;
import com.library.mapper.BookMapper;

import java.sql.SQLException;
import java.util.List;

public class BookService {
    private final BookDAO bookDAO;
    private final BookMapper bookMapper;

    public BookService(BookDAO bookDAO, BookMapper bookMapper) {
        this.bookDAO = bookDAO;
        this.bookMapper = bookMapper;
    }

    public List<BookDTO> getAllBooks() {
        try {
            return bookDAO.getAll().stream()
                    .map(bookMapper::toDTO)
                    .toList();
        } catch (SQLException e) {
            throw new BookServiceException("Ошибка при получении списка книг", e);

        }
    }

    public BookDTO getBookById(int id) {
        try {
            return bookDAO.getById(id)
                    .map(bookMapper::toDTO)
                    .orElseThrow(() -> new BookServiceException("Книга не найдена", new RuntimeException()));
        } catch (SQLException e) {
            throw new BookServiceException("Ошибка при получении книги с ID " + id, e);

        }
    }

    public void addBook(BookDTO bookDTO) {
        Book book = bookMapper.toModel(bookDTO);
        try {
            bookDAO.create(book);
        } catch (SQLException e) {
            throw new BookServiceException("Ошибка при добавлении книги", e);

        }

    }

    public void updateBook(int id, BookDTO bookDTO) {
        Book existingBook = null;
        try {
            existingBook = bookDAO.getById(id)
                    .orElseThrow(() -> new RuntimeException("Книга не найдена"));
            existingBook.setTitle(bookDTO.getTitle());
            existingBook.setPublishedDate(bookDTO.getPublishedDate());
            bookDAO.update(existingBook);
        } catch (SQLException e) {
            throw new BookServiceException("Ошибка при обновлении книги с ID " + id, e);

        }

    }

    public void deleteBook(int id) {
        try {
            bookDAO.delete(id);
        } catch (SQLException e) {
            throw new BookServiceException("Ошибка при удалении книги с ID " + id, e);

        }
    }
}