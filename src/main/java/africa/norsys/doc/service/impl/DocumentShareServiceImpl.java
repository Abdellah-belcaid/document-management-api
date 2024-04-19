package africa.norsys.doc.service.impl;

import africa.norsys.doc.entity.Document;
import africa.norsys.doc.entity.DocumentShare;
import africa.norsys.doc.entity.User;
import africa.norsys.doc.enumerator.Permission;
import africa.norsys.doc.exception.DocumentNotFoundException;
import africa.norsys.doc.exception.DocumentShareAlreadyExistsException;
import africa.norsys.doc.exception.UserNotFoundException;
import africa.norsys.doc.repository.DocumentRepository;
import africa.norsys.doc.repository.DocumentShareRepository;
import africa.norsys.doc.repository.UserRepository;
import africa.norsys.doc.service.DocumentShareService;
import africa.norsys.doc.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentShareServiceImpl implements DocumentShareService {
    private final DocumentShareRepository documentShareRepository;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    @Override
    public DocumentShare addDocumentShare(UUID documentId, UUID userId, Permission permission) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with id: " + documentId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        Optional<DocumentShare> existingShareOpt = documentShareRepository.findByDocumentAndUser(document,user);

        if (existingShareOpt.isPresent()) {
            DocumentShare existingShare = existingShareOpt.get();
            if (existingShare.getPermission() != permission) {
                existingShare.setPermission(permission);
                return documentShareRepository.save(existingShare);
            } else {
                throw new DocumentShareAlreadyExistsException("DocumentShare already exists for documentId: " + documentId + ", userId: " + userId + " with a same permission");
            }
        }
        DocumentShare newShare = DocumentShare.builder()
                .document(document)
                .user(user)
                .permission(permission)
                .build();
        return documentShareRepository.save(newShare);
    }



}
