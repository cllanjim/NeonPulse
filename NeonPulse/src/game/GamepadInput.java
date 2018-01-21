package game;

import org.gamecontrolplus.ControlDevice;
import processing.core.PVector;

public class GamepadInput implements PlayerInput {
    private final ControlDevice input;
    private final PVector aimVector;
    private boolean aiming;
    private boolean charging;
    private boolean dashing;

    private static final float AIM_THRESHOLD = 0.4f;

    public GamepadInput(ControlDevice gamepad) {
        input = gamepad;
        aimVector = new PVector(1,0);
        aiming = false;
    }

    public void handleInput(Player player) {
        float left_analog_x = input.getSlider("ANALOG_LEFT_X").getValue();
        float left_analog_y = input.getSlider("ANALOG_LEFT_Y").getValue();
        float right_analog_x = input.getSlider("ANALOG_RIGHT_X").getValue();
        float right_analog_y = input.getSlider("ANALOG_RIGHT_Y").getValue();

        player.impulse.set(left_analog_x, left_analog_y);
        aimVector.set(right_analog_x, right_analog_y);

        // TODO: Keep angle instead of aim vector
        if (aimVector.mag() > AIM_THRESHOLD) {
            player.target.set(player.position.x + right_analog_x * player.radius, player.position.y + right_analog_y * player.radius);
            player.angle = PVector.sub(player.target, player.position).heading();
        } else {
            player.target.set(PVector.add(player.position, PVector.fromAngle(player.angle).setMag(player.radius)));
        }

        // Pulse
        if (input.getButton("X").pressed()) {
            player.pulse.activate(player.position, player.target);
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
}
