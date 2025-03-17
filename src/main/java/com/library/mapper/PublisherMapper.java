package com.library.mapper;

import com.library.dto.PublisherDTO;
import com.library.model.Book;
import com.library.model.Publisher;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper
public interface PublisherMapper {
    PublisherMapper INSTANCE = Mappers.getMapper(PublisherMapper.class);

    @Mapping(source = "books", target = "bookIds", qualifiedByName = "mapBooksToBookIds")
    PublisherDTO toDTO(Publisher publisher);

    // Обратное преобразование книг можно игнорировать,
    // если при создании/обновлении издателя эта информация не передается.
    @Mapping(target = "books", ignore = true)
    Publisher toModel(PublisherDTO publisherDTO);

    @Named("mapBooksToBookIds")
    static Set<Integer> mapBooksToBookIds(Set<Book> books) {
        if (books == null) {
            return null;
        }
        return books.stream()
                .map(Book::getId)
                .collect(Collectors.toSet());
    }
}