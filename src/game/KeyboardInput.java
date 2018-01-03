package game;

import effects.Effect;
import engine.Input;
import processing.core.PVector;
import util.Pair;

import java.util.ArrayList;

import static processing.core.PConstants.LEFT;
import static processing.core.PConstants.RIGHT;

public class KeyboardInput implements PlayerInput {
    private final ArrayList<Pair<Character, Effect>> effectBindings;
    private final Input input;

    public KeyboardInput(Input keyboard_input) {
        this.input = keyboard_input;
        effectBindings = new ArrayList<>();
    }

    @Override
    public void handleInput(Player player) {
         player.impulse.set(0, 0);

        if (input.isKeyDown('W')) player.impulse.add(0, -1);
        if (input.isKeyDown('A')) player.impulse.add(-1, 0);
        if (input.isKeyDown('S')) player.impulse.add(0, 1);
        if (input.isKeyDown('D')) player.impulse.add(1, 0);
        if (player.impulse.mag() != 0)  player.impulse.normalize();

        player.target.set(input.getMousePosition());
        player.angle = PVector.sub(player.target, player.position).heading();

        for (Pair<Character, Effect> binding : effectBindings) {
            if (input.isKeyPressed(binding.first)) {
                binding.second.activate(player.position, player.target);
            }
        }

        // Laser
        if (input.isKeyDown('F')) player.laser.ready();
        if (input.isKeyReleased('F')) player.laser.activate();

        // Grenade
        if (input.isButtonDown(RIGHT)) player.grenade.ready();
        if (input.isButtonReleased(RIGHT)) player.grenade.activate();

        // Gun
        if (input.isKeyDown('C')) player.gun.activate();

        // Dash
        if (input.isButtonPressed(LEFT)) player.impulse.mult(10);
    }

    @Override
    public void addBinding(String binding_name, Effect effect) {
        effectBindings.add(new Pair<>(binding_name.charAt(0), effect));
    }
}
