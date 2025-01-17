package hsoines.oekoflex.priceforwardcurve.impl;

import hsoines.oekoflex.builder.CSVParameter;
import hsoines.oekoflex.builder.OekoFlexContextBuilder;
//import hsoines.oekoflex.builder.traderfactories.FlexPowerplant2Factory;
import hsoines.oekoflex.builder.traderfactories.FlexPowerplant3Factory;
import hsoines.oekoflex.builder.traderfactories.TotalLoadFactory;
//import hsoines.oekoflex.energytrader.impl.FlexPowerplant2;
import hsoines.oekoflex.energytrader.impl.FlexPowerplant3;
import hsoines.oekoflex.energytrader.impl.TotalLoad;
import hsoines.oekoflex.marketoperator.SpotMarketOperator;
import hsoines.oekoflex.marketoperator.impl.SpotMarketOperatorImpl;
import hsoines.oekoflex.util.TimeUtil;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

/**
 * Created by jhense on 13.03.2016.
 * erzeugt PFC
 */
public class PriceForwardCurveGenerator {
    private static final Log log = LogFactory.getLog(PriceForwardCurveGenerator.class);

    private int ticksToRun;
    private final long prerunTicks;
    private TotalLoad renewables;
    private TotalLoad totalload;
//    private final Set<FlexPowerplant2> flexPowerplants;
    private final Set<FlexPowerplant3> flexPowerplants;
    private final SpotMarketOperator spotMarketOperator;
    private CSVPrinter csvPrinter;

    public PriceForwardCurveGenerator(File configDir, int ticksToRun, final File priceForwardFile, long prerunTicks, final Properties globalProperties) throws IOException {
        this.ticksToRun = ticksToRun;
        this.prerunTicks = prerunTicks;

        spotMarketOperator = new SpotMarketOperatorImpl("pfc-spotmarketoperator", "", false);
//        flexPowerplants = FlexPowerplant2Factory.build(configDir, globalProperties);
        flexPowerplants = FlexPowerplant3Factory.build(configDir, globalProperties);

        for (FlexPowerplant3 flexPowerplant : flexPowerplants) {
            flexPowerplant.setSpotMarketOperator(spotMarketOperator);
        }

        Set<TotalLoad> totalLoads = TotalLoadFactory.build(configDir, prerunTicks);
        for (TotalLoad totalLoad : totalLoads) {
            totalLoad.setSpotMarketOperator(spotMarketOperator);
            if (totalLoad.getName().equals("renewables")) {
                renewables = totalLoad;
            }
            if (totalLoad.getName().equals("totalload")) {
                totalload = totalLoad;
            }
        }
        if (!priceForwardFile.getParentFile().exists()) {
            if (!priceForwardFile.getParentFile().mkdirs()) {
                throw new IllegalStateException("couldn't create directories.");
            }
        }
        final Appendable out;
        try {
            out = new FileWriter(priceForwardFile);
            csvPrinter = CSVParameter.getCSVFormat().withHeader("tick", "price").print(out);
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }

    public void generate() {
        TimeUtil.startAt(-prerunTicks);
        for (long tick = -prerunTicks; tick < ticksToRun; tick++) {
            log.debug("Building pfc for tick: " + tick);
//            for (FlexPowerplant2 flexPowerplant : flexPowerplants) {
            for (FlexPowerplant3 flexPowerplant : flexPowerplants) {
                flexPowerplant.makeBidEOM(tick);
            }
            renewables.makeBidEOM(tick);
            totalload.makeBidEOM(tick);
            spotMarketOperator.clearMarket();
            logPriceForward(tick, csvPrinter);
            TimeUtil.nextTick();
        }
        TimeUtil.reset();

        try {
            csvPrinter.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void logPriceForward(long tick, CSVPrinter csvPrinter) {
        try {
            csvPrinter.printRecord(tick, OekoFlexContextBuilder.defaultNumberFormat.format(spotMarketOperator.getLastClearedPrice()));
        } catch (IOException e) {
            log.error(e.toString(), e);
        }
    }

}
