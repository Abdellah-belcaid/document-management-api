package africa.norsys.doc.service;

import africa.norsys.doc.dto.UserPermissionDto;
import africa.norsys.doc.entity.DocumentShare;
import africa.norsys.doc.entity.User;
import africa.norsys.doc.enumerator.Permission;

import java.util.List;
import java.util.UUID;

public interface DocumentShareService {
    public DocumentShare addDocumentShare(UUID documentId, UUID userId, Permission permission);
    public List<UserPermissionDto> getUsersAndPermissionsByDocumentId(UUID documentId);
}
