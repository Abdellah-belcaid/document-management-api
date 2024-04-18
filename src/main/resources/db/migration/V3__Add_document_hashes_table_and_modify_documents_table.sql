CREATE TABLE document_hashes
(
    id          UUID PRIMARY KEY,
    document_id UUID REFERENCES documents (id) NOT NULL,
    hash_value  VARCHAR(255)                   NOT NULL
);
