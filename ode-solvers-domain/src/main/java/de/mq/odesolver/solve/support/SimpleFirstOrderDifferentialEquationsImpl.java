package de.mq.odesolver.solve.support;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;

class SimpleFirstOrderDifferentialEquationsImpl implements FirstOrderDifferentialEquations {

	static final String WRONG_MESSAGE_FORMAT = "%s has wrong size %s expected %s.";
	static final String MESSAGE_REQUIRED_FORMAT = "%s shound not be null.";
	private final OdeSystemResultCalculator resultCalculator;
	private final int dimension;

	SimpleFirstOrderDifferentialEquationsImpl(final OdeSystemResultCalculator resultCalculator, final int dimension) {
		this.resultCalculator = resultCalculator;
		this.dimension = dimension;
	}

	@Override
	public final void computeDerivatives(final double t, final double[] y, final double[] yDot) throws MaxCountExceededException, DimensionMismatchException {
		sizeGuard(y, "y");
		sizeGuard(yDot, "y'");
		final double[] result = resultCalculator.f(y,t);
		sizeGuard(result, "Result");

		System.arraycopy(result, 0, yDot, 0, result.length);
	}

	private void sizeGuard(final double[] vector, final String name) {
		if (vector == null) {
			throw new IllegalArgumentException(String.format(MESSAGE_REQUIRED_FORMAT, name));
		}
		if (vector.length != getDimension()) {
			throw new IllegalArgumentException(String.format(WRONG_MESSAGE_FORMAT, name, vector.length, getDimension()));
		}
	}

	@Override
	public final int getDimension() {
		return dimension;
	}

}
