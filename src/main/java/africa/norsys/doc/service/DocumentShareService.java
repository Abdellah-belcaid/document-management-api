package africa.norsys.doc.service;

import africa.norsys.doc.entity.DocumentShare;
import africa.norsys.doc.entity.User;
import africa.norsys.doc.enumerator.Permission;

import java.util.UUID;

public interface DocumentShareService {
    public DocumentShare addDocumentShare(UUID documentId, UUID userId, Permission permission);
}
