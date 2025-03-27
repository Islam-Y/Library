package MapperTest;

import static org.junit.Assert.*;

import com.library.dto.AuthorDTO;
import com.library.mapper.AuthorMapper;
import com.library.model.Author;
import com.library.model.Book;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

public class AuthorMapperTest {

    private final AuthorMapper mapper = AuthorMapper.INSTANCE;

    @Test
    public void testToDTO() {
        // Подготовка модели Author с книгами
        Author author = new Author();
        author.setId(1);
        author.setName("John");
        author.setSurname("Doe");
        author.setCountry("USA");

        Book book1 = new Book();
        book1.setId(100);
        Book book2 = new Book();
        book2.setId(101);
        Set<Book> books = new HashSet<>(Arrays.asList(book1, book2));
        author.setBooks(books);

        // Преобразование в DTO
        AuthorDTO dto = mapper.toDTO(author);

        // Проверка полей
        assertEquals(author.getId(), dto.getId());
        assertEquals(author.getName(), dto.getName());
        assertEquals(author.getSurname(), dto.getSurname());
        assertEquals(author.getCountry(), dto.getCountry());
        // Проверяем, что идентификаторы книг корректно сконвертировались
        Set<Integer> expectedBookIds = new HashSet<>(Arrays.asList(100, 101));
        assertEquals(expectedBookIds, dto.getBookIds());
    }

    @Test
    public void testToModel() {
        // Создаем DTO с заполненными полями
        AuthorDTO dto = new AuthorDTO();
        dto.setId(2);
        dto.setName("Jane");
        dto.setSurname("Smith");
        dto.setCountry("UK");
        dto.setBookIds(new HashSet<>(Arrays.asList(200, 201)));

        // Преобразование DTO в модель
        Author author = mapper.toModel(dto);

        // Проверяем, что основные поля скопированы
        assertEquals(dto.getId(), author.getId());
        assertEquals(dto.getName(), author.getName());
        assertEquals(dto.getSurname(), author.getSurname());
        assertEquals(dto.getCountry(), author.getCountry());
        // При обратном маппинге поле books игнорируется, поэтому ожидаем пустую коллекцию
        assertTrue(author.getBooks().isEmpty());
    }
}
