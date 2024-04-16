package africa.norsys.doc.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static africa.norsys.doc.constant.Constant.FILE_STORAGE_LOCATION;

@Slf4j
public class FileUtils {

    public static Map<String, String> extractMetadata(MultipartFile file) {
        if (file == null) return new HashMap<>();
        Map<String, String> metadataMap = new HashMap<>();
        metadataMap.put("size", String.valueOf(file.getSize()));
        return metadataMap;
    }


    public static String saveFileAndGenerateUrl(String id, MultipartFile file, String baseUrl) throws IOException {
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


    public static String extractFileExtension(String filename) {
        return Optional.of(filename)
                .filter(name -> name.contains("."))
                .map(name -> "." + name.substring(filename.lastIndexOf(".") + 1))
                .orElse("");
    }


}
