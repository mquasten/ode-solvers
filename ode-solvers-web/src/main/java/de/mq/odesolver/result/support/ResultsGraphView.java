package de.mq.odesolver.result.support;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.AbstractView;

import de.mq.odesolver.Result;
import de.mq.odesolver.solve.OdeResult;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ResultsGraphView extends AbstractView {
	static final int HEIGHT = 1080;
	static final int WIDTH = 1080;
	static final String RESULTS_MODEL = "results";
	static final String RESULTS_TITLE = "resultsTitle";
	static final String CONTENT_DISPOSITION_HEADER_VALUE = "filename=Funktionsgraph.png";
	static final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";
	static final String COLUMN_HEADER_X = "x";
	static final String COLUMN_HEADER_Y = "y";

	@Override
	protected void renderMergedOutputModel(final Map<String, Object> model, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final JFreeChart chart = createChart(model, response);

		final ServletOutputStream os = response.getOutputStream();

		ChartUtilities.writeChartAsPNG(os, chart, WIDTH, HEIGHT);
	}

	final JFreeChart createChart(final Map<String, Object> model, final HttpServletResponse response) {
		response.setHeader(CONTENT_DISPOSITION_HEADER,CONTENT_DISPOSITION_HEADER_VALUE);
		@SuppressWarnings("unchecked")
		final List<? extends Result> results = (List<OdeResult>) model.get(RESULTS_MODEL);
		final String ode = (String) model.get(RESULTS_TITLE);

		final XYDataset dataset = createDataset(results, ode);

		final JFreeChart chart = ChartFactory.createXYLineChart(ode, COLUMN_HEADER_X, COLUMN_HEADER_Y, dataset, PlotOrientation.VERTICAL, true, true, false);
		return chart;
	}

	private XYDataset createDataset(final List<? extends Result> results, final String title) {
		final XYSeriesCollection dataset = new XYSeriesCollection();

		if (results == null) {
			return dataset;

		}

		if (results.size() < 1) {
			return dataset;
		}

		IntStream.range(0, results.get(0).yDerivatives().length).forEach(i -> {
			final StringBuffer text = new StringBuffer(COLUMN_HEADER_Y);
			IntStream.rangeClosed(1, i).forEach(_ -> text.append("'"));

			final XYSeries series = new XYSeries(text);
			results.forEach(result -> series.add(result.x(), result.yDerivative(i)));
			dataset.addSeries(series);

		});

		return dataset;
	}

}
