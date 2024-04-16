package africa.norsys.doc.util;

import africa.norsys.doc.entity.Document;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DocumentHelperTest {
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
}
