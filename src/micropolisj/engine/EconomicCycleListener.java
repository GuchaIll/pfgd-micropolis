package micropolisj.engine;

/**
 * The listener interface for receiving economic cycle notifications.
 */
public interface EconomicCycleListener {
    /**
     * Called when the economic cycle changes.
     *
     * @param newCycle The new economic cycle.
     */
    void economicCycleChanged(Micropolis.EconomicCycle newCycle);
}