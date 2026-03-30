package de.mq.odesolver.function.support;

import static de.mq.odesolver.support.OdeFunctionUtilFactory.newOdeFunctionUtil;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import de.mq.odesolver.Result;
import de.mq.odesolver.function.FunctionSolver;
import de.mq.odesolver.support.OdeFunctionUtil.Language;


class FunctionSolverImplTest {

	@Test
	void solveWithLamda() {
		final FunctionSolver functionSolver = new FunctionSolverImpl(
				(x, k) -> 1d / 2 * Math.pow(x, 4) + k[0] * Math.pow(x, 2) + k[1] * Math.pow(x, 3));

		solveFunction(functionSolver);

	}

	private void solveFunction(final FunctionSolver functionSolver) {
		final double[] x = { 0, 1d / 4, 1d / 2, 3d / 4, 1 };
		// Bruchrechnung
		final double[] y = { 0, 49d / 512, 17d / 32, 801d / 512, 7d / 2 };
		final List<Result> results = functionSolver.solve(FunctionResultImpl.doubleArray(1, 2), 0, 1, 4);

		assertEquals(5, results.size());
		IntStream.range(0, results.size()).forEach(i -> assertEquals(x[i], results.get(i).x()));
		IntStream.range(0, results.size()).forEach(i -> assertEquals(y[i], results.get(i).yDerivative(0)));
		IntStream.range(0, results.size()).forEach(i -> assertEquals(1, results.get(i).yDerivatives().length));
	}

	@ParameterizedTest
	@EnumSource
	void solveWithLamdaLanguages(final Language language) {
		final FunctionSolver functionSolver = new FunctionSolverImpl(newOdeFunctionUtil(language, "k", false),"1/2*Math.pow(x,4)+k[0]*Math.pow(x,2)+k[1]*Math.pow(x,3)");
		solveFunction(functionSolver);
	}
}
