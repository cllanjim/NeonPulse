package game;

import effects.Action;
import effects.Effect;
import processing.core.PGraphics;

import java.util.ArrayList;

public class Loadout {
    ArrayList<Effect> effects;
    ArrayList<Action> actions;

    Loadout() {
        effects = new ArrayList<>();
        actions = new ArrayList<>();
    }

    public void display(PGraphics g) {
        for (Effect effect : effects) {
            effect.display(g);
        }

        for  (Action action : actions) {
            action.display(g);
        }
    }

    public void update(float delta_time) {
        for (Action action : actions) {
            action.update(delta_time);
        }

        for (Effect effect : effects) {
            effect.update(delta_time);
        }
    }

    public void collideWithAgent(Player player) {
        for  (Effect effect: effects) {
            effect.collideWithAgent(player);
        }

        for  (Action action: actions) {
            action.collideWithAgent(player);
        }
    }
}
