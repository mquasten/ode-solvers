package de.mq.odesolver.solve.support;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.convert.converter.Converter;
import org.springframework.validation.BindingResult;

import de.mq.odesolver.result.support.ResultModel;
import de.mq.odesolver.solve.Ode;
import de.mq.odesolver.solve.OdeResult;
import de.mq.odesolver.solve.OdeSolverService;
import de.mq.odesolver.solve.OdeSolverService.Algorithm;
import de.mq.odesolver.support.BasicMockitoControllerTest;

class SolveControllerTest extends BasicMockitoControllerTest {

	private final OdeSolverService odeSolverService = Mockito.mock(OdeSolverService.class);
	
	@SuppressWarnings("unchecked")
	private final Converter<OdeModel,Ode> converter = Mockito.mock(Converter.class);
	private final SolveController solveController = new SolveController(odeSolverService, odeSessionModelRepository(), converter, messageSource());


	@Test
	void solve() {
		assertNotNull(odeSessionModel().getOdeModel());
		assertNotNull(odeSessionModel().getSettings());
		assertNotNull(odeSessionModel().getSettings().getScriptLanguage());

		assertEquals(SolveController.SOLVE_VIEW, solveController.solve(model()));

		assertEquals(3, attributes().size());
		assertEquals(odeSessionModel().getOdeModel(), attributes().get(SolveController.ATTRIBUTE_ODE));
		assertInitModelAttributes();
	}
	
	@Test
	void solveSubmit() {
		
		final  List<OdeResult> results = Arrays.asList(new OdeResultImpl(new double[] {0}, 0),new OdeResultImpl( new double[] {1}, 1) );
		final var odeModel = Mockito.mock(OdeModel.class);
		Mockito.when(odeModel.getOrder()).thenReturn(1);
		final BindingResult  bindingResult = Mockito.mock(BindingResult.class);
		final Ode ode = Mockito.mock(Ode.class);
		Mockito.when(ode.checkOrder(odeModel.getOrder())).thenReturn(true);
		Mockito.when(ode.beautifiedOde()).thenReturn("y'=y+x");
		Mockito.when(converter.convert(odeModel)).thenReturn(ode);
		Mockito.when(ode.checkStartBeforeStop()).thenReturn(true);
		Mockito.when(odeSolverService.solve(ode)).thenReturn(results);
		
		assertEquals(SolveController.REDIRECT_RESULT_VIEW, solveController.solveSubmit(odeModel,  bindingResult, model(), locale()));
		
		Mockito.verify(odeSolverService).validateRightSide(ode);
		Mockito.verify(odeSolverService).solve(ode);
		
		assertEquals(results,odeSessionModel().getResult().getResults());
		assertEquals(ode.beautifiedOde(), odeSessionModel().getResult().getTitle());
		assertEquals(SolveController.SOLVE_VIEW, odeSessionModel().getResult().getBack());
		assertInitModelAttributes();
	}

	private void assertInitModelAttributes() {
		assertEquals(odeSessionModel().getSettings().getScriptLanguage(), attributes().get(SolveController.ATTRIBUTE_SCRIPT_LANGUAGE));
		@SuppressWarnings("unchecked")
		final List<Entry<String, String>> algorithms = (List<Entry<String, String>>) attributes().get(SolveController.ATTRIBUTE_ALGORITHMS);
		assertEquals(4, algorithms.size());
		assertEquals(Algorithm.DormandPrince853Integrator.name(), algorithms.get(0).getKey());
		assertEquals(Algorithm.DormandPrince853Integrator.name(), algorithms.get(0).getValue());
		assertEquals(Algorithm.RungeKutta4thOrder.name(), algorithms.get(1).getKey());
		assertEquals(Algorithm.RungeKutta4thOrder.name(), algorithms.get(1).getValue());
		assertEquals(Algorithm.RungeKutta2ndOrder.name(), algorithms.get(2).getKey());
		assertEquals(Algorithm.RungeKutta2ndOrder.name(), algorithms.get(2).getValue());
		assertEquals(Algorithm.EulerPolygonal.name(), algorithms.get(3).getKey());
		assertEquals(Algorithm.EulerPolygonal.name(), algorithms.get(3).getValue());
	}
	
	
	@Test
	void solveSubmitFieldErrors() {
		final var odeModel = Mockito.mock(OdeModel.class);
		Mockito.when(bindingResult().hasFieldErrors()).thenReturn(true);
		
		assertEquals(SolveController.SOLVE_VIEW, solveController.solveSubmit(odeModel,  bindingResult(), model(), locale()));
		
		assertInitModelAttributes();
	}
	
	@Test
	void solveSubmitWrongOrderAndStartNotBeforeStop() {
		final var odeModel = Mockito.mock(OdeModel.class);
		final Ode ode = Mockito.mock(Ode.class);
		Mockito.when(converter.convert(odeModel)).thenReturn(ode);
		
		assertEquals(SolveController.SOLVE_VIEW, solveController.solveSubmit(odeModel,  bindingResult(), model(), locale()));
		
		assertEquals(2, globalErrors().size());
		final var wrongOrder = globalErrors().get(0);
		assertEquals(SolveController.ATTRIBUTE_ODE, wrongOrder.getObjectName());
		assertEquals(SolveController.I18N_WRONG_NUMBER_INITIAL_VALUES, wrongOrder.getDefaultMessage());
	
		final var startNotBeforeStop = globalErrors().get(1);
		assertEquals(SolveController.ATTRIBUTE_ODE, startNotBeforeStop.getObjectName());
		assertEquals(SolveController.I18N_START_LESS_THAN_STOP, startNotBeforeStop.getDefaultMessage());
		
		assertInitModelAttributes();
	}
	
	@Test
	void solveSubmitCalculationForInitialValesFailed() {
		final var  resultModel = Mockito.mock(ResultModel.class);
		odeSessionModel().setResult(resultModel);
		final var odeModel = Mockito.mock(OdeModel.class);
		Mockito.when(odeModel.getOrder()).thenReturn(1);
		
		final Ode ode = Mockito.mock(Ode.class);
		Mockito.when(ode.checkOrder(odeModel.getOrder())).thenReturn(true);
		Mockito.when(ode.beautifiedOde()).thenReturn("y'=y+x");
		Mockito.when(converter.convert(odeModel)).thenReturn(ode);
		Mockito.when(ode.checkStartBeforeStop()).thenReturn(true);
		final var exception = new IllegalArgumentException("errormessage");
		Mockito.doThrow(exception).when(odeSolverService).validateRightSide(ode);
		
		assertEquals(SolveController.SOLVE_VIEW, solveController.solveSubmit(odeModel,  bindingResult(), model(), locale()));
		
		assertEquals(resultModel, odeSessionModel().getResult());
		assertInitModelAttributes();
		
		Mockito.verify(odeSolverService, Mockito.never()).solve(ode);
		
		assertEquals(1, globalErrors().size());
		assertEquals(SolveController.ATTRIBUTE_ODE, globalErrors().get(0).getObjectName());
		assertEquals(exception.getMessage(), globalErrors().get(0).getDefaultMessage());
	}
	
	
	@Test
	void solveSubmitCalculateFailed() {	
		final var  resultModel = Mockito.mock(ResultModel.class);
		odeSessionModel().setResult(resultModel);
		final var odeModel = Mockito.mock(OdeModel.class);
		Mockito.when(odeModel.getOrder()).thenReturn(1);
		final Ode ode = Mockito.mock(Ode.class);
		Mockito.when(ode.checkOrder(odeModel.getOrder())).thenReturn(true);
		Mockito.when(ode.beautifiedOde()).thenReturn("y'=y+x");
		Mockito.when(converter.convert(odeModel)).thenReturn(ode);
		Mockito.when(ode.checkStartBeforeStop()).thenReturn(true);
		final var exception = new IllegalArgumentException("errormessage");
		Mockito.doThrow(exception).when(odeSolverService).solve(ode);
		
		assertEquals(SolveController.SOLVE_VIEW, solveController.solveSubmit(odeModel,  bindingResult(), model(), locale()));
		
		Mockito.verify(odeSolverService).validateRightSide(ode);
		assertEquals(resultModel, odeSessionModel().getResult());
		assertInitModelAttributes();
		
		assertEquals(SolveController.ATTRIBUTE_ODE, globalErrors().get(0).getObjectName());
		assertEquals(exception.getMessage(),  globalErrors().get(0).getDefaultMessage());
	}
	
	@Test
	void solveReset() {
		final OdeModel odeModel = Mockito.mock(OdeModel.class);
		Mockito.when(odeModel.getOde()).thenReturn("y[0]+x");
		
		solveController.solveReset(model());
		
		assertInitModelAttributes();
		
		assertNull(odeSessionModel().getOdeModel().getOde());
		assertNotEquals(odeModel, odeSessionModel().getOdeModel());
	}

}
