package fr.univnantes.termsuite.tools.opt;

import java.util.List;

public interface CliOption {

	String getOptName();

	OptType getArgType();

	String getOptShortName();

	boolean hasArg();

	String getDescription();

	List<Object> getAllowedValues();

}