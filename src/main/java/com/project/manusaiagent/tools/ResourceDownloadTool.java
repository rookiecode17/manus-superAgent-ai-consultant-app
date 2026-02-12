package com.project.manusaiagent.tools;




import com.project.manusaiagent.constant.FileConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Resource download tool
 */
public class ResourceDownloadTool {

    private static final Logger log = LoggerFactory.getLogger(ResourceDownloadTool.class);
    @Tool(description = "Download a resource from a given URL")
    public String downloadResource(@ToolParam(description = "URL of the resource to download") String url, @ToolParam(description = "Name of the file to save the downloaded resource") String fileName) {
//        String fileDir = FileConstant.FILE_SAVE_DIR + "/download";
//        String filePath = fileDir + "/" + fileName;
//        try {
//            // Create a catalog
//            FileUtil.mkdir(fileDir);
//            //Use Hutool's downloadFile method to download the resource
//            HttpUtil.downloadFile(url, new File(filePath));
//            return "Resource downloaded successfully to: " + filePath;
//        } catch (Exception e) {
//            return "Error downloading resource: " + e.getMessage()
//        }
        log.info("downloadResource called: url={}, fileName={}", url, fileName);
        String fileDir = FileConstant.FILE_SAVE_DIR + "/download";

        try {
            FileUtil.mkdir(fileDir);

            // Only allow http/https
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                return "Only HTTP/HTTPS URLs are allowed.";
            }

            Path baseDir = Paths.get(fileDir);
            Path filePath = baseDir.resolve(fileName).normalize();
            if (!filePath.startsWith(baseDir)) {
                return "Invalid file name.";
            }

            HttpResponse resp = HttpRequest.get(url)
                    .setFollowRedirects(true)
                    .timeout(15000)
                    .execute();

            String ct = resp.header("Content-Type");
            if (ct == null || !ct.toLowerCase().startsWith("image/")) {
                // Returns the first 200 characters so you can see exactly what was downloaded (usually HTML)
                String body = resp.body();
                String preview = body == null ? "" : body.substring(0, Math.min(200, body.length()));
                return "URL did not return an image. Content-Type=" + ct + ", preview=" + preview;
            }

            // write documents
            FileUtil.writeBytes(resp.bodyBytes(), filePath.toFile());

            return "Image downloaded to: " + filePath + " (Content-Type=" + ct + ")";

        } catch (Exception e) {
            return "Error downloading resource: " + e.getMessage();
        }
    }
}
