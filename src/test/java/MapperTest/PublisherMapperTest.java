package MapperTest;

import static org.junit.Assert.*;

import com.library.dto.PublisherDTO;
import com.library.mapper.PublisherMapper;
import com.library.model.Book;
import com.library.model.Publisher;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class PublisherMapperTest {

    private final PublisherMapper mapper = PublisherMapper.INSTANCE;

    @Test
    public void testToDTO() {
        // Подготавливаем модель Publisher с книгами
        Publisher publisher = new Publisher();
        publisher.setId(5);
        publisher.setName("Test Publisher");

        Book book1 = new Book();
        book1.setId(50);
        Book book2 = new Book();
        book2.setId(51);
        // Предполагается, что Publisher хранит книги в виде списка
        publisher.setBooks(Arrays.asList(book1, book2));

        // Преобразуем модель в DTO
        PublisherDTO dto = mapper.toDTO(publisher);

        // Проверяем поля
        assertEquals(publisher.getId(), dto.getId());
        assertEquals(publisher.getName(), dto.getName());
        // Проверяем, что список идентификаторов книг корректно сформирован
        List<Integer> expectedBookIds = Arrays.asList(50, 51);
        assertEquals(expectedBookIds, dto.getBookIds());
    }

    @Test
    public void testToModel() {
        // Создаем DTO с заполненными полями
        PublisherDTO dto = new PublisherDTO();
        dto.setId(7);
        dto.setName("Another Publisher");
        dto.setBookIds(Arrays.asList(70, 71));

        // Преобразование DTO в модель
        Publisher publisher = mapper.toModel(dto);

        // Проверяем поля
        assertEquals(dto.getId(), publisher.getId());
        assertEquals(dto.getName(), publisher.getName());
        // При обратном маппинге поле books игнорируется
        assertTrue(publisher.getBooks().isEmpty());
    }
}
