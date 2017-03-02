package fr.univnantes.termsuite.tools.opt;

public interface CliOption {

	String getOptName();

	OptType getArgType();

	String getOptShortName();

	boolean hasArg();

	String getDescription();

}