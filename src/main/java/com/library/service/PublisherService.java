package com.library.service;

import com.library.exception.PublisherServiceException;
import com.library.repository.PublisherDAO;
import com.library.dto.PublisherDTO;
import com.library.model.Publisher;
import com.library.mapper.PublisherMapper;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class PublisherService {
    private final PublisherDAO publisherDAO;
    private final PublisherMapper publisherMapper;

    public PublisherService(PublisherDAO publisherDAO, PublisherMapper publisherMapper) {
        this.publisherDAO = publisherDAO;
        this.publisherMapper = publisherMapper;
    }

    public List<PublisherDTO> getAllPublishers() {
        try {
            return publisherDAO.getAll().stream()
                    .map(publisherMapper::toDTO)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new PublisherServiceException("Ошибка при получении списка издателей", e);
        }
    }

    public PublisherDTO getPublisherById(int id) {
        try {
            return publisherDAO.getById(id)
                    .map(publisherMapper::toDTO)
                    .orElseThrow(() -> new RuntimeException("Издатель не найден"));
        } catch (SQLException e) {
            throw new PublisherServiceException("Ошибка при получении издателя с ID " + id, e);
        }
    }

    public void addPublisher(PublisherDTO publisherDTO) {
        Publisher publisher = publisherMapper.toModel(publisherDTO);
        try {
            publisherDAO.create(publisher);
        } catch (SQLException e) {
            throw new PublisherServiceException("Ошибка при добавлении издателя", e);
        }
    }

    public void updatePublisher(int id, PublisherDTO publisherDTO) {
        Publisher existingPublisher = null;
        try {
            existingPublisher = publisherDAO.getById(id)
                    .orElseThrow(() -> new RuntimeException("Издатель не найден"));
            existingPublisher.setName(publisherDTO.getName());

            publisherDAO.update(existingPublisher);
        } catch (SQLException e) {
            throw new PublisherServiceException("Ошибка при обновлении издателя с ID " + id, e);
        }
    }

    public void deletePublisher(int id) {
        try {
            publisherDAO.delete(id);
        } catch (SQLException e) {
            throw new PublisherServiceException("Ошибка при удалении издателя с ID " + id, e);
        }
    }
}