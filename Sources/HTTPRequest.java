import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HTTPRequest {
    String type;
    String requestedData;
    String pageExt;
    boolean isImage;
    int contentLength;
    String chunked;
    String userAgent;
    HashMap<String, String> parameters;
    String contentType;
    String request;
    private static final Map<String, String> MIME_TYPES;

    public HTTPRequest(String request) {
        this.request = request;
        String[] lines = request.split("\\r?\\n");
        String[] firstLineParts = lines[0].split("\\s+");
        this.type = firstLineParts[0];
        this.requestedData = firstLineParts[1];
        this.isImage = checkIfImage(requestedData); 
        this.chunked = getHeaderValue(lines, "chunked:");
        this.userAgent = getHeaderValue(lines, "User-Agent:");
        this.parameters = extractParameters(request);
        this.contentType = getContentType(requestedData);
        this.pageExt = getPageExt(requestedData);

        String contentLengthHeader = getHeaderValue(lines, "Content-Length:");
        this.contentLength = contentLengthHeader != null ? Integer.parseInt(contentLengthHeader) : -1;

    }

    private String getPageExt(String page){
        String[] parts = page.split("\\.");
        String extension = parts[parts.length - 1].toLowerCase();
        return extension;

    }

    private boolean checkIfImage(String page) {
        String[] parts = page.split("\\.");
        String extension = parts[parts.length - 1].toLowerCase();
        return extension.equals("jpg") || extension.equals("bmp") || extension.equals("gif") || extension.equals("png")|| extension.equals("jpeg");
    }

    private String getHeaderValue(String[] lines, String headerName) {
        for (String line : lines) {
            if (line.startsWith(headerName)) {
                return line.substring(headerName.length()).trim();
            }
        }
        return null;
    }

    private HashMap<String, String> extractParameters(String requestHeader) {
        HashMap<String, String> params = new HashMap<>();

        return params;
    }


    private byte[] readFile(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data);
        fis.close();
        return data;
    }

    public String getType() {
        return type;
    }

    public String getRequestedPage() {
        return requestedData;
    }

    public boolean isImage() {
        return isImage;
    }

    public int getContentLength() {
        return contentLength;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public HashMap<String, String> getParameters() {
        return parameters;
    }


    public byte[] generateResponse(File requestedFile) throws IOException {
        if (requestedFile.exists()) {
            return readFile(requestedFile);
        } else {

            return null;
        }
    }

    static {
        MIME_TYPES = new HashMap<>();

        MIME_TYPES.put("ico", "image/x-icon");
        MIME_TYPES.put("txt", "text/plain");
        MIME_TYPES.put("html", "text/html");
        MIME_TYPES.put("htm", "text/html");
        MIME_TYPES.put("css", "text/css");
        MIME_TYPES.put("js", "application/javascript");

        // Image files
        MIME_TYPES.put("jpg", "image/jpg");
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("gif", "image/gif");
        MIME_TYPES.put("bmp", "image/bmp");
        MIME_TYPES.put("svg", "image/svg+xml");

        // Audio files
        MIME_TYPES.put("mp3", "audio/mpeg");
        MIME_TYPES.put("ogg", "audio/ogg");
        MIME_TYPES.put("wav", "audio/wav");

        // Video files
        MIME_TYPES.put("mp4", "video/mp4");
        MIME_TYPES.put("webm", "video/webm");
        MIME_TYPES.put("mov", "video/quicktime");

        // Document files
        MIME_TYPES.put("pdf", "application/pdf");
        MIME_TYPES.put("doc", "application/msword");
        MIME_TYPES.put("docx", "application/msword");
        MIME_TYPES.put("xls", "application/vnd.ms-excel");
        MIME_TYPES.put("xlsx", "application/vnd.ms-excel");
        MIME_TYPES.put("ppt", "application/vnd.ms-powerpoint");
        MIME_TYPES.put("pptx", "application/vnd.ms-powerpoint");

        // Archive files
        MIME_TYPES.put("zip", "application/zip");
        MIME_TYPES.put("tar", "application/x-tar");

    }

    public static String getContentType(String filename) {
        String extension = getFileExtension(filename);
        String contentType = MIME_TYPES.get(extension);
        if (contentType != null) {
            return contentType;
        } else {
            return "application/octet-stream"; // Default value
        }
    }


    private static String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }
}
