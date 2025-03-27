package DTOTest;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.Arrays;
import java.util.List;

import com.library.dto.PublisherDTO;
import com.library.model.Publisher;
import com.library.model.Book;

public class PublisherDTOTest {

    @Test
    public void testDefaultConstructorAndSetters() {
        PublisherDTO publisherDTO = new PublisherDTO();
        publisherDTO.setId(15);
        publisherDTO.setName("Test Publisher");
        List<Integer> bookIds = Arrays.asList(1, 2, 3);
        publisherDTO.setBookIds(bookIds);

        assertEquals(15, publisherDTO.getId());
        assertEquals("Test Publisher", publisherDTO.getName());
        assertEquals(bookIds, publisherDTO.getBookIds());
    }

    @Test
    public void testParameterizedConstructor() {
        // Создаем модель Publisher с книгами
        Publisher publisher = new Publisher();
        publisher.setId(25);
        publisher.setName("Parameterized Publisher");

        Book book1 = new Book();
        book1.setId(50);
        Book book2 = new Book();
        book2.setId(51);
        // Допустим, что Publisher хранит книги в виде списка
        publisher.setBooks(Arrays.asList(book1, book2));

        // Создаем DTO через конструктор, принимающий Publisher
        PublisherDTO publisherDTO = new PublisherDTO(publisher);
        assertEquals(25, publisherDTO.getId());
        assertEquals("Parameterized Publisher", publisherDTO.getName());
        List<Integer> expectedBookIds = Arrays.asList(50, 51);
        assertEquals(expectedBookIds, publisherDTO.getBookIds());
    }
}
