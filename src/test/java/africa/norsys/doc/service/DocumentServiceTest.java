
package africa.norsys.doc.service;

import africa.norsys.doc.entity.Document;
import africa.norsys.doc.entity.DocumentHash;
import africa.norsys.doc.exception.DocumentNotFoundException;
import africa.norsys.doc.exception.FileAlreadyExistException;
import africa.norsys.doc.repository.DocumentHashRepository;
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
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static africa.norsys.doc.constant.PaginationConstants.*;
import static africa.norsys.doc.util.DocumentHelperTest.BASE_URL;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {
    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentHashRepository documentHashRepository;

    @InjectMocks
    private DocumentServiceImpl documentService;
    private List<Document> mockDocuments;

    @BeforeEach
    public void setUp() {
        mockDocuments = DocumentHelperTest.createMockDocuments();
    }

    @Test
    @DisplayName("should add document Successfully")
    void should_add_document_Successfully() throws IOException {
        UUID userId = UUID.randomUUID();

        MockMultipartFile file = DocumentHelperTest.createMockMultipartFile();
        Document savedDocument = DocumentHelperTest.createMockDocument();
        when(documentRepository.save(any(Document.class))).thenReturn(savedDocument);

        DocumentHash savedDocumentHash = DocumentHelperTest.createMockDocumentHash(savedDocument, file);
        when(documentHashRepository.save(any(DocumentHash.class))).thenReturn(savedDocumentHash);

        Document document = Document.builder().name(file.getOriginalFilename()).build();

        Document result = documentService.addDocument(document, file, BASE_URL, userId); // Pass the userId

        verify(documentRepository, times(2)).save(any(Document.class));
        verify(documentHashRepository, times(1)).save(any(DocumentHash.class));

        assertNotNull(result);
        assertEquals(savedDocument.getId(), result.getId());
        assertEquals(savedDocument.getName(), result.getName());
        assertEquals(savedDocument.getType(), result.getType());
        assertEquals(savedDocument.getCreationDate(), result.getCreationDate());
        assertEquals(savedDocument.getMetadata(), result.getMetadata());
        assertEquals(BASE_URL + "/api/documents/file/" + savedDocument.getId() + ".txt", result.getStorageLocation());
    }


    @Test
    @DisplayName("should throw FileAlreadyExistException when adding document with existing hash")
    void should_throw_FileAlreadyExistException_when_existing_hash() throws IOException {
        // Mock dependencies
        MockMultipartFile file = DocumentHelperTest.createMockMultipartFile();
        Document savedDocument = DocumentHelperTest.createMockDocument();

        // Mocking the scenario where a document with the same hash value already exists
        when(documentRepository.existsByDocumentHash_HashValue(anyString())).thenReturn(true);

        // Attempt to add a document
        Document document = Document.builder().name(file.getOriginalFilename()).build();

        // Generate a random userId
        UUID userId = UUID.randomUUID();

        // Verify that FileAlreadyExistException is thrown
        FileAlreadyExistException exception = assertThrows(FileAlreadyExistException.class, () -> {
            documentService.addDocument(document, file, BASE_URL, userId);
        });

        // Verify the exception message
        assertEquals("A document with the same content already exists.", exception.getMessage(),
                "Exception message should match");

        // Verify that documentRepository.save() and documentHashRepository.save() are not called
        verify(documentRepository, never()).save(any(Document.class));
        verify(documentHashRepository, never()).save(any(DocumentHash.class));
    }


    @Test
    @DisplayName("Should return all documents")
    public void shouldReturnAllDocuments() {

        Pageable pageable = PageRequest.of(0, 5, Sort.Direction.ASC, "id");

        Page<Document> mockPage = new PageImpl<>(mockDocuments, pageable, mockDocuments.size());

        when(documentRepository.findAll(pageable)).thenReturn(mockPage);


        Page<Document> result = documentService.getAllDocuments(1, 5, "asc", "id");


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
    public void shouldThrowExceptionWhenNoDocumentExist() {

        Page<Document> emptyPage = new PageImpl<>(Collections.emptyList());


        when(documentRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);
        Throwable exception = Assertions.assertThrows(DocumentNotFoundException.class, () -> {
            documentService.getAllDocuments(1, 5, "asc", "id");
        });


        Assertions.assertEquals("no document found.", exception.getMessage(), "Exception message should match");
    }

    @Test
    @DisplayName("Should return specific page")
    public void shouldReturnSpecificPage() {

        Pageable pageable = PageRequest.of(0, 2, Sort.Direction.ASC, "id");

        List<Document> mockDocumentsForPage = mockDocuments.subList(0, 2);

        Page<Document> page = new PageImpl<>(mockDocumentsForPage, pageable, mockDocuments.size());


        when(documentRepository.findAll(pageable)).thenReturn(page);
        Page<Document> result = documentService.getAllDocuments(1, 2, "asc", "id");


        Assertions.assertEquals(page, result, "Returned page should match expected page");
    }

    @Test
    @DisplayName("Should return documents sorted by name ascending")
    public void shouldReturnDocumentsSortedByTitleAscending() {

        Pageable pageable = PageRequest.of(0, 5, Sort.Direction.ASC, "name");


        List<Document> sortedDocuments = mockDocuments.stream()
                .sorted(Comparator.comparing(Document::getName))
                .collect(Collectors.toList());

        Page<Document> sortedPage = new PageImpl<>(sortedDocuments, pageable, mockDocuments.size());


        when(documentRepository.findAll(pageable)).thenReturn(sortedPage);
        Page<Document> result = documentService.getAllDocuments(1, 5, "asc", "name");


        Assertions.assertEquals(sortedPage, result, "Returned page should be sorted by name in ascending order");
    }

    @Test
    @DisplayName("Should return documents sorted by id descending")
    public void shouldReturnDocumentsSortedByIdDescending() {

        Pageable pageable = PageRequest.of(0, 5, Sort.Direction.DESC, "id");


        List<Document> sortedDocuments = mockDocuments.stream()
                .sorted(Comparator.comparing(Document::getId).reversed())
                .collect(Collectors.toList());

        Page<Document> sortedPage = new PageImpl<>(sortedDocuments, pageable, mockDocuments.size());


        when(documentRepository.findAll(pageable)).thenReturn(sortedPage);
        Page<Document> result = documentService.getAllDocuments(1, 5, "desc", "id");


        Assertions.assertEquals(sortedPage, result, "Returned page should be sorted by id in descending order");

    }

    @Test
    @DisplayName("Should return document by id")
    void should_return_document_by_id() {

        UUID id = UUID.randomUUID();
        Document mockDocument = DocumentHelperTest.createMockDocument();
        mockDocument.setId(id);

        when(documentRepository.findById(id)).thenReturn(Optional.of(mockDocument));

        Optional<Document> result = documentService.getDocumentById(id);

        assertTrue(result.isPresent(), "Document should be present");
        assertEquals(mockDocument, result.get(), "Returned document should match mock document");
    }

    @Test
    @DisplayName("Should throw DocumentNotFoundException when document not found")
    void shouldThrowDocumentNotFoundExceptionWhenDocumentNotFound() {

        UUID id = UUID.randomUUID();
        when(documentRepository.findById(id)).thenReturn(Optional.empty());

        DocumentNotFoundException exception = assertThrows(DocumentNotFoundException.class, () -> {
            documentService.getDocumentById(id);
        });

        assertEquals("Document with id " + id + " not found", exception.getMessage(),
                "Exception message should match");
    }

    @Test
    @DisplayName("Should search documents by keyword and date")
    void should_search_documents_by_keyword_and_date() {

        String keyword = "test";
        String date = "2024-04-16";
        Page<Document> expectedPage = new PageImpl<>(Collections.singletonList(DocumentHelperTest.createMockDocument()));
        Pageable pageable = PageRequest.of(DEFAULT_PAGE - 1, DEFAULT_PAGE_SIZE, Sort.Direction.ASC, DEFAULT_DOCUMENT_SORT_BY);

        when(documentRepository.searchByKeyword(keyword.toLowerCase(), date, pageable)).thenReturn(expectedPage);


        Page<Document> resultPage = documentService.searchByKeyword(keyword, date, DEFAULT_PAGE, DEFAULT_PAGE_SIZE);


        assertEquals(expectedPage.getTotalElements(), resultPage.getTotalElements(), "Total number of documents should match");
        assertEquals(expectedPage.getContent(), resultPage.getContent(), "Returned documents should match expected documents");
    }

    @Test
    @DisplayName("Should search documents by keyword only")
    void should_search_documents_by_keyword_only() {

        String keyword = "test";
        String date = null;
        Page<Document> expectedPage = new PageImpl<>(Collections.singletonList(DocumentHelperTest.createMockDocument()));
        Pageable pageable = PageRequest.of(DEFAULT_PAGE - 1, DEFAULT_PAGE_SIZE, Sort.Direction.ASC, DEFAULT_DOCUMENT_SORT_BY);

        when(documentRepository.searchByKeyword(keyword.toLowerCase(), date, pageable)).thenReturn(expectedPage);


        Page<Document> resultPage = documentService.searchByKeyword(keyword, date, DEFAULT_PAGE, DEFAULT_PAGE_SIZE);


        assertEquals(expectedPage.getTotalElements(), resultPage.getTotalElements(), "Total number of documents should match");
        assertEquals(expectedPage.getContent(), resultPage.getContent(), "Returned documents should match expected documents");
    }

    @Test
    @DisplayName("Should throw DocumentNotFoundException when no documents are found")
    void shouldThrowDocumentNotFoundExceptionWhenNoDocumentsFound() {

        String keyword = "test";
        String date = "2024-04-16";
        Pageable pageable = PageRequest.of(DEFAULT_PAGE - 1, DEFAULT_PAGE_SIZE, Sort.Direction.ASC, DEFAULT_DOCUMENT_SORT_BY);


        when(documentRepository.searchByKeyword(keyword.toLowerCase(), date, pageable))
                .thenReturn(new PageImpl<>(Collections.emptyList()));


        assertThrows(DocumentNotFoundException.class, () ->
                documentService.searchByKeyword(keyword, date, DEFAULT_PAGE, DEFAULT_PAGE_SIZE));


        verify(documentRepository).searchByKeyword(keyword.toLowerCase(), date, pageable);
    }

    @Test
    void testDeleteDocumentById_WhenDocumentExists_DeletesDocumentSuccessfully() throws DocumentNotFoundException, IOException {

        Document mockDocument = DocumentHelperTest.createMockDocument();
        UUID documentId = mockDocument.getId();
        when(documentRepository.findById(documentId)).thenReturn(Optional.of(mockDocument));

        documentService.deleteDocumentById(documentId);

        verify(documentRepository, times(1)).findById(documentId);
        verify(documentRepository, times(1)).delete(mockDocument);
    }

    @Test
    void testDeleteDocumentByIdWhenDocumentNotFound() {

        UUID documentId = UUID.randomUUID();

        when(documentRepository.findById(documentId)).thenReturn(Optional.empty());

        assertThrows(DocumentNotFoundException.class, () -> {
            documentService.deleteDocumentById(documentId);
        });

        verify(documentRepository, never()).delete(any());
    }

}
