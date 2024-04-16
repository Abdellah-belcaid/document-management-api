package africa.norsys.doc.controller;

import africa.norsys.doc.entity.Document;
import africa.norsys.doc.service.DocumentService;
import africa.norsys.doc.util.DocumentHelperTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
                .thenThrow(new RuntimeException("Document service failed"));

        mockMvc.perform(multipart(DOCUMENT_API_ENDPOINT)
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.content().string("Failed to add document: Document service failed"));
    }


    @Test
    @DisplayName("should return file when file exists")
    void should_return_file_when_file_exists() throws Exception {
        String filename = "testFile.txt";
        byte[] fileContent = "Hello, World!".getBytes();
        MockMultipartFile file = DocumentHelperTest.createMockMultipartFile();

        when(documentService.getFileBytes(filename)).thenReturn(file.getBytes());

        mockMvc.perform(get(DOCUMENT_API_ENDPOINT + "/" + filename))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().bytes(fileContent));
    }

    @Test
    @DisplayName("should return 404 when file does not exist")
    void should_return_404_when_file_does_not_exist() throws Exception {
        String filename = "nonexistent.txt";

        when(documentService.getFileBytes(filename)).thenThrow(new IOException());

        mockMvc.perform(get(DOCUMENT_API_ENDPOINT + "/" + filename))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }


}
