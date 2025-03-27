package ServiceTest;

import com.library.service.AuthorService;
import com.library.service.BookService;
import com.library.service.Fabric;
import com.library.service.PublisherService;
import org.junit.Test;

import static org.junit.Assert.*;

public class FabricTest {

    @Test
    public void testGetAuthorServiceSingleton() {
        AuthorService service1 = Fabric.getAuthorService();
        AuthorService service2 = Fabric.getAuthorService();
        assertNotNull("AuthorService не должен быть null", service1);
        assertSame("Должны возвращаться один и тот же экземпляр AuthorService", service1, service2);
    }

    @Test
    public void testGetBookServiceSingleton() {
        BookService service1 = Fabric.getBookService();
        BookService service2 = Fabric.getBookService();
        assertNotNull("BookService не должен быть null", service1);
        assertSame("Должны возвращаться один и тот же экземпляр BookService", service1, service2);
    }

    @Test
    public void testGetPublisherServiceSingleton() {
        PublisherService service1 = Fabric.getPublisherService();
        PublisherService service2 = Fabric.getPublisherService();
        assertNotNull("PublisherService не должен быть null", service1);
        assertSame("Должны возвращаться один и тот же экземпляр PublisherService", service1, service2);
    }
}
