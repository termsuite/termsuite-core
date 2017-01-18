package fr.univnantes.termsuite.api;

import java.util.List;

import com.google.common.collect.Lists;

import fr.univnantes.termsuite.model.Property;
import fr.univnantes.termsuite.model.TermProperty;

public class TsvOptions {
	
	private Property<?>[] properties = new Property<?>[] {TermProperty.GROUPING_KEY, TermProperty.FREQUENCY};
	private boolean showHeaders = true;
	private boolean showVariants = true;
	private boolean showRank = true;
	private boolean tagsTermsHavingVariants = true;
	
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

	public boolean tagsTermsHavingVariants() {
		return tagsTermsHavingVariants;
	}

	public TsvOptions tagsTermsHavingVariants(boolean flag) {
		this.tagsTermsHavingVariants = flag;
		return this;
	}

}
