package com.library.service;

public class Fabric {
    private static AuthorService authorService;
    private static BookService bookService;
    private static PublisherService publisherService;

    private Fabric() {
    }

    public static AuthorService getAuthorService() {
        if (authorService == null) {
            authorService = new AuthorService();
        }
        return authorService;
    }

    public static BookService getBookService() {
        if (bookService == null) {
            bookService = new BookService();
        }
        return bookService;
    }

    public static PublisherService getPublisherService() {
        if (publisherService == null) {
            publisherService = new PublisherService();
        }
        return publisherService;
    }
}
