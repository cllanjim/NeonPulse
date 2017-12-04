package game;

import effects.Action;
import effects.Effect;

public interface PlayerInput {
    void handleInput(Player player);
    void addBinding(String binding, Effect effect);
    void addBinding(String binding, Action effect);
}
