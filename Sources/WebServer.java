import java.io.*; // importing input/output package for receiving and responding to the request.
import java.net.*; // importing .net package for socket connection.
import java.util.HashMap;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Stack;




public class WebServer {

	ServerSocket Serversocket;
	int port;
	String root;
    String defPage;
    int maxThreads;
	int threadCount = 0;

	public static void main(String[] args) throws Exception {

		try {
			//Start the server
			new WebServer().startServer(); 
		} catch (Exception e) {
			System.out.println("There was an internal error, server couldn't start. Error Message: " + e.getMessage());
		}
		 
	}

	public void initialize() {
		port = ConfigReader.readPortNumber();
		root = ConfigReader.readRootDirectory().replace("~", System.getProperty("user.home"));
		defPage = ConfigReader.readDefaultPage();
		maxThreads = ConfigReader.readMaxThreads();
	}

	public synchronized void incrementThreadCount() {
        threadCount++;
    }

    public synchronized void decrementThreadCount() {
        threadCount--;
    }

	public void startServer() throws Exception {

		initialize();
		
    	/* TRACE */ System.out.println("Server started and is listening on port " + port);
		/* TRACE */ System.out.println("Max Threads: " + maxThreads);

		//Create socket at port
		Serversocket = new ServerSocket(port);
		
		while (true) { 

			//Wait for a connection
			Socket s = Serversocket.accept();
			manageRequest mr = null;

			try {
				//Dedicated class for each connection
				mr = new manageRequest(s);

				if (threadCount < maxThreads){
					//Start the thread.
					mr.start(); 

				} else {
					/* TRACE */ System.out.println("A client tried to connect, but the current number of threads are at maxium capacity. the connection was rejected.");

					//Inform Client that Max Threads was reached
					mr.sendResponse(503, "Service Unavailable", "", 0, null);
					// Reject new connections by closing the socket immediately
					s.close();
				}
			} catch (Exception e){
				if (mr != null){
					try {
						mr.sendResponse(500, "Internal Server Error", "", 0, null);
					} catch (Exception e1) {}
					
				}
				s.close();
			}
			
		}

	}

	
	///// client handler class
	public class manageRequest extends Thread {

		Socket s;
		BufferedReader brfr;
		OutputStream os;
		String httpresponse; 
		String message = "";
		byte[] messageBytes;
		
		public manageRequest(Socket s) throws Exception {
			this.s = s;
			brfr = new BufferedReader(new InputStreamReader(s.getInputStream())); 
			os = s.getOutputStream();

		}


		@Override
		public void run() {
			try {
				incrementThreadCount();
				boolean brfrReady = false;
				/* TRACE */ System.out.println("New Client Connected. Total Threads = " + threadCount);
				while (!brfrReady){
					if (brfr.ready()){
						brfrReady = true;
					}
				}
				
				StringBuilder requestBuilder = new StringBuilder();
				String line;

				// Read request line by line until an empty line is encountered
				while ((line = brfr.readLine()) != null && !line.isEmpty()) {
					requestBuilder.append(line).append("\n");
				}
				String request = requestBuilder.toString();
				/* TRACE */ System.out.println("********************************************\nTHE REQUEST HEADER FROM THE CLIENT\n********************************************\n\n" + request + "\n********************************************\nEND \n********************************************\n\n");

				// Check if the request format is valid
				if (!isRequestFormatValid(request)) {
					sendResponse(400, "Bad Request", "", 0, null);
					brfr.close(); // closing the buffered reader.
					s.close(); //closing the socket.
				} else {
					//Send the response to HTTPRequest class, responsible for parsing the request.
					HTTPRequest httpReq = new HTTPRequest(request);

					switch (httpReq.type) {
						case "GET":
							handleGET(httpReq, os);
							break;
						case "POST":
							handlePOST(httpReq, os);
							break;
						case "HEAD":
							handleHEAD(httpReq, os);
							break;
						case "TRACE":
							handleTRACE(httpReq, os);
							break;
						default:
							sendResponse(501, "Not Implemented", "", 0, null);
							brfr.close(); // closing the buffered reader.
							s.close(); //closing the socket.
							break;
				}
				}
				
			} catch (Exception e) {
				try {
					sendResponse(500, "Internal Server Error", "", 0, null);
					s.close();
				} catch (Exception e1) {}
			} finally {
				decrementThreadCount();
				/*TRACE*/ System.out.println("A client disconnected. Total Threads = " + threadCount);
			}
		}

		private void handleTRACE(HTTPRequest httpReq, OutputStream os) throws Exception{

			byte[] reqBytes = httpReq.request.getBytes(StandardCharsets.UTF_8);
			sendResponse(200, "OK", httpReq.request , reqBytes.length, httpReq);
			brfr.close(); // closing the buffered reader.
			s.close(); //closing the socket.

		}

		private boolean isRequestFormatValid(String request) {

			String[] lines = request.split("\\n"); // Split by newline character
	
			// Check if request has at least a request line
			if (lines.length < 1) {
				return false;
			}
	
			// Check request line format
			String[] requestLine = lines[0].split(" ");
			if (requestLine.length != 3) {
				return false;
			}
	
			// Check if request has header lines and validate their format
			//int entityBodyIndex = lines.length; // Initialize to end of lines
			for (int i = 1; i < lines.length; i++) {
				String line = lines[i].trim();
				// Check for the presence of entity body
				if (line.isEmpty()) {
					//entityBodyIndex = i;
					break;
				}
				// Check header line format
				if (!line.contains(":")) {
					return false; // Header lines should contain at least one colon
				}
			}
	
			return true;
		}

		private void handleHEAD(HTTPRequest httpReq, OutputStream os) throws Exception{
			// Handle case where no file is requested - Send default page
			if (httpReq.requestedData.equals("/")) {
				httpReq.requestedData = "index.html"; 
				httpReq.contentType = "text/html";
			} else if (httpReq.requestedData.startsWith("/")){
				httpReq.requestedData = httpReq.requestedData.substring(1);
			}

			/*TRACE*/ System.out.println( "********************************************\nTHE REQUESTED PAGE FROM THE CLIENT \n********************************************\n\n"+ httpReq.requestedData + "\n********************************************\nEND \n********************************************\n\n"); 

			String filePath = root + httpReq.requestedData;
			String cleanFile = cleanFilePath(filePath);
			File f = new File(cleanFile);
			if (!f.exists() || f.isDirectory()) {
				sendResponse(404, "Not Found", "", 0, null);
            } else {
				sendResponse(200, "OK", "", f.length(), httpReq);
			}

			brfr.close(); // closing the buffered reader.
			s.close(); //closing the socket.
			


		}

		private String cleanFilePath(String filePath) {
			String[] segments = filePath.split("/");
			Stack<String> stack = new Stack<>();
		
			for (String segment : segments) {
				if (!segment.equals("..")){
					stack.push(segment);
				}
			}
		
			StringBuilder clean = new StringBuilder();
			for (String segment : stack) {
				clean.append(segment).append("/");
			}
		
			// Remove the trailing separator
			if (clean.length() > 0) {
				clean.setLength(clean.length() - 1);
			}
		
			return clean.toString();
		}

		private void handleGET(HTTPRequest httpReq, OutputStream os) throws Exception {


			// Handle case where no file is requested - Send default page
			if (httpReq.requestedData.equals("/")) {
				httpReq.requestedData = "index.html"; 
				httpReq.contentType = "text/html";
			} else if (httpReq.requestedData.startsWith("/")){
				httpReq.requestedData = httpReq.requestedData.substring(1);
			}

			/*TRACE*/ System.out.println( "********************************************\nTHE REQUESTED PAGE FROM THE CLIENT \n********************************************\n\n"+ httpReq.requestedData + "\n********************************************\nEND \n********************************************\n\n"); 

			//Store the parameters (if any) in the request's class
			httpReq.parameters = parseBody(httpReq);

			String filePath = root + httpReq.requestedData;
			String cleanFile = cleanFilePath(filePath);
			File f = new File(cleanFile);

			if (!f.exists() || f.isDirectory()) {
				sendResponse(404, "Not Found", "", 0, null);
            } else if (httpReq.pageExt.equals("png") || httpReq.pageExt.equals("jpeg") || httpReq.pageExt.equals("jpg") || httpReq.pageExt.equals("gif")){
				messageBytes = readImgFile(f, httpReq);
				sendImgResponse(200, "OK", messageBytes, f.length(), httpReq);
			} else {
				message = readFile(f, httpReq);
				sendResponse(200, "OK", message, f.length(), httpReq);
			}

			brfr.close(); // closing the buffered reader.
			s.close(); //closing the socket.
			


		}

		private String sendImgResponse(int statusCode, String statusText, byte[] message, long reqDataLength, HTTPRequest httpReq) throws Exception {

			StringBuilder responseBuilder = new StringBuilder();
			String responseHeader = "";

			// Start building the response line
			responseBuilder.append("HTTP/1.1 ").append(statusCode).append(" ").append(statusText).append("\r\n");

			if (httpReq != null) {
				responseBuilder.append("Content-Type: ").append(httpReq.contentType).append("\r\n");
			}
			if (message != null && httpReq != null && httpReq.chunked != null && httpReq.chunked.equals("yes")) {
				responseBuilder.append("Transfer-Encoding: chunked\r\n");
				responseBuilder.append("\r\n");
				responseHeader = responseBuilder.toString();
				

				int offset = 0;

				// Chunk the message data 
				String chunk = "";
				while (offset < message.length) {
					int length = Math.min(1024, message.length - offset);
					//chunk size
					responseBuilder.append(Integer.toHexString(length)).append("\r\n"); 
					//chunk data
					chunk = new String(Arrays.copyOfRange(message, offset, offset + length));
					responseBuilder.append(chunk).append("\r\n");
					offset += length;
					
				}

				// Add the last chunk indicating end of data
        		responseBuilder.append("0\r\n\r\n");
				os.write(responseBuilder.toString().getBytes());
				os.flush();
		
			} else if (message != null) {
				responseBuilder.append("Content-Length: ").append(reqDataLength).append("\r\n");
				responseHeader = responseBuilder.append("\r\n").toString();
				os.write(responseBuilder.toString().getBytes());
				os.write(message);
				os.flush();
			}



			/*TRACE*/ System.out.println("********************************************\nTHE RESPONSE HEADER SENT TO THE CLIENT\n********************************************\n\n");  //print statement
			/*TRACE*/ System.out.println(responseHeader); 			
			/*TRACE*/ System.out.println("********************************************\nEND\n********************************************\n\n");  //print statement
			return responseBuilder.toString();
		}

		@SuppressWarnings("finally")
		private byte[] readImgFile(File f, HTTPRequest httpReq) {
			byte[] buffer = new byte[(int) f.length()];
			
			try (FileInputStream fis = new FileInputStream(f);) {
				fis.read(buffer);
				return buffer;
			} catch (FileNotFoundException e) {
				httpresponse=httpresponse.replace("200 OK", "404 File Not Found"); // handling the file not found exception
				return null;
			} catch (Exception e) {
				httpresponse=httpresponse.replace("200 OK", "500 Error! "); // handling the bad request exception
				return null;
			} finally {
				return buffer;
			}
			
		}

		private HashMap<String,String> parseBody(HTTPRequest httpReq) throws Exception {
			StringBuilder bodyBuilder = new StringBuilder();
			HashMap<String, String> paramsHM = new HashMap<>();
			// Parse the form data
			if (httpReq.contentLength > 0 && brfr.ready()){

				//get the body of the request
				for (int i = 0; i < httpReq.contentLength; i++){
					char c = (char) brfr.read();
					bodyBuilder.append(c);
				}
				String body = bodyBuilder.toString().trim();

				//Get the paramater values
				String[] params = body.split("&");
				for (String param : params) {
					String[] keyAndVal = param.split("=");
					String key = keyAndVal[0];
					String val = keyAndVal.length > 1 ? java.net.URLDecoder.decode(keyAndVal[1], "UTF-8") : "";
					paramsHM.put(key, val);
				}
			}

			return paramsHM;

		}

		private void handlePOST(HTTPRequest httpReq, OutputStream os) throws Exception {
			
			httpReq.parameters = parseBody(httpReq);
			long length = 0;

			if (httpReq.parameters != null && httpReq.parameters.containsKey("submitted") && httpReq.parameters.get("submitted").equals("true") && httpReq.requestedData.equals("/params_info.html")) {
				String csStatus = "No"; 
				String likeClassesMessage = "";
				if (httpReq.parameters.containsKey("CS")){
					csStatus = "Yes";
				}
				if (httpReq.parameters.containsKey("message")){
					likeClassesMessage = httpReq.parameters.get("message");
				}

				String filePath = root + httpReq.requestedData;
				String cleanFile = cleanFilePath(filePath);
				File f = new File(cleanFile);
				//Update the param_info.html file with the updated parameter values , then send
				message = readFile(f, httpReq);
				message = message.replace("csStatusTarget", csStatus);
				message = message.replace("likeClassesMessageTarget", likeClassesMessage);
				length = f.length();
			} 
			//otherwise message = "", as defined when the class was instantiated

			sendResponse(200, "OK", message, length, httpReq);
			brfr.close(); // closing the buffered reader.
			s.close(); //closing the socket.
		}

		private String readFile(File f, HTTPRequest httpReq) throws Exception{
			StringBuilder message = new StringBuilder();

			try (FileInputStream fis = new FileInputStream(f);) {


				if (httpReq.pageExt.equals("png") || httpReq.pageExt.equals("jpeg") || httpReq.pageExt.equals("jpg") || httpReq.pageExt.equals("gif")) {
					byte[] buffer = new byte[(int) f.length()];
					fis.read(buffer);
				} else {

					//Write the contents of the file onto the output stream
					int li;
					while ((li = fis.read()) != -1) {
						message.append((char) li); // reading the file content and putting it in the httpreqest
					}
					fis.close();
				}

			} catch (FileNotFoundException e) {
				httpresponse=httpresponse.replace("200 OK", "404 File Not Found"); // handling the file not found exception
				return null;
				
			} catch (Exception e) {
				httpresponse=httpresponse.replace("200 OK", "500 Error! "); // handling the bad request exception
				return null;
				
			}

			return message.toString();

		}

		// Utility method to generate HTTP response
		private String sendResponse(int statusCode, String statusText, String message, long reqDataLength, HTTPRequest httpReq) throws Exception {

			StringBuilder responseBuilder = new StringBuilder();
			String responseHeader = "";

			// Start building the response line
			responseBuilder.append("HTTP/1.1 ").append(statusCode).append(" ").append(statusText).append("\r\n");

			if (httpReq != null) {
				responseBuilder.append("Content-Type: ").append(httpReq.contentType).append("\r\n");
			}
			if (message != null && httpReq != null && httpReq.chunked != null && httpReq.chunked.equals("yes")) {
				responseBuilder.append("Transfer-Encoding: chunked\r\n");
				responseBuilder.append("\r\n");
				responseHeader = responseBuilder.toString();

				byte[] messageBytes = message.getBytes();
				int offset = 0;

				// Chunk the message data 
				String chunk = "";
				while (offset < messageBytes.length) {
					int length = Math.min(1024, messageBytes.length - offset);
					//chunk size
					responseBuilder.append(Integer.toHexString(length)).append("\r\n"); 
					//chunk data
					chunk = new String(Arrays.copyOfRange(messageBytes, offset, offset + length));
					responseBuilder.append(chunk).append("\r\n");
					offset += length;
				}

				// Add the last chunk indicating end of data
        		responseBuilder.append("0\r\n\r\n");
			} else if (message != null) {
				responseBuilder.append("Content-Length: ").append(reqDataLength).append("\r\n");
				responseBuilder.append("\r\n");
				responseHeader = responseBuilder.toString();
				responseBuilder.append(message);
			} 

			os.write(responseBuilder.toString().getBytes());

			//Send the Output Stream to the client
			os.flush();

			/*TRACE*/ System.out.println("********************************************\nTHE RESPONSE HEADER SENT TO THE CLIENT\n********************************************\n\n");  //print statement
			/*TRACE*/ System.out.println(responseHeader); 			
			/*TRACE*/ System.out.println("********************************************\nEND\n********************************************\n\n");  //print statement
			return responseBuilder.toString();
		}
	}
}
 