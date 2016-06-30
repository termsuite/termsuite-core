package eu.project.ttc.tools.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;

import eu.project.ttc.models.Component;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermVariation;
import eu.project.ttc.models.VariationType;
import eu.project.ttc.models.Word;

/**
 * 
 * A tool that generates all control files required for 
 * functional tests from a {@link TermIndex}.
 * 
 * @author Damien Cram
 *
 */
public class ControlFilesGenerator {
	
	private TermIndex termIndex;
	
	
	public ControlFilesGenerator(TermIndex termIndex) {
		super();
		this.termIndex = termIndex;
	}


	/**
	 * 
	 * @param directory
	 * 			the directory where to create the files.
	 */
	public void generate(File directory) throws IOException {
		if(!directory.exists())
			directory.mkdirs();
		
		Set<String> distinctRuleNames = Sets.newHashSet();
		Set<TermVariation> variations = getVariations();
		for(TermVariation tv:variations)
			if(tv.getVariationType() == VariationType.SYNTACTICAL)
				distinctRuleNames.add((String)tv.getInfo());

		/*
		 * Write syntactic rules
		 */
		for(String ruleName:distinctRuleNames) {
			String pathname = directory.getAbsolutePath() + "/syntactic-" + ruleNametoFileName(ruleName) + ".txt";
			writeVariations(pathname, selectTermVariations(VariationType.SYNTACTICAL, ruleName));
		}
		
		/*
		 * Write prefix variations
		 */
		String prefixPath = directory.getAbsolutePath() + "/prefixes.txt";
		writeVariations(prefixPath, selectTermVariations(VariationType.IS_PREFIX_OF));
		

		/*
		 * Write derivative variations
		 */
		String derivativePath = directory.getAbsolutePath() + "/derivates.txt";
		writeVariations(derivativePath, selectTermVariations(VariationType.DERIVES_INTO));

		/*
		 * Write compounds
		 */
		String compoundPath = directory.getAbsolutePath() + "/compounds.txt";
		writeCompounds(compoundPath);
		
	}

	private static String ruleNametoFileName(String ruleName) {
		return ruleName.replaceAll("\\|", "-or-");
	}


	private void writeCompounds(String filePath) throws IOException {
		Writer writer = new FileWriter(filePath);
		for(Term t:termIndex.getTerms()) {
			if(t.isSingleWord() && t.isCompound()) {
				List<String> componentStrings = Lists.newArrayList();
				Word word = t.getWords().get(0).getWord();
				for(Component c:word.getComponents()) {
					componentStrings.add(String.format("%s:%s", 
							word.getLemma().substring(c.getBegin(), c.getEnd()),
							c.getLemma()
							));
				}
				
				writer.append(String.format("%s\t%s\t%s%n", 
						t.getGroupingKey(),
						word.getCompoundType(),
						Joiner.on("|").join(componentStrings)
					));
			}
		}
		writer.flush();
		writer.close();
		
	}
	
	private void writeVariations(String path, Collection<TermVariation> variations) throws IOException {
		Writer writer = new FileWriter(path);
		for(TermVariation tv:variations) {
			writer.append(String.format("%s\t%s\t%s\t%s%n", 
					tv.getBase().getGroupingKey(),
					tv.getVariant().getGroupingKey(),
					tv.getVariationType(),
					tv.getInfo()
				));
		}
		writer.flush();
		writer.close();
	}

	
	private Set<TermVariation> getVariations() {
		Set<TermVariation> variations = Sets.newHashSet();
		for(Term t:termIndex.getTerms()) {
			for(TermVariation tv:t.getVariations())
				variations.add(tv);
			for(TermVariation tv:t.getBases())
				variations.add(tv);
		}
		return variations;
	}


	private Set<TermVariation> selectTermVariations(VariationType type, String ruleName) {
		Set<TermVariation> selected = Sets.newHashSet();
		for(TermVariation tv:selectTermVariations(type))
			if(Objects.equal(ruleName, tv.getInfo()))
				selected.add(tv);
		return selected;
	}
	
	private Set<TermVariation> selectTermVariations(VariationType type) {
		Set<TermVariation> selected = Sets.newHashSet();
		for(TermVariation tv:getVariations())
			if(tv.getVariationType() == type)
				selected.add(tv);
		return selected;
	}


}
