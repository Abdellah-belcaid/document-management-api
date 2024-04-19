package africa.norsys.doc.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static africa.norsys.doc.constant.Constant.FILE_STORAGE_LOCATION;

@Slf4j
public class FileUtils {

    public static Map<String, String> extractMetadata(MultipartFile file, UUID userId) {
        if (file == null) return new HashMap<>();
        Map<String, String> metadataMap = new HashMap<>();
        metadataMap.put("size", String.valueOf(file.getSize()));
        metadataMap.put("extension", (extractFileExtension(file.getOriginalFilename())));
        metadataMap.put("owner", String.valueOf(userId));
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
            return baseUrl + "/api/documents/file/" + filename;
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

    public static String generateFileHash(InputStream inputStream) throws IOException {
        try {
            // Create MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // Create DigestInputStream to read the file and update the MessageDigest
            try (DigestInputStream dis = new DigestInputStream(inputStream, md)) {
                // Read the file content (and update the MessageDigest)
                while (dis.read() != -1) ;
            }

            // Get the hash bytes
            byte[] hashBytes = md.digest();

            // Convert hash bytes to hexadecimal representation
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // Handle NoSuchAlgorithmException
            e.printStackTrace();
            return null;
        }
    }

}
