package africa.norsys.doc.repository;

import africa.norsys.doc.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {


/*    @Query("SELECT d FROM Document d " +
            "WHERE LOWER(d.name) LIKE %:keyword% " +
            "OR LOWER(d.type) LIKE %:keyword% " +
            "AND (:date IS NULL OR DATE(d.creationDate) = DATE(:date)) " +
            "OR EXISTS (SELECT 1 FROM d.metadata m WHERE LOWER(m.key) LIKE %:keyword% AND LOWER(m.value) LIKE %:keyword%)")
    List<Document> searchByKeyword(String keyword, @Param("date") String date);

 */

    @Query(value = "SELECT DISTINCT d.* FROM documents d " +
            "LEFT JOIN document_metadata m ON d.id = m.document_id " +
            "WHERE LOWER(d.name) LIKE lower(concat('%', :keyword, '%')) " +
            "OR LOWER(d.type) LIKE lower(concat('%', :keyword, '%')) " +
            "AND (:date IS NULL OR DATE(d.creation_date) = DATE(:date)) " +
            "OR d.id IN (SELECT dm.document_id FROM document_metadata dm WHERE LOWER(dm.key) LIKE lower(concat('%', :keyword, '%')) AND LOWER(dm.value) LIKE lower(concat('%', :keyword, '%')))",
            nativeQuery = true)
    List<Document> searchByKeyword(@Param("keyword") String keyword, @Param("date") String date);


}
