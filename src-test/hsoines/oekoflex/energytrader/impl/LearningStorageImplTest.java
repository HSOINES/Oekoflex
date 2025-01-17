//package hsoines.oekoflex.energytrader.impl;
//
//import hsoines.oekoflex.energytrader.tools.TestBalancingMarketOperator;
//import hsoines.oekoflex.energytrader.tools.TestSpotMarketOperator;
//import hsoines.oekoflex.priceforwardcurve.PriceForwardCurve;
//import hsoines.oekoflex.priceforwardcurve.impl.PriceForwardCurveImpl;
//import hsoines.oekoflex.tools.RepastTestInitializer;
//import hsoines.oekoflex.util.TimeUtil;
//
//import org.apache.log4j.BasicConfigurator;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.io.File;
//
//import static org.junit.Assert.assertEquals;
//
///**
// * 
// */
//public class LearningStorageImplTest {
//
//    private TestBalancingMarketOperator testBalancingMarketOperator;
//    private TestSpotMarketOperator testEomOperator;
//    private PriceForwardCurve priceForwardCurve;
//    LearningStorage lst = null;
//
//    @Before
//    public void setUp() throws Exception {
//        BasicConfigurator.configure();
//
//        RepastTestInitializer.init();
//        testEomOperator = new TestSpotMarketOperator();
//        testBalancingMarketOperator = new TestBalancingMarketOperator();
//
//        final File priceForwardOutFile = new File("src-test/resources/pfc-learning-storage.csv");
//        priceForwardCurve = new PriceForwardCurveImpl(priceForwardOutFile);
//        priceForwardCurve.readData();
//        lst = new LearningStorage("l1", "skl", 10, 10, 0, priceForwardCurve, 0,160.0f,0.0f);
//        lst.setBalancingMarketOperator(testBalancingMarketOperator);
//        lst.setSpotMarketOperator(testEomOperator);
//
//    }
//
//    @Test
//    public void testSpotMarketBid() throws Exception {
//        TimeUtil.startAt(0);
////        testEomOperator.makeBid(lst).checkQuantities(new float[]{500, 16.666667f}).checkSupplyPrices(new float[]{-120 + 2, 2}).notifySupplyRates(new float[]{1f, 1f}).checkPower(2066.66667f);
//        testEomOperator.makeBid(lst).checkDemandQuantities(new float[]{2.5f}).checkDemandPrices(new float[]{3000}).notifyDemandRates(new float[]{1f}).checkPower(10);
//        TimeUtil.startAt(1);
//        testEomOperator.makeBid(lst).checkDemandQuantities(new float[]{2.5f}).checkDemandPrices(new float[]{3000}).notifyDemandRates(new float[]{.5f}).checkPower(5);
//
//        
////        TimeUtil.startAt(1);
////        testEomOperator.makeBid(flexpowerplant).checkQuantities(new float[]{2,5f}).checkSupplyPrices(new float[]{3000}).notifySupplyRates(new float[]{1f}).checkPower(10);
////        TimeUtil.startAt(2);
////        testEomOperator.makeBid(flexpowerplant).checkQuantities(new float[]{500, 50}).checkSupplyPrices(new float[]{-120 + 2, 2}).notifySupplyRates(new float[]{1f, 1f}).checkPower(2200);
////        TimeUtil.startAt(3);
////        testEomOperator.makeBid(flexpowerplant).checkQuantities(new float[]{500, 66.6666f}).checkSupplyPrices(new float[]{-120 + 2, 2}).notifySupplyRates(new float[]{1f, 1f}).checkPower(2266.6666f);
////        TimeUtil.startAt(4);
////        testEomOperator.makeBid(flexpowerplant).checkQuantities(new float[]{516.6666f, 66.6666f}).checkSupplyPrices(new float[]{-120 + 2, 2}).notifySupplyRates(new float[]{1f, 1f}).checkPower(2333.3333f);
////        TimeUtil.startAt(5);
////        testEomOperator.makeBid(flexpowerplant).checkQuantities(new float[]{533.3333f, 58.3333f}).checkSupplyPrices(new float[]{-120 + 2, 2}).notifySupplyRates(new float[]{1f, 1f}).checkPower(2366.6666f);
////        for (int i = 6; i < 16; i++) {
////            TimeUtil.startAt(i);
////            testEomOperator.makeBid(flexpowerplant).checkQuantities(new float[]{541.6666f, 50f}).checkSupplyPrices(new float[]{-120 + 2, 2}).notifySupplyRates(new float[]{1f, 1f}).checkPower(2366.6666f);
////        }
////        TimeUtil.startAt(16);
////        //PFC = -9
////        testBalancingMarketOperator.makeBid(flexpowerplant).checkPowerPos(33.3333f, 2641.6064f).checkPowerNeg(66.6666f, 0).notifyRatePos(1).notifyRateNeg(0);
//
//    }
//
////    @Test
////    public void testMarginalCosts() throws Exception {
////        final int variableCosts = 3;
////        final int fuelCosts = 6;
////        final int co2CertificateCosts = 7;
////        emissionRate = .6f;
////        efficiency = .9f;
////        assertEquals(14.333333f, FlexPowerplant2.calculateMarginalCosts(variableCosts, fuelCosts, co2CertificateCosts, emissionRate, efficiency), 0.00001f);
////    }
//
//
//}
