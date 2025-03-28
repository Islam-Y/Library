package com.library.mapper;

import com.library.dto.AuthorDTO;
import com.library.model.Author;
import com.library.model.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper
public interface AuthorMapper {
    AuthorMapper INSTANCE = Mappers.getMapper(AuthorMapper.class);

    @Mapping(target = "bookIds", source = "books", qualifiedByName = "mapBooksToBookIds")
    AuthorDTO toDTO(Author author);

    @Mapping(target = "books", source = "bookIds", qualifiedByName = "mapBookIdsToBooks")
    Author toModel(AuthorDTO authorDTO);

    @Named("mapBooksToBookIds")
    static Set<Integer> mapBooksToBookIds(Set<Book> books) {
        if (books == null) {
            return new HashSet<>();
        }
        return books.stream()
                .map(Book::getId)
                .collect(Collectors.toSet());
    }

    @Named("mapBookIdsToBooks")
    static Set<Book> mapBookIdsToBooks(Set<Integer> bookIds) {
        if (bookIds == null) {
            return new HashSet<>();
        }
        return bookIds.stream()
                .map(id -> {
                    Book book = new Book();
                    book.setId(id);
                    return book;
                })
                .collect(Collectors.toSet());
    }
}
