package engine;

import processing.core.PConstants;
import processing.core.PVector;

import java.util.Arrays;

public class Input {
    private PVector prev_mouse_position;
    private PVector curr_mouse_position;
    private boolean[] curr_key_state;
    private boolean[] prev_key_state;
    private boolean[] curr_button_state;
    private boolean[] prev_button_state;

    public Input() {
        prev_mouse_position = new PVector(0, 0);
        curr_mouse_position = new PVector(0, 0);
        curr_key_state = new boolean[255];
        prev_key_state = new boolean[255];
        curr_button_state = new boolean[2];
        prev_button_state = new boolean[2];
    }

    public static int getKeyIndex(char key_character) {
        int key_index = -1;
        if (key_character >= 'A' && key_character <= 'Z') {
            key_index = key_character - 'A';
        } else if (key_character >= 'a' && key_character <= 'z') {
            key_index = key_character - 'a';
        }
        return key_index;
    }

    public static int getButtonIndex(int mouse_button) {
        int mouse_index = -1;
        if (mouse_button == PConstants.LEFT) {
            mouse_index = 0;
        } else if (mouse_button == PConstants.RIGHT) {
            mouse_index = 1;
        }
        return mouse_index;
    }

    public void pressButton(int mouseButton) {
        int mouse_index = getButtonIndex(mouseButton);
        if (mouse_index < 0) return;
        curr_button_state[mouse_index] = true;
    }

    public void releaseButton(int mouseButton) {
        int mouse_index = getButtonIndex(mouseButton);
        if (mouse_index < 0) return;
        curr_button_state[mouse_index] = false;
    }

    public void setKeyState(int key_index, boolean state) {
        if (key_index < 0) return;
        curr_key_state[key_index] = state;
    }

    public void pressKey(char key) {
        int key_index = getKeyIndex(key);
        if (key_index < 0) return;
        curr_key_state[key_index] = true;
    }

    public void releaseKey(char key) {
        int key_index = getKeyIndex(key);
        if (key_index < 0) return;
        curr_key_state[key_index] = false;
    }

    public void saveInputState(float mouse_x, float mouse_y) {
        prev_key_state = Arrays.copyOf(curr_key_state, curr_key_state.length);
        prev_button_state = Arrays.copyOf(curr_button_state, curr_key_state.length);
        prev_mouse_position.set(curr_mouse_position);
        curr_mouse_position.set(mouse_x, mouse_y);
    }

    public boolean isKeyDown(char letter) {
        int key_index = getKeyIndex(letter);
        return curr_key_state[key_index];
    }

    public boolean isKeyPressed(char letter) {
        int key_index = getKeyIndex(letter);
        return !prev_key_state[key_index] && curr_key_state[key_index];
    }

    public boolean isKeyReleased(char letter) {
        int key_index = getKeyIndex(letter);
        return prev_key_state[key_index] && !curr_key_state[key_index];
    }

    public boolean isButtonDown(int mouse_button) {
        int button_index = getButtonIndex(mouse_button);
        return prev_button_state[button_index];
    }

    public boolean isButtonPressed(int mouse_button) {
        int button_index = getButtonIndex(mouse_button);
        return !prev_button_state[button_index] && curr_button_state[button_index];
    }

    public boolean isButtonReleased(int mouse_button) {
        int button_index = getButtonIndex(mouse_button);
        return prev_button_state[button_index] && !curr_button_state[button_index];
    }

    public PVector getMousePosition() {
        return curr_mouse_position;
    }
}

