package hsoines.oekoflex.energytrader.impl;

import hsoines.oekoflex.bid.*;
import hsoines.oekoflex.energytrader.BalancingMarketTrader;
import hsoines.oekoflex.energytrader.EOMTrader;
import hsoines.oekoflex.energytrader.MarketOperatorListener;
import hsoines.oekoflex.energytrader.TradeRegistry;
import hsoines.oekoflex.marketoperator.BalancingMarketOperator;
import hsoines.oekoflex.marketoperator.SpotMarketOperator;
import hsoines.oekoflex.util.Market;
import hsoines.oekoflex.util.TimeUtil;

import java.util.Date;
import java.util.List;

/**
 * User: jh
 * Date: 18/01/16
 * Time: 16:14
 */
public final class FlexPowerplant implements EOMTrader, BalancingMarketTrader, MarketOperatorListener {
    private final String name;
    private final String description;
    private final int powerMax;
    private final int powerMin;
    private final float shutdownCosts;
    private final int powerRampUp;
    private final int powerRampDown;
    private final float marginalCosts;
    private SpotMarketOperator eomMarketOperator;
    private final TradeRegistry energyTradeRegistry;
    private final TradeRegistry powerTradeRegistry;
    private BalancingMarketOperator balancingMarketOperator;
    private float lastAssignmentRate;
    private float lastClearedPrice;

    public FlexPowerplant(final String name, final String description, final int powerMax, final int powerMin, final int powerRampUp, final int powerRampDown, final float marginalCosts, final float shutdownCosts) {
        this.name = name;
        this.description = description;
        this.powerMax = powerMax;
        this.powerMin = powerMin;
        this.powerRampUp = powerRampUp;
        this.powerRampDown = powerRampDown;
        this.marginalCosts = marginalCosts;
        this.shutdownCosts = shutdownCosts;
        energyTradeRegistry = new TradeRegistryImpl(TradeRegistry.Type.PRODUCE, powerMax);
        powerTradeRegistry = new TradeRegistryImpl(TradeRegistry.Type.PRODUCE, powerMax);
    }

    @Override
    public void setSpotMarketOperator(final SpotMarketOperator spotMarketOperator) {
        this.eomMarketOperator = spotMarketOperator;
    }

    @Override
    public void makeBidEOM() {
        float t = TimeUtil.HOUR_PER_TICK;

        Date currentDate = TimeUtil.getCurrentDate();
        Date precedingDate = TimeUtil.precedingDate(currentDate);

        float pPositiveCommited = powerTradeRegistry.getPositiveQuantityUsed(currentDate);
        float pNegativeCommited = powerTradeRegistry.getNegativeQuantityUsed(currentDate);
        float ePreceding = energyTradeRegistry.getPositiveQuantityUsed(precedingDate);
        if (ePreceding == 0) {
            ePreceding = powerMin * t;
        }

        float eMustRun = Math.max((powerMin + pNegativeCommited) * t, ePreceding - powerRampDown * t);
        eomMarketOperator.addSupply(new EnergySupplyMustRun(-shutdownCosts / eMustRun, eMustRun, this));

        float eFlex = Math.min((powerMax - pPositiveCommited) * t - eMustRun, ePreceding + powerRampUp * t - eMustRun);
        eomMarketOperator.addSupply(new EnergySupply(marginalCosts, eFlex, this));
    }

    @Override
    public void makeBidBalancingMarket() {
        Date currentDate = TimeUtil.getCurrentDate();
        Date precedingDate = TimeUtil.precedingDate(currentDate);

        int pPreceding = (int) (energyTradeRegistry.getQuantityUsed(precedingDate) / TimeUtil.HOUR_PER_TICK);
        if (pPreceding == 0) {
            pPreceding = powerMin;
        }

        int pNeg = Math.min(pPreceding - powerMin, powerRampDown);
        balancingMarketOperator.addNegativeSupply(new PowerNegative(marginalCosts, pNeg, this));   //price???

        int pPos = Math.min(powerMax - pPreceding, powerRampUp);
        balancingMarketOperator.addPositiveSupply(new PowerPositive(marginalCosts, pPos, this));   //price???
    }

    @Override
    public void setBalancingMarketOperator(final BalancingMarketOperator balancingMarketOperator) {
        this.balancingMarketOperator = balancingMarketOperator;
    }

    @Override
    public void notifyClearingDone(final Date currentDate, final Market market, final Bid bid, final float clearedPrice, final float rate) {
        switch (bid.getBidType()) {
            case ENERGY_SUPPLY_MUSTRUN:
            case ENERGY_SUPPLY:
                energyTradeRegistry.addAssignedQuantity(currentDate, market, bid.getPrice(), clearedPrice, bid.getQuantity(), rate, bid.getBidType());
                break;
            case POWER_NEGATIVE:
            case POWER_POSITIVE:
                powerTradeRegistry.addAssignedQuantity(currentDate, market, bid.getPrice(), clearedPrice, bid.getQuantity(), rate, bid.getBidType());
                break;
        }
        if (market.equals(Market.SPOT_MARKET)) {
            this.lastClearedPrice = clearedPrice;
            this.lastAssignmentRate = rate;
        }
    }

    @Override
    public float getLastClearedPrice() {
        return lastClearedPrice;
    }

    @Override
    public float getLastAssignmentRate() {
        return lastAssignmentRate;
    }

    @Override
    public List<TradeRegistryImpl.EnergyTradeElement> getCurrentAssignments() {
        List<TradeRegistryImpl.EnergyTradeElement> powerTradeElements = powerTradeRegistry.getEnergyTradeElements(TimeUtil.getCurrentDate());
        List<TradeRegistryImpl.EnergyTradeElement> energyTradeElements = energyTradeRegistry.getEnergyTradeElements(TimeUtil.getCurrentDate());
        powerTradeElements.addAll(energyTradeElements);
        return powerTradeElements;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }
}