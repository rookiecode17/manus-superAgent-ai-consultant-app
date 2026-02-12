package com.project.manussearchmcpserver.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ImageSearchTool {

    // Replace with your Pexels API key (required from the official website)
    private static final String API_KEY = "";

    // Pexels General search interface (please refer to the documentation)
    private static final String API_URL = "https://api.pexels.com/v1/search";

    @Tool(description = "search image from web")
    public String searchImage(@ToolParam(description = "Search query keyword") String query) {
        try {
            return String.join(",", searchMediumImages(query));
        } catch (Exception e) {
            return "Error search image: " + e.getMessage();
        }
    }

    /**
     * Search for a list of medium sized images
     *
     * @param query
     * @return
     */
    public List<String> searchMediumImages(String query) {
        // Set the request header (contains the API key)
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", API_KEY);

        // Set request parameters (only query is included, and parameters such as page and per_page can be supplemented according to the document)
        Map<String, Object> params = new HashMap<>();
        params.put("query", query);

        // Send a GET request
        String response = HttpUtil.createGet(API_URL)
                .addHeaders(headers)
                .form(params)
                .execute()
                .body();

        // Parse response JSON (assuming the response structure contains an array of "photos" and each element contains a "medium" field)
        return JSONUtil.parseObj(response)
                .getJSONArray("photos")
                .stream()
                .map(photoObj -> (JSONObject) photoObj)
                .map(photoObj -> photoObj.getJSONObject("src"))
                .map(photo -> photo.getStr("medium"))
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
    }
}
