-- Drop the existing foreign key constraint
ALTER TABLE document_share
DROP
CONSTRAINT IF EXISTS document_share_document_id_fkey;

-- Add the foreign key constraint with both ON DELETE CASCADE and ON UPDATE CASCADE
ALTER TABLE document_share
    ADD CONSTRAINT document_share_document_id_fkey
        FOREIGN KEY (document_id) REFERENCES documents (id) ON DELETE CASCADE ON UPDATE CASCADE;
