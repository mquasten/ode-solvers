package de.mq.odesolver.system.support;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.math3.ode.nonstiff.DormandPrince853Integrator;
import org.junit.jupiter.api.Test;

import de.mq.odesolver.solve.OdeResult;

class OdeSystemSolverImplTest {

	@Test
	void dormandPrince853Integrator() {
		final OdeSystemResultCalculator resultCalculator = new OdeSystemResultCalculator() {

			@Override
			public double[] calculate(double t, double[] y) {
				// y'=y
				return new double[] { y[0] };
			}
		};
		final var odeSystemSolver = new OdeSystemSolverImpl(DormandPrince853Integrator.class, resultCalculator);
		final double[] y0 = { 1d };
		final var xMin = 0.0;
		final var xMax = 1.0;
		final var steps = 1000;
		final var results = odeSystemSolver.solve(y0, xMin, xMax, steps);
		assertTrue(results.size() >= steps);
		final var last = results.get(results.size() - 1);
		assertEquals(xMax, last.x());

		final var first = results.get(0);
		assertEquals(xMin, first.x());

		for (int i = 0; i < results.size(); i++) {
			final OdeResult result = results.get(i);
			if (i > 0) {
				var delta = result.x() - results.get(i - 1).x();
				assertTrue(Math.abs(delta - 1e-3) < 1e-12);
			}
			assertEquals(OdeSystemSolverImpl.ERROR_SIZE, result.errorEstimaion());
			assertTrue(Math.abs(Math.exp(result.x()) - result.yDerivative(0)) < last.errorEstimaion());
		}

	}

}
