package de.mq.odesolver.system.support;

interface OdeSystemResultCalculator {
	double[] calculate(final double t, final double[] y);
}
