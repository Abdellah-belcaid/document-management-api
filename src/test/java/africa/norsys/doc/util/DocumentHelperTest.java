package africa.norsys.doc.util;

import africa.norsys.doc.entity.Document;
import africa.norsys.doc.entity.DocumentHash;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import static africa.norsys.doc.util.FileUtils.generateFileHash;

public class DocumentHelperTest {

    public static final String BASE_URL = "http://localhost:8080";

    public static MockMultipartFile createMockMultipartFile() {
        return new MockMultipartFile(
                "file",
                "testFile.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );
    }

    public static Document createMockDocument() {
        Map<String, String> metadataMap = new HashMap<>();
        metadataMap.put("size", "1024");

        Document document = new Document();
        document.setId(UUID.randomUUID());
        document.setName("testFile.txt");
        document.setType("text/plain");
        document.setCreationDate(LocalDate.now());
        document.setMetadata(metadataMap);

        return document;
    }


    public static List<Document> createMockDocuments() {
        return Arrays.asList(
                Document.builder()
                        .id(UUID.randomUUID())
                        .name("doc1")
                        .type("pdf")
                        .storageLocation("desktop/url/doc1")
                        .creationDate(LocalDate.now())
                        .build(),
                Document.builder()
                        .id(UUID.randomUUID())
                        .name("doc2")
                        .type("pdf")
                        .storageLocation("desktop/url/doc2")
                        .creationDate(LocalDate.now())
                        .build()
        );
    }

    public static DocumentHash createMockDocumentHash(Document savedDocument, MockMultipartFile file) throws IOException, IOException {
        String fileHash = generateFileHash(file.getInputStream());

        DocumentHash documentHash = new DocumentHash();
        documentHash.setId(UUID.randomUUID());
        documentHash.setDocument(savedDocument);
        documentHash.setHashValue(fileHash);

        return documentHash;
    }

}
