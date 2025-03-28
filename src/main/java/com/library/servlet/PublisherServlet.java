package com.library.servlet;

import com.library.dto.PublisherDTO;
import com.library.service.Fabric;
import com.library.service.PublisherService;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/publishers/*")
public class PublisherServlet extends HttpServlet {
    private static final String ERROR_INVALID_ID_FORMAT = "{\"error\":\"Invalid publisher ID format\"}";
    private static final String ERROR_SERVER_PREFIX = "{\"error\":\"Server error: ";
    private static final String ERROR_SERVER_SUFFIX = "\"}";
    private static final String ERROR_ID_MISMATCH = "{\"error\":\"ID in path and body mismatch\"}";
    private static final String ERROR_INVALID_REQUEST = "{\"error\":\"Invalid request: ";

    private PublisherService publisherService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void init() throws ServletException {
        super.init();
        this.publisherService = Fabric.getPublisherService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String pathInfo = req.getPathInfo();
        resp.setContentType("application/json");

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                List<PublisherDTO> publishers = publisherService.getAllPublishers();
                objectMapper.writeValue(resp.getWriter(), publishers);
            } else {
                String[] parts = pathInfo.split("/");
                if (parts.length != 2) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }

                int publisherId = Integer.parseInt(parts[1]);
                PublisherDTO publisher = publisherService.getPublisherById(publisherId);
                if (publisher != null) {
                    objectMapper.writeValue(resp.getWriter(), publisher);
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
            PublisherDTO publisher = objectMapper.readValue(req.getReader(), PublisherDTO.class);
            publisherService.addPublisher(publisher);
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

            int publisherId = Integer.parseInt(parts[1]);
            PublisherDTO publisher = objectMapper.readValue(req.getReader(), PublisherDTO.class);

            if (publisher.getId() != publisherId) {
                handleError(resp, HttpServletResponse.SC_BAD_REQUEST, ERROR_ID_MISMATCH);
                return;
            }

            publisherService.updatePublisher(publisherId, publisher);
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

            int publisherId = Integer.parseInt(parts[1]);
            publisherService.deletePublisher(publisherId);
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