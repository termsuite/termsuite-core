package fr.univnantes.termsuite.export;

import java.io.IOException;
import java.io.Writer;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.base.Joiner;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.model.Component;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.model.Word;

public class CompoundExporter {
	private static final String LINE_FORMAT = "%-30s %-10s %-35s %d\n";

	private Terminology termino;
	private Writer writer;
	
	private CompoundExporter(Terminology termino, Writer writer) {
		super();
		this.termino = termino;
		this.writer = writer;
	}

	public static void export(Terminology termino, Writer writer) {
		new CompoundExporter(termino, writer).doExport();
	}

	private void doExport() {
		try {
			Multimap<Word,Term> terms = HashMultimap.create();
			Set<Word> compounds = Sets.newHashSet();
			for(Term t:termino.getTerms().values()) {
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
				List<String> compStrings = Lists.newArrayList();
				for(Component c:w.getComponents())
					compStrings.add(c.getSubstring());
				writer.write(String.format(LINE_FORMAT, 
					w.getLemma(),
					w.getCompoundType(),
					Joiner.on('|').join(compStrings),
					frequencies.get(w)
				));
			}
		} catch (IOException e) {
			throw new TermSuiteException(e);
		}		
	}
}
