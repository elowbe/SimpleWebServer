package web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public abstract class App {
	public int port = 5000;// 80;
	private ServerSocket ss;
	public String templateDir;
	private volatile boolean stop;
	String name;
	private volatile boolean isStopped = true;

	public App(String name) {
		this.name = name;

	}

	public void configure(String templateDir) {
		this.templateDir = templateDir;

	}

	public void stop() {
		Logger.log(4, "Stopping app: " + this);
		// new Exception().printStackTrace();
		if (ss != null) {
			try {
				ss.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		stop = true;

	}

	private void intializeServer() throws IOException {
		System.setProperty("http.keepAlive", "false");
		stop = false;
	}

	public void listen() throws IOException {
		listen(port);
	}

	public void listen(int port) throws IOException {
		intializeServer();
		ss = new ServerSocket(port);
		InetAddress ip = null;
		try {
			ip = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String hostname = ip.getHostName();

		Logger.log(4, "Started App: " + name + " (" + this + ")" + " here -> " + ip + ":" + port);

		while (!stop) {

			isStopped = false;
			try {
				connections();
			} catch (Exception e) {
				// Logger.log(5, e.getMessage());
				e.printStackTrace();
				stop = true;
			}

		}
		isStopped = true;
		Logger.log(4, this + " Server closed");
	}

	public static String getHeader(String type, int contentLength) {
		String out = "";

		// Start sending our reply, using the HTTP 1.1 protocol
		out += "HTTP/1.1 200 (OK)\r\n"; // Version & status code
		out += "Content-Type: " + type + "\r\n"; // The type of data
		if (contentLength > 0) {
			out += "Content-Length: " + contentLength + "\r\n";
		}
		out += "Connection: close\r\n"; // Will close stream
		return out + "\r\n";
	}

	public static void sendText(Socket client, String type, String data) {
		try {
			String header = getHeader(type, data.getBytes().length);
			// out.print(header + data);
			client.getOutputStream().write((header + data).getBytes());
			// Sout.print(data);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void sendFile(Socket client, String type, File file) {
		try (OutputStream out = client.getOutputStream(); FileInputStream fileInputStream = new FileInputStream(file)) {
			// Send HTTP Header
			out.write(("HTTP/1.1 200 OK\r\n").getBytes());
			out.write(("ContentType: " + type + "\r\n").getBytes());
			out.write("\r\n".getBytes());

			// Send file content
			byte[] buffer = new byte[4096];
			int bytes;
			while ((bytes = fileInputStream.read(buffer)) != -1) {
				out.write(buffer, 0, bytes);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public void connections() throws IOException {
		// Wait for a client to connect. The method will block;
		// when it returns the socket will be connected to the client
		Socket client = ss.accept();
		Runnable run = new Runnable() {

			@Override
			public void run() {
				// Get input and output streams to talk to the client
				BufferedReader in;
				try {
					in = new BufferedReader(new InputStreamReader(client.getInputStream()));

					OutputStream out = client.getOutputStream();
					String line = "";
					String response = line = in.readLine();
					try {
						if (line != null && line.contains(" ")) {
							// TODO check this output out again
							// System.out.println("--- " + line);
							String[] pieces = line.split(" ");
							if (pieces.length > 1) {
								String method = pieces[0].trim().toLowerCase();
								String path = pieces[1].trim().toLowerCase();

								String r = "";
								final String contentHeader = "Content-Length:";
								int contentLength = 0;
								while ((r = in.readLine()) != null) {
									if (r.length() == 0)
										break;
									if (r.toLowerCase().startsWith(contentHeader.toLowerCase())) {
										contentLength = Integer.parseInt(r.substring(contentHeader.length()).trim());
									}
									response += r + "\n";
								}

								// Logger.log(1, "METHOD: " + method + "; MESSAGE: " + path);

								if (method.equals("get")) {
									get(client, in, out, path);
								} else if (method.equals("post")) {

									StringBuilder body = new StringBuilder();

									int c = 0;
									for (int i = 0; i < contentLength; i++) {
										c = in.read();
										body.append((char) c);
									}
									// Logger.log(1, "BODY:" + body.toString());

									post(client, in, out, path, body.toString());
								} else if (method.equals("put")) {
									StringBuilder body = new StringBuilder();
									int c = 0;
									for (int i = 0; i < contentLength; i++) {
										c = in.read();
										body.append((char) c);
									}
									// Logger.log(1, "BODY:" + body.toString());

									put(client, in, out, path, body.toString());
								} else if (method.equals("delete")) {
									delete(client, in, out, path);
								}
							}
						}
					} finally {
						out.close();
						in.close();
						client.close();
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		run.run();
		//new Thread(run).start();
	}

	public boolean isStopped() {
		return isStopped;
	}

	public abstract void get(Socket client, BufferedReader in, OutputStream out, String message);

	public abstract void post(Socket client, BufferedReader in, OutputStream out, String message, String body);

	public abstract void put(Socket client, BufferedReader in, OutputStream out, String message, String body);

	public abstract void delete(Socket client, BufferedReader in, OutputStream out, String message);

}
