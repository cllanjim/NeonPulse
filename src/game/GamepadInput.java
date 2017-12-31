package game;

import effects.Effect;
import org.gamecontrolplus.ControlDevice;
import processing.core.PVector;
import util.Pair;

import java.util.ArrayList;

public class GamepadInput implements PlayerInput {
    private ControlDevice input;
    private PVector aim_vector;
    private boolean aiming;
    private boolean charging;
    private boolean dashing;
    private ArrayList<Pair<String, Effect>> effect_bindings;

    private static final float AIM_THRESHOLD = 0.4f;

    public GamepadInput(ControlDevice gamepad) {
        input = gamepad;
        aim_vector = new PVector(1,0);
        aiming = false;
        effect_bindings = new ArrayList<>();
    }

    public void handleInput(Player player) {
        float left_analog_x = input.getSlider("ANALOG_LEFT_X").getValue();
        float left_analog_y = input.getSlider("ANALOG_LEFT_Y").getValue();
        float right_analog_x = input.getSlider("ANALOG_RIGHT_X").getValue();
        float right_analog_y = input.getSlider("ANALOG_RIGHT_Y").getValue();

        player.impulse.set(left_analog_x, left_analog_y);
        aim_vector.set(right_analog_x, right_analog_y);

        // TODO: Keep angle instead of aim vector
        if (aim_vector.mag() > AIM_THRESHOLD) {
            player.target.set(player.position.x + right_analog_x * player.radius, player.position.y + right_analog_y * player.radius);
        }

        for (Pair<String, Effect> binding : effect_bindings) {
            if (input.getButton(binding.first).pressed()) {
                binding.second.activate(player.position, player.target);
            }
        }

        // Dash
        if (input.getButton("SQUARE").pressed() && !dashing) {
            player.impulse.mult(10);
            dashing = true;
        }
        if (dashing && !input.getButton("SQUARE").pressed()) {
            dashing = false;
        }

        // Grenade
        if (input.getButton("RIGHT_SHOULDER").pressed()) {
            player.grenade.ready();
            aiming = true;
        }
        if (aiming && !input.getButton("RIGHT_SHOULDER").pressed()) {
            player.grenade.activate();
            aiming = false;
        }

        // Laser
        if (input.getButton("LEFT_SHOULDER").pressed()) {
            player.laser.ready();
            charging = true;
        }
        if (charging && !input.getButton("LEFT_SHOULDER").pressed()){
            player.laser.activate();
            charging = false;
        }
    }

    @Override
    public void addBinding(String binding_name, Effect effect) {
        effect_bindings.add(new Pair<>(binding_name, effect));
    }
}
