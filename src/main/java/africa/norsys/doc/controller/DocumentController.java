package africa.norsys.doc.controller;

import africa.norsys.doc.entity.Document;
import africa.norsys.doc.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    public ResponseEntity<?> addDocument(@RequestParam("file") MultipartFile file) {
        try {
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();

            Document savedDocument = documentService.addDocument(file, baseUrl);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDocument);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add document: " + e.getMessage());
        }
    }


    @GetMapping("/{filename:.+}")
    public ResponseEntity<byte[]> getFile(@PathVariable String filename) {
        try {

            byte[] fileBytes = documentService.getFileBytes(filename);
            return ResponseEntity.ok().body(fileBytes);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }


}
