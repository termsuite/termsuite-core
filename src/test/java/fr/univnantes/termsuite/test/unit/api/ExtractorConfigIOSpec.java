package fr.univnantes.termsuite.test.unit.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import fr.univnantes.termsuite.api.ExtractorConfigIO;
import fr.univnantes.termsuite.api.ExtractorOptions;
import fr.univnantes.termsuite.api.TermSuite;
import fr.univnantes.termsuite.engines.cleaner.TerminoFilterOptions.FilterType;
import fr.univnantes.termsuite.engines.contextualizer.LogLikelihood;
import fr.univnantes.termsuite.engines.contextualizer.MutualInformation;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.test.func.FunctionalTests;

public class ExtractorConfigIOSpec {

	
	@Test
	public void testToJson() {
		ExtractorOptions opts = TermSuite.getDefaultExtractorConfig(Lang.EN);
		opts.getContextualizerOptions().setAssociationRate(MutualInformation.class);
		opts.getPreFilterConfig().by(TermProperty.SPECIFICITY).keepTopN(156);
		String json = ExtractorConfigIO.toJson(opts);
		
		assertThat(json)
			.contains("\"property\" : \"SPECIFICITY\"")
			.contains("\"top-n\" : 156")
			.contains("\"association-rate\" : \"fr.univnantes.termsuite.metrics.MutualInformation\"")
			;
	}
	
	
	@Test
	public void testFromJson() throws IOException {
		ExtractorOptions opts = ExtractorConfigIO.fromJson(FunctionalTests.EXTRACTOR_CONFIG_1);

		assertTrue(0.1d == opts.getMorphologicalConfig().getAlpha());
		assertTrue(0.2 == opts.getMorphologicalConfig().getBeta());
		assertTrue(0.3 == opts.getMorphologicalConfig().getGamma());
		assertTrue(0.4 == opts.getMorphologicalConfig().getDelta());
		assertTrue(opts.getContextualizerOptions().getAssociationRate().equals(LogLikelihood.class));
		assertTrue(opts.getPreFilterConfig().getFilterProperty().equals(TermProperty.FREQUENCY));
		assertTrue(opts.getPreFilterConfig().getFilterType().equals(FilterType.THRESHOLD));
		
		assertTrue(opts.getPostFilterConfig().getFilterProperty().equals(TermProperty.SPECIFICITY));
		assertTrue(opts.getPostFilterConfig().getFilterType().equals(FilterType.TOP_N));
		assertTrue(opts.getPostFilterConfig().getTopN() == 156);
	}

}
