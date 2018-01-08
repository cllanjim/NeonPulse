package engine;

import processing.core.PConstants;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.Arrays;

import static processing.core.PConstants.*;

public class InputState implements InputHandler {
    private final PVector prevMousePosition;
    private final PVector currMousePosition;
    private boolean[] currKeyState;
    private boolean[] prevKeyState;
    private boolean[] currButtonState;
    private boolean[] prevButtonState;

    public InputState() {
        prevMousePosition = new PVector(0, 0);
        currMousePosition = new PVector(0, 0);
        currKeyState = new boolean[255];
        prevKeyState = new boolean[255];
        currButtonState = new boolean[2];
        prevButtonState = new boolean[2];
    }

    public static int getKeyIndex(int key_character) {
        int key_index = key_character;
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

    public void onButtonPressed(int mouseButton) {
        int mouse_index = getButtonIndex(mouseButton);
        if (mouse_index < 0) return;
        currButtonState[mouse_index] = true;
}

    public void onButtonReleased(int mouseButton) {
        int mouse_index = getButtonIndex(mouseButton);
        if (mouse_index < 0) return;
        currButtonState[mouse_index] = false;
    }

    public void onKeyPressed(int key, int keyCode) {
        if (key == CODED) {
            switch (keyCode) {
                case ALT:
                case SHIFT:
                case CONTROL:
                case UP:
                case DOWN:
                case LEFT:
                case RIGHT:
                    currKeyState[keyCode] = true;
            }
        } else {
            int key_index = getKeyIndex(key);
            if (key_index < 0) return;
            currKeyState[key_index] = true;
        }
    }

    public void onKeyReleased(int key, int keyCode) {
        if (key == CODED) {
            switch (keyCode) {
                case ALT:
                case SHIFT:
                case CONTROL:
                case UP:
                case DOWN:
                case LEFT:
                case RIGHT:
                    currKeyState[keyCode] = false;
            }
        } else {
            int key_index = getKeyIndex(key);
            if (key_index < 0) return;
            currKeyState[key_index] = false;
        }
    }

    public void setKeyState(int key_index, boolean state) {
        if (key_index < 0) return;
        currKeyState[key_index] = state;
    }

    public void setButtonState(int button_index, boolean state) {
        if (button_index < 0) return;
        currKeyState[button_index] = state;
    }

    public void saveInputState(float mouse_x, float mouse_y) {
        prevKeyState = Arrays.copyOf(currKeyState, currKeyState.length);
        prevButtonState = Arrays.copyOf(currButtonState, currKeyState.length);
        prevMousePosition.set(currMousePosition);
        currMousePosition.set(mouse_x, mouse_y);
    }

    public boolean isKeyDown(char letter) {
        int key_index = getKeyIndex(letter);
        return currKeyState[key_index];
    }

    public boolean isKeyPressed(char letter) {
        int key_index = getKeyIndex(letter);
        return !prevKeyState[key_index] && currKeyState[key_index];
    }

    public boolean isKeyReleased(char letter) {
        int key_index = getKeyIndex(letter);
        return prevKeyState[key_index] && !currKeyState[key_index];
    }

    public boolean isButtonDown(int mouse_button) {
        int button_index = getButtonIndex(mouse_button);
        return prevButtonState[button_index];
    }

    public boolean isButtonPressed(int mouse_button) {
        int button_index = getButtonIndex(mouse_button);
        return !prevButtonState[button_index] && currButtonState[button_index];
    }

    public boolean isButtonReleased(int mouse_button) {
        int button_index = getButtonIndex(mouse_button);
        return prevButtonState[button_index] && !currButtonState[button_index];
    }

    public PVector getMousePosition() {
        return currMousePosition;
    }
}

