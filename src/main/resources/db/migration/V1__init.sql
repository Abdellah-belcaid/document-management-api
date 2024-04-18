CREATE TABLE documents
(
    id               UUID PRIMARY KEY,
    name             VARCHAR(255),
    type             VARCHAR(255),
    creation_date    TIMESTAMP,
    storage_location VARCHAR(255)
);

CREATE TABLE document_metadata
(
    document_id UUID         NOT NULL,
    key         VARCHAR(255) NOT NULL,
    value       VARCHAR(255),
    CONSTRAINT fk_document_id FOREIGN KEY (document_id) REFERENCES documents (id),
    PRIMARY KEY (document_id, key)
);


