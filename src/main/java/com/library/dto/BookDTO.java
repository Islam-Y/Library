package com.library.dto;

import com.library.model.Book;
import com.library.model.Author;

import java.util.Set;
import java.util.stream.Collectors;

public class BookDTO {
    private int id;
    private String title;
    private String publishedDate;
    private String genre;
    private Integer publisherId;
    private Set<Integer> authorIds;

    public BookDTO(Book book) {
        this.id = book.getId();
        this.title = book.getTitle();
        this.publishedDate = book.getPublishedDate();
        this.genre = book.getGenre();
        this.publisherId = book.getPublisher() != null ? book.getPublisher().getId() : null;
        this.authorIds = book.getAuthors().stream()
                .map(Author::getId)
                .collect(Collectors.toSet());
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public String getGenre() {
        return genre;
    }

    public Integer getPublisherId() {
        return publisherId;
    }

    public Set<Integer> getAuthorIds() {
        return authorIds;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setPublisherId(Integer publisherId) {
        this.publisherId = publisherId;
    }

    public void setAuthorIds(Set<Integer> authorIds) {
        this.authorIds = authorIds;
    }
}
