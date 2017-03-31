package fr.univnantes.termsuite.test.func.speed;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import com.google.common.io.Files;

import fr.univnantes.termsuite.metrics.EditDistance;
import fr.univnantes.termsuite.metrics.FastDiacriticInsensitiveLevenshtein;
import fr.univnantes.termsuite.metrics.Levenshtein;
import fr.univnantes.termsuite.metrics.LongestCommonSubsequence;

public class EditDistanceBenchmark {
	
	private String allowedChars;
	public static final int NB_TERMS = 1000;
	private List<String> terms = Lists.newArrayList();
	
//	private EditDistance dist1 = new DiacriticInsensitiveLevenshtein(Locale.FRENCH);
	private EditDistance dist2 = new Levenshtein();
	private EditDistance dist3 = new LongestCommonSubsequence();
	private EditDistance dist4 = new FastDiacriticInsensitiveLevenshtein(true);
	private EditDistance dist5 = new FastDiacriticInsensitiveLevenshtein(false);

	
	@Before
	public void before() throws IOException {
		Path charPath = Paths.get("src", "main", "resources", "fr", "univnantes", "termsuite", "resources", "fr", "french-allowed-chars.txt");
		allowedChars = Files.toString(charPath.toFile(), Charsets.UTF_8).trim();
		String base = "abcdefgh abcdefgh abcdefgh";
		for(int i = 0; i < NB_TERMS; i++) {
			terms.add(generateTerm(base));
		}
	}

	private String generateTerm(String base) {
		List<Character> characters = Lists.newArrayList();
		for(char c:base.toCharArray())
			characters.add(c);
		
		if(new Random().nextBoolean())
			subsitute(characters);
		if(new Random().nextBoolean())
			insert(characters);
		if(new Random().nextBoolean())
			remove(characters);
		if(new Random().nextBoolean())
			subsitute(characters);
		if(new Random().nextBoolean())
			insert(characters);
		if(new Random().nextBoolean())
			remove(characters);		
		
		return toString(characters);
	}

	private String toString(List<Character> characters) {
		StringBuilder b = new StringBuilder();
		for(Character c:characters)
			b.append(c);
		return b.toString();
	}

	private void subsitute(List<Character> base) {
		base.set(
				new Random().nextInt(base.size()), 
				allowedChars.charAt(new Random().nextInt(allowedChars.length())));
	}
	
	private void insert(List<Character> base) {
		base.add(
				new Random().nextInt(base.size()+1), 
				allowedChars.charAt(new Random().nextInt(allowedChars.length())));
	}

	private void remove(List<Character> base) {
		base.remove(new Random().nextInt(base.size()));
	}
	
	@Test
	public void test() {
		doDistance(dist2, "dist2");
		doDistance(dist3, "dist3");
		doDistance(dist4, "dist4");
		doDistance(dist5, "dist5");
	}

	public int doDistance(EditDistance dist, String distName) {
		Stopwatch sw = Stopwatch.createStarted();
		int cnt = 0;
		List<Double> distances = Lists.newArrayList();
		String t1, t2;
		for(int i=0;i<terms.size();i++) {
			t1 = terms.get(i);
			for(int j=i+1;j<terms.size();j++) {
				t2 = terms.get(j);
				cnt++;
				distances.add(dist.computeNormalized(t1, t2, 0.9));
			}
		}
		sw.stop();
		System.out.format("%s finished. %d distances computed in %s (%.2f per seconds)%n",
				distName, 
				cnt, 
				sw,
				1000000*((float)cnt/sw.elapsed(TimeUnit.MICROSECONDS)));
		return cnt;
	}

}
