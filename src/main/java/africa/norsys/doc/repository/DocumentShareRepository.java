package africa.norsys.doc.repository;

import africa.norsys.doc.dto.UserPermissionDto;
import africa.norsys.doc.entity.Document;
import africa.norsys.doc.entity.DocumentShare;
import africa.norsys.doc.entity.User;
import africa.norsys.doc.enumerator.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentShareRepository extends JpaRepository<DocumentShare, UUID> {
    Optional<DocumentShare> findByDocumentAndUser(Document document, User user);

    boolean existsByDocumentIdAndUserIdAndPermission(UUID documentId, UUID userId, Permission permission);

    @Query("SELECT new africa.norsys.doc.dto.UserPermissionDto(ds.user, ds.permission) FROM DocumentShare ds WHERE ds.document.id = :documentId")
    List<UserPermissionDto> findUsersAndPermissionsByDocumentId(UUID documentId);

}
