package ServiceTest;

import com.library.dto.PublisherDTO;
import com.library.exception.PublisherServiceException;
import com.library.mapper.PublisherMapper;
import com.library.model.Publisher;
import com.library.repository.PublisherDAO;
import com.library.service.PublisherService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PublisherServiceTest {

    @Mock
    private PublisherDAO publisherDAO;
    @Mock
    private PublisherMapper publisherMapper;

    private PublisherService publisherService;

    private Publisher testPublisher;
    private PublisherDTO testPublisherDTO;

    @Before
    public void setUp() {
        publisherService = PublisherService.forTest(publisherDAO, publisherMapper);

        testPublisher = new Publisher();
        testPublisher.setId(1);
        testPublisher.setName("Test Publisher");

        testPublisherDTO = new PublisherDTO();
        testPublisherDTO.setId(1);
        testPublisherDTO.setName("Test Publisher");

        when(publisherMapper.toDTO(any(Publisher.class))).thenReturn(testPublisherDTO);
    }

    @Test
    public void getAllPublishers_Success() throws SQLException {
        when(publisherDAO.getAll()).thenReturn(Collections.singletonList(testPublisher));

        List<PublisherDTO> result = publisherService.getAllPublishers();

        assertEquals(1, result.size());
        assertEquals("Test Publisher", result.get(0).getName());
    }

    @Test(expected = PublisherServiceException.class)
    public void getAllPublishers_Exception() throws SQLException {
        when(publisherDAO.getAll()).thenThrow(new SQLException("DB error"));
        publisherService.getAllPublishers();
    }

    @Test
    public void getPublisherById_Success() throws SQLException {
        when(publisherDAO.getById(1)).thenReturn(Optional.of(testPublisher));

        PublisherDTO result = publisherService.getPublisherById(1);

        assertEquals(1, result.getId());
        assertEquals("Test Publisher", result.getName());
    }

    @Test(expected = PublisherServiceException.class)
    public void getPublisherById_NotFound() throws SQLException {
        when(publisherDAO.getById(anyInt())).thenReturn(Optional.empty());
        publisherService.getPublisherById(1);
    }

    @Test
    public void addPublisher_Success() throws SQLException {
        PublisherDTO inputDTO = new PublisherDTO();
        inputDTO.setName("New Publisher");

        Publisher expectedPublisher = new Publisher();
        expectedPublisher.setName("New Publisher");

        when(publisherMapper.toModel(inputDTO)).thenReturn(expectedPublisher);

        publisherService.addPublisher(inputDTO);

        ArgumentCaptor<Publisher> captor = ArgumentCaptor.forClass(Publisher.class);
        verify(publisherDAO).create(captor.capture());

        Publisher savedPublisher = captor.getValue();
        assertEquals("New Publisher", savedPublisher.getName());
    }

    @Test(expected = PublisherServiceException.class)
    public void addPublisher_SQLException() throws SQLException {
        PublisherDTO inputDTO = new PublisherDTO();
        inputDTO.setName("New Publisher");

        Publisher expectedPublisher = new Publisher();
        expectedPublisher.setName("New Publisher");

        when(publisherMapper.toModel(inputDTO)).thenReturn(expectedPublisher);
        doThrow(new SQLException("DB error")).when(publisherDAO).create(expectedPublisher);

        publisherService.addPublisher(inputDTO);
    }

    @Test
    public void updatePublisher_Success() throws SQLException {
        Publisher existingPublisher = new Publisher();
        existingPublisher.setId(1);
        existingPublisher.setName("Old Publisher");

        when(publisherDAO.getById(1)).thenReturn(Optional.of(existingPublisher));

        PublisherDTO updateDTO = new PublisherDTO();
        updateDTO.setName("Updated Publisher");

        publisherService.updatePublisher(1, updateDTO);

        verify(publisherDAO).update(existingPublisher);
        assertEquals("Updated Publisher", existingPublisher.getName());
    }

    @Test(expected = PublisherServiceException.class)
    public void updatePublisher_NotFound() throws SQLException {
        when(publisherDAO.getById(1)).thenReturn(Optional.empty());
        publisherService.updatePublisher(1, new PublisherDTO());
    }

    @Test(expected = PublisherServiceException.class)
    public void updatePublisher_SQLException() throws SQLException {
        Publisher existingPublisher = new Publisher();
        existingPublisher.setId(1);
        existingPublisher.setName("Old Publisher");

        when(publisherDAO.getById(1)).thenReturn(Optional.of(existingPublisher));
        doThrow(new SQLException()).when(publisherDAO).update(existingPublisher);

        PublisherDTO updateDTO = new PublisherDTO();
        updateDTO.setName("Updated Publisher");

        publisherService.updatePublisher(1, updateDTO);
    }

    @Test
    public void deletePublisher_Success() throws SQLException {
        publisherService.deletePublisher(1);
        verify(publisherDAO).delete(1);
    }

    @Test(expected = PublisherServiceException.class)
    public void deletePublisher_SQLException() throws SQLException {
        doThrow(new SQLException()).when(publisherDAO).delete(1);
        publisherService.deletePublisher(1);
    }
}

