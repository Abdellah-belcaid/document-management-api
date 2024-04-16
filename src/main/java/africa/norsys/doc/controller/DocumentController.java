package africa.norsys.doc.controller;

import africa.norsys.doc.entity.Document;
import africa.norsys.doc.service.DocumentService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import static africa.norsys.doc.constant.PaginationConstants.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

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
}
