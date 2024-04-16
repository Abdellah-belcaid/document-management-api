package africa.norsys.doc.service;

import africa.norsys.doc.entity.Document;
import africa.norsys.doc.exception.DocumentNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentService {
    Document addDocument(MultipartFile file, String baseUrl) throws IOException;

    byte[] getFileBytes(String filename) throws IOException;


    Page<Document> getAllDocuments(Integer page, Integer pageSize, String sortBy, String sortDirection);
    void deleteDocumentById(UUID documentId)throws DocumentNotFoundException, IOException;;

    Optional<Document> getDocumentById(UUID id);

    List<Document> searchByKeyword(String keyword, String date);
}
