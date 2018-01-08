package game;

import effects.Effect;
import engine.InputState;
import processing.core.PVector;
import util.Pair;

import java.util.ArrayList;

import static processing.core.PConstants.LEFT;
import static processing.core.PConstants.RIGHT;

public class KeyboardInput implements PlayerInput {
    private final ArrayList<Pair<Character, Effect>> effectBindings;
    private final InputState inputState;

    public KeyboardInput(InputState keyboard_inputState) {
        this.inputState = keyboard_inputState;
        effectBindings = new ArrayList<>();
    }

    @Override
    public void handleInput(Player player) {
         player.impulse.set(0, 0);

        if (inputState.isKeyDown('W')) player.impulse.add(0, -1);
        if (inputState.isKeyDown('A')) player.impulse.add(-1, 0);
        if (inputState.isKeyDown('S')) player.impulse.add(0, 1);
        if (inputState.isKeyDown('D')) player.impulse.add(1, 0);
        if (player.impulse.mag() != 0)  player.impulse.normalize();

        player.target.set(inputState.getMousePosition());
        player.angle = PVector.sub(player.target, player.position).heading();

        for (Pair<Character, Effect> binding : effectBindings) {
            if (inputState.isKeyPressed(binding.first)) {
                binding.second.activate(player.position, player.target);
            }
        }

        // Laser
        if (inputState.isKeyDown('F')) player.laser.ready();
        if (inputState.isKeyReleased('F')) player.laser.activate();

        // Grenade
        if (inputState.isButtonDown(RIGHT)) player.grenade.ready();
        if (inputState.isButtonReleased(RIGHT)) player.grenade.activate();

        // Gun
        if (inputState.isKeyDown('C')) player.gun.activate();

        // Dash
        if (inputState.isButtonPressed(LEFT)) player.impulse.mult(10);
    }

    @Override
    public void addBinding(String binding_name, Effect effect) {
        effectBindings.add(new Pair<>(binding_name.charAt(0), effect));
    }
}
