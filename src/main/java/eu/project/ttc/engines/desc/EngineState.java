package eu.project.ttc.engines.desc;

public enum EngineState {

	ENABLED, DISABLED;
	
	public boolean isEnabled() {
		return this == ENABLED;
	}
	
	public boolean isDisabled() {
		return this == DISABLED;
	}

}
