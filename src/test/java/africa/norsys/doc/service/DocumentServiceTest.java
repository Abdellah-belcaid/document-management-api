package africa.norsys.doc.service;

import africa.norsys.doc.entity.Document;
import africa.norsys.doc.exception.DocumentNotFoundException;
import africa.norsys.doc.repository.DocumentRepository;
import africa.norsys.doc.service.impl.DocumentServiceImpl;
import africa.norsys.doc.util.DocumentHelperTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DocumentServiceTest {
    @Mock
    private DocumentRepository documentRepository;
    @InjectMocks
    private DocumentServiceImpl documentService;
    private List<Document> mockDocuments;
    @BeforeEach
    public void setUp() {
        mockDocuments = DocumentHelperTest.createMockDocuments();
    }
    @Test
    @DisplayName("Should return all documents")
    public void shouldReturnAllDocuments() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 5, Sort.Direction.ASC, "id");

        Page<Document> mockPage = new PageImpl<>(mockDocuments, pageable, mockDocuments.size());

        when(documentRepository.findAll(pageable)).thenReturn(mockPage);

        // Act
        Page<Document> result = documentService.getAllDocuments(0, 5, "asc", "id");

        // Assert
        Assertions.assertAll(
                () -> Assertions.assertEquals(mockPage, result, "Returned page should match mock page"),
                () -> Assertions.assertEquals(mockPage.getContent().size(), result.getContent().size(),
                        "Number of elements in returned page should match mock page"),
                () -> Assertions.assertEquals(mockPage.getContent(), result.getContent(),
                        "Content of returned page should match mock page content"),
                () -> Assertions.assertEquals(mockPage.getTotalElements(), result.getTotalElements(),
                        "Total number of elements in returned page should match mock page")
        );
    }
    @Test
    @DisplayName("Should return empty page when no document exist")
    public void shouldThrowExceptionWhenNoExamsExist() {
        // Arrange
        Page<Document> emptyPage = new PageImpl<>(Collections.emptyList());

        // Act
        when(documentRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);
        Throwable exception = Assertions.assertThrows(DocumentNotFoundException.class, () -> {
            documentService.getAllDocuments(0, 5, "asc", "id");
        });

        // Assert
        Assertions.assertEquals("no document found.", exception.getMessage(), "Exception message should match");
    }
    @Test
    @DisplayName("Should return specific page")
    public void shouldReturnSpecificPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 2, Sort.Direction.ASC, "id");

        List<Document> mockDocumentsForPage = mockDocuments.subList(0, 2);

        Page<Document> page = new PageImpl<>(mockDocumentsForPage, pageable, mockDocuments.size());

        // Act
        when(documentRepository.findAll(pageable)).thenReturn(page);
        Page<Document> result = documentService.getAllDocuments(0, 2, "asc", "id");

        // Assert
        Assertions.assertEquals(page, result, "Returned page should match expected page");
    }
    @Test
    @DisplayName("Should return exams sorted by name ascending")
    public void shouldReturnExamsSortedByTitleAscending() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 5, Sort.Direction.ASC, "name");

        // Sort mockDocuments by id in ascending order
        List<Document> sortedDocuments = mockDocuments.stream()
                .sorted(Comparator.comparing(Document::getName))
                .collect(Collectors.toList());

        Page<Document> sortedPage = new PageImpl<>(sortedDocuments, pageable, mockDocuments.size());

        // Act
        when(documentRepository.findAll(pageable)).thenReturn(sortedPage);
        Page<Document> result = documentService.getAllDocuments(0, 5, "asc", "name");

        // Assert
        Assertions.assertEquals(sortedPage, result, "Returned page should be sorted by name in ascending order");
    }
    @Test
    @DisplayName("Should return exams sorted by id descending")
    public void shouldReturnExamsSortedByIdDescending() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 5, Sort.Direction.DESC, "id");

        // Sort mockExams by id in descending order
        List<Document> sortedDocuments = mockDocuments.stream()
                .sorted(Comparator.comparing(Document::getId).reversed())
                .collect(Collectors.toList());

        Page<Document> sortedPage = new PageImpl<>(sortedDocuments, pageable, mockDocuments.size());

        // Act
        when(documentRepository.findAll(pageable)).thenReturn(sortedPage);
        Page<Document> result = documentService.getAllDocuments(0, 5, "desc", "id");

        // Assert
        Assertions.assertEquals(sortedPage, result, "Returned page should be sorted by id in descending order");

    }
}
