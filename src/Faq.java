import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Faq {

	public static void main(String[] args) throws IOException {
		List<String> lines = Files.readAllLines(Paths.get("sample.html"),
				Charset.forName("UTF-8"));
		StringBuilder sb = new StringBuilder();
		for (String line : lines) {
			sb.append(line);
			sb.append('\n');
		}
		Pattern FAQ_P = Pattern.compile("<p>(Ｑ：.*?)</p>", Pattern.MULTILINE
				| Pattern.DOTALL);
		Matcher m = FAQ_P.matcher(sb.toString());
		while (m.find()) {
			System.out.println("find");
			System.out.println(m.group());
		}
	}

}
