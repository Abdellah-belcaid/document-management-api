package africa.norsys.doc.service.impl;

import africa.norsys.doc.entity.Document;
import africa.norsys.doc.entity.DocumentHash;
import africa.norsys.doc.enumerator.Permission;
import africa.norsys.doc.exception.DocumentNotAddedException;
import africa.norsys.doc.exception.DocumentNotFoundException;
import africa.norsys.doc.exception.FileAlreadyExistException;
import africa.norsys.doc.exception.FileNotFoundException;
import africa.norsys.doc.repository.DocumentHashRepository;
import africa.norsys.doc.repository.DocumentRepository;
import africa.norsys.doc.repository.DocumentShareRepository;
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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static africa.norsys.doc.constant.Constant.FILE_STORAGE_LOCATION;
import static africa.norsys.doc.constant.PaginationConstants.DEFAULT_DOCUMENT_SORT_BY;
import static africa.norsys.doc.util.FileUtils.generateFileHash;
import static africa.norsys.doc.util.FileUtils.saveFileAndGenerateUrl;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentHashRepository documentHashRepository;
    private final DocumentShareRepository documentShareRepository;


    @Override
    public Document addDocument(Document document, MultipartFile file, String baseUrl, UUID userId) throws DocumentNotAddedException, IOException {

        // Generate hash for the file content
        String fileHash = generateFileHash(file.getInputStream());

        // Check if a document with the same hash exists
        if (documentRepository.existsByDocumentHash_HashValue(fileHash)) {
            throw new FileAlreadyExistException("A document with the same content already exists.");
        }

        // If document name is not provided, use the original file name
        if (document.getName() == null || document.getName().isEmpty())
            document.setName(file.getOriginalFilename());

        document.setType(file.getContentType());

        // Extract metadata with the user ID
        Map<String, String> metadata = FileUtils.extractMetadata(file, userId);
        document.setMetadata(metadata);

        // Save the document to the database
        document = documentRepository.save(document);

        // Save the hash to the database
        DocumentHash documentHash = DocumentHash.builder()
                .id(UUID.randomUUID())
                .document(document)
                .hashValue(fileHash)
                .build();
        documentHashRepository.save(documentHash);

        try {
            // Generate and set the storage location URL
            String fileUrl = saveFileAndGenerateUrl(document.getId().toString(), file, baseUrl);
            document.setStorageLocation(fileUrl);
        } catch (IOException e) {
            // Rollback the saved document and hash
            documentRepository.delete(document);
            documentHashRepository.delete(documentHash);
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
    public Page<Document> searchByKeyword(String keyword, String date, int page, int size) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.ASC, DEFAULT_DOCUMENT_SORT_BY);
        Page<Document> documents = documentRepository.searchByKeyword(keyword.toLowerCase(), date, pageable);
        if (documents.isEmpty()) {
            throw new DocumentNotFoundException("No documents found with the provided keyword and date.");
        }
        return documents;

    }

    @Override
    public void deleteDocumentById(UUID documentId) throws DocumentNotFoundException, IOException {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with id: " + documentId));

        String filename = documentId + document.getMetadata().get("extension");
        Path filePath = Paths.get(FILE_STORAGE_LOCATION).resolve(filename).normalize();
        Files.deleteIfExists(filePath);
        documentRepository.delete(document);
    }

    @Override
    public Page<Document> getUserDocuments(UUID userId, int page, int size) {
        log.info("Fetching documents for user with ID {}", userId);
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Document> userDocuments = documentRepository.findByUserId(userId, pageable);
        log.info("Retrieved {} documents for user with ID {}", userDocuments.getTotalElements(), userId);
        return userDocuments;
    }

    @Override
    public boolean checkUserAccess(UUID documentId, UUID userId, Permission permission) {
        // Retrieve the document by its ID
        Optional<Document> optionalDocument = documentRepository.findById(documentId);

        if (optionalDocument.isPresent()) {
            Document document = optionalDocument.get();

            // Log the document owner and user ID for debugging
            log.debug("Document owner: {}", document.getMetadata().get("owner"));
            log.debug("User ID: {}", userId);

            // Check if the document owner matches the user ID or if there's a sharing entry for the document and user with READ permission
            boolean isOwner = document.getMetadata().get("owner").equals(userId.toString());
            boolean hasReadPermission = documentShareRepository.existsByDocumentIdAndUserIdAndPermission(documentId, userId, permission);

            // Log the access check results
            log.debug("Is owner: {}", isOwner);
            log.debug("Has read permission: {}", hasReadPermission);

            return isOwner || hasReadPermission; // User has access
        }

        // If the document doesn't exist or the user doesn't have access, return false
        return false;
    }


}
