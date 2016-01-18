package hsoines.oekoflex.builder;

import hsoines.oekoflex.OekoflexAgent;
import hsoines.oekoflex.energytrader.impl.test.ParametrizableEnergyProducer;
import hsoines.oekoflex.marketoperator.RegelEnergieMarketOperator;
import hsoines.oekoflex.marketoperator.impl.EOMOperatorImpl;
import hsoines.oekoflex.marketoperator.impl.RegelEnergieMarketOperatorImpl;
import hsoines.oekoflex.summary.EnergyTraderTypeLogger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import repast.simphony.context.Context;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;

import java.io.File;
import java.io.IOException;

public class OekoFlexContextBuilder implements ContextBuilder<OekoflexAgent> {
    private static final Log log = LogFactory.getLog(OekoFlexContextBuilder.class);

    @Override
    public Context build(Context<OekoflexAgent> context) {

        context.setId("OekoFlex");

        Parameters p = RunEnvironment.getInstance().getParameters();
        RunEnvironment re = RunEnvironment.getInstance();
        int daysToRun = (int) p.getValue("daysToRun");
        re.endAt(daysToRun * 96);

        String configDirString = (String) p.getValue("runConfigDir");
        File configDir = new File(configDirString);
        if (!configDir.exists()) {
            log.error("Configuration directory is not existing: " + configDirString);
            re.endRun();
        }

        EOMOperatorImpl eomOperator = new EOMOperatorImpl("EOM_Operator");
        RegelEnergieMarketOperator regelenergieMarketOperator = new RegelEnergieMarketOperatorImpl("RegelEnergieMarketOperator");
        context.add(eomOperator);
        context.add(regelenergieMarketOperator);

        try {
            CombinedEnergyProducerFactory.build(configDir, context, eomOperator, regelenergieMarketOperator);
            DaytimeEnergyConsumerFactory.build(configDir, context, eomOperator);
            FixedDemandConsumerFactory.build(configDir, context, eomOperator);
        } catch (IOException e) {
            log.error(e.toString(), e);
            re.endRun();
        }

        for (int i = 0; i < 3; i++) {
            ParametrizableEnergyProducer producer = new ParametrizableEnergyProducer("ParametrizableEnergyProducer_" + i);
            producer.setEOMOperator(eomOperator);
            context.add(producer);
        }

        EnergyTraderTypeLogger energyTraderTypeLogger = new EnergyTraderTypeLogger(context);
        context.add(energyTraderTypeLogger);

        return context;
    }

}
