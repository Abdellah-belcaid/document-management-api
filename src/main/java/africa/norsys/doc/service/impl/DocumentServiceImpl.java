package africa.norsys.doc.service.impl;

import africa.norsys.doc.entity.Document;
import africa.norsys.doc.exception.DocumentNotFoundException;
import africa.norsys.doc.repository.DocumentRepository;
import africa.norsys.doc.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    @Override
    public Page<Document> getAllDocuments(Integer page, Integer pageSize, String sortDirection, String sortBy) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.Direction.fromString(sortDirection), sortBy);
        System.out.println("page"+page + "size"+pageSize + "sortDirection"+ sortDirection+ "sortBy" + sortBy );
        Page<Document> documentPage = documentRepository.findAll(pageable);
        return documentPage;
    }
}
