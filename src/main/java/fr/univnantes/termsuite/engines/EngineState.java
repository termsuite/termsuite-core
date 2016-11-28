package fr.univnantes.termsuite.engines;

public enum EngineState {

	ENABLED, DISABLED;
	
	public boolean isEnabled() {
		return this == ENABLED;
	}
	
	public boolean isDisabled() {
		return this == DISABLED;
	}

}
