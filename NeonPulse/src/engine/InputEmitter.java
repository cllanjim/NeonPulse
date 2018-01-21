package engine;

import java.util.ArrayList;

public class InputEmitter implements InputHandler {
    private final ArrayList<InputHandler> listeners = new ArrayList<>();

    @Override
    public void onKeyPressed(int key, int key_code) {
        for (InputHandler inputHandler: listeners) {
            inputHandler.onKeyPressed(key, key_code);
        }
    }

    @Override
    public void onKeyReleased(int key, int key_code) {
        for (InputHandler inputHandler: listeners) {
            inputHandler.onKeyReleased(key, key_code);
        }
    }

    @Override
    public void onButtonPressed(int button) {
        for (InputHandler inputHandler: listeners) {
            inputHandler.onButtonPressed(button);
        }
    }

    @Override
    public void onButtonReleased(int button) {
        for (InputHandler inputHandler: listeners) {
            inputHandler.onButtonReleased(button);
        }
    }

    public void addListener(InputHandler inputHandler) {
        listeners.add(inputHandler);
    }
    public void removeListener(InputHandler inputHandler) {
        listeners.remove(inputHandler);
    }
}
