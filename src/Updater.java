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
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yappy
 */
public class Updater {

	private static final long TIMER_PERIOD = 5000;
	public static final String DATABASE_FILE = "database.xml";
	public static final String BACKUP_FILE = "backup.xml";

	private static Timer timer;
	private static List<CardData> cardData = new ArrayList<>();

	private static void writeList() throws IOException {
		// mv data backup
		if (Files.exists(Paths.get(DATABASE_FILE))) {
			Files.move(Paths.get(DATABASE_FILE), Paths.get(BACKUP_FILE),
					StandardCopyOption.REPLACE_EXISTING,
					StandardCopyOption.ATOMIC_MOVE);
		}
		// write > data
		try (XMLEncoder out = new XMLEncoder(
				new FileOutputStream(DATABASE_FILE))) {
			out.writeObject(cardData);
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

	private static void getAllCards() throws IOException {
		System.out.println("Creating card list...");
		long start = System.currentTimeMillis();

		Map<String, String> query = new TreeMap<>();
		query.put("encode_hint", "ぷ");
		query.put("word", "調整中");
		try (Scanner sc = new Scanner(openPost(
				"http://yugioh-wiki.net/?cmd=search", query), "JISAutoDetect")) {
			Pattern p = Pattern.compile("<a href=\"(.*)\">.*《(.*)》");
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				if (line.indexOf("《") != -1) {
					Matcher m = p.matcher(line);
					if (m.find()) {
						String name = m.group(2).replaceAll("&amp;", "&");
						String url = m.group(1).replaceAll("&amp;", "&");
						cardData.add(new CardData(name, url));
					}
				}
			}
		}
		System.out.printf("Creating card list succeeded (%d, %dms)%n",
				cardData.size(), System.currentTimeMillis() - start);
	}

	private static int updateIndex = 0;

	private static class UpdateTask extends TimerTask {

		private static final Pattern FAQ_P = Pattern.compile("<p>(Ｑ：.*?)</p>",
				Pattern.MULTILINE | Pattern.DOTALL);

		@Override
		public void run() {
			while (cardData.get(updateIndex).getTexts() != null) {
				updateIndex = (updateIndex + 1) % cardData.size();
			}
			CardData data = cardData.get(updateIndex);
			System.out.printf("Process %d/%d: %s%n", updateIndex + 1,
					cardData.size(), data);
			updateIndex++;
			try {
				String htmlText = readAll(new URL(data.getUrl()).openStream());
				List<String> faqs = new ArrayList<>();
				Matcher m = FAQ_P.matcher(htmlText);
				while (m.find()) {
					String text = m.group(1).replace('\n', ' ');
					text = text.replaceAll(" 　　", "");
					text = text.replaceAll("調整中。", "調整中");
					text = text.replaceAll("<(\"[^\"]*\"|'[^']*'|[^'\">])*>",
							"");
					if (text.indexOf("調整中") != -1) {
						faqs.add(text);
						System.out.println(text);
					}
				}
				data.setTexts(faqs.toArray(new String[0]));
				writeList();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (endCheck()) {
				System.out.println("Finish!");
				timer.cancel();
			}
		}

		private boolean endCheck() {
			for (CardData data : cardData) {
				if (data.getTexts() == null)
					return false;
			}
			return true;
		}

	}

	public static void main(String[] args) {
		if (args.length >= 2) {
			System.setProperty("proxySet", "true");
			System.setProperty("proxyHost", args[0]);
			System.setProperty("proxyPort", args[1]);
			System.out.printf("Set proxy: %s:%s%n", args[0], args[1]);
		}
		try {
			getAllCards();
			writeList();
		} catch (IOException e) {
			e.printStackTrace();
		}
		timer = new Timer(false);
		timer.scheduleAtFixedRate(new UpdateTask(), 0, TIMER_PERIOD);
	}

}
