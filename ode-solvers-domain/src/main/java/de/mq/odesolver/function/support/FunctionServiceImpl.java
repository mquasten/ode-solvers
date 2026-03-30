package de.mq.odesolver.function.support;

import java.lang.reflect.Constructor;
import java.util.List;

import javax.script.Invocable;

import de.mq.odesolver.Result;
import de.mq.odesolver.function.Function;
import de.mq.odesolver.function.FunctionService;
import de.mq.odesolver.function.FunctionSolver;
import de.mq.odesolver.support.ExceptionUtil;
import de.mq.odesolver.support.OdeFunctionUtil;
import de.mq.odesolver.support.OdeFunctionUtil.Language;

class FunctionServiceImpl implements FunctionService {

	@Override
	public final FunctionSolver functionSolver(final Language language, final String function) {

		try {
			final OdeFunctionUtil odeFunctionUtil = newOdeFunctionUtil(language);
			return new FunctionSolverImpl(odeFunctionUtil, function);
		} catch (final Exception exception) {
			throw ExceptionUtil.translateToRuntimeException(exception);
		}
	}

	@Override
	public List<Result> solve(final Function function) {
		final var functionSolver = functionSolver(function.language(), function.function());
		return functionSolver.solve(function.k(), function.start(), function.stop(), function.steps());
	}

	@Override
	public final double validate(final Language language, final String function, final double x0, final double k[]) {

		try {
			final OdeFunctionUtil odeFunctionUtil = newOdeFunctionUtil(language);
			final Invocable invocable = odeFunctionUtil.prepareFunction(function);
			return odeFunctionUtil.invokeFunction(invocable, k, x0);
		} catch (final Exception exception) {
			throw ExceptionUtil.translateToRuntimeException(exception);
		}
	}

	@Override
	public double validate(Function function) {
		return validate(function.language(), function.function(), function.start(), function.k());
	}

	private OdeFunctionUtil newOdeFunctionUtil(final Language language) throws Exception {
		@SuppressWarnings("unchecked")
		final Constructor<? extends OdeFunctionUtil> constructor = (Constructor<? extends OdeFunctionUtil>) Class.forName("de.mq.odesolver.support.OdeFunctionUtilImpl")
				.getDeclaredConstructor(Language.class, String.class, boolean.class);
		constructor.setAccessible(true);
		return constructor.newInstance(language, "k", false);
	}

}
