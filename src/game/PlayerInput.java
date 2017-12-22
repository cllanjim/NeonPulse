package game;

import effects.Effect;

public interface PlayerInput {
    void handleInput(Player player);
    void addBinding(String binding, Effect effect);
}
