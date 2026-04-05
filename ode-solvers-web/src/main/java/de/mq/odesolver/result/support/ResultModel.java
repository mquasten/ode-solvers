package de.mq.odesolver.result.support;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.IntStream;

import org.springframework.util.CollectionUtils;

import de.mq.odesolver.Result;
import de.mq.odesolver.function.support.FunctionResultImpl;

public class ResultModel {

	static final String KEY_X0 = "x0";
	static final String KEY_Y = "y";
	static final String KEY_X = "x";
	static final String KEY_VECTOR_NAME = "k";
	static final String BACK_FUNCTION = "function";
	static final String BACK_SOLVE = "solve";
	private final String back;
	private final String title;
	private final Collection<Result> results = new ArrayList<>();

	private final Collection<Entry<String, double[]>> ranges = new ArrayList<>();

	private final Collection<Entry<String, Double>> initialValues = new ArrayList<>();

	ResultModel() {
		this.back = EMPTY;
		this.title = EMPTY;
	}

	public ResultModel(final List<? extends Result> results, final String title) {
		this.title = title;
		this.results.addAll(results);
		this.back = BACK_SOLVE;
		calculateRanges(results);
		calculateInitialValues(results);
	}

	public ResultModel(final List<? extends Result> results, final String title, final double[] kVector) {
		this.title = title;
		this.results.addAll(results);
		this.back = BACK_FUNCTION;
		calculateRanges(results);
		calculateInitialValues(kVector);
	}

	private void calculateRanges(final List<? extends Result> results) {
		ranges.clear();
		if (CollectionUtils.isEmpty(results)) {
			return;
		}

		if (results.size() < 2) {
			return;
		}

		final Result firstResult = results.get(0);
		final Result lastResult = results.get(results.size() - 1);
		ranges.add(new SimpleImmutableEntry<>(KEY_X, FunctionResultImpl.doubleArray(firstResult.x(), lastResult.x())));

		IntStream.range(0, firstResult.yDerivatives().length).forEach(i -> addRangeDerivative(ranges, results, i));

	}

	private void calculateInitialValues(final List<? extends Result> results) {
		initialValues.clear();

		if (CollectionUtils.isEmpty(results)) {
			return;
		}
		final Result initialValue = results.get(0);

		initialValues.add(new SimpleImmutableEntry<>(KEY_X0, initialValue.x()));

		IntStream.range(0, initialValue.yDerivatives().length).forEach(i -> {
			final StringBuffer text = new StringBuffer(KEY_Y);
			IntStream.rangeClosed(1, i).forEach(_ -> text.append("'"));
			text.append("(x0)");
			initialValues.add(new SimpleImmutableEntry<>(text.toString(), initialValue.yDerivative(i)));
		});

	}

	private void calculateInitialValues(final double[] kVector) {
		initialValues.clear();

		if (kVector == null) {
			return;
		}

		IntStream.range(0, kVector.length)
				.forEach(i -> initialValues.add(new SimpleImmutableEntry<>(String.format("%s[%d]",KEY_VECTOR_NAME, i), kVector[i])));
	}

	private void addRangeDerivative(final Collection<Entry<String, double[]>> ranges,
			final Collection<? extends Result> results, final int yDerivative) {
		final Optional<Double> min = results.stream().map(r -> r.yDerivative(yDerivative))
				.min((x1, x2) -> x1.compareTo(x2));
		final Optional<Double> max = results.stream().map(r -> r.yDerivative(yDerivative))
				.max((x1, x2) -> x1.compareTo(x2));
		final StringBuffer text = new StringBuffer(KEY_Y);
		IntStream.rangeClosed(1, yDerivative).forEach(_ -> text.append("'"));
		
		min.ifPresent(minVal -> max.ifPresent(maxVal -> ranges.add(new SimpleImmutableEntry<>(text.toString(), FunctionResultImpl.doubleArray(minVal, maxVal)))));
		
	}

	public String getBack() {
		return back;
	}

	public String getTitle() {
		return title;
	}

	public Collection<Result> getResults() {
		return results;
	}

	public final Collection<Entry<String, double[]>> getRanges() {
		return ranges;
	}

	public final Collection<Entry<String, Double>> getInitialValues() {
		return initialValues;
	}

}
