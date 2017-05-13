package ntu.csie.keydial;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.junit.Test;

public class PredictionTest {

	static Prediction PREDICTION = new Prediction(new File("prediction.dat"));

	@Test
	public void completeWord() {
		List<String> suggestions = PREDICTION.completeWord("Whe", 30);

		assertEquals("[when, where, whether, whereas, wheel, wherever, wheat, whenever, whereby, wheel's, wheels, where's, when the, where the, whereabouts, wheeled, wheeler, wheeze, wheezed, wheezing, when he, when you, wherein, whereof, whereupon, wherewithal, whetted, wheal, wheals, wheaten, wheat's]", suggestions.toString());
	}

	@Test
	public void completeSentence() {
		List<String> suggestions = PREDICTION.completeSentence("Whe", 30);

		assertEquals("[when, where, whether, whereas, wheel, wherever, wheat, whenever, whereby, wheel's, wheels, where's, when the, where the, whereabouts, wheeled, wheeler, wheeze, wheezed, wheezing, when he, when you, wherein, whereof, whereupon, wherewithal, whetted, wheal, wheals, wheaten, wheat's]", suggestions.toString());
	}

}
