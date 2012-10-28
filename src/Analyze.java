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
		int[] table = new int[50];
		for (CardData card : list) {
			for (String text : card.getTexts()) {
				table[text.length() / 10]++;
			}
		}
		for (int i = 0; i < table.length; i++) {
			System.out.printf("%3d..%3d: %d%n", i * 10, i * 10 + 9, table[i]);
		}
	}

}
