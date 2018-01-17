package extra.network;

public abstract class NetworkEvent {
    public byte[] data = {0, 0, 0, 0};

    public static final byte LEAVE = 0;
    public static final byte KEY = 1;
    public static final byte BUTTON = 2;
    public static final byte MOUSE = 3;
    public static final byte JOIN = 4;
}
