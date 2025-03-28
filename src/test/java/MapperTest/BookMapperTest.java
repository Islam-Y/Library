package MapperTest;

import static org.junit.Assert.*;

import com.library.dto.BookDTO;
import com.library.mapper.BookMapper;
import com.library.model.Author;
import com.library.model.Book;
import com.library.model.Publisher;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

public class BookMapperTest {
    private final BookMapper mapper = BookMapper.INSTANCE;

    @Test
   public void toDTO_WithFullData_ShouldMapCorrectly() {
        Book book = new Book();
        book.setId(1);
        book.setTitle("Test Book");

        Publisher publisher = new Publisher();
        publisher.setId(100);
        book.setPublisher(publisher);

        book.setAuthors(Set.of(createAuthor(10), createAuthor(20)));

        BookDTO dto = mapper.toDTO(book);

        assertEquals(1, dto.getId());
        assertEquals("Test Book", dto.getTitle());
        assertEquals(Integer.valueOf(100), dto.getPublisherId());
        assertTrue(dto.getAuthorIds().containsAll(Set.of(10, 20)));
    }

    @Test
    public void toDTO_WithNullCollections_ShouldReturnEmptyIds() {
        Book book = new Book();
        book.setAuthors(null);
        book.setPublisher(null);

        BookDTO dto = mapper.toDTO(book);

        assertNull(dto.getPublisherId());
        assertTrue(dto.getAuthorIds().isEmpty());
    }

    @Test
    public void toModel_WithFullData_ShouldMapCorrectly() {
        BookDTO dto = new BookDTO();
        dto.setId(2);
        dto.setTitle("DTO Book");
        dto.setPublisherId(200);
        dto.setAuthorIds(Set.of(30, 40));

        Book book = mapper.toModel(dto);

        assertEquals(2, book.getId());
        assertEquals("DTO Book", book.getTitle());
        assertEquals(200, book.getPublisher().getId());
        assertEquals(2, book.getAuthors().size());
        assertTrue(book.getAuthors().stream()
                .map(Author::getId)
                .collect(Collectors.toSet())
                .containsAll(Set.of(30, 40)));
    }

    @Test
    public void toModel_WithNullCollections_ShouldHaveEmptyRelations() {
        BookDTO dto = new BookDTO();
        dto.setAuthorIds(null);
        dto.setPublisherId(null);

        Book book = mapper.toModel(dto);

        assertNull(book.getPublisher());
        assertTrue(book.getAuthors().isEmpty());
    }

    @Test
    public void mapAuthorIdsToAuthors_WithNullInput_ShouldReturnEmptySet() {
        Set<Author> result = BookMapper.mapAuthorIdsToAuthors(null);
        assertTrue(result.isEmpty());
    }

    @Test
    public void mapAuthorsToAuthorIds_WithNullInput_ShouldReturnEmptySet() {
        Set<Integer> result = BookMapper.mapAuthorsToAuthorIds(null);
        assertTrue(result.isEmpty());
    }

    private Author createAuthor(int id) {
        Author author = new Author();
        author.setId(id);
        return author;
    }
}