package com.library.dto;

import com.library.model.Author;
import com.library.model.Book;

import java.util.Set;
import java.util.stream.Collectors;

public class AuthorDTO {
    private int id;
    private String name;
    private String surname;
    private String country;
    private Set<Integer> bookIds;

    public AuthorDTO(Author author) {
        this.id = author.getId();
        this.name = author.getName();
        this.surname = author.getName();
        this.country = author.getCountry();
        this.bookIds = author.getBooks().stream()
                .map(Book::getId)
                .collect(Collectors.toSet());
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getCountry() {
        return country;
    }

    public Set<Integer> getBookIds() {
        return bookIds;
    }
}
