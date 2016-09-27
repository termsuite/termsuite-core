package eu.project.ttc.api;

import java.util.List;

import com.google.common.collect.Lists;

import eu.project.ttc.engines.cleaner.TermProperty;

public class TSVOptions {
	
	private TermProperty[] properties = new TermProperty[] {TermProperty.GROUPING_KEY, TermProperty.FREQUENCY};
	private boolean showHeaders = true;
	private boolean showVariants = true;
	private boolean showScores = false;
	private boolean showRank = true;
	
	public TSVOptions setProperties(Iterable<TermProperty> properties) {
		List<TermProperty> list = Lists.newArrayList(properties);
		return setProperties((TermProperty[]) list.toArray()); 
	}
	
	public TSVOptions setProperties(TermProperty... properties) {
		this.properties = properties;
		return this;
	}

	public boolean showHeaders() {
		return showHeaders;
	}

	public TSVOptions setShowScores(boolean showScores) {
		this.showScores = showScores;
		return this;
	}
	
	public boolean showScores() {
		return showScores;
	}
	
	public TSVOptions setShowHeaders(boolean showHeaders) {
		this.showHeaders = showHeaders;
		return this;
	}

	public boolean showVariants() {
		return showVariants;
	}

	public TSVOptions setShowVariants(boolean showVariants) {
		this.showVariants = showVariants;
		return this;
	}

	public boolean showRank() {
		return showRank;
	}

	public TSVOptions setShowRank(boolean showRank) {
		this.showRank = showRank;
		return this;
	}

	public TermProperty[] properties() {
		return properties;
	}
}
