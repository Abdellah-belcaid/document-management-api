package africa.norsys.doc.repository;

import africa.norsys.doc.entity.Document;
import africa.norsys.doc.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
