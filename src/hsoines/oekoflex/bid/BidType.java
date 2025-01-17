package hsoines.oekoflex.bid;

/**
 * This enum specifies all types of bids to market operators
 */
public enum BidType {
    ENERGY_DEMAND(false), 
    ENERGY_SUPPLY(true), ENERGY_SUPPLY_MUSTRUN(true), ENERGY_SUPPLY_MUSTRUN_COMPLEMENT(true), 
    POWER_POSITIVE(true), POWER_NEGATIVE(false), POWER_POSITIVE_LEISTUNGSPREIS(true),POWER_NEGATIVE_LEISTUNGSPREIS(false),POWER_POSITIVE_ARBEITSPREIS(true),POWER_NEGATIVE_ARBEITSPREIS(false),
    START_VALUE(true), NULL_BID(false);

    private final boolean positive;

    BidType(final boolean positive) {
        this.positive = positive;
    }
    public boolean isPositiveAmount() {
        return positive;
    }
}
