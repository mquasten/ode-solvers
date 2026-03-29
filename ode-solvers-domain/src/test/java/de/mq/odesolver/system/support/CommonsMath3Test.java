package de.mq.odesolver.system.support;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.nonstiff.DormandPrince853Integrator;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;
import org.junit.jupiter.api.Test;

import de.mq.odesolver.Result;
import de.mq.odesolver.solve.OdeResult;

class CommonsMath3Test {

	private static final double STEP_SIZE = 1.0e-3;
	private static final double ERROR_SIZE = 1.0e-10;

	// y'=y
	// allgemeine Lösung: y=C*exp(x)
	// y(0)=1: spezielle Lösung: y=exp(x);
	private final FirstOrderDifferentialEquations dgl01 = new FirstOrderDifferentialEquations() {

		@Override
		public int getDimension() {
			return 1;
		}

		@Override
		public void computeDerivatives(final double t, final double[] y, final double[] yDot) throws MaxCountExceededException, DimensionMismatchException {
			yDot[0] = y[0];
		}

	};

	// y''=-2*y+3*y
	// allgemeine Lösung: y= C1*exp(x)+C2*exp(-3 * x)
	// y(0)=0, y'(0)=4: spezielle Lösung: y=exp(x)-exp(-3 * x)
	private final FirstOrderDifferentialEquations dgl02 = new FirstOrderDifferentialEquations() {

		@Override
		public int getDimension() {
			return 2;
		}

		@Override
		public void computeDerivatives(final double t, final double[] y, final double[] yDot) throws MaxCountExceededException, DimensionMismatchException {
			yDot[0] = y[1];
			yDot[1] = 3 * y[0] - 2 * y[1];
		}

	};

	@Test
	void solveSystemFirstOrderOde() {

		final List<OdeResult> results = new ArrayList<>();

		final double xMin = 0.0;
		final double xMax = 2.0;
		final double[] y = new double[] { 1.0 };
		assertEquals(xMax, solveOde(dgl01, results, xMin, xMax, y));

		final int lastIndex = results.size() - 1;

		assertEquals(y[0], results.get(lastIndex).yDerivative(0));
		assertEquals(xMax, results.get(lastIndex).x());
		assertTrue(Math.abs(Math.exp(xMax) - y[0]) < ERROR_SIZE);

		IntStream.range(1, results.size()).forEach(i -> {
			final Result result = results.get(i);
			assertTrue(Math.abs(Math.exp(result.x()) - result.yDerivatives()[0]) < ERROR_SIZE);
			if (result.x() < xMax) {
				assertTrue(STEP_SIZE - (result.x() - results.get(i - 1).x()) < ERROR_SIZE);
			}

		});
	}

	private double solveOde(final FirstOrderDifferentialEquations ode, final List<OdeResult> results, double xMin, double xMax, double[] y) {
		final FirstOrderIntegrator firstOrderIntegrator = new DormandPrince853Integrator(STEP_SIZE, STEP_SIZE, ERROR_SIZE, ERROR_SIZE);
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
		return firstOrderIntegrator.integrate(ode, xMin, y, xMax, y);

	}

	@Test
	void solveSystemSecondOrderOde() {

		final List<OdeResult> results = new ArrayList<>();

		final Function<Double, Double> resultDlg = x -> Math.exp(x) - Math.exp(-3 * x);
		final Function<Double, Double> dy = x -> Math.exp(x) + 3 * Math.exp(-3 * x);
		final double xMin = 0.0;
		final double xMax = 1.0;
		final double[] y = { 0, 4 };
		assertEquals(xMax, solveOde(dgl02, results, xMin, xMax, y));

		final int lastIndex = results.size() - 1;

		assertEquals(y[0], results.get(lastIndex).yDerivative(0));
		assertEquals(xMax, results.get(lastIndex).x());
		assertTrue(Math.abs(resultDlg.apply(xMax) - y[0]) < ERROR_SIZE);
		assertTrue(Math.abs(dy.apply(xMax) - y[1]) < ERROR_SIZE);

		IntStream.range(0, results.size()).forEach(i -> {
			final Result result = results.get(i);
			assertTrue(Math.abs(result.yDerivative(0) - resultDlg.apply(result.x())) < ERROR_SIZE);
			assertTrue(Math.abs(result.yDerivative(1) - dy.apply(result.x())) < ERROR_SIZE);
			if ((result.x() < xMax) && (i > 0)) {
				assertTrue(STEP_SIZE - (result.x() - results.get(i - 1).x()) < ERROR_SIZE);
			}

		});

	}

}
