package com.library.mapper;

import com.library.dto.PublisherDTO;
import com.library.model.Book;
import com.library.model.Publisher;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface PublisherMapper {
    PublisherMapper INSTANCE = Mappers.getMapper(PublisherMapper.class);

    @Mapping(source = "books", target = "bookIds", qualifiedByName = "mapBooksToBookIds")
    PublisherDTO toDTO(Publisher publisher);

    @Mapping(target = "books", ignore = true)
    Publisher toModel(PublisherDTO publisherDTO);

    @Named("mapBooksToBookIds")
    static List<Integer> mapBooksToBookIds(List<Book> books) {
        if (books == null) {
            return new ArrayList<>();
        }
        return books.stream()
                .map(Book::getId)
                .toList();
    }
}