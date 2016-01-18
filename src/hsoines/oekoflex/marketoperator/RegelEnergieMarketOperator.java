package hsoines.oekoflex.marketoperator;

import hsoines.oekoflex.OekoflexAgent;
import hsoines.oekoflex.bid.Supply;
import hsoines.oekoflex.domain.SequenceDefinition;
import repast.simphony.engine.schedule.ScheduledMethod;

public interface RegelEnergieMarketOperator extends OekoflexAgent {
	void addSupply(Supply supply);

	@ScheduledMethod(start = SequenceDefinition.SimulationStart, interval = SequenceDefinition.RegelenergieMarketInterval, priority = SequenceDefinition.RegelenergieMarketClearingPriority)
	void clearMarket();


	long getTotalClearedQuantity();

	float getLastAssignmentRate();
}
