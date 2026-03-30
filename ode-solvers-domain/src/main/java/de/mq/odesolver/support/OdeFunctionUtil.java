package de.mq.odesolver.support;

import javax.script.Invocable;

public interface OdeFunctionUtil {
	
	
	public enum Language {
		Nashorn,
		Groovy;
	}

	double invokeFunction(Invocable invocable, double[] vector, double x);

	/**
	 * Funktion die die rechte Seite einer gewoehnlichen DGL beschreibt aus einem
	 * String als Invocable erzeugen
	 * 
	 * @param function die Funktion als String, die Ableitungen sind y[0]: die 0.
	 *                 Ableitung, d.h. y y[1]: die 1. Ableitung, d.h. y'
	 * @return die compilierte Funktion
	 */
	Invocable prepareFunction(String function);

}