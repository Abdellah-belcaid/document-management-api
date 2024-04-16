package africa.norsys.doc.controller;

import africa.norsys.doc.entity.Document;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static africa.norsys.doc.constant.PaginationConstants.*;
@WebMvcTest(DocumentController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
public class DocumentControllerTest {
    private static final String EXAM_API_ENDPOINT = "/api/documents";

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
    @DisplayName("Should return all Documents DTOs")
    public void shouldReturnAllDocumentsDTOs() throws Exception {

        Page<Document> mockedPage = new PageImpl<>(mockDocuments);
        when(documentService.getAllDocuments(DEFAULT_PAGE, DEFAULT_PAGE_SIZE, DEFAULT_SORT_DIRECTION, DEFAULT_DOCUMENT_SORT_BY))
                .thenReturn(mockedPage);

        mockMvc.perform(get(EXAM_API_ENDPOINT)
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

        mockMvc.perform(get(EXAM_API_ENDPOINT)
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
        mockMvc.perform(get(EXAM_API_ENDPOINT)
                        .param("page", String.valueOf(-1))
                        .param("size", String.valueOf(0))
                        .param("sortBy", DEFAULT_DOCUMENT_SORT_BY)
                        .param("sortDirection", DEFAULT_SORT_DIRECTION)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

    }
}
