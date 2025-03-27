package DTOTest;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.library.dto.AuthorDTO;
import com.library.model.Author;
import com.library.model.Book;

public class AuthorDTOTest {

    @Test
    public void testDefaultConstructorAndSetters() {
        AuthorDTO authorDTO = new AuthorDTO();
        authorDTO.setId(1);
        authorDTO.setName("Ivan");
        authorDTO.setSurname("Ivanov");
        authorDTO.setCountry("Russia");
        Set<Integer> bookIds = new HashSet<>(Arrays.asList(10, 20));
        authorDTO.setBookIds(bookIds);

        assertEquals(1, authorDTO.getId());
        assertEquals("Ivan", authorDTO.getName());
        assertEquals("Ivanov", authorDTO.getSurname());
        assertEquals("Russia", authorDTO.getCountry());
        assertEquals(bookIds, authorDTO.getBookIds());
    }

    @Test
    public void testParameterizedConstructor() {
        // Подготавливаем модель Author с набором книг
        Author author = new Author();
        author.setId(2);
        author.setName("Dostoevsky");
        author.setSurname("Fyodor");
        author.setCountry("Russia");

        Book book1 = new Book();
        book1.setId(100);
        Book book2 = new Book();
        book2.setId(200);
        Set<Book> books = new HashSet<>();
        books.add(book1);
        books.add(book2);
        author.setBooks(books);

        // Создаем DTO через конструктор, принимающий Author
        AuthorDTO authorDTO = new AuthorDTO(author);
        assertEquals(2, authorDTO.getId());
        assertEquals("Dostoevsky", authorDTO.getName());
        assertEquals("Fyodor", authorDTO.getSurname());
        assertEquals("Russia", authorDTO.getCountry());
        Set<Integer> expectedBookIds = new HashSet<>(Arrays.asList(100, 200));
        assertEquals(expectedBookIds, authorDTO.getBookIds());
    }
}
