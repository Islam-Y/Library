package com.library.servlet;

import com.library.config.DataSourceProvider;
import com.library.dto.AuthorDTO;
import com.library.repository.AuthorDAO;
import com.library.mapper.AuthorMapper;
import com.library.service.AuthorService;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;

@WebServlet("/authors/*")
public class AuthorServlet extends HttpServlet {

    private static final String ERROR_INVALID_ID_FORMAT = "{\"error\":\"Invalid author ID format\"}";
    private static final String ERROR_SERVER_PREFIX = "{\"error\":\"Server error: ";
    private static final String ERROR_SERVER_SUFFIX = "\"}";
    private static final String ERROR_ID_MISMATCH = "{\"error\":\"ID in path and body mismatch\"}";
    private static final String ERROR_INVALID_REQUEST = "{\"error\":\"Invalid request: ";

    private AuthorService authorService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void init() throws ServletException {
        super.init();
        DataSource dataSource = DataSourceProvider.getDataSource();
        AuthorDAO authorDAO = new AuthorDAO(dataSource);
        AuthorMapper authorMapper = AuthorMapper.INSTANCE;
        this.authorService = new AuthorService(authorDAO, authorMapper);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp){
        String pathInfo = req.getPathInfo();
        resp.setContentType("application/json");

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Получение всех авторов
                List<AuthorDTO> authors = authorService.getAllAuthors();
                objectMapper.writeValue(resp.getWriter(), authors);
            } else {
                // Получение одного автора
                String[] parts = pathInfo.split("/");
                if (parts.length != 2) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }

                int authorId = Integer.parseInt(parts[1]);
                AuthorDTO author = authorService.getAuthorById(authorId);
                if (author != null) {
                    objectMapper.writeValue(resp.getWriter(), author);
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            }
        } catch (NumberFormatException e) {
            handleError(resp, HttpServletResponse.SC_BAD_REQUEST, ERROR_INVALID_ID_FORMAT);
        } catch (Exception e) {
            handleServerError(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            AuthorDTO author = objectMapper.readValue(req.getReader(), AuthorDTO.class);
            authorService.addAuthor(author);
            resp.setStatus(HttpServletResponse.SC_CREATED);
        } catch (Exception e) {
            handleError(resp, HttpServletResponse.SC_BAD_REQUEST, ERROR_INVALID_REQUEST + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp){
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            String[] parts = pathInfo.split("/");
            if (parts.length != 2) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            int authorId = Integer.parseInt(parts[1]);
            AuthorDTO author = objectMapper.readValue(req.getReader(), AuthorDTO.class);

            if (author.getId() != authorId) {
                handleError(resp, HttpServletResponse.SC_BAD_REQUEST, ERROR_ID_MISMATCH);
                return;
            }

            authorService.updateAuthor(authorId, author);
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (NumberFormatException e) {
            handleError(resp, HttpServletResponse.SC_BAD_REQUEST, ERROR_INVALID_ID_FORMAT);
        } catch (Exception e) {
            handleServerError(resp, e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            String[] parts = pathInfo.split("/");
            if (parts.length != 2) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            int authorId = Integer.parseInt(parts[1]);
            authorService.deleteAuthor(authorId);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (NumberFormatException e) {
            handleError(resp, HttpServletResponse.SC_BAD_REQUEST, ERROR_INVALID_ID_FORMAT);
        } catch (Exception e) {
            handleServerError(resp, e);
        }
    }

    private void handleError(HttpServletResponse resp, int statusCode, String errorMessage) {
        try {
            resp.setStatus(statusCode);
            resp.getWriter().write(errorMessage);
        } catch (IOException ioException) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleServerError(HttpServletResponse resp, Exception e) {
        try {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(ERROR_SERVER_PREFIX + e.getMessage() + ERROR_SERVER_SUFFIX);
        } catch (IOException ioException) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}