package game;

import effects.Effect;
import processing.core.PGraphics;

import java.util.ArrayList;

public class Loadout {
    ArrayList<Effect> effects;

    Loadout() {
        effects = new ArrayList<>();
    }

    public void display(PGraphics g) {
        for (Effect effect : effects) {
            effect.display(g);
        }
    }

    public void update(float delta_time) {
        for (Effect effect : effects) {
            effect.update(delta_time);
        }
    }

    public void collideWithAgent(Player player) {
        for  (Effect effect: effects) {
            effect.collideWithAgent(player);
        }
    }
}
