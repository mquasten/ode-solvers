package de.mq.odesolver.system.support;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SimpleFirstOrderDifferentialEquationsImplTest {

	@Test
	void dimension() {
		final OdeSystemResultCalculator resultCalculator = Mockito.mock(OdeSystemResultCalculator.class);
		final int dimension = (int) (Math.random() * 10d);

		final var dgl = new SimpleFirstOrderDifferentialEquationsImpl(resultCalculator, dimension);
		assertEquals(dimension, dgl.getDimension());
	}

	@Test
	void computeDerivatives() {
		final OdeSystemResultCalculator resultCalculator = Mockito.mock(OdeSystemResultCalculator.class);
		final double[] dy = { 1d, 2d, 3d };
		final var x = randomDouble();
		final var dgl = new SimpleFirstOrderDifferentialEquationsImpl(resultCalculator, 3);
		// y, y', Y''
		final double[] y = { randomDouble(), randomDouble(), randomDouble() };
		Mockito.when(resultCalculator.calculate(x, y)).thenReturn(dy);
		final double[] result = { Double.NaN, Double.NaN, Double.NaN };
		dgl.computeDerivatives(x, y, result);

		assertArrayEquals(dy, result, 1e-99);

	}

	private double randomDouble() {
		return Math.random() * 1000d;
	}

	@Test
	void computeDerivativesWrongVectors() {
		final OdeSystemResultCalculator resultCalculator = Mockito.mock(OdeSystemResultCalculator.class);
		final double[] dy = { 1d, 2d, 3d };
		final var x = randomDouble();
		final var dgl = new SimpleFirstOrderDifferentialEquationsImpl(resultCalculator, 3);
		// y, y', Y''
		final double[] y = { randomDouble(), randomDouble(), randomDouble() };
		Mockito.when(resultCalculator.calculate(x, y)).thenReturn(dy);
		final double[] result = { Double.NaN, Double.NaN, Double.NaN };

		assertEquals(String.format(SimpleFirstOrderDifferentialEquationsImpl.MESSAGE_REQUIRED_FORMAT, "y"),
				assertThrows(IllegalArgumentException.class, () -> dgl.computeDerivatives(x, null, result)).getMessage());
		assertEquals(String.format(SimpleFirstOrderDifferentialEquationsImpl.MESSAGE_REQUIRED_FORMAT, "y'"),
				assertThrows(IllegalArgumentException.class, () -> dgl.computeDerivatives(x, y, null)).getMessage());
		assertEquals(String.format(SimpleFirstOrderDifferentialEquationsImpl.WRONG_MESSAGE_FORMAT, "y", 2, 3),
				assertThrows(IllegalArgumentException.class, () -> dgl.computeDerivatives(x, new double[] { randomDouble(), randomDouble() }, result)).getMessage());
		assertEquals(String.format(SimpleFirstOrderDifferentialEquationsImpl.WRONG_MESSAGE_FORMAT, "y'", 2, 3),
				assertThrows(IllegalArgumentException.class, () -> dgl.computeDerivatives(x, y, new double[] { randomDouble(), randomDouble() })).getMessage());
	}

}
