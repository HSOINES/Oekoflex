package hsoines.oekoflex.summary;

import hsoines.oekoflex.OekoflexAgent;
import hsoines.oekoflex.domain.SequenceDefinition;
import hsoines.oekoflex.energytrader.EOMTrader;
import hsoines.oekoflex.energytrader.MarketTrader;
import hsoines.oekoflex.energytrader.MarketTraderVisitor;
import hsoines.oekoflex.energytrader.RegelenergieMarketTrader;
import hsoines.oekoflex.energytrader.impl.EnergyTradeHistoryImpl;
import hsoines.oekoflex.util.TimeUtilities;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.util.collections.IndexedIterable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: jh
 * Date: 15/01/16
 * Time: 23:18
 */
public final class EnergyTraderTypeLogger implements MarketTraderVisitor, OekoflexAgent {
    private static final Log log = LogFactory.getLog(EnergyTraderTypeLogger.class);
    private final Context<OekoflexAgent> context;

    private final Map<Class, LoggerFile> loggerFiles = new HashMap<>();

    public EnergyTraderTypeLogger(final Context<OekoflexAgent> context) {
        this.context = context;
    }

    public void add(final MarketTrader marketTrader) {
        if (!loggerFiles.keySet().contains(loggerFiles.getClass())) {
            LoggerFile loggerFile = null;
            try {
                String header = buildHeaderString(marketTrader);
                loggerFile = new LoggerFile(marketTrader.getClass().getSimpleName());
                loggerFiles.put(marketTrader.getClass(), loggerFile);
                loggerFile.log(header);
            } catch (IOException e) {
                log.error(e.toString(), e);
            }
        }
    }

    String buildHeaderString(final MarketTrader marketTrader) {
        final StringBuilder header = new StringBuilder();
        header.append("tick;ClassName;InstanceName;Capacity;QuantityAssigned;Price;");

        marketTrader.accept(new MarketTraderVisitor() {
            @Override
            public void visit(final EOMTrader eomTrader) {
                header.append("ClearedPrice;");
            }

            @Override
            public void visit(final RegelenergieMarketTrader regelenergieMarketTrader) {
                header.append("AssignedQuantity;");
            }
        });
        return header.toString();
    }

    @Override
    public void visit(final EOMTrader eomTrader) {
        LoggerFile loggerFile = loggerFiles.get(eomTrader.getClass());
        logCommonData(eomTrader, loggerFile);
    }

    @Override
    public void visit(final RegelenergieMarketTrader regelenergieMarketTrader) {
        LoggerFile loggerFile = loggerFiles.get(regelenergieMarketTrader.getClass());
        logCommonData(regelenergieMarketTrader, loggerFile);
    }

    void logCommonData(final MarketTrader marketTrader, final LoggerFile loggerFile) {
        List<EnergyTradeHistoryImpl.EnergyTradeHistoryElement> currentAssignments = marketTrader.getCurrentAssignments();
        for (EnergyTradeHistoryImpl.EnergyTradeHistoryElement currentAssignment : currentAssignments) {
            float price = currentAssignment.getPrice();
            int quantity = currentAssignment.getQuantity();
            int capacity = currentAssignment.getCapacity();
            loggerFile.log(TimeUtilities.getTick(TimeUtilities.getCurrentDate()) + ";"
                    + marketTrader.getClass().getName() + ";" + marketTrader.getName() + ";"
                    + capacity + ";" + quantity + ";" + price + ";");
        }
    }

    @ScheduledMethod(start = 1, interval = 1, priority = SequenceDefinition.ReportingPriority)
    public void execute() {
        IndexedIterable<OekoflexAgent> oekoflexAgents = context.getObjects(MarketTrader.class);
        for (OekoflexAgent oekoflexAgent : oekoflexAgents) {
            MarketTrader marketTrader = (MarketTrader) oekoflexAgent;
            marketTrader.accept(this);
        }
    }

    @Override
    public String getName() {
        return "EnergyTraderTypeLogger";
    }
}
