package MapperTest;

import static org.junit.Assert.*;

import com.library.dto.BookDTO;
import com.library.mapper.BookMapper;
import com.library.model.Author;
import com.library.model.Book;
import com.library.model.Publisher;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

public class BookMapperTest {

    private final BookMapper mapper = BookMapper.INSTANCE;

    @Test
    public void testToDTO() {
        // Подготавливаем модель Book с издателем и авторами
        Book book = new Book();
        book.setId(1);
        book.setTitle("Test Book");
        book.setPublishedDate("2025-03-27");
        book.setGenre("Fiction");

        Publisher publisher = new Publisher();
        publisher.setId(10);
        book.setPublisher(publisher);

        Author author1 = new Author();
        author1.setId(100);
        Author author2 = new Author();
        author2.setId(101);
        Set<Author> authors = new HashSet<>(Arrays.asList(author1, author2));
        book.setAuthors(authors);

        // Преобразуем модель в DTO
        BookDTO dto = mapper.toDTO(book);

        // Проверяем основные поля
        assertEquals(book.getId(), dto.getId());
        assertEquals(book.getTitle(), dto.getTitle());
        assertEquals(book.getPublishedDate(), dto.getPublishedDate());
        assertEquals(book.getGenre(), dto.getGenre());
        // Проверяем, что id издателя корректно сконвертировался
        assertEquals((Integer) publisher.getId(), dto.getPublisherId());
        // Проверяем преобразование списка авторов в набор идентификаторов
        Set<Integer> expectedAuthorIds = new HashSet<>(Arrays.asList(100, 101));
        assertEquals(expectedAuthorIds, dto.getAuthorIds());
    }

    @Test
    public void testToModel() {
        // Создаем DTO с заполненными полями
        BookDTO dto = new BookDTO();
        dto.setId(2);
        dto.setTitle("Another Book");
        dto.setPublishedDate("2025-04-01");
        dto.setGenre("Mystery");
        dto.setPublisherId(20);
        Set<Integer> authorIds = new HashSet<>(Arrays.asList(200, 201));
        dto.setAuthorIds(authorIds);

        // Преобразование DTO в модель
        Book book = mapper.toModel(dto);

        // Проверяем основные поля
        assertEquals(dto.getId(), book.getId());
        assertEquals(dto.getTitle(), book.getTitle());
        assertEquals(dto.getPublishedDate(), book.getPublishedDate());
        assertEquals(dto.getGenre(), book.getGenre());
        // Из-за настройки маппера, поле publisher создается с id, а остальные поля остаются null
        assertNotNull(book.getPublisher());
        assertEquals((Integer) dto.getPublisherId(), (Integer) book.getPublisher().getId());
        // При обратном преобразовании поле authors игнорируется – ожидаем, что коллекция пуста
        assertTrue(book.getAuthors().isEmpty());
    }
}
