package com.library.service;

import com.library.exception.AuthorServiceException;
import com.library.repository.AuthorDAO;
import com.library.dto.AuthorDTO;
import com.library.model.Author;
import com.library.mapper.AuthorMapper;

import java.sql.SQLException;
import java.util.List;

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
        Author author = authorMapper.toModel(authorDTO);
        try {
            authorDAO.create(author);
        } catch (SQLException e) {
            throw new AuthorServiceException("Ошибка при добавлении автора", e);
        }
    }

    public void updateAuthor(int id, AuthorDTO authorDTO) {
        try {
            Author existingAuthor = authorDAO.getById(id)
                    .orElseThrow(() -> new AuthorServiceException("Автор не найден", new RuntimeException()));

            existingAuthor.setName(authorDTO.getName());
            existingAuthor.setSurname(authorDTO.getSurname());
            existingAuthor.setCountry(authorDTO.getCountry());

            authorDAO.update(existingAuthor);
            authorDAO.updateBookAuthors(existingAuthor);
        } catch (SQLException e) {
            throw new AuthorServiceException("Ошибка при обновлении автора с ID " + id, e);
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
