package fr.univnantes.termsuite.eval.resources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import fr.univnantes.termsuite.framework.TermSuiteFactory;
import fr.univnantes.termsuite.framework.service.TermService;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.index.TermIndex;
import fr.univnantes.termsuite.index.TermIndexType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;

public class Tsv3ColFile {

	private static final Logger LOGGER = LoggerFactory.getLogger(Tsv3ColFile.class);
	
	private Path path;
	
	public Tsv3ColFile(Path path) {
		super();
		Preconditions.checkArgument(path.toFile().exists(), "File %s does not exist", path.toString());
		Preconditions.checkArgument(path.toFile().isFile(), "Not a file: %s", path.toString());
		this.path = path;
	}

	public List<String[]> getLines() throws IOException {
		return lines()
			.collect(Collectors.toList());
	}
	
	public Stream<TermService[]> pairs(TerminologyService sourceTermino, TerminologyService targetTermino) throws IOException {
		TermIndex sourceLemmaIndex = TermSuiteFactory.createTermIndex(sourceTermino, TermIndexType.LEMMA_LOWER_CASE);
		TermIndex targetLemmaIndex = TermSuiteFactory.createTermIndex(targetTermino, TermIndexType.LEMMA_LOWER_CASE);

		return lines().filter(line -> {
			if(!sourceLemmaIndex.containsKey(line[1])) {
				LOGGER.debug("Ignoring ref line <{}> (term not found in source terminology)", Joiner.on(" ").join(line));
				return false;
			}
			if(!targetLemmaIndex.containsKey(line[2])) {
				LOGGER.debug("Ignoring ref line <{}> (term not found in target terminology)", Joiner.on(" ").join(line));
				return false;
			}
			return true;
		}).map(line -> {
			List<Term> sources = sourceLemmaIndex.getTerms(line[1]);
			Collections.sort(sources, TermProperty.FREQUENCY.getComparator(true));
			List<Term> targets = targetLemmaIndex.getTerms(line[2]);
			Collections.sort(targets, TermProperty.FREQUENCY.getComparator(true));
			LOGGER.debug("Reading eval pair. Source: <{}>. Target: <{}>", sources.get(0), targets.get(0));
			return new TermService[]{sourceTermino.asTermService(sources.get(0)), targetTermino.asTermService(targets.get(0))};
		});
	}


	public Stream<String[]> lines() throws IOException {
		return Files.lines(path)
			.map(line -> line.contains("#") ? line.substring(0, line.indexOf('#')) : line)
			.map(line -> line.trim())
			.filter(line -> !line.isEmpty())
			.map(line -> {
				List<String> list = Arrays.stream(line.split("\t")).map(col -> col.trim()).collect(Collectors.toList());
				return list.toArray(new String[list.size()]);
			})
			.filter(line -> {
				if(line.length < 3) {
					LOGGER.warn("Ignoring line <{}>. Only {} columns", Joiner.on('t').join(line), line.length);
					return false;
				} else 
					return true;
			})
			.map(line -> {
				if(line.length > 3) { 
					LOGGER.warn("Line <{}> has {} columns (only three columns required). Ignoring additional columns.", Joiner.on('t').join(line), line.length);
					return new String[] {line[0], line[1], line[2]};
				} else
					return line;
			});
	}
}
