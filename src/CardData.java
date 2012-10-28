import java.io.Serializable;
import java.util.Arrays;

/**
 * @author yappy
 */
public class CardData implements Comparable<CardData>, Serializable {

	private static final long serialVersionUID = 6304902030548615178L;

	private String name;
	private String url;
	private String[] texts;

	public CardData() {
	}

	public CardData(String name, String url) {
		this.name = name;
		this.url = url;
		this.texts = null;
	}

	@Override
	public int compareTo(CardData o) {
		return this.name.compareTo(o.name);
	}

	@Override
	public String toString() {
		return name + ", " + url + "," + Arrays.toString(texts);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String[] getTexts() {
		return texts;
	}

	public void setTexts(String[] texts) {
		this.texts = texts;
	}

}
