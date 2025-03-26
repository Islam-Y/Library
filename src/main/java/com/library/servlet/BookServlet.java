package com.library.servlet;

import com.library.dto.BookDTO;
import com.library.mapper.BookMapper;
import com.library.repository.BookDAO;
import com.library.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.service.Fabric;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

@WebServlet("/books/*")
public class BookServlet extends HttpServlet {
    private static final String ERROR_INVALID_ID_FORMAT = "{\"error\":\"Invalid book ID format\"}";
    private static final String ERROR_SERVER_PREFIX = "{\"error\":\"Server error: ";
    private static final String ERROR_SERVER_SUFFIX = "\"}";
    private static final String ERROR_ID_MISMATCH = "{\"error\":\"ID in path and body mismatch\"}";
    private static final String ERROR_INVALID_REQUEST = "{\"error\":\"Invalid request: ";

    private BookService bookService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void init() throws ServletException {
        super.init();
        this.bookService = Fabric.getBookService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String pathInfo = req.getPathInfo();
        resp.setContentType("application/json");

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                List<BookDTO> books = bookService.getAllBooks();
                objectMapper.writeValue(resp.getWriter(), books);
            } else {
                String[] parts = pathInfo.split("/");
                if (parts.length != 2) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }

                int bookId = Integer.parseInt(parts[1]);
                BookDTO book = bookService.getBookById(bookId);
                if (book != null) {
                    objectMapper.writeValue(resp.getWriter(), book);
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
            BookDTO book = objectMapper.readValue(req.getReader(), BookDTO.class);
            // Убедитесь, что коллекции инициализированы
            if (book.getAuthorIds() == null) {
                book.setAuthorIds(new HashSet<>());
            }
            bookService.addBook(book);
            resp.setStatus(HttpServletResponse.SC_CREATED);
        } catch (Exception e) {
            handleError(resp, HttpServletResponse.SC_BAD_REQUEST, ERROR_INVALID_REQUEST + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
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

            int bookId = Integer.parseInt(parts[1]);
            BookDTO book = objectMapper.readValue(req.getReader(), BookDTO.class);

            if (book.getId() != bookId) {
                handleError(resp, HttpServletResponse.SC_BAD_REQUEST, ERROR_ID_MISMATCH);
                return;
            }

            bookService.updateBook(bookId, book);
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

            int bookId = Integer.parseInt(parts[1]);
            bookService.deleteBook(bookId);
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