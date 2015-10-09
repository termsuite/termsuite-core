package eu.project.ttc.engines.exporter;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;

import com.google.common.base.Joiner;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import eu.project.ttc.engines.AbstractTermIndexExporter;
import eu.project.ttc.models.Component;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.Word;

public class CompoundExporter extends AbstractTermIndexExporter {

	private static final String LINE_FORMAT = "%-30s %-35s %d\n";

	@Override
	protected void processAcceptedTerms(TreeSet<Term> acceptedTerms) throws AnalysisEngineProcessException {
		
		try {
			Multimap<Word,Term> terms = HashMultimap.create();
			Set<Word> compounds = Sets.newHashSet();
			for(Term t:acceptedTerms) {
				Word w = t.getWords().get(0).getWord();
				if(t.getWords().size() == 1 && w.isCompound()) {
					compounds.add(w);
					terms.put(w, t);
				}
			}
			final Map<Word,Integer> frequencies = Maps.newHashMap();
			for(Word w: terms.keySet()) {
				int f = 0;
				for(Term t:terms.get(w))
					f += t.getFrequency();
				
				frequencies.put(w, f);
			}
			
			
			Set<Word> sortedCompounds = new TreeSet<Word>(new Comparator<Word>() {
				@Override
				public int compare(Word o1, Word o2) {
					return ComparisonChain.start()
							.compare(frequencies.get(o2), frequencies.get(o1))
							.result();
				}
			});
			sortedCompounds.addAll(compounds);
			
			for(Word w:sortedCompounds) {
				List<String> compLemmas = Lists.newArrayList();
				for(Component c:w.getComponents())
					compLemmas.add(c.getLemma());
				writer.write(String.format(LINE_FORMAT, 
					w.getLemma(),
					Joiner.on('|').join(compLemmas),
					frequencies.get(w)
				));
			}
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}		
	}
}
