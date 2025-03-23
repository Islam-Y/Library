CREATE TABLE books
(
    id             SERIAL PRIMARY KEY,
    title          VARCHAR(255) NOT NULL,
    published_date DATE,
    genre          VARCHAR(100),
    publisher_id   INTEGER      REFERENCES publishers (id) ON DELETE SET NULL
);