package de.mq.odesolver.solve.support;

public interface OdeSystemResultCalculator {
	double[] f(final double[] y, final double x);
}
