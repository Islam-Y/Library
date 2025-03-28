package com.library.mapper;

import com.library.dto.BookDTO;
import com.library.model.Author;
import com.library.model.Book;
import com.library.model.Publisher;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper
public interface BookMapper {
    BookMapper INSTANCE = Mappers.getMapper(BookMapper.class);

    @Mapping(source = "publisher.id", target = "publisherId")
    @Mapping(source = "authors", target = "authorIds", qualifiedByName = "mapAuthorsToAuthorIds")
    BookDTO toDTO(Book book);

    @Mapping(target = "authors", source = "authorIds", qualifiedByName = "mapAuthorIdsToAuthors")
    @Mapping(target = "publisher", source = "publisherId", qualifiedByName = "mapPublisherIdToPublisher")
    Book toModel(BookDTO bookDTO);

    @Named("mapAuthorsToAuthorIds")
    static Set<Integer> mapAuthorsToAuthorIds(Set<Author> authors) {
        if (authors == null) return Collections.emptySet();
        return authors.stream()
                .map(Author::getId)
                .collect(Collectors.toSet());
    }

    @Named("mapAuthorIdsToAuthors")
    static Set<Author> mapAuthorIdsToAuthors(Set<Integer> authorIds) {
        if (authorIds == null) return Collections.emptySet();
        return authorIds.stream()
                .map(id -> {
                    Author author = new Author();
                    author.setId(id);
                    return author;
                })
                .collect(Collectors.toSet());
    }

    @Named("mapPublisherIdToPublisher")
    static Publisher mapPublisherIdToPublisher(Integer publisherId) {
        if (publisherId == null) return null;
        Publisher publisher = new Publisher();
        publisher.setId(publisherId);
        return publisher;
    }
}
