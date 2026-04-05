package de.mq.odesolver.solve.support;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import de.mq.odesolver.solve.Ode;
import de.mq.odesolver.solve.OdeSolverService.Algorithm;
import de.mq.odesolver.support.OdeFunctionUtil.Language;


class OdeImpl implements Ode {

	private final String REGEX_Y_DERIVATIVE = "y\\[\\s*%s\\s*\\]";

	private final Language language;

	private final String ode;

	private final Algorithm algorithm;

	private final double[] y;

	private final double start;

	private final double stop;

	private final int steps;

	OdeImpl(final Language language, String ode, final Algorithm algorithm, final double[] y, final double start, final double stop, final int steps) {
		notNull(language, "Language is mandatory.");
		notBlank(ode, "Ode is madatory.");
		notNull(algorithm, "Algorithm is madatory.");
		notNull(y, "Y is mandatory.");
		isTrue(y.length > 0, "At least one y element required.");
		isTrue(steps > 0, "Steps must be > 0.");

		this.language = language;
		this.ode = ode;
		this.algorithm = algorithm;
		this.y = y;
		this.start = start;
		this.stop = stop;
		this.steps = steps;
	}

	@Override
	public final boolean checkOrder(final int order) {
		Validate.isTrue(order > 0);
		if (algorithm.isSystem()) {
			return parseOrderfromSystem() == order;
		}
		return y.length == order;
	}

	private int parseOrderfromSystem() {
		final String[] parts = StringUtils.splitByWholeSeparator(StringUtils.deleteWhitespace(ode), "dy[");
		int order = -1;
		for (int i = 0; i < parts.length; i++) {
			final String index = parts[i].replaceFirst("\\]=.*", "");
			if (!StringUtils.isNumeric(index)) {
				return -1;
			}
			order = Integer.max(order, Integer.parseInt(index));
		}
		if (order < 0) {
			return -1;
		}

		return order + 1;
	}

	@Override
	public final boolean checkStartBeforeStop() {
		return start < stop;
	}

	@Override
	public final double[] y() {
		return y;
	}

	@Override
	public final double start() {
		return start;
	}

	@Override
	public final double stop() {
		return stop;
	}

	@Override
	public final int steps() {
		return steps;
	}

	@Override
	public final Algorithm algorithm() {
		return algorithm;
	}

	@Override
	public final String ode() {
		return ode;
	}

	@Override
	public final Language language() {
		return language;
	}

	@Override
	public final String beautifiedOde() {
		if (this.algorithm.isSystem()) {
			return beautifiedOdeSystem();
		}

		final String prefix = y.length == 1 ? "y'=" : "y''=";
		return prefix + ode.replaceAll(String.format(REGEX_Y_DERIVATIVE, 0), "y").replaceAll(String.format(REGEX_Y_DERIVATIVE, 1), "y'");

	}

	private String beautifiedOdeSystem() {
		final String[] parts = StringUtils.splitByWholeSeparator(StringUtils.deleteWhitespace(ode), "dy[");
		final StringBuffer buffer = new StringBuffer();

		for (int i = 0; i < parts.length; i++) {
			final String index = parts[i].replaceFirst("\\]=.*", "");
			Validate.isTrue(StringUtils.isNumeric(index), "Left side index shoud be a Number.");
			final String gleichung = parts[i].replaceFirst(index + "\\]=", "");
			buffer.append("y" + (Integer.parseInt(index) + 1) + "'" + "=" + gleichung + " ");
		}
		String odeString = buffer.toString();
		for (int i = 0; i < parts.length; i++) {
			odeString = odeString.replaceAll("y\\[" + i + "\\]", "y" + (i + 1));
		}
		return odeString.trim();
	}

}
