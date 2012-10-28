import java.beans.XMLDecoder;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

/**
 * @author yappy
 */
public class CreateList {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {
		List<CardData> list;
		try (XMLDecoder in = new XMLDecoder(new FileInputStream(
				Updater.DATABASE_FILE))) {
			list = (List<CardData>) in.readObject();
		}

		System.setOut(new PrintStream("list.txt"));

		final int TWEET_MAX = 140;
		for (CardData card : list) {
			String name = "《" + card.getName() + "》 ";
			for (String text : card.getTexts()) {
				if (text.length() + name.length() < TWEET_MAX) {
					System.out.println(name + text);
				} else if (text.length() < TWEET_MAX) {
					System.out.println(text);
				} else {
					System.err.println(text);
				}
			}
		}
	}

}
