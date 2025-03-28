package MapperTest;

import static org.junit.Assert.*;

import com.library.dto.AuthorDTO;
import com.library.mapper.AuthorMapper;
import com.library.model.Author;
import com.library.model.Book;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

public class AuthorMapperTest {
    private final AuthorMapper mapper = AuthorMapper.INSTANCE;

    @Test
    public void toDTO_WithBooks_ShouldMapToBookIds() {
        // Given
        Author author = new Author();
        author.setId(1);
        author.setName("Test Author");

        Set<Book> books = new HashSet<>();
        books.add(createBook(10));
        books.add(createBook(20));
        author.setBooks(books);

        // When
        AuthorDTO dto = mapper.toDTO(author);

        // Then
        assertEquals(1, dto.getId());
        assertEquals("Test Author", dto.getName());
        assertEquals(2, dto.getBookIds().size());
        assertTrue(dto.getBookIds().containsAll(Set.of(10, 20)));
    }

    @Test
    public void toDTO_WithNullBooks_ShouldReturnEmptyIds() {
        Author author = new Author();
        author.setId(2);
        author.setBooks(null);

        AuthorDTO dto = mapper.toDTO(author);

        assertTrue(dto.getBookIds().isEmpty());
    }

    @Test
    public void toModel_WithBookIds_ShouldMapToBooksWithIds() {
        AuthorDTO dto = new AuthorDTO();
        dto.setId(3);
        dto.setName("DTO Author");
        dto.setBookIds(Set.of(30, 40));

        Author author = mapper.toModel(dto);

        assertEquals(3, author.getId());
        assertEquals("DTO Author", author.getName());
        assertEquals(2, author.getBooks().size());
        assertTrue(author.getBooks().stream()
                .map(Book::getId)
                .collect(Collectors.toSet())
                .containsAll(Set.of(30, 40)));
    }

    @Test
    public void toModel_WithNullIds_ShouldHaveEmptyBooks() {
        AuthorDTO dto = new AuthorDTO();
        dto.setBookIds(null);

        Author author = mapper.toModel(dto);

        assertTrue(author.getBooks().isEmpty());
    }

    private Book createBook(int id) {
        Book book = new Book();
        book.setId(id);
        return book;
    }
}