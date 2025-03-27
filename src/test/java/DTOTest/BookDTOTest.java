package DTOTest;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.HashSet;
import java.util.Set;

import com.library.dto.BookDTO;
import com.library.model.Book;
import com.library.model.Author;
import com.library.model.Publisher;

public class BookDTOTest {

    @Test
    public void testDefaultConstructorAndSetters() {
        BookDTO bookDTO = new BookDTO();
        bookDTO.setId(10);
        bookDTO.setTitle("Book Title");
        bookDTO.setPublishedDate("2023-01-01");
        bookDTO.setGenre("Fiction");
        bookDTO.setPublisherId(5);
        Set<Integer> authorIds = new HashSet<>();
        authorIds.add(1);
        authorIds.add(2);
        bookDTO.setAuthorIds(authorIds);

        assertEquals(10, bookDTO.getId());
        assertEquals("Book Title", bookDTO.getTitle());
        assertEquals("2023-01-01", bookDTO.getPublishedDate());
        assertEquals("Fiction", bookDTO.getGenre());
        assertEquals((Integer) 5, bookDTO.getPublisherId());
        assertEquals(authorIds, bookDTO.getAuthorIds());
    }

    @Test
    public void testParameterizedConstructor() {
        // Создаем модель Book
        Book book = new Book();
        book.setId(20);
        book.setTitle("Parameterized Book");
        book.setPublishedDate("2024-05-05");
        book.setGenre("Mystery");

        // Задаем издателя
        Publisher publisher = new Publisher();
        publisher.setId(3);
        book.setPublisher(publisher);

        // Задаем авторов
        Author author1 = new Author();
        author1.setId(100);
        Author author2 = new Author();
        author2.setId(101);
        Set<Author> authors = new HashSet<>();
        authors.add(author1);
        authors.add(author2);
        book.setAuthors(authors);

        // Создаем DTO через конструктор, принимающий Book
        BookDTO bookDTO = new BookDTO(book);
        assertEquals(20, bookDTO.getId());
        assertEquals("Parameterized Book", bookDTO.getTitle());
        assertEquals("2024-05-05", bookDTO.getPublishedDate());
        assertEquals("Mystery", bookDTO.getGenre());
        assertEquals((Integer) 3, bookDTO.getPublisherId());

        Set<Integer> expectedAuthorIds = new HashSet<>();
        expectedAuthorIds.add(100);
        expectedAuthorIds.add(101);
        assertEquals(expectedAuthorIds, bookDTO.getAuthorIds());
    }
}
