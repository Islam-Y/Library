package ServiceTest;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.library.dto.PublisherDTO;
import com.library.exception.PublisherServiceException;
import com.library.mapper.PublisherMapper;
import com.library.model.Book;
import com.library.model.Publisher;
import com.library.repository.BookDAO;
import com.library.repository.PublisherDAO;
import com.library.service.PublisherService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class PublisherServiceTest {

    @Mock
    private PublisherDAO publisherDAO;

    @Mock
    private BookDAO bookDAO;

    @Mock
    private PublisherMapper publisherMapper;

    @InjectMocks
    private PublisherService publisherService;

    @Test
    void getAllPublishers_ShouldReturnList() throws SQLException {
        // Arrange
        Publisher publisher = createTestPublisher(1);
        PublisherDTO publisherDTO = new PublisherDTO(publisher);

        when(publisherDAO.getAll()).thenReturn(List.of(publisher));
        when(publisherMapper.toDTO(publisher)).thenReturn(publisherDTO); // Настройка маппера

        // Act
        List<PublisherDTO> result = publisherService.getAllPublishers();

        // Assert
        assertThat(result)
                .hasSize(1)
                .first()
                .extracting(PublisherDTO::getName)
                .isEqualTo("Эксмо");
        verify(publisherMapper).toDTO(publisher); // Проверка вызова маппера
    }

    @Test
    void getPublisherById_WhenExists_ShouldReturnDTO() throws SQLException {
        // Arrange
        Publisher publisher = createTestPublisher(1);
        PublisherDTO publisherDTO = new PublisherDTO(publisher);

        when(publisherDAO.getById(1)).thenReturn(Optional.of(publisher));
        when(publisherMapper.toDTO(publisher)).thenReturn(publisherDTO); // Настройка маппера

        // Act
        PublisherDTO result = publisherService.getPublisherById(1);

        // Assert
        assertThat(result)
                .extracting(
                        PublisherDTO::getName,
                        dto -> dto.getBookIds().size()
                )
                .containsExactly("Эксмо", 1);
        verify(publisherMapper).toDTO(publisher);
    }

    @Test
    void getPublisherById_WhenNotExists_ShouldThrow() throws SQLException {
        // Arrange
            when(publisherDAO.getById(anyInt())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> publisherService.getPublisherById(1))
                .isInstanceOf(PublisherServiceException.class)
                .hasMessageContaining("Издатель не найден");
    }

    @Test
    void addPublisher_ShouldMapAndSave() throws SQLException {
        // Arrange
        Publisher publisher = createTestPublisher(0);
        PublisherDTO publisherDTO = new PublisherDTO(publisher);

        when(publisherMapper.toModel(publisherDTO)).thenReturn(publisher); // Настройка преобразования DTO -> Model

        // Act
        publisherService.addPublisher(publisherDTO);

        // Assert
        verify(publisherMapper).toModel(publisherDTO);
        verify(publisherDAO).create(refEq(publisher, "id", "books"));
    }

    @Test
    void updatePublisher_WhenExists_ShouldUpdate() throws SQLException {
        // Arrange
        Publisher existing = createTestPublisher(1);
        Publisher updated = createTestPublisher(1);
        updated.setName("Новое название");
        PublisherDTO updatedDTO = new PublisherDTO(updated);

        when(publisherDAO.getById(1)).thenReturn(Optional.of(existing));

        // Act
        publisherService.updatePublisher(1, updatedDTO);

        // Assert
        verify(publisherDAO).update(argThat(p ->
                p.getName().equals("Новое название")
        ));
    }

    @Test
    void deletePublisher_ShouldCallDAO() throws SQLException {
        // Act
        publisherService.deletePublisher(1);

        // Assert
        verify(publisherDAO).delete(1);
    }

    private Publisher createTestPublisher(int id) {
        Publisher publisher = new Publisher();
        publisher.setId(id);
        publisher.setName("Эксмо");
        publisher.setBooks(List.of(new Book(1, "Книга", "2023-01-01", "Жанр", null, new HashSet<>())));
        return publisher;
    }
}