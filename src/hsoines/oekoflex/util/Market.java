package hsoines.oekoflex.util;

/**
 * User: jh
 * Date: 07/01/16
 * Time: 13:52
 */
public enum Market {
    SPOT_MARKET(1),       //EOM
    BALANCING_MARKET(16); //Regelenergie

    private final int ticks;

    Market(final int ticks) {
        this.ticks = ticks;
    }

    public int getTicks() {
        return ticks;
    }
}
