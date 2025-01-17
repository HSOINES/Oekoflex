package hsoines.oekoflex.energytrader.impl;

import hsoines.oekoflex.bid.Bid;
import hsoines.oekoflex.bid.EnergyDemand;
import hsoines.oekoflex.bid.EnergySupply;
import hsoines.oekoflex.builder.CSVParameter;
import hsoines.oekoflex.builder.OekoFlexContextBuilder;
import hsoines.oekoflex.energytrader.EOMTrader;
import hsoines.oekoflex.energytrader.TradeRegistry;
import hsoines.oekoflex.marketoperator.SpotMarketOperator;
import hsoines.oekoflex.util.Market;
import hsoines.oekoflex.util.TimeUtil;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * Die csv-Datei beschreibt die Energie pro 15 Minuten.
 */
public final class TotalLoad implements EOMTrader {
    private static final Log log = LogFactory.getLog(TotalLoad.class);
    public static final float MAX_DEMAND_PRICE = 3000f;
    public static final float MIN_SUPPLY_PRICE = -3000f;
    private TradeRegistry energyTradeRegistry;
    private final String name;
    private final String description;
    private final Type type;
    private File csvFile;

    private SpotMarketOperator marketOperator;
    private float lastClearedPrice;
    private float lastAssignmentRate;

    public TotalLoad(final String name, final String description, Type type, final File csvFile, long prerunTicks) throws IOException {
        this.name = name;
        this.description = description;
        this.type = type;
        this.csvFile = csvFile;
        energyTradeRegistry = new TradeRegistryImpl(TradeRegistry.Type.CONSUM, 0, 1000000);
        init();
        energyTradeRegistry.duplicateCapacity(prerunTicks);
    }

    private void init() {
        FileReader reader = null;
        try {
            reader = new FileReader(csvFile);
            CSVParser parser = CSVParameter.getCSVFormat().parse(reader);
            float data = -1;
            for (CSVRecord parameters : parser) {
                long tick = Long.parseLong(parameters.get("tick"));
                switch (type) {
                    case DEMAND:
                        data = OekoFlexContextBuilder.defaultNumberFormat.parse(parameters.get("demand")).floatValue();
                        break;
                    case SUPPLY:
                        data = OekoFlexContextBuilder.defaultNumberFormat.parse(parameters.get("supply")).floatValue();
                        break;
                }
                energyTradeRegistry.setCapacity(tick, data);
            }
        } catch (IOException | NumberFormatException e) {
            log.error(e.getMessage(), e);
        } catch (ParseException e) {
            log.error(e.toString(), e);
        }
    }

    @Override
    public void setSpotMarketOperator(final SpotMarketOperator spotMarketOperator) {
        this.marketOperator = spotMarketOperator;
    }

    @Override
    public void makeBidEOM() {
        long currentTick = TimeUtil.getCurrentTick();
        makeBidEOM(currentTick);
    }

    public void makeBidEOM(long currentTick) {
        Date currentDate = TimeUtil.getDate(currentTick);
        float remainingCapacity = energyTradeRegistry.getRemainingCapacity(currentDate, Market.SPOT_MARKET);
        switch (type) {
            case DEMAND:
                marketOperator.addDemand(new EnergyDemand(MAX_DEMAND_PRICE, remainingCapacity, this));
                break;
            case SUPPLY:
                marketOperator.addSupply(new EnergySupply(MIN_SUPPLY_PRICE, remainingCapacity, this));
                break;
            default:
                throw new IllegalStateException("type unknown:" + type);
        }
    }

    @Override
    public void notifyClearingDone(final Date currentDate, final Market market, final Bid bid, final float clearedPrice, final float rate) {
        energyTradeRegistry.addAssignedQuantity(currentDate, market, bid.getPrice(), clearedPrice, bid.getQuantity(), rate, bid.getBidType());
        lastClearedPrice = clearedPrice;
        lastAssignmentRate = rate;
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
        return energyTradeRegistry.getEnergyTradeElements(TimeUtil.getCurrentDate());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }


    @Override
    public float getCurrentPower() {
        throw new IllegalStateException("not implemented");
    }


    public enum Type {DEMAND, SUPPLY}
}
