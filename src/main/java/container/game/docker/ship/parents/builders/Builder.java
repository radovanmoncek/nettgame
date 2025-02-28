package container.game.docker.ship.parents.builders;

public interface Builder<T> {

    T build();
    Builder<T> reset();
}
