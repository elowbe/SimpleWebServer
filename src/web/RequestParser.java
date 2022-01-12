package web;

import java.util.HashMap;
import java.util.regex.Pattern;

public class RequestParser {
	public static String getPath(String path) {
		if (path.contains("?")) {
			return path.substring(1, path.indexOf("?")).toLowerCase();
		}
		return path.substring(0).toLowerCase();
	}

	public static String[] getParameters(String path) {
		if (path.contains("?") && path.contains("#")) {
			String cut = path.substring(path.indexOf("?") + 1, path.indexOf("#")).toLowerCase();
			return cut.split("&");
		} else if (path.contains("?")) {
			String cut = path.substring(path.indexOf("?") + 1, path.length()).toLowerCase();
			return cut.split("&");
		}
		return null;
	}

	public static String getMessageBody(String path) {
		if (path.contains("?") && path.contains("#")) {
			String cut = path.substring(path.indexOf("?") + 1, path.indexOf("#")).toLowerCase();
			return cut;
		} else if (path.contains("?")) {
			String cut = path.substring(path.indexOf("?") + 1, path.length()).toLowerCase();
			return cut;
		}
		return "";
	}
	public static HashMap<String, String> parseBody(String body) {
		HashMap<String, String> bodyMap = new HashMap<String, String>();
		return parseBody(bodyMap, body);
	}
	public static HashMap<String, String> parseBody(HashMap<String, String> bodyMap, String body) {
		String[] pairs = body.split("&");
		for (String s : pairs) {
			String[] split = s.split("=");
			if (split.length == 2) {
				bodyMap.put(split[0], split[1].replaceAll(Pattern.quote("+"), " "));
			} else {
				bodyMap.put(split[0], "");
			}
		}
		return bodyMap;
	}

}