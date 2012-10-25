import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yappy
 */
public class Updater {

	private static String readAll(InputStream in) throws IOException {
		char[] buf = new char[1024];
		int len;
		StringBuilder builder = new StringBuilder();
		Reader r = new InputStreamReader(in, "JISAutoDetect");
		while ((len = r.read(buf)) != -1) {
			builder.append(buf, 0, len);
		}
		return builder.toString();
	}

	private static String getText(String strURL) throws IOException {
		URL url = new URL(strURL);
		try (InputStream in = url.openStream()) {
			return readAll(in);
		}
	}

	private static InputStream openPost(String strURL,
			Map<String, String> postData) throws IOException {
		URL url = new URL(strURL);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		StringBuilder postStr = new StringBuilder();
		for (Map.Entry<String, String> e : postData.entrySet()) {
			postStr.append(e.getKey());
			postStr.append('=');
			postStr.append(URLEncoder.encode(e.getValue(), "UTF-8"));
			postStr.append('&');
		}
		if (postStr.length() != 0) {
			postStr.deleteCharAt(postStr.length() - 1);
		}
		try (Writer out = new OutputStreamWriter(con.getOutputStream())) {
			out.write(postStr.toString());
		}
		return con.getInputStream();
	}

	public static void main(String[] args) throws IOException {
		// System.out.print(getText("http://yugioh-wiki.net/"));
		Map<String, String> map = new TreeMap<>();
		map.put("encode_hint", "ぷ");
		map.put("word", "《");
		try (InputStream in = openPost("http://yugioh-wiki.net/?cmd=search",
				map)) {
			Scanner sc = new Scanner(in, "JISAutoDetect");
			Pattern p = Pattern
					.compile("<a href=\"\"(.*)\"\"*.</string>(.*)》</a>");
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				if (line.indexOf("《") != -1) {
					Matcher m = p.matcher(line);
					if(m.find()){
						System.out.println(m.group());
					}
				}
			}
		}
	}

}
