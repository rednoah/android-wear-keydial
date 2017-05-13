package ntu.csie.keydial;

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

import java.io.File;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.regex.Pattern;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

public class Prediction {

	static final Pattern SENTENCE = Pattern.compile("^.*\\w+\\s?$");
	static final Pattern SPACE = Pattern.compile("\\s+");
	static final Pattern ALPHA = Pattern.compile("\\p{Alpha}+");
	static final Pattern PUNCTUATION = Pattern.compile("\\p{Punct}");
	static final Pattern NEWLINE = Pattern.compile("\\R");

	static final String OMEGA = "Ω";

	BTreeMap<String, int[]> index;
	BTreeMap<Integer, String> value;

	public String getLastWord(String s) {
		if (SENTENCE.matcher(s).matches()) {
			String[] input = SPACE.split(s);
			if (input.length > 0) {
				String word = input[input.length - 1];
				return s.substring(s.lastIndexOf(word));
			}
		}

		return "";
	}

	public List<String> completeSentence(String s, int limit) {
		String prefix = getLastWord(s);
		if (prefix.length() > 0) {
			return completeWord(prefix, limit);
		}

		return emptyList();
	}

	public List<String> completeWord(String s, int limit) {
		String k = getKey(s);
		NavigableMap<String, int[]> indexSubMap = index.subMap(k, k + OMEGA);

		return indexSubMap.entrySet().stream().sorted(Comparator.comparing(m -> m.getValue()[0], Comparator.reverseOrder())).limit(limit).map(m -> {
			return value.get(m.getValue()[1]);
		}).flatMap(NEWLINE::splitAsStream).collect(toList());
	}

	public Set<String> guessNextCharacter(String s, int limit) {
		String prefix = getLastWord(s);
		if (ALPHA.matcher(prefix).matches()) {
			List<String> options = completeWord(prefix, limit);
			if (options.size() > 0) {
				Set<String> letters = new HashSet<String>();
				for (String it : options) {
					if (it.length() > prefix.length()) {
						String letter = it.substring(prefix.length(), prefix.length() + 1);
						if (ALPHA.matcher(letter).matches()) {
							letters.add(letter.toUpperCase());
						}
					}
				}
				return letters;
			}
		}
		return emptySet();
	}

	public Prediction(File volume) {
		DB db = DBMaker.newFileDB(volume).readOnly().make();

		index = db.createTreeMap("index").keySerializer(BTreeKeySerializer.STRING).valueSerializer(Serializer.INT_ARRAY).makeOrGet();
		value = db.createTreeMap("value").keySerializer(BTreeKeySerializer.ZERO_OR_POSITIVE_INT).valueSerializer(Serializer.STRING).makeOrGet();
	}

	public static String getKey(String value) {
		return PUNCTUATION.matcher(value).replaceAll("").toLowerCase();
	}

}
