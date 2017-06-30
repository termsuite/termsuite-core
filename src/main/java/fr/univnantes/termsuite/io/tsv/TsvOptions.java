package fr.univnantes.termsuite.io.tsv;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import fr.univnantes.termsuite.api.RelationOrdering;
import fr.univnantes.termsuite.api.TermOrdering;
import fr.univnantes.termsuite.model.Property;
import fr.univnantes.termsuite.model.TermProperty;

public class TsvOptions {
	
	private List<TsvPropertyConfig> properties = null;
	private boolean showHeaders = true;
	private boolean showVariants = true;
	private boolean showRank = true;
	private boolean tagsTermsHavingVariants = true;
	private TermOrdering termOrdering = TermOrdering.natural();
	private RelationOrdering variantOrdering = RelationOrdering.byRank();
	
	public boolean showHeaders() {
		return showHeaders;
	}

	public TsvOptions property(Property<?> property, TsvPropertyType type) {
		Preconditions.checkArgument(
				type != TsvPropertyType.VARIATION_SOURCE_TERM || (property instanceof TermProperty),
				"Properties of variation source terms can only be of type TermProperty. Got property %s and type %s",
				property,
				type);
		if(properties == null)
			properties = new ArrayList<>();
		this.properties.add(new TsvPropertyConfig(type, property));
		return this;
	}

	public TsvOptions baseOrVariationTargetProperty(Property<?> property) {
		return property(property, TsvPropertyType.BASE_TERM_OR_VARIATION_TARGET_TERM);
	}

	public TsvOptions sourceProperty(Property<?> property) {
		return property(property, TsvPropertyType.VARIATION_SOURCE_TERM);
	}

	public TsvOptions property(Property<?> property) {
		return this.baseOrVariationTargetProperty(property);
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

	public List<TsvPropertyConfig> properties() {
		if(this.properties == null)
			return Lists.newArrayList(
				new TsvPropertyConfig(TermProperty.GROUPING_KEY),
				new TsvPropertyConfig(TermProperty.FREQUENCY)
				);
		else
			return this.properties;
	}	

	public List<TsvPropertyConfig> getProperties() {
		return properties;
	}
	
	public boolean tagsTermsHavingVariants() {
		return tagsTermsHavingVariants;
	}

	public TsvOptions tagsTermsHavingVariants(boolean flag) {
		this.tagsTermsHavingVariants = flag;
		return this;
	}
	
	public TsvOptions setTermOrdering(TermOrdering termOrdering) {
		this.termOrdering = termOrdering;
		return this;
	}
	
	public TermOrdering getTermOrdering() {
		return termOrdering;
	}
	
	public RelationOrdering getVariantOrdering() {
		return variantOrdering;
	}
	
	public TsvOptions setVariantOrdering(RelationOrdering variantOrdering) {
		this.variantOrdering = variantOrdering;
		return this;
	}
	
	public TsvOptions setProperties(Iterable<Property<?>> properties) {
		this.properties = Lists.newArrayList();
		for(Property<?> p:properties)
			this.properties.add(new TsvPropertyConfig(p));
		return this;
	}
	
}
