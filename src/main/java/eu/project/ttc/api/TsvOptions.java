package eu.project.ttc.api;

import java.util.List;

import com.google.common.collect.Lists;

import eu.project.ttc.models.Property;
import eu.project.ttc.models.TermProperty;

public class TsvOptions {
	
	private Property<?>[] properties = new Property<?>[] {TermProperty.GROUPING_KEY, TermProperty.FREQUENCY};
	private boolean showHeaders = true;
	private boolean showVariants = true;
	private boolean showRank = true;
	private int maxVariantsPerTerm = Integer.MAX_VALUE;
	
	public TsvOptions properties(Iterable<Property<?>> properties) {
		List<Property<?>> list = Lists.newArrayList(properties);
		return properties(list.toArray(new Property<?>[list.size()])); 
	}
	
	public TsvOptions properties(Property<?>... properties) {
		this.properties = properties;
		return this;
	}

	public boolean showHeaders() {
		return showHeaders;
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

	public Property<?>[] properties() {
		return properties;
	}

	public TsvOptions maxVariantsPerTerm(int maxVariantsPerTerm) {
		this.maxVariantsPerTerm = maxVariantsPerTerm;
		return this;
	}
	
	public int getMaxVariantsPerTerm() {
		return maxVariantsPerTerm;
	}
}
