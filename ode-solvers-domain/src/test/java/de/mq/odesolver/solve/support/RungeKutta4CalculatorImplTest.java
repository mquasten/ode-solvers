package de.mq.odesolver.solve.support;

import static de.mq.odesolver.support.OdeFunctionUtilFactory.newOdeFunctionUtil;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import de.mq.odesolver.solve.OdeResultCalculator;
import de.mq.odesolver.support.OdeFunctionUtil.Language;

class RungeKutta4CalculatorImplTest {

	private final double maxTol = 1e-6;
	// Papuala Seite 238
	private final OdeResultCalculator odeResultCalculator = new RungeKutta4CalculatorImpl(
			last -> last.yDerivative(0) - last.x());

	@Test
	final void calculateforFirstOrderOdeLamda() {
		// Papuala Seite 238
		final double[] y = odeResultCalculator.calculate(new OdeResultImpl(new double[] { 0 }, 0), 0.1);
		assertEquals(1, y.length);
		assertEquals(0. - 0.005171, y[0], maxTol);
	}

	@ParameterizedTest
	@EnumSource
	final void calculateforFirstOrderOdeString(final Language language) {
		// Papuala Seite 238
		final OdeResultCalculator odeResultCalculator = new RungeKutta4CalculatorImpl(newOdeFunctionUtil(language),
				"y[0]-x");
		final double[] y = odeResultCalculator.calculate(new OdeResultImpl(new double[] { 0 }, 0), 0.1);
		assertEquals(1, y.length);
		assertEquals(0. - 0.005171, y[0], maxTol);
	}

	@Test
	final void errorEstimaion() {
		assertEquals(0.1d / 15d, odeResultCalculator.errorEstimaion(1.1, 1), 1e-17);
	}

	@Test
	final void calculateforSecondOrderOdeLamda() {
		// Papula Seite 246
		final OdeResultCalculator odeResultCalculator = new RungeKutta4CalculatorImpl(
				last -> -2 * last.yDerivative(1) + 3 * last.yDerivative(0));

		final double[] y = odeResultCalculator.calculate(new OdeResultImpl(new double[] { 0, 4 }, 0), 0.1);
		assertEquals(2, y.length);
		assertEquals(0.364333, y[0], maxTol);
		assertEquals(3.327683, y[1], maxTol);
	}

	@ParameterizedTest
	@EnumSource
	final void calculateforSecondOrderOdeString(final Language language) {
		// Papula Seite 246
		final OdeResultCalculator odeResultCalculator = new RungeKutta4CalculatorImpl(newOdeFunctionUtil(language),
				"-2*y[1]+3*y[0]");

		final double[] y = odeResultCalculator.calculate(new OdeResultImpl(new double[] { 0, 4 }, 0), 0.1);
		assertEquals(2, y.length);
		assertEquals(0.364333, y[0], maxTol);
		assertEquals(3.327683, y[1], maxTol);
	}

	@Test
	final void calculateWrongOrder() {
		assertThrows(IllegalArgumentException.class,
				() -> odeResultCalculator.calculate(new OdeResultImpl(new double[] { 0, 4, 0 }, 0), 0.1));
	}
}
