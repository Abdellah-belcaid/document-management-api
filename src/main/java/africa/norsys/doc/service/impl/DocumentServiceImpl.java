package africa.norsys.doc.service.impl;

import africa.norsys.doc.entity.Document;
import africa.norsys.doc.exception.DocumentNotFoundException;
import africa.norsys.doc.repository.DocumentRepository;
import africa.norsys.doc.service.DocumentService;
import africa.norsys.doc.util.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static africa.norsys.doc.constant.Constant.FILE_STORAGE_LOCATION;
import static africa.norsys.doc.util.FileUtils.saveFileAndGenerateUrl;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;


    @Override
    public Document addDocument(MultipartFile file, String baseUrl) throws IOException {

        Document document = Document.builder()
                .name(file.getOriginalFilename())
                .type(file.getContentType())
                .metadata(FileUtils.extractMetadata(file))
                .build();

        // Save the document to the database
        document = documentRepository.save(document);

        try {
            // Generate and set the storage location URL
            String fileUrl = saveFileAndGenerateUrl(document.getId().toString(), file, baseUrl);
            document.setStorageLocation(fileUrl);
        } catch (IOException e) {
            documentRepository.delete(document);
            throw e;
        }
        // Update the document in the database with the storage location URL
        return documentRepository.save(document);
    }


    @Override
    public byte[] getFileBytes(String filename) throws IOException {
        Path filePath = Paths.get(FILE_STORAGE_LOCATION).resolve(filename).normalize();
        return Files.readAllBytes(filePath);
    }


    @Override
    public Page<Document> getAllDocuments(Integer page, Integer pageSize, String sortDirection, String sortBy) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.Direction.fromString(sortDirection), sortBy);
        System.out.println("page" + page + "size" + pageSize + "sortDirection" + sortDirection + "sortBy" + sortBy);
        Page<Document> documents = documentRepository.findAll(pageable);
        if (documents.isEmpty()) throw new DocumentNotFoundException("no document found.");
        return documents;
    }

}
