package eu.project.ttc.eval.bilangaligner;

import java.util.Arrays;
import java.util.List;

import org.assertj.core.util.Lists;

public class ConfigListBuilder {

	private List<Integer> frequencyTh = Lists.newArrayList(1);
	private List<Integer> scopes = Lists.newArrayList(3);
	
	public static ConfigListBuilder  start() {
		return new ConfigListBuilder();
	}
	
	public ConfigListBuilder frequencies(Integer... frequencies) {
		this.frequencyTh = Arrays.asList(frequencies);
		return this;
	}

	public ConfigListBuilder scopes(Integer... scopes) {
		this.scopes = Arrays.asList(scopes);
		return this;
	}

	
	public List<TerminoConfig> list() {
		List<TerminoConfig> configs = Lists.newArrayList();
		for(Integer frequencyTh:frequencyTh)
			for(Integer scope:scopes)
				configs.add(new TerminoConfig().setFrequencyTh(frequencyTh).setScope(scope));
		
		return configs;
	}
}
