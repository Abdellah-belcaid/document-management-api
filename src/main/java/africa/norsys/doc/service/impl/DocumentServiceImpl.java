package africa.norsys.doc.service.impl;

import africa.norsys.doc.entity.Document;
import africa.norsys.doc.exception.DocumentNotAddedException;
import africa.norsys.doc.exception.DocumentNotFoundException;
import africa.norsys.doc.exception.FileNotFoundException;
import africa.norsys.doc.repository.DocumentRepository;
import africa.norsys.doc.service.DocumentService;
import africa.norsys.doc.util.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static africa.norsys.doc.constant.Constant.FILE_STORAGE_LOCATION;
import static africa.norsys.doc.util.FileUtils.saveFileAndGenerateUrl;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;


    @Override
    public Document addDocument(MultipartFile file, String baseUrl) throws IOException {

        Document document = Document.builder()
                .name(file.getOriginalFilename())
                .type(file.getContentType())
                .metadata(FileUtils.extractMetadata(file))
                .build();

        // Save the document to the database
        document = documentRepository.save(document);

        try {
            // Generate and set the storage location URL
            String fileUrl = saveFileAndGenerateUrl(document.getId().toString(), file, baseUrl);
            document.setStorageLocation(fileUrl);
        } catch (IOException e) {
            documentRepository.delete(document);
            throw new DocumentNotAddedException("Failed to add document: " + e.getMessage());
        }
        // Update the document in the database with the storage location URL
        return documentRepository.save(document);
    }


    @Override
    public byte[] getFileBytes(String filename) throws IOException {
        Path filePath = Paths.get(FILE_STORAGE_LOCATION).resolve(filename).normalize();
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("File '" + filename + "' not found");
        }
        return Files.readAllBytes(filePath);
    }


    @Override
    public Page<Document> getAllDocuments(Integer page, Integer pageSize, String sortDirection, String sortBy) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.Direction.fromString(sortDirection), sortBy);
        log.info("Retrieving documents with page {}, size {}, sort direction {}, sorted by {}", page, pageSize, sortDirection, sortBy);

        Page<Document> documents = documentRepository.findAll(pageable);
        if (documents.isEmpty()) throw new DocumentNotFoundException("no document found.");
        return documents;
    }


    @Override
    public Optional<Document> getDocumentById(UUID id) {
        Optional<Document> document = documentRepository.findById(id);
        document.ifPresentOrElse(
                d -> log.info("Retrieved document by id: {}", id),
                () -> {
                    log.warn("Document with id {} not found", id);
                    throw new DocumentNotFoundException("Document with id " + id + " not found");
                }
        );
        return document;
    }


    @Override
    public List<Document> searchByKeyword(String keyword, String date) {
        try {
            List<Document> documents = documentRepository.searchByKeyword(keyword.toLowerCase(), date);
            if (documents.isEmpty()) {
                throw new DocumentNotFoundException("No documents found with the provided keyword and date.");
            }
            return documents;
        } catch (Exception e) {
            log.error("An error occurred while searching documents by keyword: {}", e.getMessage());
            throw new DocumentNotFoundException("Failed to search documents by keyword: " + e.getMessage());
        }
    }
    @Override
    public void deleteDocumentById(UUID documentId) throws DocumentNotFoundException, IOException {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with id: " + documentId));

        String filename = documentId+ document.getMetadata().get("extension");
        Path filePath = Paths.get(FILE_STORAGE_LOCATION).resolve(filename).normalize();
        Files.deleteIfExists(filePath);
        documentRepository.delete(document);
    }

}
