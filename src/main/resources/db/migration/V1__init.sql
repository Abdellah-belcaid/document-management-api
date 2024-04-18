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

CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       email VARCHAR(100) NOT NULL,
                       username VARCHAR(100) UNIQUE NOT NULL,
                       password VARCHAR(100) NOT NULL,
                       create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);



CREATE TABLE documentshare (
                               id UUID PRIMARY KEY,
                               document_id UUID REFERENCES documents,
                               user_id UUID REFERENCES users,
                               permission VARCHAR(255) CHECK (permission IN ('READ', 'READ_WRITE'))
);

