package com.library.mapper;

import com.library.dto.BookDTO;
import com.library.model.Author;
import com.library.model.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper
public interface BookMapper {
    BookMapper INSTANCE = Mappers.getMapper(BookMapper.class);

    @Mapping(source = "publisher.id", target = "publisherId")
    @Mapping(source = "authors", target = "authorIds", qualifiedByName = "mapAuthorsToAuthorIds")
    BookDTO toDTO(Book book);

    // При обратном преобразовании можно проигнорировать поле authors,
    // так как обычно при создании/обновлении книги передаются только идентификаторы.
    @Mapping(target = "authors", ignore = true)
    @Mapping(target = "publisher.id", source = "publisherId")
    Book toModel(BookDTO bookDTO);

    @Named("mapAuthorsToAuthorIds")
    static Set<Integer> mapAuthorsToAuthorIds(Set<Author> authors) {
        return authors.stream()
                .map(Author::getId)
                .collect(Collectors.toSet());
    }
}
