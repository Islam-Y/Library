package com.library.service;

import com.library.exception.AuthorServiceException;
import com.library.repository.AuthorDAO;
import com.library.dto.AuthorDTO;
import com.library.model.Author;
import com.library.mapper.AuthorMapper;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class AuthorService {
    private final AuthorDAO authorDAO;
    private final AuthorMapper authorMapper;

    public AuthorService(AuthorDAO authorDAO, AuthorMapper authorMapper) {
        this.authorDAO = authorDAO;
        this.authorMapper = authorMapper;
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
                .collect(Collectors.toList());
    }

    public AuthorDTO getAuthorById(int id) {
        try {
            return authorDAO.getById(id)
                    .map(authorMapper::toDTO)
                    .orElseThrow(() -> new RuntimeException("Автор не найден"));
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
        Author existingAuthor = null;
        try {
            existingAuthor = authorDAO.getById(id)
                    .orElseThrow(() -> new RuntimeException("Автор не найден"));

            existingAuthor.setName(authorDTO.getName());
            existingAuthor.setSurname(authorDTO.getSurname());
            existingAuthor.setCountry(authorDTO.getCountry());

            authorDAO.update(existingAuthor);
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
