package ntu.csie.keydial;

import static java.nio.charset.StandardCharsets.*;
import static java.util.stream.Collectors.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

public class PredictionGen {

	public static void main(String[] args) throws Exception {
		String[] corpus = { "engus_inclusion_utf8.txt", "engus_corpus_utf8.txt" };

		TreeMap<String, List<String>> wordFrequencyMap = Stream.of(corpus).map(Paths::get).flatMap(f -> {
			try {
				return Files.lines(f, UTF_8);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}).collect(groupingBy(Prediction::getKey, TreeMap::new, toList()));

		File volume = new File("prediction.dat");
		volume.delete();
		volume.createNewFile();

		DB db = DBMaker.newFileDB(volume).closeOnJvmShutdown().make();

		BTreeMap<String, int[]> index = db.createTreeMap("index").keySerializer(BTreeKeySerializer.STRING).valueSerializer(Serializer.INT_ARRAY).make();
		BTreeMap<Integer, String> value = db.createTreeMap("value").keySerializer(BTreeKeySerializer.ZERO_OR_POSITIVE_INT).valueSerializer(Serializer.STRING).make();

		// feed content into consumer
		AtomicInteger i = new AtomicInteger(0);

		wordFrequencyMap.forEach((k, v) -> {
			int id = i.getAndIncrement();
			int frequency = v.size();

			TreeSet<String> values = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
			values.addAll(v);

			index.put(k, new int[] { frequency, id });
			value.put(id, String.join("\n", values));
		});

		System.out.println(index.size());
		System.out.println(value.size());

		db.commit();
		db.compact();
	}

}
