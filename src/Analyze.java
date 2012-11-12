import java.beans.XMLDecoder;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author yappy
 */
public class Analyze {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {
		List<CardData> list;
		try (XMLDecoder in = new XMLDecoder(new FileInputStream(
				Updater.DATABASE_FILE))) {
			list = (List<CardData>) in.readObject();
		}

		final int TWEET_MAX = 140;
		int all = 0;
		int msgok = 0;
		int msgnameok = 0;
		for (CardData card : list) {
			String name = "《" + card.getName() + "》 ";
			for (String text : card.getTexts()) {
				if (text.length() + name.length() < TWEET_MAX) {
					msgnameok++;
				}
				if (text.length() < TWEET_MAX) {
					msgok++;
				}
				all++;
			}
		}
		System.out.printf("tweet OK: %d/%d%n", msgok, all);
		System.out.printf("name + tweet OK: %d/%d%n", msgnameok, all);
	}

}
