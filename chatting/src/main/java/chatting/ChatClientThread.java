package chatting;

import java.io.BufferedReader;
import java.io.IOException;

public class ChatClientThread extends Thread {

	BufferedReader br;

	public ChatClientThread(BufferedReader br) {
		this.br = br;
	}

	@Override
	public void run() {
		while (true) {
			String data = null;
			try {
				data = br.readLine();

				if ("OK".equals(data)) {
					System.out.println("[client] connnect complete");
					continue;
				}

				System.out.println(data);
			} catch (IOException e) {
				System.out.println("\r\n[client] closed by client");
				break;
			}
		}
	}
}