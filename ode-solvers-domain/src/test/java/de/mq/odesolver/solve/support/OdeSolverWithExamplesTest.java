package de.mq.odesolver.solve.support;

import static de.mq.odesolver.solve.support.OdeResultImpl.doubleArray;
import static de.mq.odesolver.support.OdeFunctionUtilFactory.newOdeFunctionUtil;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import de.mq.odesolver.solve.OdeResult;
import de.mq.odesolver.solve.OdeSolver;
import de.mq.odesolver.support.OdeFunctionUtil.Language;

class OdeSolverWithExamplesTest {

	private final Map<TestProperties, Double> rungeKutta4Properties = Map.ofEntries(
			Map.entry(TestProperties.MaxTolExamplePapulaFirstOrder, 1e-6),
			Map.entry(TestProperties.MaxTolStepSize, 1e-12), Map.entry(TestProperties.MaxTolSqrtYPlusYStart1, 1e-8),
			Map.entry(TestProperties.MaxTolSqrtYPlusYStart0, 1e-9),
			Map.entry(TestProperties.MaxTolErrorEstimaions, 2e-4),
			Map.entry(TestProperties.MaxTolExamplePapulaSecondOrder, 1e-6),
			Map.entry(TestProperties.MaxTolExamplePapulaSecondOrderErrorEstimate, 3e-2),
			Map.entry(TestProperties.EndExamplePapulaFirstOrder, 0.3),
			Map.entry(TestProperties.StepsExamplePapulaFirstOrder, 3d),
			Map.entry(TestProperties.MaxTolMinMaxDerivateZero, 1e-11), Map.entry(TestProperties.MaxTolMinMax, 1e-12));

	private final Map<TestProperties, Double> rungeKutta2Properties = Map.ofEntries(
			Map.entry(TestProperties.MaxTolExamplePapulaFirstOrder, 1e-6),
			Map.entry(TestProperties.MaxTolStepSize, 1e-12), Map.entry(TestProperties.MaxTolSqrtYPlusYStart1, 3e-6),
			Map.entry(TestProperties.MaxTolSqrtYPlusYStart0, 6e-6),
			Map.entry(TestProperties.MaxTolErrorEstimaions, 6e-4),
			Map.entry(TestProperties.MaxTolExamplePapulaSecondOrder, 2e-6),
			Map.entry(TestProperties.MaxTolExamplePapulaSecondOrderErrorEstimate, 1e-1),
			Map.entry(TestProperties.EndExamplePapulaFirstOrder, 0.3),
			Map.entry(TestProperties.StepsExamplePapulaFirstOrder, 3d),
			Map.entry(TestProperties.MaxTolMinMaxDerivateZero, 8e-6), Map.entry(TestProperties.MaxTolMinMax, 6e-6));

	private final Map<TestProperties, Double> eulerProperties = Map.ofEntries(
			Map.entry(TestProperties.MaxTolExamplePapulaFirstOrder, 1e-6),
			Map.entry(TestProperties.MaxTolStepSize, 1e-12), Map.entry(TestProperties.MaxTolSqrtYPlusYStart1, 6e-3),
			Map.entry(TestProperties.MaxTolSqrtYPlusYStart0, 3e-3),
			Map.entry(TestProperties.MaxTolErrorEstimaions, 2e-3),
			Map.entry(TestProperties.MaxTolExamplePapulaSecondOrder, 3e-3),
			Map.entry(TestProperties.MaxTolExamplePapulaSecondOrderErrorEstimate, 5e-1),
			Map.entry(TestProperties.EndExamplePapulaFirstOrder, 0.2d),
			Map.entry(TestProperties.StepsExamplePapulaFirstOrder, 4d),
			Map.entry(TestProperties.MaxTolMinMaxDerivateZero, 2e-2), Map.entry(TestProperties.MaxTolMinMax, 8e-3));

	private final Map<Result, double[]> rungeKutta4ExpectedResults = Map.of(Result.ExamplePapulaFirstOrder,
			new double[] { 0, -0.005171, -0.021403, -0.049859 }, Result.ExamplePapulaSecondOrderY,
			new double[] { 0, 0.364333, 0.672562 }, Result.ExamplePapulaSecondOrderDerivative,
			new double[] { 4, 3.327683, 2.867923 });

	private final Map<Result, double[]> rungeKutta2ExpectedResults = Map.of(Result.ExamplePapulaFirstOrder,
			new double[] { 0, -0.005, -0.021025, -0.049233 }, Result.ExamplePapulaSecondOrderY,
			new double[] { 0, 0.36000, 0.66600 }, Result.ExamplePapulaSecondOrderDerivative,
			new double[] { 4, 3.3400, 2.8861 });

	private final Map<Result, double[]> eulerExpectedResults = Map.of(Result.ExamplePapulaFirstOrder,
			new double[] { 0, 0, -0.0025, -0.007625, -0.015506 }, Result.ExamplePapulaSecondOrderY,
			new double[] { 0, 0.4, 0.72 }, Result.ExamplePapulaSecondOrderDerivative, new double[] { 4, 3.2, 2.68 });

	private final Map<TestSolver, Map<TestProperties, Double>> properties = new HashMap<>();
	private final Map<TestSolver, Map<Result, double[]>> expectedResults = new HashMap<>();


	private final Map<TestSolver, Function<TestDgl, OdeSolver>> odeSolvers = Map.of(TestSolver.Euler_Lamdas,
			testDgl -> new OdeSolverImpl(new EulerCalculatorImpl(testDgl.odeFunction())), TestSolver.Euler_Nashorn,
			testDgl -> new OdeSolverImpl(
					new EulerCalculatorImpl(newOdeFunctionUtil(Language.Nashorn), testDgl.functionAsString())),
			TestSolver.Euler_JRuby,
			testDgl -> new OdeSolverImpl(
					new EulerCalculatorImpl(newOdeFunctionUtil(Language.Groovy), testDgl.functionAsString())),
			TestSolver.RungeKutta2_Lamdas,
			testDgl -> new OdeSolverImpl(new RungeKutta2CalculatorImpl(testDgl.odeFunction())),
			TestSolver.RungeKutta2_Nashorn,
			testDgl -> new OdeSolverImpl(
					new RungeKutta2CalculatorImpl(newOdeFunctionUtil(Language.Nashorn), testDgl.functionAsString())),
			TestSolver.RungeKutta2_JRuby,
			testDgl -> new OdeSolverImpl(
					new RungeKutta2CalculatorImpl(newOdeFunctionUtil(Language.Groovy), testDgl.functionAsString())),
			TestSolver.RungeKutta4_Lamdas,
			testDgl -> new OdeSolverImpl(new RungeKutta4CalculatorImpl(testDgl.odeFunction())),
			TestSolver.RungeKutta4_Nashorn,
			testDgl -> new OdeSolverImpl(
					new RungeKutta4CalculatorImpl(newOdeFunctionUtil(Language.Nashorn), testDgl.functionAsString())),
			TestSolver.RungeKutta4_JRuby, testDgl -> new OdeSolverImpl(
					new RungeKutta4CalculatorImpl(newOdeFunctionUtil(Language.Groovy), testDgl.functionAsString())));

	enum TestDgl {
		DGL01(odeArguments -> odeArguments.yDerivative(0) - odeArguments.x(), "y[0]-x"),
		DGL02(_ -> 0d, "0.0"),
		DGL03(odeArguments -> Math.sqrt(odeArguments.yDerivative(0)) + odeArguments.yDerivative(0),
				"Math.sqrt(y[0])+y[0]"),
		DGL04(odeArguments -> 3 * odeArguments.yDerivative(0) - 2 * odeArguments.yDerivative(1), "3*y[0]-2*y[1]"),

		DGL05(odeArguments -> 4 / odeArguments.x() * odeArguments.yDerivatives()[1]
				- 6 / Math.pow(odeArguments.x(), 2) * odeArguments.yDerivative(0) + Math.pow(odeArguments.x(), 2),
				"4/x*y[1]-6/(x*x)*y[0]+x*x");

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

	enum TestProperties {
		MaxTolExamplePapulaFirstOrder, MaxTolStepSize, MaxTolSqrtYPlusYStart1, MaxTolSqrtYPlusYStart0,
		MaxTolErrorEstimaions, MaxTolExamplePapulaSecondOrder, MaxTolExamplePapulaSecondOrderErrorEstimate,
		EndExamplePapulaFirstOrder, StepsExamplePapulaFirstOrder, MaxTolMinMaxDerivateZero, MaxTolMinMax;
	}

	enum Result {
		ExamplePapulaFirstOrder, ExamplePapulaSecondOrderY, ExamplePapulaSecondOrderDerivative;
	}

	public enum TestSolver {
		Euler_Lamdas, Euler_Nashorn, Euler_JRuby, RungeKutta2_Lamdas, RungeKutta2_Nashorn, RungeKutta2_JRuby,
		RungeKutta4_Lamdas, RungeKutta4_Nashorn, RungeKutta4_JRuby;

	}

	@BeforeEach
	void setProperties() {
		properties.put(TestSolver.RungeKutta4_Lamdas, rungeKutta4Properties);
		properties.put(TestSolver.RungeKutta4_Nashorn, rungeKutta4Properties);
		properties.put(TestSolver.RungeKutta4_JRuby, rungeKutta4Properties);

		properties.put(TestSolver.RungeKutta2_Lamdas, rungeKutta2Properties);
		properties.put(TestSolver.RungeKutta2_Nashorn, rungeKutta2Properties);
		properties.put(TestSolver.RungeKutta2_JRuby, rungeKutta2Properties);

		properties.put(TestSolver.Euler_Lamdas, eulerProperties);
		properties.put(TestSolver.Euler_Nashorn, eulerProperties);
		properties.put(TestSolver.Euler_JRuby, eulerProperties);

		expectedResults.put(TestSolver.RungeKutta4_Lamdas, rungeKutta4ExpectedResults);
		expectedResults.put(TestSolver.RungeKutta4_Nashorn, rungeKutta4ExpectedResults);
		expectedResults.put(TestSolver.RungeKutta4_JRuby, rungeKutta4ExpectedResults);

		expectedResults.put(TestSolver.RungeKutta2_Lamdas, rungeKutta2ExpectedResults);
		expectedResults.put(TestSolver.RungeKutta2_Nashorn, rungeKutta2ExpectedResults);
		expectedResults.put(TestSolver.RungeKutta2_JRuby, rungeKutta2ExpectedResults);

		expectedResults.put(TestSolver.Euler_Lamdas, eulerExpectedResults);
		expectedResults.put(TestSolver.Euler_Nashorn, eulerExpectedResults);
		expectedResults.put(TestSolver.Euler_JRuby, eulerExpectedResults);
	}

	private OdeSolver odeSolver(final TestSolver solver, final TestDgl testDgl) {
		return odeSolvers.get(solver).apply(testDgl);
	}

	private Double property(TestSolver solver, TestProperties testProperties) {
		return properties.get(solver).get(testProperties);
	}

	private double[] expectedResult(TestSolver solver, Result result) {
		return expectedResults.get(solver).get(result);
	}

	@ParameterizedTest()
	@EnumSource
	void solveExamplePapula1(final TestSolver solver) {
		// Seite 238 Papula Formelsammlung
		final double maxTol = property(solver, TestProperties.MaxTolExamplePapulaFirstOrder);
		final double[] expected = expectedResult(solver, Result.ExamplePapulaFirstOrder);

		// y' = y - x;
		final OdeSolver odeResolver = odeSolver(solver, TestDgl.DGL01);

		final List<OdeResult> results = odeResolver.solve(doubleArray(0), 0,
				property(solver, TestProperties.EndExamplePapulaFirstOrder),
				property(solver, TestProperties.StepsExamplePapulaFirstOrder).intValue());
		assertEquals(expected.length, results.size());
		IntStream.range(0, expected.length).boxed()
				.forEach(n -> assertEquals(expected[n], results.get(n).yDerivative(0), maxTol));
	}

	@ParameterizedTest()
	@EnumSource
	void solveStepSize(final TestSolver solver) {
		final double maxTol = property(solver, TestProperties.MaxTolStepSize);
		// y' = 0
		final OdeSolver odeResolver = odeSolver(solver, TestDgl.DGL02);
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

	@ParameterizedTest()
	@EnumSource
	void solveSqrtYPlusYStart1(final TestSolver solver) {
		final double maxTol = property(solver, TestProperties.MaxTolSqrtYPlusYStart1);
		final int maxSteps = 1000;
		final double start = 0;
		final double stop = 1;
		final double[] y0 = { 1 };
		// y'= sqrt(y) + y
		final OdeSolver odeResolver = odeSolver(solver, TestDgl.DGL03);

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
	@EnumSource
	void solveSqrtYPlusYStart0(final TestSolver solver) {
		testPicardLindeloef(solver, 0);
		testPicardLindeloef(solver, 1e-3);

	}

	private void testPicardLindeloef(final TestSolver solver, double y0) {
		final int maxSteps = 1000;
		final double start = 0;
		final double stop = 1;

		// y'= sqrt(y) + y
		// Satz Picard-Lindeloef, y(0)=0: rechte Seite nicht lokal lipschitzstetig
		final OdeSolver odeResolver = odeSolver(solver, TestDgl.DGL03);
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
			IntStream.range(0, results.size()).boxed().forEach(n -> assertEquals(f.apply(results.get(n).x()),
					results.get(n).yDerivative(0), property(solver, TestProperties.MaxTolSqrtYPlusYStart0)));
		}
	}

	@ParameterizedTest()
	@EnumSource
	void errorEstimaions(final TestSolver solver) {
		final OdeSolver odeResolver = odeSolver(solver, TestDgl.DGL01);

		final List<OdeResult> results = odeResolver.solve(doubleArray(0), 0, 1, 1000);

		assertEquals(0, results.get(0).errorEstimaion());
		IntStream.range(1, results.size()).boxed().forEach(n -> assertTrue(
				results.get(n).errorEstimaion() < property(solver, TestProperties.MaxTolErrorEstimaions)));
	}

	@ParameterizedTest()
	@EnumSource
	void solveExamplePapula2(final TestSolver solver) {
		// Seite 238 Papula Formelsammlung
		// y'' = -2y' + 3y
		final double[] expectedY = expectedResult(solver, Result.ExamplePapulaSecondOrderY);

		final double[] expectedY1 = expectedResult(solver, Result.ExamplePapulaSecondOrderDerivative);
		final double maxTol = property(solver, TestProperties.MaxTolExamplePapulaSecondOrder);
		final double y0[] = doubleArray(0, 4);
		final OdeSolver odeResolver = odeSolver(solver, TestDgl.DGL04);
		final List<OdeResult> results = odeResolver.solve(y0, 0, 0.2, 2);
		IntStream.range(0, expectedY.length).boxed()
				.forEach(n -> assertEquals(expectedY[n], results.get(n).yDerivative(0), maxTol));
		IntStream.range(0, expectedY.length).boxed()
				.forEach(n -> assertEquals(expectedY1[n], results.get(n).yDerivative(1), maxTol));
		IntStream.range(0, expectedY.length).boxed().forEach(n -> assertEquals(n * 0.1, results.get(n).x()));
		IntStream.range(1, expectedY.length).boxed()
				.forEach(n -> assertTrue(Math.abs(results.get(n).errorEstimaion()) < property(solver,
						TestProperties.MaxTolExamplePapulaSecondOrderErrorEstimate)));
		assertEquals(0, results.get(0).errorEstimaion());
		assertEquals(y0[0], results.get(0).yDerivative(0));
		assertEquals(y0[1], results.get(0).yDerivative(1));
	}

	@ParameterizedTest()
	@EnumSource
	void solveExamplePapula2CompareGeneralSolution(final TestSolver solver) {
		final double y0[] = doubleArray(0, 4);
		final double maxTol = property(solver, TestProperties.MaxTolExamplePapulaSecondOrder);
		final OdeSolver odeResolver = odeSolver(solver, TestDgl.DGL04);
		final List<OdeResult> results = odeResolver.solve(y0, 0, 1, 1000);

		final Function<Double, Double> y = x -> Math.exp(x) - Math.exp(-3 * x);

		final Function<Double, Double> dy = x -> Math.exp(x) + 3 * Math.exp(-3 * x);

		IntStream.range(0, results.size()).boxed()
				.forEach(n -> assertEquals(y.apply(results.get(n).x()), results.get(n).yDerivative(0), maxTol));
		IntStream.range(0, results.size()).boxed()
				.forEach(n -> assertEquals(dy.apply(results.get(n).x()), results.get(n).yDerivative(1), maxTol));
	}

	@ParameterizedTest()
	@EnumSource()
	void solveOwnExampleMinMax(final TestSolver solver) {
		// Man muss nicht immer etwas so langweiliges wie y(xs) und y'(xs) für die
		// spezielle Lösung verwenden, lokale Minima und Maxima, Wendepunkte etc. gehen
		// auch...
		// Das Fundamentalsystem habe ich geziehlt geraten. DGL: y''+a/x*y' + b/x^2*y
		// +x^2 , Fundamentalsystem soll x^2 und x^3 sein.
		// dann kann man a, b bestimmen: a= -4, b=6
		// mittels Variation der Konstanten erhält man die allgemeine Lösung: y=1/2*x^4
		// + k1*x^2 + k2*x^3= x^2*(1/2x^2 + k1 + k2*x)
		// dann y'=0 xmax und xmin sind gegeben: man findet: k1=xmax*xmin
		// k2=-2/3(xmax+xmin)
		// xs, ys, ys' sind die Startwerte
		// ys=1/2*xs^4 + xs^2*xman*xmin - 2/3*xs^3*(xmax+xmin)
		// ys'=2*xs^3+2*xs*xmax*xmin - 2*xs^2*(xmax+xmin)
		// y''<0 bzw. y''> 0 führt auf:
		// Existenz lokales Minimum xmax > 0: xmax < xmin, xmax < 0: xmax > xmin
		// Existenz lokales Maximun xmin > 0 : xmin > xmax, xmin < 0: xmin < xmax
		// Schöne Analysis-Übung ...
		checklocalMaxMin(solver, 2d, 3d, doubleArray(19d / 6, 4), 1, 4);
		checklocalMaxMin(solver, -2d, -3d, doubleArray(32d / 3, -16), -4, 1);
	}

	private void checklocalMaxMin(final TestSolver solver, final double xMax, final double xMin, final double[] y0,
			final double start, final double stop) {
		final double tol = property(solver, TestProperties.MaxTolMinMaxDerivateZero);
		final OdeSolver odeSolver = odeSolver(solver, TestDgl.DGL05);
		final int steps = 3000;
		final List<OdeResult> results = odeSolver.solve(y0, start, stop, steps);

		final OdeResult max = filterByX(results, xMax, tol);

		assertEquals(xMax, max.x(), 1e-12);
		assertEquals(0, max.yDerivative(1), tol);
		assertTrue(TestDgl.DGL05.odeFunction.apply(max) < 0d);
		assertTrue(Math.abs(TestDgl.DGL05.odeFunction.apply(max)) > 1d);

		final OdeResult min = filterByX(results, xMin, tol);

		assertEquals(xMin, min.x(), 1e-12);
		assertEquals(0, min.yDerivative(1), tol);
		assertTrue(TestDgl.DGL05.odeFunction.apply(min) > 0d);
		assertTrue(Math.abs(TestDgl.DGL05.odeFunction.apply(min)) > 1d);
	}

	private OdeResult filterByX(final List<OdeResult> results, final double x, final double tol) {

		return results.stream().filter(odeResult -> odeResult.x() >= (x - 1e-12)).findFirst().orElseThrow();
	}

	@ParameterizedTest()
	@EnumSource()
	void solveOwnExampleCompareSolution(final TestSolver solver) {
		// ysp=1/2*x^4 + 5/2k1*x^2 -2*x^3 für y(1)=1, y'(1)=1
		final double tol = property(solver, TestProperties.MaxTolMinMax);
		final OdeSolver odeSolver = odeSolver(solver, TestDgl.DGL05);
		final int steps = 1000;
		final double x0 = 1;
		final double y0 = 1;
		final double dy0 = 1;
		final List<OdeResult> results = odeSolver.solve(doubleArray(y0, dy0), x0, 2, steps);

		final Function<Double, Double> y = x -> 1d / 2 * Math.pow(x, 4) + 5d / 2 * Math.pow(x, 2) - 2 * Math.pow(x, 3);
		final Function<Double, Double> dy = x -> 2 * Math.pow(x, 3) + 5 * x - 6 * Math.pow(x, 2);

		assertEquals(x0, results.get(0).x());
		assertEquals(y0, results.get(0).yDerivative(0));
		assertEquals(dy0, results.get(0).yDerivative(1));
		results.forEach(result -> assertEquals(y.apply(result.x()), result.yDerivative(0), tol));
		results.forEach(result -> assertEquals(dy.apply(result.x()), result.yDerivative(1), tol));

	}

}
