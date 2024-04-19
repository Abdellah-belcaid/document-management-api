package africa.norsys.doc.controller;

import africa.norsys.doc.entity.DocumentShare;
import africa.norsys.doc.enumerator.Permission;
import africa.norsys.doc.exception.DocumentNotFoundException;
import africa.norsys.doc.exception.DocumentShareAlreadyExistsException;
import africa.norsys.doc.exception.UserNotFoundException;
import africa.norsys.doc.service.DocumentShareService;
import africa.norsys.doc.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sh")
public class DocumentShareController {
    private final DocumentShareService documentShareService;
    @PostMapping
    public ResponseEntity<DocumentShare> shareDocumentWithUser(@RequestParam UUID documentId,
                                                               @RequestParam UUID userId,
                                                               @RequestParam Permission permission) {
        try {
            DocumentShare documentShare = documentShareService.addDocumentShare(documentId, userId, permission);
            return ResponseEntity.ok().body(documentShare);
        } catch (DocumentNotFoundException | UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (DocumentShareAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
