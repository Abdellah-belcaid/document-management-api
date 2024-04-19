package africa.norsys.doc.controller;

import africa.norsys.doc.entity.Document;
import africa.norsys.doc.enumerator.Permission;
import africa.norsys.doc.exception.DocumentNotAddedException;
import africa.norsys.doc.exception.DocumentNotFoundException;
import africa.norsys.doc.service.DocumentService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static africa.norsys.doc.constant.PaginationConstants.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;


    @PostMapping
    public ResponseEntity<?> addDocument(@ModelAttribute Document document,
                                         @RequestParam("file") MultipartFile file,
                                         @RequestParam("userId") UUID userId)
            throws DocumentNotAddedException, IOException {
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();

        Document savedDocument = documentService.addDocument(document, file, baseUrl, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDocument);
    }


    @GetMapping("/file/{filename:.+}")
    public ResponseEntity<byte[]> getFile(@PathVariable String filename,
                                          @RequestParam UUID documentId,
                                          @RequestParam("userId") UUID userId) throws IOException {
        // Check if the user has access to the document
        boolean hasAccess = documentService.checkUserAccess(documentId, userId, Permission.READ);

        if (!hasAccess) {
            // If the user does not have access, return a forbidden status code
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        byte[] fileBytes = documentService.getFileBytes(filename);
        return ResponseEntity.ok().body(fileBytes);
    }


    @GetMapping
    public ResponseEntity<Page<Document>> getAllDocuments(
            @RequestParam(defaultValue = DEFAULT_PAGE + "") @Min(0) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE + "") @Min(1) int size,
            @RequestParam(defaultValue = DEFAULT_SORT_DIRECTION) String sortDirection,
            @RequestParam(defaultValue = DEFAULT_DOCUMENT_SORT_BY) String sortBy
    ) {
        Page<Document> documentPage = documentService.getAllDocuments(page, size, sortDirection, sortBy);
        return documentPage == null || documentPage.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(documentPage);
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable UUID documentId, @RequestParam("userId") UUID userId) {
        try {

            // Check if the user has access to the document
            boolean hasAccess = documentService.checkUserAccess(documentId, userId, Permission.WRITE);

            if (!hasAccess) {
                // If the user does not have access, return a forbidden status code
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }


            documentService.deleteDocumentById(documentId);
            return ResponseEntity.ok().build();
        } catch (DocumentNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<Document> getDocumentById(
            @PathVariable UUID documentId,
            @RequestParam("userId") UUID userId) {
        // Check if the user has access to the document
        boolean hasAccess = documentService.checkUserAccess(documentId, userId, Permission.READ);

        if (!hasAccess) {
            // If the user does not have access, return a forbidden status code
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // If the user has access, retrieve and return the document details
        Optional<Document> optionalDocument = documentService.getDocumentById(documentId);
        return optionalDocument.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/search")
    public ResponseEntity<Page<Document>> searchDocuments(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(defaultValue = DEFAULT_PAGE + "") @Min(0) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE + "") @Min(1) int size) {

        Page<Document> documents = documentService.searchByKeyword(keyword, date, page, size);

        return documents.isEmpty() ?
                ResponseEntity.noContent().build() :
                ResponseEntity.ok(documents);
    }


    @GetMapping("/user")
    public ResponseEntity<Page<Document>> getUserDocuments(
            @RequestParam(defaultValue = DEFAULT_PAGE + "") @Min(0) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE + "") @Min(1) int size,
            @RequestParam UUID userId
    ) {
        Page<Document> userDocuments = documentService.getUserDocuments(userId, page, size);
        return userDocuments.isEmpty() ?
                ResponseEntity.noContent().build() :
                ResponseEntity.ok(userDocuments);
    }
}



