package net.evermemo.mport;

import java.nio.ByteBuffer;

public abstract class Multiplexing {

    public abstract void forward();
    
    protected ByteBuffer bb;
    
    public static Multiplexing guess(ByteBuffer bb) {
        byte[] buffer = new byte[bb.limit()];
        bb.get(buffer);
        if (isHTTP(buffer)) {
            Multiplexing m = new HTTPMultiplexing();
            m.bb = bb;
            return m;
        }
        return null;
    }
    
    private static boolean isHTTP(byte[] buffer) {
        String method = new String(buffer, 0, 7);
        for (String m : HTTPMultiplexing.HTTP_METHOD) {
            if (method.startsWith(m)) {
                return true;
            }
        }
        return false;
    }
}
