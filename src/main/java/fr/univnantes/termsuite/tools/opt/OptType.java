package fr.univnantes.termsuite.tools.opt;

public enum OptType {
	T_TERM_LIST("TERM_LIST"),
	T_INT("INT"),
	T_FLOAT("FLOAT"),
	T_INT_OR_FLOAT("INT or FLOAT"),
	T_STRING("STRING"),
	T_FILE("FILE"),
	T_DIR("DIR"),
	T_LANG("LANG"),
	T_ENC("ENC"),
	T_NONE(null)
	;
	
	private String str;

	private OptType(String str) {
		this.str = str;
	}
	
	public String getStr() {
		return str;
	}
}
