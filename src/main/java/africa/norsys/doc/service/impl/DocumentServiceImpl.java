package africa.norsys.doc.service.impl;

import africa.norsys.doc.entity.Document;
import africa.norsys.doc.entity.DocumentHash;
import africa.norsys.doc.entity.DocumentShare;
import africa.norsys.doc.entity.User;
import africa.norsys.doc.enumerator.Permission;
import africa.norsys.doc.exception.*;
import africa.norsys.doc.repository.DocumentHashRepository;
import africa.norsys.doc.repository.DocumentRepository;
import africa.norsys.doc.repository.DocumentShareRepository;
import africa.norsys.doc.repository.UserRepository;
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
import java.util.Optional;
import java.util.Set;
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
    private final UserRepository userRepository;
    private final DocumentShareRepository documentShareRepository;
    @Override
    public Document addDocument(Document document, MultipartFile file, String baseUrl) throws DocumentNotAddedException, IOException {

        // Generate hash for the file content
        String fileHash = generateFileHash(file.getInputStream());

        // Check if a document with the same hash exists
        if (documentRepository.existsByDocumentHash_HashValue(fileHash)) {
            throw new FileAlreadyExistException("A document with the same content already exists.");
        }

        // If document name is not provided, use the original file name
        if (document.getName() == null || document.getName().isEmpty() )
            document.setName(file.getOriginalFilename());

        document.setType(file.getContentType());
        document.setMetadata(FileUtils.extractMetadata(file));

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
    public void shareDocumentWithUsers(UUID documentId, Set<UUID> userIds, Permission permission) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with id: " + documentId));
        Permission actualPermission = (permission == Permission.READ_WRITE) ? Permission.READ : permission;

        for (UUID userId : userIds) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

            DocumentShare existingShare = documentShareRepository.findByDocumentAndUser(document, user)
                    .orElse(DocumentShare.builder()
                            .document(document)
                            .user(user)
                            .build());

            existingShare.setPermission(actualPermission);
            documentShareRepository.save(existingShare);
        }
    }
}
