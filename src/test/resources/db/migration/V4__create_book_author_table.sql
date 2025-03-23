CREATE TABLE book_author
(
    book_id   INTEGER REFERENCES books (id) ON DELETE CASCADE,
    author_id INTEGER REFERENCES authors (id) ON DELETE CASCADE,
    PRIMARY KEY (book_id, author_id)
);