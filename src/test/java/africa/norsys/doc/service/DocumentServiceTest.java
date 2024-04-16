package africa.norsys.doc.service;

import africa.norsys.doc.entity.Document;
import africa.norsys.doc.repository.DocumentRepository;
import africa.norsys.doc.service.impl.DocumentServiceImpl;
import africa.norsys.doc.util.DocumentHelperTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.UUID;

import static africa.norsys.doc.util.DocumentHelperTest.BASE_URL;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;


    @InjectMocks
    private DocumentServiceImpl documentService;


    @Test
    @DisplayName("should_ add document Successfully")
    void should_add_document_Successfully() throws IOException {
        // Mock dependencies
        MockMultipartFile file = DocumentHelperTest.createMockMultipartFile();
        Document savedDocument = DocumentHelperTest.createMockDocument();
        when(documentRepository.save(any(Document.class))).thenReturn(savedDocument);

        Document result = documentService.addDocument(file, BASE_URL);

        verify(documentRepository, times(2)).save(any(Document.class));

        assertNotNull(result);
        assertEquals(savedDocument.getId(), result.getId());
        assertEquals(savedDocument.getName(), result.getName());
        assertEquals(savedDocument.getType(), result.getType());
        assertEquals(savedDocument.getCreationDate(), result.getCreationDate());
        assertEquals(savedDocument.getMetadata(), result.getMetadata());
        assertEquals(BASE_URL + "/api/documents/" + savedDocument.getId() + ".txt", result.getStorageLocation());
    }



}
