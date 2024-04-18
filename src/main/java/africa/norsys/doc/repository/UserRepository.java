package africa.norsys.doc.repository;

import africa.norsys.doc.entity.User;
import africa.norsys.doc.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    User findByUsernameAndPassword(String username, String Password);

    Optional<User> findByUsername(String username);

    @Modifying
    @Query("update User set role = :role where username = :username")
    void updateUserRole(@Param("username") String username, @Param("role") Role role);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    User findByEmailIgnoreCase(String email);
}
