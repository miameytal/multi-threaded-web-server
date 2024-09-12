

WebServer
   - This class represents the main entry point of the web server.
   - It initializes the server socket, reads configuration parameters from the `config.ini` file using the `ConfigReader` class, and starts listening for incoming connections.
   - It manages the incoming connections by creating a dedicated `manageRequest` thread for each client connection.

manageRequest 
   - This inner class extends `Thread` and represents a handler for each client request.
   - It reads the incoming HTTP request, parses it, and determines the appropriate action to take based on the request type (GET, POST, HEAD, TRACE).
   - It assigns the request handling to methods like `handleGET`, `handlePOST`, `handleHEAD`, and `handleTRACE` depending on the request type.
   - It manages the threading aspects of the server by limiting the number of concurrent threads to a configurable maximum value.
   - It generates appropriate HTTP responses based on the request and sends them back to the client.

HTTPRequest
   - This class represents an HTTP request and is responsible for parsing the request headers and extracting relevant information.
   - It extracts information such as request type, requested page, content length, user agent, parameters, and content type.

ConfigReader
   - This class reads configuration parameters from the `config.ini` file.
   - It has methods to read port number, root directory, default page, and maximum threads from the configuration file.

Design
- The server follows a multithreaded architecture to handle multiple client connections concurrently, with each connection being managed by a separate thread.
- Configuration parameters such as port number, root directory, default page, and maximum threads are externalized to a configuration file (`config.ini`) for easy customization and maintenance.
- The server uses simple file I/O operations for reading configuration parameters and serving requested files.
- HTTP request handling is implemented using string parsing techniques.

