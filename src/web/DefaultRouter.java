package web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class DefaultRouter extends App {

	public HashMap<String, String> routes = new HashMap<String, String>();

	public DefaultRouter() {
		super("default");
		PORT = 80;
		if (System.getenv("PORT") != null) {
			PORT = Integer.parseInt(System.getenv("PORT"));
			System.out.println("port: " + PORT);
		}
	}
	public static void main(String args[]) {
		
		System.setProperty("http.keepAlive", "false");
		
		InetAddress ip = null;
		try {
			ip = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String hostname = ip.getHostName();

		System.out.println("info: " + hostname + " " + ip + ":");

		Logger.priorityLevel = 1;
		DefaultRouter app = new DefaultRouter();
		try {
			System.out.println("Working Directory = " + System.getProperty("user.dir"));
			app.configure(System.getProperty("user.dir") + "//resources");
			File file = new File(System.getProperty("user.dir") + "//.routes");

			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
				for (String line; (line = br.readLine()) != null;) {
					String[] split = line.split("':'");
					String route = split[0].substring(1);
					String path = split[1].substring(0, split[1].length() - 1);
					System.out.println(route + " -> " + path);
					app.routes.put(route, path);
				}
				// line is not visible here.
			}

			

			// PenControl.init();
			app.listen();

		} catch (

		IOException e) {
			e.printStackTrace();
		}

	}

	public void get(Socket client, BufferedReader in, OutputStream out, String message) {
		// TODO Auto-generated method stub
		StringWriter stringWriter = new StringWriter();
		String path = RequestParser.getPath(message);

		try {
			if (routes.containsKey(path)) {
				File file = new File(templateDir + routes.get(path));
				Logger.log(1, file.getAbsolutePath());
				Map<String, Object> input = new HashMap<String, Object>();
				String p = templateDir + path;

				if (file.exists() && !file.isDirectory()) {
					String extension = "";

					int i = p.lastIndexOf('.');
					if (i > 0) {
						extension = p.substring(i + 1);
					}

					sendFile(client, extension, file);
				} else {
					sendText(client, "html", stringWriter.toString());
				}
			} else if (path.contains("/stylesheets")) {
				String contents = new String(Files.readAllBytes(Paths.get((templateDir + path).toLowerCase().trim())));
				sendText(client, "css", contents);
			} else if (path.contains("/scripts")) {
				String contents = new String(Files.readAllBytes(Paths.get((templateDir + path).toLowerCase().trim())));
				sendText(client, "javascript", contents);
			} else {
				String p = templateDir + path;

				File file = new File(p);
				if (file.exists() && !file.isDirectory()) {
					String extension = "";

					int i = p.lastIndexOf('.');
					if (i > 0) {
						extension = p.substring(i + 1);
					}

					sendFile(client, extension, file);
				} else {
					sendText(client, "html", "404: " + path + " not found");

				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void post(Socket client, BufferedReader in, OutputStream out, String message, String body) {
		// TODO Auto-generated method stub
		StringWriter stringWriter = new StringWriter();
		String path = RequestParser.getPath(message);
		sendText(client, "html", "");
	}

	@Override
	public void put(Socket client, BufferedReader in, OutputStream out, String message, String body) {
		// TODO Auto-generated method stub
		String path = RequestParser.getPath(message);
		sendText(client, "html", "");
	}

	@Override
	public void delete(Socket client, BufferedReader in, OutputStream out, String message) {
		// TODO Auto-generated method stub
		sendText(client, "html", "");
	}
}
