package hsoines.oekoflex.energytrader.impl;

import hsoines.oekoflex.bid.EnergySupply;
import hsoines.oekoflex.marketoperator.impl.BalancingMarketOperatorImpl;
import hsoines.oekoflex.priceforwardcurve.PriceForwardCurve;
import hsoines.oekoflex.priceforwardcurve.impl.PriceForwardCurveImpl;
import hsoines.oekoflex.tools.RepastTestInitializer;
import hsoines.oekoflex.util.Market;
import hsoines.oekoflex.util.TimeUtil;
import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * User: jh
 * Date: 15/06/16
 * Time: 21:22
 */
public class FlexPowerplant2Test {
    public static final float SHUTDOWN_COSTS = 50000f;
    public static final float MARGINAL_COSTS = 2f;
    public static final int POWER_RAMP_DOWN = 200;
    public static final int POWER_RAMP_UP = 100;
    public static final int POWER_MIN = 2000;
    public static final int POWER_MAX = 2400;
    public static final int POSITIVE_DEMAND_BALANCING = 100;
    public static final int NEGATIVE_DEMAND_BALANCING = 100;
    private BalancingMarketOperatorImpl balancingMarketOperator;
    private FlexPowerplant2 flexpowerplant;
    private TestSpotMarketOperator eomOperator;

    @Before
    public void setUp() throws Exception {
        BasicConfigurator.configure();

        RepastTestInitializer.init();
        eomOperator = new TestSpotMarketOperator();

        final File priceForwardOutFile = new File("src-test/resources/price-forward.csv");
        final PriceForwardCurve priceForwardCurve = new PriceForwardCurveImpl(priceForwardOutFile);
        priceForwardCurve.readData();
        flexpowerplant = new FlexPowerplant2("flexpowerplant", "description",
                POWER_MAX, POWER_MIN, POWER_RAMP_UP, POWER_RAMP_DOWN, MARGINAL_COSTS, SHUTDOWN_COSTS,
                priceForwardCurve);
        flexpowerplant.setBalancingMarketOperator(balancingMarketOperator);
        flexpowerplant.setSpotMarketOperator(eomOperator);
    }

    @Test
    public void testSpotMarketBid() throws Exception {
        float pFlex;
        TimeUtil.startAt(0);

        float eMustRun = POWER_MIN * TimeUtil.HOUR_PER_TICK;
        pFlex = POWER_RAMP_UP;
        checkEOMBids(eMustRun, pFlex, 1, 1);
        //->POWER: 2100

        eMustRun = (POWER_MIN) * TimeUtil.HOUR_PER_TICK;
        pFlex = 2 * POWER_RAMP_UP;
        checkEOMBids(eMustRun, pFlex, 1, 1);
        //->POWER: 2200

        eMustRun = POWER_MIN * TimeUtil.HOUR_PER_TICK;
        pFlex = 3 * POWER_RAMP_UP;
        checkEOMBids(eMustRun, pFlex, 1, 1);
        //->POWER: 2300

        pFlex = POWER_RAMP_DOWN + POWER_RAMP_UP;
        eMustRun = (2300 - POWER_RAMP_DOWN) * TimeUtil.HOUR_PER_TICK;
        checkEOMBids(eMustRun, pFlex, 1, 1);
        //->POWER: 2400

        pFlex = POWER_RAMP_DOWN;
        eMustRun = (2400 - POWER_RAMP_DOWN) * TimeUtil.HOUR_PER_TICK;
        checkEOMBids(eMustRun, pFlex, 1, 1);
        //->POWER: 2400

        pFlex = POWER_RAMP_DOWN;
        eMustRun = (2400 - POWER_RAMP_DOWN) * TimeUtil.HOUR_PER_TICK;
        checkEOMBids(eMustRun, pFlex, 1, 0);
        //->POWER: 2200

        pFlex = POWER_RAMP_DOWN + POWER_RAMP_UP;
        eMustRun = (2200 - POWER_RAMP_DOWN) * TimeUtil.HOUR_PER_TICK;
        checkEOMBids(eMustRun, pFlex, 1, 0);
        //->POWER: 2000

        pFlex = POWER_RAMP_UP;
        eMustRun = (2000) * TimeUtil.HOUR_PER_TICK;
        checkEOMBids(eMustRun, pFlex, 1, 0);
        //->POWER: 2000
    }

    void checkEOMBids(final float eMustRun, final float pFlex, final float mustRunRate, final float flexRate) {
        flexpowerplant.makeBidEOM();
        int i = (int) TimeUtil.getCurrentTick() * 2;
        final EnergySupply energySupply0 = eomOperator.getEnergySupply(i);
        assertEquals(eMustRun, energySupply0.getQuantity(), 0.00001f);
        assertEquals(-SHUTDOWN_COSTS / eMustRun + MARGINAL_COSTS, energySupply0.getPrice(), 0.00001f);
        final EnergySupply energySupply1 = eomOperator.getEnergySupply(i + 1);
        assertEquals(pFlex * TimeUtil.HOUR_PER_TICK, energySupply1.getQuantity(), 0.00001f);
        assertEquals(MARGINAL_COSTS, energySupply1.getPrice(), 0.00001f);
        flexpowerplant.notifyClearingDone(TimeUtil.getCurrentDate(), Market.SPOT_MARKET, energySupply0, 0f, mustRunRate);
        flexpowerplant.notifyClearingDone(TimeUtil.getCurrentDate(), Market.SPOT_MARKET, energySupply1, 0f, flexRate);
        TimeUtil.nextTick();
    }
}