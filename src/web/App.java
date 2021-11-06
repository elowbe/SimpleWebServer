package web;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;

public abstract class App {
	public static App runningApp;
	public static int PORT = 5000;// 80;
	private ServerSocket ss;
	public String templateDir;
	private boolean stop;
	String name;

	public App(String name) {
		this.name = name;

	}

	public void configure(String templateDir) {
		this.templateDir = templateDir;

	}

	public void stop() {
		try {
			ss.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		stop = true;

	}

	private void intializeServer() throws IOException {
		System.setProperty("http.keepAlive", "false");
		if (runningApp != null) {
			runningApp.stop();
			Logger.log(4, "Stopped App: " + runningApp);
		}
		ss = new ServerSocket(PORT);
	}

	public void listen() throws IOException {
		intializeServer();
		InetAddress ip = null;
		try {
			ip = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String hostname = ip.getHostName();

		Logger.log(4, "Started App: " + name + " (" + this + ")" + " here -> " + ip + ":" + PORT);

		runningApp = this;
		try {
			while (!stop) {
				connections();
			}
		} catch (Exception e) {
			//Logger.log(5, e.getMessage());
		}
	}

	public String getHeader(String type, int contentLength) {
		String out = "";
		if (type.equals("js")) {
			type = "javascript";
		} else if (type.equals("txt")) {
			type = "plain";
		}
		// Start sending our reply, using the HTTP 1.1 protocol
		out += "HTTP/1.1 200 (OK)\r\n"; // Version & status code
		if (type.equals("png") || type.equals("jpeg")) {
			out += "Content-Type: image/" + type + "\r\n"; // The type of data
		} else {
			out += "Content-Type: text/" + type + "\r\n"; // The type of data
		}
		if (contentLength > 0) {
			out += "Content-Length: " + contentLength + "\r\n";
		}
		out += "Connection: close\r\n"; // Will close stream
		return out + "\r\n";
	}

	public void sendText(Socket client, String type, String data) {
		try {
			String header = getHeader(type, data.getBytes().length);
			// out.print(header + data);
			client.getOutputStream().write((header + data).getBytes());
			// Sout.print(data);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void sendFile(Socket client, String type, File file) {
		try {
			byte[] data = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
			String header = getHeader(type, data.length);
			client.getOutputStream().write((header).getBytes());
			client.getOutputStream().write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void connections() throws IOException {
		// Wait for a client to connect. The method will block;
		// when it returns the socket will be connected to the client
		Socket client = ss.accept();

		// Get input and output streams to talk to the client
		BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		OutputStream out = client.getOutputStream();
		String line = "";

		String response = line = in.readLine();
		if (line != null) {
			String[] pieces = line.split(" ");
			String method = pieces[0].trim().toLowerCase();
			String path = pieces[1].trim().toLowerCase();

			String r = "";
			final String contentHeader = "Content-Length: ";
			int contentLength = 0;
			while ((r = in.readLine()) != null) {
				if (r.length() == 0)
					break;
				if (r.startsWith(contentHeader)) {
					contentLength = Integer.parseInt(r.substring(contentHeader.length()));
				}
				response += r + "\n";
			}
			Logger.log(0, response);
			Logger.log(1, "METHOD: " + method + "; MESSAGE: " + path);

			if (method.equals("get")) {
				get(client, in, out, path);
			} else if (method.equals("post")) {

				StringBuilder body = new StringBuilder();
				int c = 0;
				for (int i = 0; i < contentLength; i++) {
					c = in.read();
					body.append((char) c);
				}
				Logger.log(1, "BODY:" + body.toString());

				post(client, in, out, path, body.toString());
			} else if (method.equals("put")) {
				StringBuilder body = new StringBuilder();
				int c = 0;
				for (int i = 0; i < contentLength; i++) {
					c = in.read();
					body.append((char) c);
				}
				Logger.log(1, "BODY:" + body.toString());

				put(client, in, out, path, body.toString());
			} else if (method.equals("delete")) {
				delete(client, in, out, path);
			}

			out.close();
			in.close();
			client.close();
		}
	}

	public abstract void get(Socket client, BufferedReader in, OutputStream out, String message);

	public abstract void post(Socket client, BufferedReader in, OutputStream out, String message, String body);

	public abstract void put(Socket client, BufferedReader in, OutputStream out, String message, String body);

	public abstract void delete(Socket client, BufferedReader in, OutputStream out, String message);

}
