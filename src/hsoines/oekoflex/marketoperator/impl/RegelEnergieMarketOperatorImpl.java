package hsoines.oekoflex.marketoperator.impl;

import hsoines.oekoflex.bid.Supply;
import hsoines.oekoflex.energytrader.EOMOperatorListener;
import hsoines.oekoflex.marketoperator.RegelEnergieMarketOperator;
import hsoines.oekoflex.util.Duration;
import hsoines.oekoflex.util.TimeUtilities;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;

import java.util.ArrayList;
import java.util.List;

/**
 * User: jh
 * Date: 07/01/16
 * Time: 10:55
 */
public final class RegelEnergieMarketOperatorImpl implements RegelEnergieMarketOperator {
    private static final Log log = LogFactory.getLog(RegelEnergieMarketOperatorImpl.class);

    private final String name;
    private final int quantity;
    private final List<Supply> supplies = new ArrayList<Supply>();
    private long totalClearedQuantity;
    private float lastClearedMaxPrice;
    private float lastAssignmentRate;

    public RegelEnergieMarketOperatorImpl(String name) {
        this.name = name;
        Parameters p = RunEnvironment.getInstance().getParameters();
        this.quantity = (int) p.getValue("rigidDemandEnergyOnlyMarket");
    }

    RegelEnergieMarketOperatorImpl(int quantity) {
        this.quantity = quantity;
        this.name = "test_only";
    }

    @Override
    public void addSupply(final Supply supply) {
        supplies.add(supply);
    }

    @Override
    public void clearMarket() {
        supplies.sort((o1, o2) -> Float.compare(o1.getPrice(), o2.getPrice()));
        totalClearedQuantity = 0;
        lastAssignmentRate = 0;
        for (Supply supply : supplies) {
            EOMOperatorListener marketOperatorListener = supply.getMarketOperatorListener();
            if (totalClearedQuantity + supply.getQuantity() < quantity) {
                totalClearedQuantity += supply.getQuantity();
                lastAssignmentRate = 1;
                doNotify(supply, marketOperatorListener, 1);
            } else if (totalClearedQuantity >= quantity) {
                break;
            } else {
                lastAssignmentRate = (quantity - totalClearedQuantity) / (float) supply.getQuantity();
                doNotify(supply, marketOperatorListener, lastAssignmentRate);
                totalClearedQuantity += supply.getQuantity() * lastAssignmentRate;
            }
        }
        supplies.clear();
    }

    @Override
    public long getTotalClearedQuantity() {
        log.info("Cleared Quantity:" + totalClearedQuantity);
        return totalClearedQuantity;
    }

    @Override
    public float getLastAssignmentRate() {
        return lastAssignmentRate;
    }

    void doNotify(final Supply supply, final EOMOperatorListener marketOperatorListener, float assignRate) {
        long tick = TimeUtilities.getTick(TimeUtilities.getCurrentDate());
        for (int i = 0; i < Duration.FOUR_HOURS.getTicks(); i++) {
            marketOperatorListener.notifyEOMClearingDone(supply.getPrice(), assignRate, supply, TimeUtilities.getDate(tick + i));
        }
        lastClearedMaxPrice = supply.getPrice();
    }

    @Override
    public String getName() {
        return name;
    }

    public float getLastClearedMaxPrice() {
        return lastClearedMaxPrice;
    }
}
