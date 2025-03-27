package ModelTest;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.HashSet;
import java.util.Set;

import com.library.model.Book;
import com.library.model.Author;
import com.library.model.Publisher;

public class BookTest {

    @Test
    public void testGettersAndSetters() {
        Book book = new Book();
        book.setId(10);
        book.setTitle("War and Peace");
        book.setPublishedDate("1869");
        book.setGenre("Novel");

        Publisher publisher = new Publisher();
        publisher.setId(5);
        book.setPublisher(publisher);

        Set<Author> authors = new HashSet<>();
        book.setAuthors(authors);

        assertEquals(10, book.getId());
        assertEquals("War and Peace", book.getTitle());
        assertEquals("1869", book.getPublishedDate());
        assertEquals("Novel", book.getGenre());
        assertEquals(publisher, book.getPublisher());
        assertEquals(authors, book.getAuthors());
    }

    @Test
    public void testAddAndRemoveAuthor() {
        Book book = new Book();
        book.setId(10);

        Author author = new Author();
        author.setId(1);

        // До добавления список авторов должен быть пустым
        assertTrue(book.getAuthors().isEmpty());

        // Добавляем автора – проверяем двустороннюю связь
        book.addAuthor(author);
        assertTrue(book.getAuthors().contains(author));
        assertTrue(author.getBooks().contains(book));

        // Удаляем автора
        book.removeAuthor(author);
        assertFalse(book.getAuthors().contains(author));
        assertFalse(author.getBooks().contains(book));
    }

    @Test
    public void testEqualsAndHashCode() {
        Book book1 = new Book();
        book1.setId(10);
        book1.setTitle("War and Peace");
        book1.setPublishedDate("1869");
        book1.setGenre("Novel");

        Book book2 = new Book();
        book2.setId(10);
        book2.setTitle("War and Peace");
        book2.setPublishedDate("1869");
        book2.setGenre("Novel");

        assertEquals(book1, book2);
        assertEquals(book1.hashCode(), book2.hashCode());

        book2.setTitle("Anna Karenina");
        assertNotEquals(book1, book2);
    }

    @Test
    public void testToString() {
        Book book = new Book(10, "War and Peace", "1869", "Novel", null, new HashSet<>());
        String str = book.toString();
        assertTrue(str.contains("War and Peace"));
        assertTrue(str.contains("1869"));
        assertTrue(str.contains("Novel"));
    }
}
