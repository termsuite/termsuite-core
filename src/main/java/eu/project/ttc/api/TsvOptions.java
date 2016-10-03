package eu.project.ttc.api;

import java.util.List;

import com.google.common.collect.Lists;

import eu.project.ttc.engines.cleaner.TermProperty;

public class TsvOptions {
	
	private TermProperty[] properties = new TermProperty[] {TermProperty.GROUPING_KEY, TermProperty.FREQUENCY};
	private boolean showHeaders = true;
	private boolean showVariants = true;
	private boolean showScores = false;
	private boolean showRank = true;
	
	public TsvOptions properties(Iterable<TermProperty> properties) {
		List<TermProperty> list = Lists.newArrayList(properties);
		return properties(list.toArray(new TermProperty[list.size()])); 
	}
	
	public TsvOptions properties(TermProperty... properties) {
		this.properties = properties;
		return this;
	}

	public boolean showHeaders() {
		return showHeaders;
	}

	public TsvOptions showScores(boolean showScores) {
		this.showScores = showScores;
		return this;
	}
	
	public boolean isShowScores() {
		return showScores;
	}
	
	public TsvOptions showHeaders(boolean showHeaders) {
		this.showHeaders = showHeaders;
		return this;
	}

	public boolean isShowVariants() {
		return showVariants;
	}

	public TsvOptions setShowVariants(boolean showVariants) {
		this.showVariants = showVariants;
		return this;
	}

	public boolean isShowRank() {
		return showRank;
	}

	public TsvOptions showRank(boolean showRank) {
		this.showRank = showRank;
		return this;
	}

	public TermProperty[] properties() {
		return properties;
	}
}
