package africa.norsys.doc.repository;

import africa.norsys.doc.entity.Document;
import africa.norsys.doc.entity.DocumentShare;
import africa.norsys.doc.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DocumentShareRepository extends JpaRepository<DocumentShare, UUID> {
    Optional<DocumentShare> findByDocumentAndUser(Document document, User user);
}
