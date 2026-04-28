package com.timetable.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class OllamaClient {

    private final String apiUrl;
    private final String model;

    public OllamaClient(String apiUrl, String model) {
        this.apiUrl = apiUrl;
        this.model = model;
    }

    public String generate(String prompt) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(120000);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");

        String payload = "{\"model\":\"" + escapeJson(model)
                + "\",\"prompt\":\"" + escapeJson(prompt)
                + "\",\"stream\":false}";

        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(payload.getBytes(StandardCharsets.UTF_8));
        }

        int status = connection.getResponseCode();
        InputStream stream = status >= 200 && status < 300 ? connection.getInputStream() : connection.getErrorStream();
        String responseJson = readStream(stream);

        if (status < 200 || status >= 300) {
            String errorMessage = extractJsonStringValue(responseJson, "error");
            if (errorMessage == null || errorMessage.trim().isEmpty()) {
                errorMessage = responseJson;
            }
            throw new IOException("Ollama API error (" + status + "): " + errorMessage);
        }

        String responseText = extractJsonStringValue(responseJson, "response");
        if (responseText == null) {
            throw new IOException("Unexpected Ollama response: missing 'response' field.");
        }

        return responseText.trim();
    }

    private String readStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        }
        return content.toString();
    }

    private String extractJsonStringValue(String json, String key) {
        if (json == null || key == null) {
            return null;
        }

        String token = "\"" + key + "\"";
        int keyIndex = json.indexOf(token);
        if (keyIndex < 0) {
            return null;
        }

        int colonIndex = json.indexOf(':', keyIndex + token.length());
        if (colonIndex < 0) {
            return null;
        }

        int quoteStart = json.indexOf('"', colonIndex + 1);
        if (quoteStart < 0) {
            return null;
        }

        StringBuilder value = new StringBuilder();
        boolean escaped = false;

        for (int i = quoteStart + 1; i < json.length(); i++) {
            char ch = json.charAt(i);
            if (escaped) {
                switch (ch) {
                case '"':
                    value.append('"');
                    break;
                case '\\':
                    value.append('\\');
                    break;
                case '/':
                    value.append('/');
                    break;
                case 'b':
                    value.append('\b');
                    break;
                case 'f':
                    value.append('\f');
                    break;
                case 'n':
                    value.append('\n');
                    break;
                case 'r':
                    value.append('\r');
                    break;
                case 't':
                    value.append('\t');
                    break;
                case 'u':
                    if (i + 4 < json.length()) {
                        String hex = json.substring(i + 1, i + 5);
                        try {
                            value.append((char) Integer.parseInt(hex, 16));
                            i += 4;
                        } catch (NumberFormatException ex) {
                            value.append("\\u").append(hex);
                            i += 4;
                        }
                    } else {
                        value.append("\\u");
                    }
                    break;
                default:
                    value.append(ch);
                    break;
                }
                escaped = false;
                continue;
            }

            if (ch == '\\') {
                escaped = true;
            } else if (ch == '"') {
                return value.toString();
            } else {
                value.append(ch);
            }
        }

        return null;
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
            case '"':
                escaped.append("\\\"");
                break;
            case '\\':
                escaped.append("\\\\");
                break;
            case '\b':
                escaped.append("\\b");
                break;
            case '\f':
                escaped.append("\\f");
                break;
            case '\n':
                escaped.append("\\n");
                break;
            case '\r':
                escaped.append("\\r");
                break;
            case '\t':
                escaped.append("\\t");
                break;
            default:
                if (ch < 0x20) {
                    escaped.append(String.format("\\u%04x", Integer.valueOf(ch)));
                } else {
                    escaped.append(ch);
                }
                break;
            }
        }
        return escaped.toString();
    }
}
