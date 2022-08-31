package de.mq.odesolver.result.support;

import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import de.mq.odesolver.support.OdeSessionModel;

@Controller
abstract class ResultController {
	
	private final ResultsExcelView resultsExcelView;
	private final ResultsGraphView resultsGraphView;
	
	ResultController (final ResultsExcelView resultsExcelView, final ResultsGraphView resultsGraphView) {
		this.resultsExcelView=resultsExcelView;
		this.resultsGraphView=resultsGraphView;
	}
	
	@GetMapping("/result")
	public String solve(final Model model) {
		final ResultModel result = odeSessionModel().getResult();
		model.addAttribute("result", result);
		model.addAttribute(model);
		return "result";
	}
	
	@PostMapping(value = "/result", params = "back")
	public String backSubmit() {
		return String.format("redirect:%s", odeSessionModel().getResult().getBack());
	}
	
	@PostMapping(value = "/result", params = "valueTable")
	public ModelAndView excelSubmit(final Model model) {
		model.addAttribute("results" , odeSessionModel().getResult().getResults());
		model.addAttribute("resultsTitle" ,odeSessionModel().getResult().getTitle());
		return new ModelAndView( resultsExcelView);
		
	}
	
	@PostMapping(value = "/result", params = "graph")
	public ModelAndView graphSubmit(final Model model) {
		model.addAttribute("results" , odeSessionModel().getResult().getResults());
		model.addAttribute("resultsTitle" ,odeSessionModel().getResult().getTitle());
		return new ModelAndView( resultsGraphView);
	}
	
	@Lookup
	abstract OdeSessionModel odeSessionModel( );

}