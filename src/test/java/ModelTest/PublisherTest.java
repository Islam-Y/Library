package ModelTest;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

import com.library.model.Publisher;
import com.library.model.Book;

public class PublisherTest {

    @Test
    public void testGettersAndSetters() {
        Publisher publisher = new Publisher();
        publisher.setId(5);
        publisher.setName("Penguin");

        List<Book> books = new ArrayList<>();
        publisher.setBooks(books);

        assertEquals(5, publisher.getId());
        assertEquals("Penguin", publisher.getName());
        assertEquals(books, publisher.getBooks());
    }

    @Test
    public void testAddAndRemoveBook() {
        Publisher publisher = new Publisher();
        publisher.setId(5);

        Book book = new Book();
        book.setId(10);

        // До добавления список книг должен быть пустым
        assertTrue(publisher.getBooks().isEmpty());

        // Добавляем книгу – проверяем, что двусторонняя связь устанавливается (publisher в book и книга в списке)
        publisher.addBook(book);
        assertTrue(publisher.getBooks().contains(book));
        assertEquals(publisher, book.getPublisher());

        // Удаляем книгу
        publisher.removeBook(book);
        assertFalse(publisher.getBooks().contains(book));
        assertNull(book.getPublisher());
    }

    @Test
    public void testEqualsAndHashCode() {
        Publisher publisher1 = new Publisher();
        publisher1.setId(5);
        publisher1.setName("Penguin");

        Publisher publisher2 = new Publisher();
        publisher2.setId(5);
        publisher2.setName("Penguin");

        assertEquals(publisher1, publisher2);
        assertEquals(publisher1.hashCode(), publisher2.hashCode());

        publisher2.setName("Random House");
        assertNotEquals(publisher1, publisher2);
    }

    @Test
    public void testToString() {
        Publisher publisher = new Publisher(5, "Penguin", new ArrayList<>());
        String str = publisher.toString();
        assertTrue(str.contains("Penguin"));
        assertTrue(str.contains("5"));
    }
}
