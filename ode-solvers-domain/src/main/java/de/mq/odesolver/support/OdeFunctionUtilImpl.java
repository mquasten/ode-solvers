package de.mq.odesolver.support;
import java.util.Map;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Function, die die rechte Seite einer gewoehnlichen DGL beschreibt als
 * Invocable aus einem String erzeugen und den Wert der n-ten Ableitung
 * berechnen.
 * 
 * @author mq
 *
 */
class OdeFunctionUtilImpl implements OdeFunctionUtil {

	private final Map<Language, String> functionPatterns = Map.of(Language.Nashorn, "function %s(%s, x) {return %s}", Language.Groovy, "def %s(%s, x) {return %s}");

	private final Map<Language, String> vectorFunctionPatterns = Map.of(Language.Nashorn, "function %s(%s, x) {var DoubleArrayType = Java.type(\"double[]\"); var dy=new DoubleArrayType(y.length);%s;return dy }", Language.Groovy, "def %s(%s, x) {double[] dy= new double[y.length]; %s; return dy}");
	private final Language language;

	private final String vectorName;
	private final boolean resultIsVector;

	OdeFunctionUtilImpl(final Language language) {
		this(language, "y", false);
	}
	
	OdeFunctionUtilImpl(final Language language, final boolean resultIsVector ) {
		this(language, "y", resultIsVector);
	}

	OdeFunctionUtilImpl(final Language language, final String vectorName, final boolean resultIsVector) {
		this.language = language;
		this.vectorName = vectorName;
		this.resultIsVector=resultIsVector;
	}

	private final String FUNCTION_NAME = "f";

	@Override
	public double invokeFunction(final Invocable invocable, final double[] vector, double x) {
		try {
			final Number result = ((Number) invocable.invokeFunction(FUNCTION_NAME, vector, x));
			notNullGuard(x, result);
			resultGuard(x, result.doubleValue());
			return result.doubleValue();
		} catch (final NoSuchMethodException | ScriptException e) {
			throw new IllegalStateException(e);
		} catch (final ClassCastException castException) {
			throw new IllegalStateException("Function do not return a Number.");
		}

	}

	private void notNullGuard(double x, final Object result) {
		if (result == null) {
			throw new IllegalStateException(String.format("Function returns null for x=%e, may be wrong vector size.", x));
		}
	}

	private void resultGuard(final Double x, final double result) {
		if (Double.isNaN(result)) {
			throw new IllegalStateException(String.format("Function returns NaN for x=%e, may be wrong vector size.", x));
		}
		if (Double.isInfinite(result)) {
			throw new IllegalArgumentException(String.format("Function returns Infinite for x=%e", x));
		}
	}

	/**
	 * Funktion die die rechte Seite einer gewoehnlichen DGL beschreibt aus einem
	 * String als Invocable erzeugen
	 * 
	 * @param function die Funktion als String, die Ableitungen sind y[0]: die 0.
	 *                 Ableitung, d.h. y y[1]: die 1. Ableitung, d.h. y'
	 * @return die compilierte Funktion
	 */
	@Override
	public Invocable prepareFunction(final String function) {

		final ScriptEngine engine = new ScriptEngineManager().getEngineByName(language.name().toLowerCase());
		final Compilable compilable = (Compilable) engine;
		final Invocable invocable = (Invocable) engine;
		
		final String pattern = this.resultIsVector  ? vectorFunctionPatterns.get(language): functionPatterns.get(language);
		final String statement = String.format(pattern, FUNCTION_NAME, vectorName, function);
		try {
			final CompiledScript compiled = compilable.compile(statement);
			compiled.eval();
			return invocable;
		} catch (final ScriptException e) {
			throw new IllegalStateException(String.format("Unable to compile function.", function), e.getCause());
		}

	}

}
