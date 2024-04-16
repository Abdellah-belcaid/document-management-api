
package africa.norsys.doc.controller;

import africa.norsys.doc.entity.Document;
import africa.norsys.doc.exception.DocumentNotAddedException;
import africa.norsys.doc.exception.DocumentNotFoundException;
import africa.norsys.doc.exception.FileNotFoundException;
import africa.norsys.doc.service.DocumentService;
import africa.norsys.doc.util.DocumentHelperTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static africa.norsys.doc.constant.PaginationConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

@WebMvcTest(DocumentController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {
    private static final String DOCUMENT_API_ENDPOINT = "/api/documents";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;


    private List<Document> mockDocuments;

    @BeforeEach
    public void setUp() {
        mockDocuments = DocumentHelperTest.createMockDocuments();
    }

    @Test
    @DisplayName("should upload document successfully")
    void should_upload_document_successfully() throws Exception {

        Document mockDocument = DocumentHelperTest.createMockDocument();
        MockMultipartFile file = DocumentHelperTest.createMockMultipartFile();

        when(documentService.addDocument(any(MultipartFile.class), any(String.class))).thenReturn(mockDocument);

        mockMvc.perform(multipart(DOCUMENT_API_ENDPOINT)
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(mockDocument.getId().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(mockDocument.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.type").value(mockDocument.getType()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.creationDate").value(mockDocument.getCreationDate().toString()));
    }


    @Test
    @DisplayName("should handle exception when adding document")
    void should_handle_exception_when_adding_document() throws Exception {
        MockMultipartFile file = DocumentHelperTest.createMockMultipartFile();

        when(documentService.addDocument(any(MultipartFile.class), any(String.class)))
                .thenThrow(new DocumentNotAddedException("Document could not be added"));

        mockMvc.perform(multipart(DOCUMENT_API_ENDPOINT)
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.content().string("Document could not be added"));
    }



    @Test
    @DisplayName("should return file when file exists")
    void should_return_file_when_file_exists() throws Exception {
        String filename = "testFile.txt";
        byte[] fileContent = "Hello, World!".getBytes();
        MockMultipartFile file = DocumentHelperTest.createMockMultipartFile();

        when(documentService.getFileBytes(filename)).thenReturn(file.getBytes());

        mockMvc.perform(get(DOCUMENT_API_ENDPOINT + "/file/" + filename))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().bytes(fileContent));
    }

    @Test
    @DisplayName("should return 404 when file does not exist")
    void should_return_404_when_file_does_not_exist() throws Exception {
        String filename = "nonexistent.txt";

        when(documentService.getFileBytes(filename)).thenThrow(new FileNotFoundException("File '" + filename + "' not found"));

        mockMvc.perform(get(DOCUMENT_API_ENDPOINT + "/file/" + filename))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }


    @Test
    @DisplayName("Should return all Documents DTOs")
    public void shouldReturnAllDocumentsDTOs() throws Exception {

        Page<Document> mockedPage = new PageImpl<>(mockDocuments);
        when(documentService.getAllDocuments(DEFAULT_PAGE, DEFAULT_PAGE_SIZE, DEFAULT_SORT_DIRECTION, DEFAULT_DOCUMENT_SORT_BY))
                .thenReturn(mockedPage);

        mockMvc.perform(get(DOCUMENT_API_ENDPOINT)
                        .param("page", String.valueOf(DEFAULT_PAGE))
                        .param("size", String.valueOf(DEFAULT_PAGE_SIZE))
                        .param("sortBy", DEFAULT_DOCUMENT_SORT_BY)
                        .param("sortDirection", DEFAULT_SORT_DIRECTION)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name").value("doc1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].name").value("doc2"));
    }

    @Test
    @DisplayName("Should return no content when documents are empty")
    public void shouldReturnNoContentWhenDocumentsEmpty() throws Exception {

        List<Document> emptyExamsDTO = Collections.emptyList();
        Page<Document> emptyPage = new PageImpl<>(emptyExamsDTO);

        when(documentService.getAllDocuments(DEFAULT_PAGE, DEFAULT_PAGE_SIZE, DEFAULT_SORT_DIRECTION, DEFAULT_DOCUMENT_SORT_BY))
                .thenReturn(emptyPage);

        mockMvc.perform(get(DOCUMENT_API_ENDPOINT)
                        .param("page", String.valueOf(DEFAULT_PAGE))
                        .param("size", String.valueOf(DEFAULT_PAGE_SIZE))
                        .param("sortBy", DEFAULT_DOCUMENT_SORT_BY)
                        .param("sortDirection", DEFAULT_SORT_DIRECTION)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @DisplayName("Should return bad request when page or size is invalid")
    public void shouldReturnBadRequestWhenPageOrSizeInvalid() throws Exception {
        mockMvc.perform(get(DOCUMENT_API_ENDPOINT)
                        .param("page", String.valueOf(-1))
                        .param("size", String.valueOf(0))
                        .param("sortBy", DEFAULT_DOCUMENT_SORT_BY)
                        .param("sortDirection", DEFAULT_SORT_DIRECTION)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

    }

    @Test
    @DisplayName("Should return document by id")
    void should_return_document_by_id() throws Exception {

        UUID id = UUID.randomUUID();
        Document mockDocument = DocumentHelperTest.createMockDocument();
        mockDocument.setId(id);

        when(documentService.getDocumentById(id)).thenReturn(Optional.of(mockDocument));

        mockMvc.perform(get(DOCUMENT_API_ENDPOINT + "/" + id))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(mockDocument.getId().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(mockDocument.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.type").value(mockDocument.getType()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.creationDate").value(mockDocument.getCreationDate().toString()));
    }

    @Test
    @DisplayName("Should return 404 with custom message when document with given id does not exist")
    void should_return_404_with_custom_message_when_document_with_given_id_does_not_exist() throws Exception {

        UUID id = UUID.randomUUID();
        String expectedErrorMessage = "Document with id " + id + " not found";

        when(documentService.getDocumentById(id)).thenThrow(new DocumentNotFoundException(expectedErrorMessage));


        mockMvc.perform(get(DOCUMENT_API_ENDPOINT + "/" + id))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string(expectedErrorMessage));
    }


    @Test
    @DisplayName("Should search documents by keyword and date")
    void should_search_documents_by_keyword_and_date() throws Exception {

        String keyword = "test";
        String date = "2024-04-16";
        List<Document> expectedDocuments = Collections.singletonList(DocumentHelperTest.createMockDocument());

        when(documentService.searchByKeyword(keyword, date)).thenReturn(expectedDocuments);

        mockMvc.perform(get(DOCUMENT_API_ENDPOINT + "/search")
                        .param("keyword", keyword)
                        .param("date", date)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(expectedDocuments.size()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(expectedDocuments.get(0).getName()));
    }
    @Test
    @DisplayName("Should handle DocumentNotFoundException when no documents are found")
    void should_handle_document_not_found_exception_when_no_documents_found() throws Exception {
        String keyword = "test";
        String date = "2024-04-16";

        when(documentService.searchByKeyword(keyword, date)).thenThrow(new DocumentNotFoundException("No documents found"));

        mockMvc.perform(get(DOCUMENT_API_ENDPOINT + "/search")
                        .param("keyword", keyword)
                        .param("date", date)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string("No documents found"));
    }


}

