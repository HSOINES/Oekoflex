package hsoines.oekoflex.priceforwardcurve;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * User: jh
 * Date: 30/03/16
 * Time: 18:45
 * H�lt die Daten der ermittelten prognostizierten Preise
 */
public interface PriceForwardCurve {
    /*
        Liest die Daten aus der PFC-Datei aus
     */
    void readData() throws IOException, ParseException;

    /*
        Addiert alle Preise
        @fromTick: start
        @ticks: Anzahl Ticks
     */
    float getPriceSummation(long fromTick, int ticks);

    /*
        Maximale Differenz
        @fromTick: start
        @ticks: Anzahl Ticks
     */
    float getSpread(long fromTick, int ticks);

    /*
        Maximum/Minimum
        @fromTick: start
        @ticks: Anzahl Ticks
     */
    float getMinimum(long fromTick, int ticks);
    float getMaximum(long fromTick, int ticks);

    /*
        Summe der negativen Preise
        @fromTick: start
        @ticks: Anzahl der Ticks
     */
    float getNegativePriceSummation(long fromTick, int ticks);

    /*
        Preis zum entsprechenden Tick
     */
    float getPriceOnTick(long tick);

    /*
        nTicks: Zahl der gew�nschten Ticks
        fromTick: Start-Tick
        intervalTicks: Anzahl der Ticks in denen gesucht wird
        @return: Die Liste mit den Ticks, die die Vorgaben erf�llen. Enth�lt maximal nTicks Ticks.
     */
    List<Long> getTicksWithLowestPrices(int nTicks, long fromTick, int intervalTicks);
    List<Long> getTicksWithHighestPrices(int nTicks, long fromTick, int intervalTicks);
}
