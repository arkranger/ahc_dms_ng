package ahc.dms.utils;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService {

    public String uploadImage(String path, MultipartFile file) throws IOException {

        String name = file.getOriginalFilename();
        //random name generation
        String randomId = UUID.randomUUID().toString();
        assert name != null;
        String fileName = randomId.concat(name.substring(name.lastIndexOf(".")));
        String filePath = path + File.separator + fileName;
        //create folder if not created
        File f = new File(path);
        if (!f.exists()) {
            f.mkdir();
        }
        //file copy
        Files.copy(file.getInputStream(), Paths.get(filePath));

        return fileName;
    }

    public InputStream getResource(String path, String fileName) throws FileNotFoundException {
        String fullPath = path + File.separator + fileName;
        InputStream is = new FileInputStream(fullPath);
        //db logic to return input stream
        return is;
    }

}
