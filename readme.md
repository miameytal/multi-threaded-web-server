# Multi-Threaded Web Server

A multi-threaded HTTP/1.1 web server implementation in Java that handles concurrent client connections with configurable thread limits and supports multiple HTTP methods.

## Features

### Core Functionality
- **Multi-threaded Architecture**: Handles multiple client connections simultaneously using Java threads
- **Thread Management**: Configurable maximum thread limit with automatic connection rejection when capacity is reached
- **HTTP/1.1 Protocol**: Full HTTP/1.1 compliance with proper request/response handling
- **Multiple HTTP Methods**: 
  - `GET` - Retrieve resources
  - `POST` - Submit form data
  - `HEAD` - Retrieve headers only
  - `TRACE` - Echo back the request for debugging

### Advanced Features
- **Chunked Transfer Encoding**: Supports chunked data transfer for efficient streaming
- **MIME Type Detection**: Automatic content-type detection for various file formats (HTML, CSS, JS, images, documents, audio, video)
- **Image Support**: Native handling of PNG, JPEG, JPG, and GIF formats
- **Path Traversal Protection**: Security mechanism to prevent directory traversal attacks
- **Form Parameter Parsing**: Handles URL-encoded form data with UTF-8 decoding
- **Dynamic Content**: Parameter substitution in HTML files for POST requests
- **Error Handling**: Comprehensive HTTP status code support (200, 400, 404, 500, 501, 503)

### Bonus Feature
- **WebClient**: Custom HTTP client implementation for testing and interacting with the server

## Architecture

The server follows a modular design with clear separation of concerns:

```
WebServer (Main)
├── manageRequest (Thread Handler)
│   ├── Request Validation
│   ├── HTTP Method Routing
│   ├── File Operations
│   └── Response Generation
├── HTTPRequest (Request Parser)
│   ├── Header Parsing
│   ├── MIME Type Mapping
│   └── Parameter Extraction
└── ConfigReader (Configuration)
    └── Server Settings
```

## Prerequisites

- **Java Development Kit (JDK)**: Version 8 or higher
- **Operating System**: Linux, macOS, or Windows with Bash support
- **Terminal/Command Line**: For running compilation and execution scripts

## Installation

1. Clone the repository:
```bash
git clone https://github.com/miameytal/multi-threaded-web-server.git
cd multi-threaded-web-server
```

2. Compile the project:
```bash
chmod +x compile.sh
./compile.sh
```

3. Verify compilation:
```
Compilation successful
```

## Configuration

Edit the `config.ini` file to customize server settings:

```ini
[port]
port = 8080

[root]
root = ~/lab-MiaRoldan-SaritWandam/server_root/

[defaultPage]
defaultPage = index.html

[maxThreads]
maxThreads = 10
```

### Configuration Parameters

| Parameter | Description | Default |
|-----------|-------------|---------|
| `port` | Server listening port | 8080 |
| `root` | Document root directory (supports `~` for home directory) | `~/lab-MiaRoldan-SaritWandam/server_root/` |
| `defaultPage` | Default file served for `/` requests | `index.html` |
| `maxThreads` | Maximum concurrent client connections | 10 |

## Usage

### Starting the Server

```bash
chmod +x run.sh
./run.sh
```

Expected output:
```
Server started and is listening on port 8080
Max Threads: 10
```

### Using the Web Client (Bonus)

Run the included WebClient to test the server:

```bash
cd Sources
java WebClient
```

Follow the prompts to enter your HTTP request. Example:
```
GET /index.html HTTP/1.1
Host: localhost

```
(Press Enter twice to send the request)

### Testing with Browser

Navigate to `http://localhost:8080` in your web browser to access the default page.

## Project Structure

```
multi-threaded-web-server/
├── Sources/
│   ├── WebServer.java          # Main server implementation
│   ├── HTTPRequest.java         # Request parser and MIME handler
│   ├── WebClient.java           # Bonus: HTTP client implementation
│   ├── ConfigReader.java        # Configuration file reader
│   └── *.class                  # Compiled bytecode
├── server_root/
│   ├── index.html               # Default homepage
│   ├── params_info.html         # Form response page
│   ├── favicon.ico              # Site icon
│   └── ...                      # Other web resources
├── config.ini                   # Server configuration
├── compile.sh                   # Compilation script
├── run.sh                       # Server launch script
├── bonus.txt                    # Bonus feature documentation
└── readme.md                    # This file
```

## Implementation Details

### Thread Management
- Each client connection spawns a new thread via the `manageRequest` class
- Thread count is synchronized to prevent race conditions
- Connections are rejected with `503 Service Unavailable` when `maxThreads` is reached
- Automatic thread cleanup using `finally` blocks

### Security Features
- **Path Traversal Protection**: The `cleanFilePath()` method sanitizes file paths by removing `..` segments
- **Request Validation**: Validates HTTP request format before processing
- **Error Boundaries**: Comprehensive try-catch blocks prevent server crashes

### Response Handling
- Automatic Content-Type detection based on file extension
- Separate handlers for text and binary content
- Chunked encoding support with 1KB chunks
- Proper Content-Length headers for non-chunked responses

### Supported MIME Types
- **Text**: HTML, CSS, JavaScript, Plain Text
- **Images**: PNG, JPEG, JPG, GIF, BMP, SVG, ICO
- **Documents**: PDF, Word, Excel, PowerPoint
- **Media**: MP3, MP4, WebM, WAV, OGG
- **Archives**: ZIP, TAR

## Error Handling

| Status Code | Description | Trigger |
|-------------|-------------|---------|
| 200 OK | Successful request | Resource found and returned |
| 400 Bad Request | Malformed request | Invalid HTTP format |
| 404 Not Found | Resource not found | Requested file doesn't exist |
| 500 Internal Server Error | Server error | Exception during processing |
| 501 Not Implemented | Method not supported | Unsupported HTTP method |
| 503 Service Unavailable | Server overloaded | Maximum threads reached |

## Bonus: WebClient

The included `WebClient.java` provides a command-line HTTP client for testing the server:

**Features:**
- Interactive request composition
- Automatic response parsing
- Status code interpretation
- Header and body display

**Usage:**
1. Compile: `javac Sources/WebClient.java`
2. Run: `java -cp Sources WebClient`
3. Enter your HTTP request line by line
4. Press Enter on an empty line to send

## Troubleshooting

**Server won't start:**
- Check if port 8080 is already in use: `lsof -i :8080`
- Verify Java is installed: `java -version`

**Connection refused:**
- Ensure the server is running
- Check firewall settings
- Verify correct port in `config.ini`

**404 errors:**
- Confirm file exists in `server_root` directory
- Check file permissions
- Verify `root` path in `config.ini`

**Compilation errors:**
- Ensure all `.java` files are in the `Sources` directory
- Check JDK version compatibility
