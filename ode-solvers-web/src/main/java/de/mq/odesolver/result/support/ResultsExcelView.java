package de.mq.odesolver.result.support;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import de.mq.odesolver.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component()
public class ResultsExcelView extends AbstractXlsxView {

	static final String COLUMN_HEADER_DERIVATIVE = "y'(x)";
	static final String COLUMN_HEADER_Y = "y(x)";
	static final String COLUMN_HEADER_X = "x";
	static final String RESULTS_TITLE = "resultsTitle";
	static final String SHEET_NAME = "Wertetabelle";
	static final String CONTENT_DISPOSITION_HEADER_VALUE = "attachment; filename=Wertetabelle.xls";
	static final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";
	static final String RESULTS_MODEL = "results";
	private static final int MAX_CELL_HEADLINE = 10;

	@Override
	protected void buildExcelDocument(final Map<String, Object> model, final Workbook workbook, final HttpServletRequest request, final HttpServletResponse response) throws Exception {

		@SuppressWarnings("unchecked")
		final List<Result> results = (List<Result>) model.get(RESULTS_MODEL);
		final String title = (String) model.get(RESULTS_TITLE);

		response.setHeader(CONTENT_DISPOSITION_HEADER, CONTENT_DISPOSITION_HEADER_VALUE);
		final Sheet sheet = workbook.createSheet(SHEET_NAME);
		sheet.setFitToPage(true);

		final Row headlineRow = sheet.createRow(0);
		final Cell headlineCell = headlineRow.createCell(0);
		headlineCell.setCellValue(title);
		headlineRow.createCell(10).setBlank();

		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, MAX_CELL_HEADLINE));

		final CellStyle cellStyle = boldCellStyle(workbook);

		headlineCell.setCellStyle(cellStyle);

		final Row header = sheet.createRow(1);
		header.createCell(0).setCellValue(COLUMN_HEADER_X);
		header.createCell(1).setCellValue(COLUMN_HEADER_Y);

		if (results == null) {
			return;
		}
		if (results.size() == 0) {
			return;
		}

		if (results.get(0).yDerivatives().length > 1) {
			header.createCell(2).setCellValue(COLUMN_HEADER_DERIVATIVE);
		}

		IntStream.range(0, results.size()).forEach(i -> writeRow(results, sheet, i));

		model.remove(RESULTS_MODEL);

	}

	private CellStyle boldCellStyle(final Workbook workbook) {
		final Font font = workbook.createFont();
		font.setBold(true);
		final CellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setFont(font);
		return cellStyle;
	}

	private void writeRow(final List<? extends Result> results, final Sheet sheet, final int i) {
		final Row date = sheet.createRow(i + 2);
		final Result odeResult = results.get(i);
		date.createCell(0).setCellValue(odeResult.x());
		IntStream.range(0, odeResult.yDerivatives().length).forEach(k -> date.createCell(1 + k).setCellValue(odeResult.yDerivative(k)));
	}

}
