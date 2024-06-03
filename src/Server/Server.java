package Server;

import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.Set;


public class Server {
	private ServerSocket serverSocket;
	private Socket clientSocket;
	private DataInputStream is;
	private Set<DataOutputStream> os;

	public Server(int port) {
		try {
			serverSocket = new ServerSocket(port);
			System.out.println("Server started on port " + port);
			ClientHandler();
			os = new HashSet<DataOutputStream>();
			

		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public void ClientHandler() {
		new Thread(() -> {
			while (true) {
				try {
					clientSocket = serverSocket.accept();
					this.is = new DataInputStream(clientSocket.getInputStream());
					DataOutputStream newOs = new DataOutputStream(clientSocket.getOutputStream());
					os.add(newOs);
					getMessages();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void getMessages() {

		new Thread(() -> {
			String msg;
			while (true) {
				try {
					msg = is.readUTF();
					if(msg != null)
					{
						broadcast(msg);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void broadcast(String msg) {
		for (DataOutputStream outputStream : os) {
			try {
				outputStream.writeUTF(msg + "\n");
				outputStream.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		Server server = new Server(12345);
	}
}