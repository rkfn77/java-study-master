package chatting;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {
	static final String log = "127.0.0.1";
	static final int PORT = 5000;

	public static void main(String[] args) {
		ServerSocket serverSocket = null;
		List<Writer> listWriters = new ArrayList<Writer>();

		try {
			// 1. 서버 소켓 생성
			serverSocket = new ServerSocket();

			// 2. 바인딩
			String hostAddress = InetAddress.getLocalHost().getHostAddress();
			serverSocket.bind(new InetSocketAddress(log, PORT));
			log("연결 기다림 - " + log + ":" + PORT);

			// 3. 요청 대기
			while (true) {
				Socket socket = serverSocket.accept();
				new ChatServerThread(socket, listWriters).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (serverSocket != null && !serverSocket.isClosed()) {
					serverSocket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void log(String message) {
		System.out.println("[server " + Thread.currentThread().getId() + "] " + message);
	}

}