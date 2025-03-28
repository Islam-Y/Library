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
    public void toDTO_WithFullData_ShouldMapCorrectly() {
        // Given
        Publisher publisher = new Publisher();
        publisher.setId(1);
        publisher.setName("Test Publisher");
        publisher.setBooks(Arrays.asList(createBook(10), createBook(20)));

        // When
        PublisherDTO dto = mapper.toDTO(publisher);

        // Then
        assertEquals(1, dto.getId());
        assertEquals("Test Publisher", dto.getName());
        assertEquals(2, dto.getBookIds().size());
        assertTrue(dto.getBookIds().contains(10));
        assertTrue(dto.getBookIds().contains(20));
    }

    @Test
    public void toDTO_WithNullBooks_ShouldReturnEmptyIds() {
        Publisher publisher = new Publisher();
        publisher.setBooks(null);

        PublisherDTO dto = mapper.toDTO(publisher);

        assertNotNull(dto.getBookIds());
        assertTrue(dto.getBookIds().isEmpty());
    }

    @Test
    public void toModel_WithFullData_ShouldMapCorrectly() {
        PublisherDTO dto = new PublisherDTO();
        dto.setId(2);
        dto.setName("DTO Publisher");
        dto.setBookIds(Arrays.asList(30, 40));

        Publisher publisher = mapper.toModel(dto);

        assertEquals(2, publisher.getId());
        assertEquals("DTO Publisher", publisher.getName());
        assertEquals(2, publisher.getBooks().size());
        assertTrue(publisher.getBooks().stream()
                .map(Book::getId)
                .allMatch(id -> id == 30 || id == 40));
    }

    @Test
    public void toModel_WithNullBookIds_ShouldHaveEmptyBooks() {
        PublisherDTO dto = new PublisherDTO();
        dto.setBookIds(null);

        Publisher publisher = mapper.toModel(dto);

        assertNotNull(publisher.getBooks());
        assertTrue(publisher.getBooks().isEmpty());
    }

    @Test
    public void mapBooksToBookIds_WithNullInput_ShouldReturnEmptyList() {
        List<Integer> result = PublisherMapper.mapBooksToBookIds(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void mapBookIdsToBooks_WithNullInput_ShouldReturnEmptyList() {
        List<Book> result = PublisherMapper.mapBookIdsToBooks(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    private Book createBook(int id) {
        Book book = new Book();
        book.setId(id);
        return book;
    }
}