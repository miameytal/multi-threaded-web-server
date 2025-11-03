# Multi-Threaded Web Server in Java

This project is a simple, multi-threaded HTTP web server built from scratch in Java. It demonstrates a core understanding of network programming, socket communication, and concurrency by handling multiple client requests simultaneously.

The server is capable of serving static files (HTML, CSS, images) and can correctly handle GET requests, responding with appropriate HTTP status codes.

## Key Features

*   **Multi-Threading:** Utilizes a thread pool to efficiently manage and serve multiple client connections concurrently without blocking.
*   **HTTP Request Handling:** Parses incoming GET requests to identify the requested file from the root directory.
*   **MIME Type Support:** Automatically detects the content type (e.g., `text/html`, `image/jpeg`) of the requested file and sets the appropriate `Content-Type` header in the HTTP response.
*   **Robust Error Handling:** Gracefully handles requests for non-existent files by returning a 404 Not Found error page.
*   **Built from Scratch:** Implemented using core Java libraries (`java.net.Socket`, `java.util.concurrent.ExecutorService`) with no external web frameworks.

## Tech Stack

*   **Language:** Java
*   **Core Libraries:** `java.net`, `java.io`, `java.util.concurrent` for threading and networking.

## How to Run the Project

To run this server on your local machine, follow these steps:

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/miameytal/multi-threaded-web-server.git
    cd multi-threaded-web-server
    ```

2.  **Compile the Java source code:**
    ```bash
    javac WebServer.java
    ```

3.  **Run the compiled server:**
    The server requires a port number and a root directory path as command-line arguments. For example, to run it on port 8080 and serve files from a directory named `www`:
    ```bash
    java WebServer 8080 www
    ```
    *(Note: Ensure the `www` directory exists and contains files like `index.html` for testing.)*

4.  **Access the server:**
    Open your web browser and navigate to `http://localhost:8080/index.html` (or the name of any other file you have in your root directory).






