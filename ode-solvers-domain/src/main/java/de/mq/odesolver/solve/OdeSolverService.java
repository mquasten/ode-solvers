package de.mq.odesolver.solve;

import java.util.List;

import de.mq.odesolver.support.OdeFunctionUtil.Language;

public interface OdeSolverService {

	public enum Algorithm {
		EulerPolygonal(1), RungeKutta2ndOrder(2), RungeKutta4thOrder(4) , DormandPrince853Integrator(8,true);

		private final int order;
		private final boolean system;

		Algorithm(final int order, boolean system) {
			this.order = order;
			this.system=system;
		}
		Algorithm(final int order) {
			this(order, false);
		}

		public final int order() {
			return order;
		}
		
		public final boolean isSystem() {
			return system;
		}
		
		

	}

	OdeSolver odeSolver(final Language language, final Algorithm algorithm, final String function);

	List<OdeResult> solve(final Ode ode);

	double validateRightSide(final Language language, final String function, final double y0[], final double x0);

	double validateRightSide(final Ode ode);

}