package ModelTest;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.HashSet;
import java.util.Set;

import com.library.model.Author;
import com.library.model.Book;

public class AuthorTest {

    @Test
    public void testGettersAndSetters() {
        Author author = new Author();
        author.setId(1);
        author.setName("Leo");
        author.setSurname("Tolstoy");
        author.setCountry("Russia");
        Set<Book> books = new HashSet<>();
        author.setBooks(books);

        assertEquals(1, author.getId());
        assertEquals("Leo", author.getName());
        assertEquals("Tolstoy", author.getSurname());
        assertEquals("Russia", author.getCountry());
        assertEquals(books, author.getBooks());
    }

    @Test
    public void testAddAndRemoveBook() {
        Author author = new Author();
        author.setId(1);
        Book book = new Book();
        book.setId(10);

        // До добавления множество книг должно быть пустым.
        assertTrue(author.getBooks().isEmpty());
        // Добавляем книгу
        author.addBook(book);
        // Проверяем, что книга добавилась и устанавливается двусторонняя связь:
        assertTrue(author.getBooks().contains(book));
        assertTrue(book.getAuthors().contains(author));

        // Удаляем книгу
        author.removeBook(book);
        assertFalse(author.getBooks().contains(book));
        assertFalse(book.getAuthors().contains(author));
    }

    @Test
    public void testEqualsAndHashCode() {
        Author author1 = new Author();
        author1.setId(1);
        author1.setName("Leo");
        author1.setSurname("Tolstoy");
        author1.setCountry("Russia");

        Author author2 = new Author();
        author2.setId(1);
        author2.setName("Leo");
        author2.setSurname("Tolstoy");
        author2.setCountry("Russia");

        // Объекты с одинаковыми полями должны быть равны
        assertEquals(author1, author2);
        assertEquals(author1.hashCode(), author2.hashCode());

        // Изменив одно из полей, равенство должно нарушиться
        author2.setName("Different");
        assertNotEquals(author1, author2);
    }

    @Test
    public void testToString() {
        Author author = new Author(1, "Leo", "Tolstoy", "Russia", new HashSet<>());
        String str = author.toString();
        assertTrue(str.contains("Leo"));
        assertTrue(str.contains("Tolstoy"));
        assertTrue(str.contains("Russia"));
    }
}
