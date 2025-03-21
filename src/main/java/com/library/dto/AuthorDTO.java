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

    public AuthorDTO() {}

    public AuthorDTO(Author author) {
        this.id = author.getId();
        this.name = author.getName();
        this.surname = author.getSurname();
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

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setBookIds(Set<Integer> bookIds) {
        this.bookIds = bookIds;
    }
}
