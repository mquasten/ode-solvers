package de.mq.odesolver.support;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

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

	private final Language language;

	private final String vectorName;

	OdeFunctionUtilImpl(final Language language) {
		this(language, "y");
	}

	OdeFunctionUtilImpl(final Language language, final String vectorName) {
		this.language = language;
		this.vectorName = vectorName;
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

	@Override
	public double[] invokeVectorFunction(final Invocable invocable, final double[] vector, double x) {
		try {
			final Object result = (Object) invocable.invokeFunction(FUNCTION_NAME, vector, x);
			notNullGuard(x, result);
			final double[] results = new double[vector.length];
			if (result instanceof ScriptObjectMirror) {
				final ScriptObjectMirror scriptObjectMirror = (ScriptObjectMirror) result;
				resultSizeGuard(vector, scriptObjectMirror.entrySet());
				for (int i = 0; i < vector.length; i++) {
					final Number value = (Number) scriptObjectMirror.get("" + i);
					resultGuard(x, value.doubleValue());
					results[i] = value.doubleValue();
				}
				return results;
			}

			if (result instanceof List) {
				final List<?> list = (List<?>) result;
				resultSizeGuard(vector, list);
				for (int i = 0; i < list.size(); i++) {
					final Number value = (Number) list.get(i);
					resultGuard(x, value.doubleValue());
					results[i] = value.doubleValue();
				}
				return results;

			}

			throw new IllegalArgumentException(String.format("Function return invalid type %s", result.getClass()));
		} catch (final NoSuchMethodException | ScriptException e) {
			throw new IllegalStateException(e);
		} catch (final ClassCastException castException) {
			throw new IllegalStateException(String.format("Can't convert value to Number x=%e.", x));
		}

	}

	private void resultSizeGuard(final double[] y, final Collection<?> collection) {
		if (y.length != collection.size()) {
			throw new IllegalArgumentException(String.format("Resultvector have %d components, expected %d.", collection.size(), y.length));
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
		final String statement = String.format(functionPatterns.get(language), FUNCTION_NAME, vectorName, function);

		try {
			final CompiledScript compiled = compilable.compile(statement);
			compiled.eval();
			return invocable;
		} catch (final ScriptException e) {
			throw new IllegalStateException(String.format("Unable to compile function.", function), e.getCause());
		}

	}

}
