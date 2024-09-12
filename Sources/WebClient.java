import java.io.BufferedReader;  //// importing input/output package for receiving and responding to the request. 
import java.io.InputStreamReader; //// importing io input stream reader.
import java.io.PrintWriter; ////importing io printwriter
import java.net.Socket;  ////importing .net.socket.
import java.util.Scanner; //importing util package 

 //global class

public class WebClient {
	Socket skt;  //initializing socket variable
	int port = ConfigReader.readPortNumber();
	//String root = ConfigReader.readRootDirectory();
	//String defPage = ConfigReader.readDefaultPage();
	//int maxThreads = ConfigReader.readMaxThreads();

//main method
	public static void main(String[] args) {

		new WebClient().connectionrequest();  // To avoid the Static problem, we are using the default main class to call the connectionrequest() method.

	}
///method to create the socket and call the httprequest method.
	public void connectionrequest() {
		try {

			String ip = "localhost";
			skt = new Socket(ip, port);  //creating the socket.
			Sendhttpreq http = new Sendhttpreq(); //creating the class object.
			System.out.print("You are connected to the socket. Before attempting to communicate with the server, please specify your HTTP request: \n");
			http.httprequest(); // calling the method.

		} catch (Exception e) {

			e.printStackTrace(); ///printing the stack trace.
		}

	}
 ///class for managing the client  request sent to server.
	public class clientmanagement {

		BufferedReader bfr; //declaring buffered reader.

		public void clienthandler() {
			try {
				bfr = new BufferedReader(new InputStreamReader(skt.getInputStream()));  //// creating object

				String Response = (String) ""; ////creating string
                String [] A;  //declaring string array
				while (bfr.ready() || Response.length() == 0) // while loop to read the response.
					Response += (char) bfr.read();


		
                A=Response.split("\r\n\r\n");

				System.out.println("\n\n The Http Response from the server\n............................................\n" + A[0] + "\n............................................\n");

				// Check if the server rejected the connection
                if (Response.contains("HTTP/1.1 503 Service Unavailable")) {
                    System.out.println("As you can see from the above response from the server, the server reached maximum capacity. You can try again later.");
                } else if (Response.contains("HTTP/1.1 404 Not Found")) {
					System.out.println("File not found on the server.");
				} else if (Response.contains("HTTP/1.1 501 Not Implemented")) {
					System.out.println("The requested operation is not implemented on the server.");
				} else if (Response.contains("HTTP/1.1 400 Bad Request")) {
					System.out.println("The server could not understand the request.");
				} else if (Response.contains("HTTP/1.1 500 Internal Server Error")) {
					System.out.println("An internal server error occurred.");
				} 
				if (A.length > 1){
					System.out.print("Response Body:\n" + A[1]);
				}
                
				bfr.close(); //closing the buffer reader.
				skt.close(); //closing the socket.

			} catch (Exception e) {

				e.printStackTrace();
			}
		}
	}
	
/// Class to send http reqeuset to the client.
	public class Sendhttpreq {
		 
		String httpreq = "";
		PrintWriter pw;

		public void httprequest() throws Exception {

			Scanner input = new Scanner(System.in);

			StringBuilder requestBuilder = new StringBuilder();
			String line;

			// Read request line by line until an empty line is encountered
			while ((line = input.nextLine()) != null && !line.isEmpty()) {
				requestBuilder.append(line).append("\n");
			}

			String request = requestBuilder.toString();
			
			pw = new PrintWriter(skt.getOutputStream());  ///creating the printwriter object.
			
			pw.println(request);
			pw.flush(); /// flusing the request to the server.
			clientmanagement cm = new clientmanagement();
			cm.clienthandler();
			pw.close(); //closing the printwriter.
			input.close();

		}

	}

}
