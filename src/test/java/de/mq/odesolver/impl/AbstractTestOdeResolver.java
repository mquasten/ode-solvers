package de.mq.odesolver.impl;

import static de.mq.odesolver.impl.OdeResultImpl.doubleArray;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.mq.odesolver.OdeResolver;
import de.mq.odesolver.OdeResult;

abstract class AbstractTestOdeResolver {

	enum TestDgl {
		DGL01(firstOderOdeArguments -> firstOderOdeArguments.yDerivative(0) - firstOderOdeArguments.x(), "y[0]-x"),
		DGL02(firstOderOdeArguments -> 0d, "parseFloat(0)"),
		DGL03(firstOderOdeArguments -> Math.sqrt(firstOderOdeArguments.yDerivative(0))
				+ firstOderOdeArguments.yDerivative(0), "Math.sqrt(y[0])+y[0]"),
		DGL04(firstOderOdeArguments -> 3 * firstOderOdeArguments.yDerivative(0)
				- 2 * firstOderOdeArguments.yDerivative(1), "3*y[0]-2*y[1]");

		private final Function<OdeResult, Double> odeFunction;
		private String functionAsString;

		private TestDgl(final Function<OdeResult, Double> odeFunction, final String functionAsString) {
			this.odeFunction = odeFunction;
			this.functionAsString = functionAsString;
		}

		Function<OdeResult, Double> odeFunction() {
			return this.odeFunction;
		}

		String functionAsString() {
			return this.functionAsString;
		}

	}

	@Test
	void solveExamplePapula1() {
		// Seite 238 Papula Formelsammlung
		final double maxTol = 1e-6;
		final double[] expected = { 0, -0.005171, -0.021403, -0.049859 };

		// y' = y - x;
		final OdeResolver odeResolver = newOdeResolver(TestDgl.DGL01);
		final List<OdeResult> results = odeResolver.solve(doubleArray(0), 0, 0.3, 3);
		assertEquals(expected.length, results.size());
		IntStream.range(0, expected.length).boxed()
				.forEach(n -> assertTrue(Math.abs(results.get(n).yDerivative(0) - expected[n]) < maxTol));
	}

	@Test
	void solveStepSize() {
		final double maxTol = 1e-12;
		// y' = 0
		final OdeResolver odeResolver = newOdeResolver(TestDgl.DGL02);
		final double[] y0 = { 10 };
		final double start = 1;
		final double stop = 2;
		final int maxSteps = 1000;
		final List<OdeResult> results = odeResolver.solve(y0, start, stop, maxSteps);
		assertEquals(maxSteps + 1, results.size());
		assertEquals(start, results.get(0).x());
		assertEquals(stop, results.get(maxSteps).x(), maxTol);
		IntStream.rangeClosed(1, maxSteps).boxed().forEach(
				n -> assertEquals((stop - start) / maxSteps, results.get(n).x() - results.get(n - 1).x(), maxTol));

		IntStream.rangeClosed(0, maxSteps).boxed().forEach(n -> assertEquals(y0[0], results.get(n).yDerivative(0)));

	}

	@Test
	void solveSqrtYPlusYStart1() {
		final double maxTol = 1e-8;
		final int maxSteps = 1000;
		final double start = 0;
		final double stop = 1;
		final double[] y0 = { 1 };
		// y'= sqrt(y) + y
		final OdeResolver odeResolver = newOdeResolver(TestDgl.DGL03);

		final List<OdeResult> results = odeResolver.solve(y0, start, stop, maxSteps);
		assertEquals(maxSteps + 1, results.size());

		assertEquals(start, results.get(0).x());
		assertEquals(stop, results.get(maxSteps).x(), maxTol);

		// allgemeine Loesung: y=(c*exp(x/2)-1)^2, c>=1 und y=0
		final double c = (Math.sqrt(y0[0]) + 1) / Math.exp(start / 2);
		final Function<Double, Double> f = x -> Math.pow(c * Math.exp(x / 2) - 1, 2) / Math.exp(start / 2);

		IntStream.rangeClosed(0, maxSteps).boxed()
				.forEach(n -> assertEquals(f.apply(results.get(n).x()), results.get(n).yDerivative(0), maxTol));
	}

	@ParameterizedTest()
	@ValueSource(doubles = { 0, 1e-3 })
	void solveSqrtYPlusYStart0(double y0) {
		final int maxSteps = 1000;
		final double start = 0;
		final double stop = 1;
		// y'= sqrt(y) + y
		// Satz Picard-Lindeloef, y(0)=0: rechte Seite nicht lokal lipschitzstetig
		final OdeResolver odeResolver = newOdeResolver(TestDgl.DGL03);
		final List<OdeResult> results = odeResolver.solve(doubleArray(y0), start, stop, maxSteps);
		// allgemeine L�sung: y=(c*exp(x/2)-1)^2, c>=1 und y=0, f�r y(0)=0 nicht
		// eindeutig
		if (y0 == 0d) {
			// Der Algorithmus folgt der konstanten Loesung y(0)=0, wenn der Startwert der
			// konstanten Loesung entspricht
			IntStream.range(0, results.size()).boxed().forEach(n -> assertEquals(0d, results.get(n).yDerivative(0)));
		} else {
			final double c = (Math.sqrt(y0) + 1) / Math.exp(start / 2);
			final Function<Double, Double> f = x -> Math.pow(c * Math.exp(x / 2) - 1, 2) / Math.exp(start / 2);
			IntStream.range(0, results.size()).boxed()
					.forEach(n -> assertEquals(f.apply(results.get(n).x()), results.get(n).yDerivative(0), 1e-9));
		}
	}

	@Test
	void errorEstimaions() {
		final OdeResolver odeResolver = newOdeResolver(TestDgl.DGL01);

		final List<OdeResult> results = odeResolver.solve(doubleArray(0), 0, 1, 1000);

		assertEquals(0, results.get(0).errorEstimaion());
		IntStream.range(1, results.size()).boxed().forEach(n -> assertTrue(results.get(n).errorEstimaion() < 2e-4));
	}

	@Test
	void solveExamplePapula2() {
		// Seite 238 Papula Formelsammlung
		// y'' = -2y' + 3y
		final double[] expectedY = { 0, 0.364333, 0.672562 };

		final double[] expectedY1 = { 4, 3.327683, 2.867923 };
		final double maxTol = 1e-6;
		final double y0[] = doubleArray(0, 4);
		final OdeResolver odeResolver = newOdeResolver(TestDgl.DGL04);
		final List<OdeResult> results = odeResolver.solve(y0, 0, 0.2, 2);
		IntStream.range(0, expectedY.length).boxed()
				.forEach(n -> assertEquals(expectedY[n], results.get(n).yDerivative(0), maxTol));
		IntStream.range(0, expectedY.length).boxed()
				.forEach(n -> assertEquals(expectedY1[n], results.get(n).yDerivative(1), maxTol));
		IntStream.range(0, expectedY.length).boxed().forEach(n -> assertEquals(n * 0.1, results.get(n).x()));
		IntStream.range(1, expectedY.length).boxed()
				.forEach(n -> assertTrue(Math.abs(results.get(n).errorEstimaion()) < 3e-2));
		assertEquals(0, results.get(0).errorEstimaion());
		assertEquals(y0[0], results.get(0).yDerivative(0));
		assertEquals(y0[1], results.get(0).yDerivative(1));
	}

	@Test
	void solveExamplePapula2CompareGeneralSolution() {
		final double y0[] = doubleArray(0, 4);
		final double maxTol = 1e-6;
		final OdeResolver odeResolver = newOdeResolver(TestDgl.DGL04);
		final List<OdeResult> results = odeResolver.solve(y0, 0, 1, 1000);

		final Function<Double, Double> y = x -> Math.exp(x) - Math.exp(-3 * x);

		final Function<Double, Double> dy = x -> Math.exp(x) + 3 * Math.exp(-3 * x);

		IntStream.range(0, results.size()).boxed()
				.forEach(n -> assertEquals(y.apply(results.get(n).x()), results.get(n).yDerivative(0), maxTol));
		IntStream.range(0, results.size()).boxed()
				.forEach(n -> assertEquals(dy.apply(results.get(n).x()), results.get(n).yDerivative(1), maxTol));
	}

	abstract OdeResolver newOdeResolver(TestDgl testDgl);

}