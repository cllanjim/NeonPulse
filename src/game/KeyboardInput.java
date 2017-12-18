package game;

import effects.Effect;
import engine.Input;
import util.Pair;

import java.util.ArrayList;

import static processing.core.PConstants.LEFT;
import static processing.core.PConstants.RIGHT;

public class KeyboardInput implements PlayerInput {
    Input input;
    private ArrayList<Pair<Character, Effect>> effect_bindings;

    public KeyboardInput(Input keyboard_input) {
        this.input = keyboard_input;
        effect_bindings = new ArrayList<>();
    }

    @Override
    public void handleInput(Player player) {
        player.impulse.set(0, 0);

        if (input.isKeyDown('W')) {
            player.impulse.add(0, -1);
        }
        if (input.isKeyDown('A')) {
            player.impulse.add(-1, 0);
        }
        if (input.isKeyDown('S')) {
            player.impulse.add(0, 1);
        }
        if (input.isKeyDown('D')) {
            player.impulse.add(1, 0);
        }

        if (player.impulse.mag() != 0) {
            player.impulse.normalize();
        }

        player.target.set(input.getMousePosition());

        // Shield
        if (input.isKeyPressed('Q')) {
            player.shield.activate();
        }

        for (Pair<Character, Effect> binding : effect_bindings) {
            if (input.isKeyPressed(binding.first)) {
                binding.second.activate(player.position, player.target);
            }
        }

        if (input.isKeyDown('F')) {
            player.laser.ready();
        }
        if (input.isKeyReleased('F')){
            player.laser.activate();
        }

        // Grenade
        if (input.isButtonDown(RIGHT)) {
            player.grenade.ready();
        }
        if (input.isButtonReleased(RIGHT)) {
            player.grenade.activate();
        }

        if (input.isKeyDown('C')) {
            player.gun.activate();
        }

        // Dash
        if (input.isButtonPressed(LEFT)) {
            player.impulse.mult(10);
        }
    }

    @Override
    public void addBinding(String binding_name, Effect effect) {
        effect_bindings.add(new Pair<>(binding_name.charAt(0), effect));
    }
}
