package de.mq.odesolver.function.support;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import de.mq.odesolver.Result;
import de.mq.odesolver.function.FunctionService;
import de.mq.odesolver.function.FunctionSolver;
import de.mq.odesolver.solve.support.ResultsExcelView;
import de.mq.odesolver.solve.support.ResultsGraphView;

@Controller
class FunctionController {

	private final ModelAndView functionModelAndView = new ModelAndView("function");
	private final Converter<FunctionModel, Function> converter;
	private final Map<String, ModelAndView> commands;
	
	private final FunctionService functionService; 

	FunctionController(final FunctionService functionService, final ResultsExcelView resultsExcelView, final ResultsGraphView resultsGraphView,
			final Converter<FunctionModel, Function> converter) {
		this.functionService=functionService;
		this.converter = converter;
		this.commands = Map.of("valueTable", new ModelAndView(resultsExcelView), "graph",
				new ModelAndView(resultsGraphView));
	}

	@GetMapping("/function")
	public ModelAndView solve(final Model model) {
		model.addAttribute("function", new FunctionModel());
		return functionModelAndView;
	}

	@PostMapping(value = "/function")
	public ModelAndView solveSubmit(@RequestParam(name = "command") final String command,
			@ModelAttribute("function") @Valid final FunctionModel functionModel, final BindingResult bindingResult,
			final Model model) {
		if (bindingResult.hasFieldErrors()) {
			return functionModelAndView;
		}

		final Function function = converter.convert(functionModel);

		if (!validate(function, bindingResult)) {
			return functionModelAndView;
		}

		if (!commands.containsKey(command)) {
			return functionModelAndView;
		}

		if (!calculate(function, model, bindingResult)) {
			return functionModelAndView;
		}
		
		

		return commands.get(command);

	}

	private boolean calculate(final Function function, final Model model, final BindingResult bindingResult) {
		try {
			final FunctionSolver functionSolver = functionService.functionSolver(function.function());
			
			final List<Result> results = functionSolver.solve(function.k(), function.start(), function.stop(), function.steps());

			 model.addAttribute("results",results);
			 model.addAttribute("resultsTitle", "y="+function.function());
			return true;
		} catch (final Exception exception) {
			bindingResult.addError(new ObjectError("function", exception.getMessage()));
			return false;
		}

	}

	private boolean validate(final Function function, final BindingResult bindingResult) {

		if (!function.checkStartBeforeStop()) {
			bindingResult.addError(new ObjectError("function", "Start muß < stop sein."));
		}

		try {
            functionService.validateValue(function.function(), function.start(), function.k()) ;
		} catch (final Exception exception) {
			bindingResult.addError(new ObjectError("function", exception.getMessage()));
		}

		return !bindingResult.hasGlobalErrors();
	}

}