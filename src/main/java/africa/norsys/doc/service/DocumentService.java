package africa.norsys.doc.service;

import africa.norsys.doc.entity.Document;
import org.springframework.data.domain.Page;

public interface DocumentService {
    Page<Document> getAllDocuments(Integer page, Integer pageSize, String sortBy, String sortDirection);
}
