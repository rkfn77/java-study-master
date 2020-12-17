package chatting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ChatServerThread extends Thread {
	private String nickname;
	private Socket socket;
	List<Writer> listWriters;

	BufferedReader br = null;
	PrintWriter pw = null;

	public ChatServerThread(Socket socket, List<Writer> listWriters) {
		this.socket = socket;
		this.listWriters = listWriters;
	}

	@Override
	public void run() {
		InetSocketAddress remoteInetSocketAddress = (InetSocketAddress) socket.getRemoteSocketAddress();

		InetAddress remoteInetAddress = remoteInetSocketAddress.getAddress();
		String remoteHostAddress = remoteInetAddress.getHostAddress();
		int remotePort = remoteInetSocketAddress.getPort();

		System.out.println("[server] connected by client[" + remoteHostAddress + ":" + remotePort + "]");
		try {
			// 1. IOStream 생성(받아 오기)
			br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
			pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

			while (true) {
				// 2. 데이터 읽기
				String data = br.readLine();
				ChatServer.log(data);
				if (data == null) {
					// client가 소켓을 정상적으로 종료
					doQuit(pw);
					ChatServer.log("클라이언트로 부터 연결 끊김");
					break;
				}

				String[] tokens = data.split(":");

				if ("join".equals(tokens[0])) {
					doJoin(tokens[1], pw);
				} else if ("message".equals(tokens[0])) {
					doMessage(tokens[1]);
				} else if ("quit".equals(tokens[0])) {
					doQuit(pw);
					break;
				}
			}
		} catch (SocketException e) {
			// client가 비정상 종료
			System.out.println("[server] suddenly closed by client");
		} catch (IOException e) {
			doQuit(pw);
			System.out.println("[server] error:" + e);
		} finally {
			try {
				if (socket != null && !socket.isClosed()) {
					socket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void doJoin(String nickName, Writer writer) {
		this.nickname = nickName;

		String data = nickName + "님이 참여하였습니다";
		broadcast(data);
		/* writer pool에 저장 */
		addWriter(writer);

		pw.println("join:ok");
		pw.flush();
	}

	private void addWriter(Writer writer) {
		synchronized (listWriters) {
			listWriters.add(writer);
		}
	}

	private void removeWriter(Writer writer) {
		synchronized (listWriters) {
			listWriters.remove(writer);
		}
	}

	private void doMessage(String message) {
		broadcast(nickname + ":" + message);
	}

	private void doQuit(Writer writer) {
		removeWriter(writer);

		String data = nickname + "님이 퇴장 하였습니다!";
		broadcast(data);
	}

	private void broadcast(String data) {
		synchronized (listWriters) {
			for (Writer writer : listWriters) {
				PrintWriter printWriter = (PrintWriter) writer;
				printWriter.println(data);
				printWriter.flush();
			}
		}
	}
}