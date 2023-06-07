package at.alladin.rmbt.qosadmin.controller;

import at.alladin.rmbt.qosadmin.service.BaseStationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Controller
public class UploadController {

    @Value("${upload.path}")
    String uploadPath;

    @Value("${upload:false}")
    Boolean uploadEnabled;

    @Autowired
    BaseStationService baseStationService;

    @RequestMapping("/upload")
    public String upload() {
        return "upload";
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public @ResponseBody
    String handleFileUpload(@RequestParam("name") String name, @RequestParam("filetype") String fileType,
                            @RequestParam("file") MultipartFile file) throws Exception {
        if (!file.isEmpty() && uploadEnabled) {
            if (uploadPath == null) {
                throw new Exception("Error: upload path not set.");
            }

            try {
                switch (fileType) {
                    case "excelBaseStation":
                        final long count = baseStationService.importFile(file.getInputStream(), 0);
                        return "Successfully uploaded base station file. " + count + " items have been inserted!";
                    default:
                        break;
                }

                return "You successfully uploaded file: '" + name + "' of type: '" + fileType + "'!";
            } catch (Exception e) {
                throw new Exception("Error: you failed to upload " + name + " => " + e.getMessage());
            }
        } else if (!uploadEnabled) {
            throw new Exception("Error: Upload has been disabled by the admin.");
        } else {
            throw new Exception("Error: you failed to upload " + name + " because the file was empty.");
        }
    }

    /**
     * @param file
     * @param name
     * @throws IOException
     */
    protected void saveToFile(final MultipartFile file, final String name) throws IOException {
        byte[] bytes = file.getBytes();
        BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(uploadPath, name)));
        stream.write(bytes);
        stream.close();
    }
}
