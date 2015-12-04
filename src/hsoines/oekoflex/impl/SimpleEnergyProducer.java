package hsoines.oekoflex.impl;

import hsoines.oekoflex.Bid;
import hsoines.oekoflex.EnergyProducer;
import hsoines.oekoflex.MarketOperatorListener;
import hsoines.oekoflex.MarketOperator;
import hsoines.oekoflex.ask.Support;
import repast.simphony.engine.schedule.ScheduledMethod;

public class SimpleEnergyProducer implements EnergyProducer, MarketOperatorListener {

    private MarketOperator marketOperator;
    private float lastAssignmentRate;

    @ScheduledMethod(start = 1, interval = 1, priority = 100)
    public void makeBid(){
        marketOperator.addSupport(new Support((float)(100f * Math.random()), (int) (3000 * Math.random()), this));
    }

    @Override
    public void setMarketOperator(final MarketOperator marketOperator) {
        this.marketOperator = marketOperator;
    }

    @Override
    public void notifyAssignmentRate(final float rate, final Bid bid) {
        lastAssignmentRate = rate;
    }

    public float getLastAssignmentRate() {
        return lastAssignmentRate;
    }
}
