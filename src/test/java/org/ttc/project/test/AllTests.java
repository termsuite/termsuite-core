package org.ttc.project.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.ttc.project.test.engines.TermClassifierSpec;
import org.ttc.project.test.variants.VariantRuleSpec;
import org.ttc.project.test.variants.VariantRuleYamlIOSpec;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	VariantRuleSpec.class, 
	VariantRuleYamlIOSpec.class, 
	TermSpec.class, 
	SegmentationSpec.class, 
	DocumentSpec.class, 
	TermUtilsSpec.class,
	TermClassifierSpec.class,
	TermClassifierSpec.class,
	OccurrenceBufferSpec.class,
	SimilarityDistanceSpec.class,
	JSONTermIndexIOSpec.class,
	ContextVectorSpec.class,
	TermOccurrenceUtilsSpec.class,
	TermClassSpec.class,
//	TeiCollectionReaderSpec.class,
	CrossTableSpec.class
	})
public class AllTests {

}
