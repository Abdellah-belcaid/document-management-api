CREATE TABLE documents (
                           id UUID PRIMARY KEY,
                           name VARCHAR(255),
                           type VARCHAR(255),
                           creation_date TIMESTAMP,
                           storage_location VARCHAR(255)
);

create table document_metadata (
                                   document_id uuid not null,
                                   key varchar(255) not null,
                                   value varchar(255),
                                   constraint fk_document_id foreign key (document_id) references documents(id),
                                   primary key (document_id, key)
);

