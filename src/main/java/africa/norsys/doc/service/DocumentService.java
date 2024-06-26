package africa.norsys.doc.service;

import africa.norsys.doc.entity.Document;
import africa.norsys.doc.enumerator.Permission;
import africa.norsys.doc.exception.DocumentNotAddedException;
import africa.norsys.doc.exception.DocumentNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public interface DocumentService {
    Document addDocument(Document document, MultipartFile file, String baseUrl, UUID userId) throws DocumentNotAddedException, IOException;

    byte[] getFileBytes(String filename) throws IOException;

    Page<Document> getAllDocuments(Integer page, Integer pageSize, String sortBy, String sortDirection);

    Page<Document> searchByKeyword(String keyword, String date, int page, int size);

    void deleteDocumentById(UUID documentId) throws DocumentNotFoundException, IOException;

    Optional<Document> getDocumentById(UUID id);


    Page<Document> getUserDocuments(UUID userId, int page, int size);

    boolean checkUserAccess(UUID documentId, UUID userId, Permission permission);
}
