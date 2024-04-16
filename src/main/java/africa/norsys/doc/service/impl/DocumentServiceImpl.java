package africa.norsys.doc.service.impl;

import africa.norsys.doc.entity.Document;
import africa.norsys.doc.repository.DocumentRepository;
import africa.norsys.doc.service.DocumentService;
import africa.norsys.doc.util.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import static africa.norsys.doc.constant.Constant.FILE_STORAGE_LOCATION;

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


    public String saveFileAndGenerateUrl(String id, MultipartFile file, String baseUrl) throws IOException {
        String filename = id + extractFileExtension(file.getOriginalFilename());

        try {
            Path fileStorageLocation = Paths.get(FILE_STORAGE_LOCATION).toAbsolutePath().normalize();
            if (!Files.exists(fileStorageLocation)) {
                Files.createDirectories(fileStorageLocation);
            }

            // Save the file
            Files.copy(file.getInputStream(),
                    fileStorageLocation.resolve(filename), StandardCopyOption.REPLACE_EXISTING);

            // Generate and return the file URL
            return baseUrl + "/api/documents/" + filename;
        } catch (Exception e) {
            log.error("Unable to save file for id: {}, filename: {}", id, filename, e);
            throw new IOException("Unable to save file", e);
        }
    }


    private String extractFileExtension(String filename) {
        return Optional.of(filename)
                .filter(name -> name.contains("."))
                .map(name -> "." + name.substring(filename.lastIndexOf(".") + 1))
                .orElse("");
    }


}
