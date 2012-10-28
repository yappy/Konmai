import java.beans.XMLEncoder;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yappy
 */
public class Updater {

	public static final String DATABASE_FILE = "database.xml";
	public static final String BACKUP_FILE = "backup.xml";

	private static List<CardData> cardData = new ArrayList<>();

	private static void writeList() throws IOException {
		// mv data backup
		Files.move(Paths.get(DATABASE_FILE), Paths.get(BACKUP_FILE),
				StandardCopyOption.REPLACE_EXISTING,
				StandardCopyOption.ATOMIC_MOVE);
		// write > data
		try (XMLEncoder out = new XMLEncoder(
				new FileOutputStream(DATABASE_FILE))) {
			out.writeObject(cardData.toArray());
		}
	}

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

	private static void listAllCards() throws IOException {
		System.out.println("Creating card list...");
		long start = System.currentTimeMillis();

		Map<String, String> query = new TreeMap<>();
		query.put("encode_hint", "ぷ");
		query.put("word", "《");
		try (Scanner sc = new Scanner(openPost(
				"http://yugioh-wiki.net/?cmd=search", query), "JISAutoDetect")) {
			Pattern p = Pattern
					.compile("<a href=\"(.*)\"><strong.*</strong>(.*)》");
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				if (line.indexOf("《") != -1) {
					Matcher m = p.matcher(line);
					if (m.find()) {
						String name = m.group(1).replaceAll("&amp;", "&");
						String url = m.group(2);
						cardData.add(new CardData(name, url));
					}
				}
			}
		}
		System.out.printf("Creating card list succeeded (%d, %dms)%n",
				cardData.size(), System.currentTimeMillis() - start);
	}

	public static void main(String[] args) {
		try {
			listAllCards();
			writeList();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
