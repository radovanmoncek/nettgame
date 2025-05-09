package cz.radovanmoncek.nettgame.nettgame.ship.deck.builders;

/**
 * Each realization should include methods prefixed with 'build', and an accumulative attribute called 'result'.
 * @param <T> the buildee.
 * @since 1.0
 * @author Radovan Monček
 */
public interface Builder<T> {

    /**
     * Build the configured result.
     * @return this result.
     */
    T build();

    /**
     * Please note that this method returns this {@code Builder<T>} in its generic ancestor form.
     * To use your specialization methods, you must do a polymorphic cast:
     * {@code (YourExampleBuilder) Builder<T>.reset()}.
     * @return this builder with a newly set result.
     */
    Builder<T> reset();
}
