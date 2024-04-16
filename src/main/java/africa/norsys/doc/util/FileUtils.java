package africa.norsys.doc.util;

import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

public class FileUtils {

    public static Map<String, String> extractMetadata(MultipartFile file) {
        if (file == null) return new HashMap<>();
        Map<String, String> metadataMap = new HashMap<>();
        metadataMap.put("size", String.valueOf(file.getSize()));
        return metadataMap;
    }


}
