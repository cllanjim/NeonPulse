package game;

import effects.Effect;
import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.List;

public class Loadout {
    public List<Effect> effects;

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

    public boolean collideWithAgent(Player player) {
        boolean colliding = false;
        for  (Effect effect: effects) {
            colliding |= effect.collideWithAgent(player);
        }
        return colliding;
    }
}
