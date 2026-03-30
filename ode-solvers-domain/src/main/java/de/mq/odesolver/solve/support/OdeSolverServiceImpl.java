package de.mq.odesolver.solve.support;

import java.lang.reflect.Constructor;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.nonstiff.DormandPrince853Integrator;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.script.Invocable;

import de.mq.odesolver.solve.Ode;
import de.mq.odesolver.solve.OdeResult;
import de.mq.odesolver.solve.OdeResultCalculator;
import de.mq.odesolver.solve.OdeSolver;
import de.mq.odesolver.solve.OdeSolverService;
import de.mq.odesolver.support.ExceptionUtil;
import de.mq.odesolver.support.OdeFunctionUtil;
import de.mq.odesolver.support.OdeFunctionUtil.Language;

class OdeSolverServiceImpl implements OdeSolverService {

	private final Map<Algorithm, Class<? extends OdeResultCalculator>> solvers = Map.of(Algorithm.EulerPolygonal, EulerCalculatorImpl.class, Algorithm.RungeKutta2ndOrder,
			RungeKutta2CalculatorImpl.class, Algorithm.RungeKutta4thOrder, RungeKutta4CalculatorImpl.class);

	private final Map<Algorithm, Class<? extends FirstOrderIntegrator>> systemSolvers = Map.of(Algorithm.DormandPrince853Integrator, DormandPrince853Integrator.class);

	@Override
	public final OdeSolver odeSolver(final Language language, final Algorithm algorithm, final String function) {

		try {

			if (algorithm.isSystem()) {
				final Class<? extends FirstOrderIntegrator> clazz = systemSolvers.get(algorithm);
				final Invocable invocable = newOdeFunctionUtil(language, true).prepareFunction(function);
				final OdeSystemResultCalculator resultCalculator = invocable.getInterface(OdeSystemResultCalculator.class);
				Objects.requireNonNull(resultCalculator, "Interface OdeSystemResultCalculator is missing.");

				return new OdeSystemSolverImpl(clazz, resultCalculator);
			} else {

				final OdeResultCalculator odeResultCalculator = solvers.get(algorithm).getDeclaredConstructor(OdeFunctionUtil.class, String.class).newInstance(newOdeFunctionUtil(language, false),
						function);
				return new OdeSolverImpl(odeResultCalculator);
			}
		} catch (final Exception exception) {
			throw ExceptionUtil.translateToRuntimeException(exception);
		}

	}

	@Override
	public List<OdeResult> solve(final Ode ode) {
		final OdeSolver odeSolver = odeSolver(ode.language(), ode.algorithm(), ode.ode());
		return odeSolver.solve(ode.y(), ode.start(), ode.stop(), ode.steps());
	}

	private OdeFunctionUtil newOdeFunctionUtil(final Language language, final boolean system) throws Exception {
		@SuppressWarnings("unchecked")
		final Constructor<? extends OdeFunctionUtil> constructor = (Constructor<? extends OdeFunctionUtil>) Class.forName("de.mq.odesolver.support.OdeFunctionUtilImpl")
				.getDeclaredConstructor(Language.class, boolean.class);
		constructor.setAccessible(true);
		return constructor.newInstance(language, system);
	}

	@Override
	public final double[] validateRightSide(final Language language, final String function, final double y0[], final double x0, final boolean system) {

		try {
			final OdeFunctionUtil odeFunctionUtil = newOdeFunctionUtil(language, system);
			final Invocable invocable = odeFunctionUtil.prepareFunction(function);
			if ( system) {
				final OdeSystemResultCalculator resultCalculator = invocable.getInterface(OdeSystemResultCalculator.class);
				return resultCalculator.f(y0, x0);
			}
			final double result = odeFunctionUtil.invokeFunction(invocable, y0, x0);
			return new double[] { result };

		} catch (final Exception exception) {
			throw ExceptionUtil.translateToRuntimeException(exception);
		}

	}

	@Override
	public double[] validateRightSide(final Ode ode) {
		Objects.requireNonNull(ode, "Ode is required.");
		Objects.requireNonNull(ode.algorithm(), "Algorithm is required.");
		return validateRightSide(ode.language(), ode.ode(), ode.y(), ode.start(), ode.algorithm().isSystem());
	}

}
