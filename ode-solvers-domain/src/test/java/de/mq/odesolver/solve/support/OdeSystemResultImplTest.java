package de.mq.odesolver.solve.support;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import de.mq.odesolver.solve.OdeResult;

class OdeSystemResultImplTest {

	static final double[] Y = { randomDouble(), randomDouble(), randomDouble() };
	static final double X = randomDouble();

	final double ERROR_ESTIMATION = randomDouble();

	final OdeResult odeResult = new OdeResultImpl(Y, X, ERROR_ESTIMATION);

	private static double randomDouble() {
		return Math.random() * 10;
	}

	@Test
	void order() {
		assertEquals(Y.length, odeResult.order());
	}

	@Test
	void x() {
		assertEquals(X, odeResult.x());
	}

	@Test
	void errorEstimaion() {
		assertEquals(ERROR_ESTIMATION, odeResult.errorEstimaion());
	}

	@Test
	void yDerivatives() {
		assertArrayEquals(Y, odeResult.yDerivatives(), 1e-99);
		IntStream.range(0, Y.length).forEach(i -> assertEquals(Y[i], odeResult.yDerivative(i)));
	}
}
