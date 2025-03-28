package com.library.service;

import com.library.exception.AuthorServiceException;
import com.library.exception.BookServiceException;
import com.library.model.Book;
import com.library.repository.AuthorDAO;
import com.library.dto.AuthorDTO;
import com.library.model.Author;
import com.library.mapper.AuthorMapper;
import com.library.repository.BookDAO;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Optional;

public class AuthorService {
    private final AuthorDAO authorDAO;
    private final AuthorMapper authorMapper;

    private AuthorService(AuthorDAO authorDAO, AuthorMapper mapper) {
        this.authorDAO = authorDAO;
        this.authorMapper = mapper;
    }

    AuthorService() {
        this.authorMapper = AuthorMapper.INSTANCE;
        this.authorDAO = new AuthorDAO();
    }

    public static AuthorService forTest(AuthorDAO authorDAO, AuthorMapper authorMapper) {
        return new AuthorService(authorDAO, authorMapper);
    }

    public List<AuthorDTO> getAllAuthors() {
        List<Author> authors = null;
        try {
            authors = authorDAO.getAll();
        } catch (SQLException e) {
            throw new AuthorServiceException("Ошибка при получении списка авторов", e);
        }
        return authors.stream()
                .map(authorMapper::toDTO)
                .toList();
    }

    public AuthorDTO getAuthorById(int id) {
        try {
            return authorDAO.getById(id)
                    .map(authorMapper::toDTO)
                    .orElseThrow(() -> new AuthorServiceException("Автор не найден", new RuntimeException()));
        } catch (SQLException e) {
            throw new AuthorServiceException("Ошибка при получении автора с ID " + id, e);
        }
    }

    public void addAuthor(AuthorDTO authorDTO) {
        BookDAO bookDAO = new BookDAO();
        try {
            if (authorDTO.getName() == null || authorDTO.getName().isEmpty()) {
                throw new IllegalArgumentException("Name is required");
            }

            Author author = authorMapper.toModel(authorDTO);

            Set<Integer> bookIds = authorDTO.getBookIds() != null
                    ? authorDTO.getBookIds()
                    : Collections.emptySet();

            Set<Book> books = bookIds.stream()
                    .map(bookId -> {
                        try {
                            return bookDAO.getById(bookId);
                        } catch (SQLException e) {
                            throw new AuthorServiceException("Error adding books", new RuntimeException(e));
                        }
                    })
                    .flatMap(Optional::stream)
                    .collect(Collectors.toSet());

            author.setBooks(books);
            authorDAO.create(author);
        } catch (SQLException e) {
            if (e.getErrorCode() == 23503) {
                throw new BookServiceException("Foreign key constraint error: book not found", e);
            }
            throw new BookServiceException("Error while adding book", e);
        }
    }

    public void updateAuthor(int id, AuthorDTO authorDTO) {
        try {
            Author existingAuthor = authorDAO.getById(id)
                    .orElseThrow(() -> new AuthorServiceException("Author not found", new RuntimeException()));

            existingAuthor.setName(authorDTO.getName());
            existingAuthor.setSurname(authorDTO.getSurname());
            existingAuthor.setCountry(authorDTO.getCountry());

            Set<Integer> bookIds = authorDTO.getBookIds() != null
                    ? authorDTO.getBookIds()
                    : Collections.emptySet();

            Set<Book> books = bookIds.stream()
                    .map(bookId -> {
                        Book book = new Book();
                        book.setId(bookId);
                        return book;
                    })
                    .collect(Collectors.toSet());
            existingAuthor.setBooks(books);

            authorDAO.update(existingAuthor);
            authorDAO.updateBooksOfAuthor(existingAuthor);
        } catch (SQLException e) {
            throw new AuthorServiceException("Error while updating author with ID " + id, e);
        }
    }

    public void deleteAuthor(int id) {
        try {
            authorDAO.delete(id);
        } catch (SQLException e) {
            throw new AuthorServiceException("Ошибка при удалении автора с ID " + id, e);
        }
    }
}
