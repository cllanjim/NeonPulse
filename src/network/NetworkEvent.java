package network;

public abstract class NetworkEvent {
    public byte[] data = {0, 0, 0, 0};

    public static final byte LEAVE = 0;
    public static final byte INPUT = 1;
    public static final byte JOIN = 2;
}
