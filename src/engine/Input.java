package engine;

import processing.core.PConstants;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.Arrays;

import static processing.core.PConstants.*;

public class Input {
    private final PVector prevMousePosition;
    private final PVector currMousePosition;
    private boolean[] currKeyState;
    private boolean[] prevKeyState;
    private boolean[] currButtonState;
    private boolean[] prevButtonState;
    private ArrayList<InputHandler> listeners;

    public Input() {
        prevMousePosition = new PVector(0, 0);
        currMousePosition = new PVector(0, 0);
        currKeyState = new boolean[255];
        prevKeyState = new boolean[255];
        currButtonState = new boolean[2];
        prevButtonState = new boolean[2];
        listeners = new ArrayList<>();
    }

    public void addListener(InputHandler inputHandler) {
        listeners.add(inputHandler);
    }

    public void removeListener(InputHandler inputHandler) {
        listeners.remove(inputHandler);
    }

    public static int getKeyIndex(int key_character) {
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
        currButtonState[mouse_index] = true;

        for (InputHandler inputHandler: listeners) {
            inputHandler.onButtonPressed(mouseButton);
        }
    }

    public void releaseButton(int mouseButton) {
        int mouse_index = getButtonIndex(mouseButton);
        if (mouse_index < 0) return;
        currButtonState[mouse_index] = false;
        for (InputHandler inputHandler: listeners) {
            inputHandler.onButtonReleased(mouseButton);
        }
    }

    public void pressKey(int key, int keyCode) {
        if (key == CODED) {
            switch (keyCode) {
                case ALT:
                case SHIFT:
                case CONTROL:
                case UP:
                case DOWN:
                case LEFT:
                case RIGHT:
            }
        } else {
            int key_index = getKeyIndex(key);
            if (key_index < 0) return;
            currKeyState[key_index] = true;
        }
        for (InputHandler inputHandler: listeners) {
            inputHandler.onKeyPressed(key, keyCode);
        }
    }

    public void releaseKey(int key, int keyCode) {
        if (key == CODED) {
            switch (keyCode) {
                case ALT:
                case SHIFT:
                case CONTROL:
                case UP:
                case DOWN:
                case LEFT:
                case RIGHT:
            }
        } else {
            int key_index = getKeyIndex(key);
            if (key_index < 0) return;
            currKeyState[key_index] = false;
        }
        for (InputHandler inputHandler: listeners) {
            inputHandler.onKeyReleased(key, keyCode);
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

