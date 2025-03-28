package com.library.mapper;

import com.library.dto.PublisherDTO;
import com.library.model.Book;
import com.library.model.Publisher;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

@Mapper
public interface PublisherMapper {
    PublisherMapper INSTANCE = Mappers.getMapper(PublisherMapper.class);

    @Mapping(source = "books", target = "bookIds", qualifiedByName = "mapBooksToBookIds")
    PublisherDTO toDTO(Publisher publisher);

    @Mapping(source = "bookIds", target = "books", qualifiedByName = "mapBookIdsToBooks")
    Publisher toModel(PublisherDTO publisherDTO);

    @Named("mapBooksToBookIds")
    static List<Integer> mapBooksToBookIds(List<Book> books) {
        if (books == null) {
            return Collections.emptyList();
        }
        return books.stream()
                .map(Book::getId)
                .toList();
    }

    @Named("mapBookIdsToBooks")
    static List<Book> mapBookIdsToBooks(List<Integer> bookIds) {
        if (bookIds == null) {
            return Collections.emptyList();
        }
        return bookIds.stream()
                .map(id -> {
                    Book book = new Book();
                    book.setId(id);
                    return book;
                })
                .toList();
    }
}