package de.mq.odesolver.solve.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.mq.odesolver.solve.Ode;
import de.mq.odesolver.solve.OdeSolverService.Algorithm;
import de.mq.odesolver.support.OdeFunctionUtil.Language;

class OdeImplTest {
	
	private static final int STEPS = 10000;
	private static final int STOP = 10;
	private static final int START = 0;
	private static final double[] Y0 = new double[] {0,1};
	private static final String ODE2 = "y[1]/y[0]+x";
	
	private static final String ODE2_SYSTEM ="dy[0]= y[1];\n dy[1]=y[1]/y[0]+x";
	
	
	
	private final Ode ode = newOde2();
	
	private final Ode ode2System= newOde2System();

	private Ode newOde2() {
		return new OdeImpl(Language.Nashorn, ODE2, Algorithm.RungeKutta4thOrder, Y0, START, STOP, STEPS );
	}
	
	private Ode newOde2System() {
		return new OdeImpl(Language.Nashorn, ODE2_SYSTEM, Algorithm.DormandPrince853Integrator, Y0, START, STOP, STEPS );
	}
	
	private Ode newOde1() {
		return new OdeImpl(Language.Nashorn, "y[0]+x", Algorithm.RungeKutta4thOrder, new double[] {0}, START, STOP, STEPS );
	}
	
	@Test
	void ode() {
		assertEquals(ODE2, ode.ode());
	}
	
	@Test
	void language() {
		assertEquals(Language.Nashorn, ode.language());
	}
	
	@Test
	void beautifiedOdeOrder2() {
		assertEquals("y''=y'/y+x", ode.beautifiedOde());
	}
	
	@Test
	void beautifiedOdeOrder1() {
		assertEquals("y'=y+x", newOde1().beautifiedOde());
	}
	
	@Test
	void beautifiedOdeSystem() {
		assertEquals("y1'=y2; y2'=y2/y1+x", ode2System.beautifiedOde());
	}
	
	@Test
	void algorithm() {
		assertEquals(Algorithm.RungeKutta4thOrder, ode.algorithm());
	}

	@Test
	void y() {
		assertEquals(Y0, ode.y());
	}
	
	@Test
	void checkOrder() {
		assertTrue(ode.checkOrder(2));
		assertFalse(ode.checkOrder(1));
	}
	
	
	@Test
	void checkOrderSystem() throws Exception {
		assertTrue(ode2System.checkOrder(2));
		assertFalse(ode2System.checkOrder(3));
	    final var  field =OdeImpl.class.getDeclaredField("ode");
	    field.setAccessible(true);
	    field.setAccessible(true);
	    field.set(ode2System, ODE2);
	    assertFalse(ode2System.checkOrder(2));
	}
	
	@Test
	void checkOrderSystemNoSystem() throws Exception {
	    final var  field =OdeImpl.class.getDeclaredField("ode");
	    field.setAccessible(true);
	    field.setAccessible(true);
	    field.set(ode2System, ODE2);
	    assertFalse(ode2System.checkOrder(2));
	    field.set(ode2System, "");
	    assertFalse(ode2System.checkOrder(2));
	}
	
	@Test
	void checkOrderSystemOdeEmpty() throws Exception {
	    final var  field =OdeImpl.class.getDeclaredField("ode");
	    field.setAccessible(true);
	    field.setAccessible(true);
	    field.set(ode2System, "");
	    assertFalse(ode2System.checkOrder(2));
	}
	
	
	@ParameterizedTest
	@ValueSource(ints = {0,-1})
	void ckeckOrderWrongOrder(final int order) {
		assertThrows(IllegalArgumentException.class, () -> ode.checkOrder(order));
	}
	
	@Test
	void start() {
		assertEquals(START, ode.start());
	}
	
	
	@Test
	void stop() {
		assertEquals(STOP, ode.stop());
	}
	@Test
	void checkStartBeforeStop() {
		assertTrue(ode.checkStartBeforeStop());
	}
	
	@Test
	void checkStartBeforeStopFalse() {
		assertFalse( new OdeImpl(Language.Nashorn, ODE2, Algorithm.RungeKutta4thOrder, Y0, START, START, STEPS ).checkStartBeforeStop());
	}
	
	@Test
	void steps() {
		assertEquals(STEPS, ode.steps());
	}
	
	@Test
	void languageEmpty() {
		assertThrows(NullPointerException.class, () -> new OdeImpl(null ,ODE2, Algorithm.RungeKutta4thOrder, Y0, START, STOP, STEPS ));
	}
	
	@Test
	void odeEmpty() {
		assertThrows(NullPointerException.class, () -> new OdeImpl(Language.Nashorn,null, Algorithm.RungeKutta4thOrder, Y0, START, STOP, STEPS ));
	}
	
	@Test
	void AlgorithmNull() {
		assertThrows(NullPointerException.class, () -> new OdeImpl(Language.Nashorn, ODE2, null, Y0, START, STOP, STEPS ));
	}
	
	@Test
	void yNull() {
		assertThrows(NullPointerException.class, () -> new OdeImpl(Language.Nashorn, ODE2, Algorithm.RungeKutta4thOrder, null, START, STOP, STEPS ));
	}
	
	@Test
	void yLength0() {
		assertThrows(IllegalArgumentException.class, () -> new OdeImpl(Language.Nashorn,ODE2, Algorithm.RungeKutta4thOrder, new double[] {}, START, STOP, STEPS ));
	}
	
	@Test
	void steps0() {
		assertThrows(IllegalArgumentException.class, () -> new OdeImpl(Language.Nashorn,ODE2, Algorithm.RungeKutta4thOrder, Y0, START, STOP, 0 ));
	}
}
