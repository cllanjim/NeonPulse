package engine;

public interface InputHandler {
    void onKeyPressed(int key, int key_code);
    void onKeyReleased(int key, int key_code);
    void onButtonPressed(int button);
    void onButtonReleased(int button);
}
