package africa.norsys.doc.repository;

import africa.norsys.doc.entity.DocumentHash;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DocumentHashRepository extends JpaRepository<DocumentHash, UUID> {
}
