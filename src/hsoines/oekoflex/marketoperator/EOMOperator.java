package hsoines.oekoflex.marketoperator;

import hsoines.oekoflex.bid.Demand;
import hsoines.oekoflex.bid.Supply;
import hsoines.oekoflex.domain.SequenceDefinition;
import repast.simphony.engine.schedule.ScheduledMethod;

import static hsoines.oekoflex.domain.SequenceDefinition.EOMClearingPriority;
import static hsoines.oekoflex.domain.SequenceDefinition.EOMInterval;

public interface EOMOperator {
	public void addDemand(Demand demand);
	public void addSupply(Supply supply);

	@ScheduledMethod(start = SequenceDefinition.SimulationStart, interval = EOMInterval, priority = EOMClearingPriority)
	void clearMarket();

	int getTotalClearedQuantity();

	float getLastClearedPrice();

	float getLastAssignmentRate();

	enum AssignmentType{
		PartialDemand, PartialSupply, Full
	}
}
