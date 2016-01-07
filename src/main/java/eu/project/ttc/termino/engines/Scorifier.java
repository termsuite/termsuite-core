package eu.project.ttc.termino.engines;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.scored.ScoredModel;
import eu.project.ttc.models.scored.ScoredTerm;
import eu.project.ttc.models.scored.ScoredVariation;
import eu.project.ttc.utils.StringUtils;

/**
 * Turn a {@link TermIndex} to a {@link ScoredModel}
 * 
 * @author Damien Cram
 *
 */
public class Scorifier {
	private static final Logger LOGGER = LoggerFactory.getLogger(Scorifier.class);

	private ScoredModel scoredModel;

	private ScorifierConfig config;
	
	public Scorifier(ScorifierConfig config) {
		super();
		this.config = config;
	}

	private static Comparator<ScoredTerm> wrComparator = new Comparator<ScoredTerm>() {
		@Override
		public int compare(ScoredTerm o1, ScoredTerm o2) {
			return Double.compare(o2.getWRLog(), o1.getWRLog());
		}
	};

	private static Comparator<ScoredVariation> variationScoreComparator = new Comparator<ScoredVariation>() {
		@Override
		public int compare(ScoredVariation o1, ScoredVariation o2) {
			return ComparisonChain.start()
					.compare(o2.getVariationScore(), o1.getVariationScore())
					.compare(o1.getTerm().getGroupingKey(), o2.getTerm().getGroupingKey())
					.compare(o1.getVariant().getTerm().getGroupingKey(), o2.getVariant().getTerm().getGroupingKey())
					.result();
			
		}
	};
	
	public ScoredModel scorify(TermIndex termIndex) {
		LOGGER.info("Scorifying term index {}", termIndex.getName());

		// Filter terms with bad orthgraph
		doScoredModel(termIndex);

		return scoredModel;
		
	}

	private void doScoredModel(TermIndex termIndex) {
		scoredModel = new ScoredModel();
		scoredModel.importTermIndex(termIndex);
		scoredModel.sort(wrComparator);
		
		int size = scoredModel.getTerms().size();
		filterTerms();
		LOGGER.info("Filtered {} terms out of {}", size - scoredModel.getTerms().size(), size);
		
		int sizeBefore = 0;
		int sizeAfter = 0;
		for(ScoredTerm t:scoredModel.getTerms()) {
			if(t.getVariations().isEmpty())
				continue;
			List<ScoredVariation> sv = Lists.newArrayListWithExpectedSize(t.getVariations().size());
			sv.addAll(t.getVariations());
			sizeBefore += sv.size();
			filterVariations(sv);
			sizeAfter += sv.size();
			Collections.sort(sv, variationScoreComparator);
			t.setVariations(sv);
		}
		
		LOGGER.info("Filtered {} variants out of {}", sizeBefore - sizeAfter, sizeBefore);
		scoredModel.sort(wrComparator);
	}

	private void filterTerms() {
		Set<ScoredTerm> rem = Sets.newHashSet();
		for(ScoredTerm st:scoredModel.getTerms()) {
			if(StringUtils.getOrthographicScore(st.getTerm().getLemma()) < this.config.getOrthographicScoreTh())
				rem.add(st);
			else if(st.getTermIndependanceScore() < this.config.getTermIndependanceTh())
				rem.add(st);
		}
		scoredModel.removeTerms(rem);
	}

	private void filterVariations(List<ScoredVariation>  inputTerms) {
		Iterator<ScoredVariation> it = inputTerms.iterator();
		ScoredVariation v;
		while(it.hasNext()) {
			v = it.next();
			if(v.getVariantIndependanceScore() < config.getVariantIndependanceTh()
					|| v.getVariationScore() < config.getVariationScoreTh()
					) {
				it.remove();
			}
			else if(v.getExtensionAffix() != null) {
				if(v.getExtensionGainScore() < config.getExtensionGainTh()
						|| v.getExtensionSpecScore() < config.getExtensionSpecTh())
					it.remove();
			}
		}
		
	}
}
