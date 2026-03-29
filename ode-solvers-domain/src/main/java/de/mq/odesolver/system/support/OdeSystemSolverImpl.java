package de.mq.odesolver.system.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.nonstiff.DormandPrince853Integrator;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;
import org.apache.groovy.util.Maps;

import de.mq.odesolver.solve.OdeResult;
import de.mq.odesolver.solve.OdeSolver;

class OdeSystemSolverImpl implements OdeSolver {

	static final double ERROR_SIZE = 1.0e-10;
	private final Class<? extends FirstOrderIntegrator> firstOrderIntegratorClass;
	private final OdeSystemResultCalculator resultCalculator;

	private final Map<Class<? extends FirstOrderIntegrator>, Function<Double, FirstOrderIntegrator>> integrators = Maps.of(DormandPrince853Integrator.class,
			stepSize -> new DormandPrince853Integrator(stepSize, stepSize, ERROR_SIZE, ERROR_SIZE));

	OdeSystemSolverImpl(final Class<? extends FirstOrderIntegrator> firstOrderIntegratorClass, final OdeSystemResultCalculator resultCalculator) {
		this.firstOrderIntegratorClass = firstOrderIntegratorClass;
		this.resultCalculator = resultCalculator;
	}

	@Override
	public List<OdeResult> solve(final double[] y0, final double start, final double stop, final int steps) {

		final FirstOrderDifferentialEquations ode = new SimpleFirstOrderDifferentialEquationsImpl(resultCalculator, y0.length);

		final double[] y = y0.clone();

		final List<OdeResult> results = new ArrayList<>();
		final double stepSize = (stop - start) / steps;
		final FirstOrderIntegrator firstOrderIntegrator = integrators.get(firstOrderIntegratorClass).apply(stepSize);
		final StepHandler stepHandler = new StepHandler() {
			public void init(double t0, double[] y0, double t) {
				results.clear();
				results.add(new OdeSystemResultImpl(y0.clone(), t0, ERROR_SIZE));
			}

			public void handleStep(StepInterpolator interpolator, boolean isLast) {
				results.add(new OdeSystemResultImpl(interpolator.getInterpolatedState().clone(), interpolator.getCurrentTime(), ERROR_SIZE));

			}
		};
		firstOrderIntegrator.addStepHandler(stepHandler);

		firstOrderIntegrator.integrate(ode, start, y, stop, y);
		return results;
	}

}
