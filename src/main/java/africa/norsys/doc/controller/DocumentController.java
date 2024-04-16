package africa.norsys.doc.controller;

import africa.norsys.doc.entity.Document;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static africa.norsys.doc.constant.PaginationConstants.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;


    @PostMapping
    public ResponseEntity<?> addDocument(@RequestParam("file") MultipartFile file) throws IOException {
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();

        Document savedDocument = documentService.addDocument(file, baseUrl);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDocument);
    }


    @GetMapping("/file/{filename:.+}")
    public ResponseEntity<byte[]> getFile(@PathVariable String filename) throws IOException {
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
    public ResponseEntity<Void> deleteDocument(@PathVariable UUID documentId) {
        try {
            documentService.deleteDocumentById(documentId);
            return ResponseEntity.ok().build();
        } catch (DocumentNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentById(@PathVariable UUID id) {
        Optional<Document> optionalDocument = documentService.getDocumentById(id);
        return optionalDocument.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Document>> searchDocuments(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "date", required = false) String date) {

        List<Document> documents = documentService.searchByKeyword(keyword, date);

        if (documents.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(documents);
        }

    }


}
