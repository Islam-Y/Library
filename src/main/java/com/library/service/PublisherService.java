package com.library.service;

import com.library.exception.PublisherServiceException;
import com.library.model.Book;
import com.library.repository.PublisherDAO;
import com.library.dto.PublisherDTO;
import com.library.model.Publisher;
import com.library.mapper.PublisherMapper;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class PublisherService {
    private final PublisherDAO publisherDAO;
    private final PublisherMapper publisherMapper;

    PublisherService() {
        this.publisherDAO = new PublisherDAO();
        this.publisherMapper = PublisherMapper.INSTANCE;
    }

    private PublisherService(PublisherDAO publisherDAO, PublisherMapper mapper) {
        this.publisherDAO = publisherDAO;
        this.publisherMapper = mapper;
    }

    public static PublisherService forTest(PublisherDAO publisherDAO, PublisherMapper publisherMapper) {
        return new PublisherService(publisherDAO, publisherMapper);
    }

    public List<PublisherDTO> getAllPublishers() {
        try {
            return publisherDAO.getAll().stream()
                    .map(publisherMapper::toDTO)
                    .toList();
        } catch (SQLException e) {
            throw new PublisherServiceException("Error while getting list of publishers", e);
        }
    }

    public PublisherDTO getPublisherById(int id) {
        try {
            return publisherDAO.getById(id)
                    .map(publisherMapper::toDTO)
                    .orElseThrow(() -> new PublisherServiceException("Publisher not found", new RuntimeException()));
        } catch (SQLException e) {
            throw new PublisherServiceException("Error while getting publisher with ID " + id, e);
        }
    }

    public void addPublisher(PublisherDTO publisherDTO) {
        Publisher publisher = publisherMapper.toModel(publisherDTO);
        if (publisherDTO.getName() == null || publisherDTO.getName().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        try {
            publisherDAO.create(publisher);
            publisherDAO.updatePublisherBooks(publisher.getId(), publisherDTO.getBookIds());

        } catch (SQLException e) {
            throw new PublisherServiceException("Error while adding publisher to database", e);
        }
    }

    public void updatePublisher(int id, PublisherDTO publisherDTO) {
        try {
            Publisher existingPublisher = publisherDAO.getById(id)
                    .orElseThrow(() -> new PublisherServiceException("Publisher not found", new RuntimeException()));
            existingPublisher.setName(publisherDTO.getName());

            List<Integer> bookIds = publisherDTO.getBookIds() != null
                    ? publisherDTO.getBookIds()
                    : Collections.emptyList();

            List<Book> books = bookIds.stream()
                    .map(bookId -> {
                        Book book = new Book();
                        book.setId(bookId);
                        return book;
                    })
                    .toList();
            existingPublisher.setBooks(books);

            publisherDAO.update(existingPublisher);
            publisherDAO.updatePublisherBooks(id, publisherDTO.getBookIds());
        } catch (SQLException e) {
            throw new PublisherServiceException("Error while updating publisher with ID " + id, e);
        }
    }

    public void deletePublisher(int id) {
        try {
            publisherDAO.delete(id);
        } catch (SQLException e) {
            throw new PublisherServiceException("Error while deleting publisher with ID " + id, e);
        }
    }
}