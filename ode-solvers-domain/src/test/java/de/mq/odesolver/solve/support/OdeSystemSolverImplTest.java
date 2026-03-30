package de.mq.odesolver.solve.support;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Function;
import java.util.stream.IntStream;

import org.apache.commons.math3.ode.nonstiff.DormandPrince853Integrator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import de.mq.odesolver.solve.OdeResult;
import de.mq.odesolver.support.OdeFunctionUtil.Language;
import de.mq.odesolver.support.OdeFunctionUtilFactory;

class OdeSystemSolverImplTest {

	@ParameterizedTest
	@EnumSource
	void dormandPrince853IntegratorFirstOrderAsSystem(final Language language) {
		
		final OdeSystemResultCalculator resultCalculator = OdeFunctionUtilFactory.newOdeFunctionUtil(language, true).prepareFunction("dy[0]=y[0]").getInterface(OdeSystemResultCalculator.class);
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
	
	@ParameterizedTest
	@EnumSource
	void dormandPrince853IntegratorSecondOrderAsSystem(final Language language) {
		
		final OdeSystemResultCalculator resultCalculator = OdeFunctionUtilFactory.newOdeFunctionUtil(language, true).prepareFunction("dy[0]=y[1]; dy[1]=3*y[0]-2*y[1];").getInterface(OdeSystemResultCalculator.class);
		final var odeSystemSolver = new OdeSystemSolverImpl(DormandPrince853Integrator.class, resultCalculator);
		
		final Function<Double, Double> resultDlg = x -> Math.exp(x) - Math.exp(-3 * x);
		final Function<Double, Double> dy = x -> Math.exp(x) + 3 * Math.exp(-3 * x);
		
		final var expectedStepSize = 1e-3;
		final double xMin = 0.0;
		final double xMax = 1.0;
		final double[] y = { 0, 4 };
		final var steps = 1000;
	
		final var results = odeSystemSolver.solve( y, xMin,xMax, steps);

		final int lastIndex = results.size() - 1;

		assertEquals(y[0], results.get(0).yDerivative(0));
		assertEquals(y[1], results.get(0).yDerivative(1));
		assertEquals(xMax, results.get(lastIndex).x());
		assertTrue(Math.abs(resultDlg.apply(xMax) - results.get(lastIndex).yDerivative(0)) < OdeSystemSolverImpl.ERROR_SIZE);
		assertTrue(Math.abs(dy.apply(xMax) - results.get(lastIndex).yDerivative(1)) < OdeSystemSolverImpl.ERROR_SIZE);

		IntStream.range(0, results.size()).forEach(i -> {
			final OdeResult result = results.get(i);
			assertTrue(Math.abs(result.yDerivative(0) - resultDlg.apply(result.x())) < OdeSystemSolverImpl.ERROR_SIZE);
			assertTrue(Math.abs(result.yDerivative(1) - dy.apply(result.x())) < OdeSystemSolverImpl.ERROR_SIZE);
			assertEquals(result.errorEstimaion(),  OdeSystemSolverImpl.ERROR_SIZE);
			if ((result.x() < xMax) && (i > 0)) {
				assertTrue(expectedStepSize - (result.x() - results.get(i - 1).x()) < OdeSystemSolverImpl.ERROR_SIZE);
			}

		});

		
	}
	
	

}
