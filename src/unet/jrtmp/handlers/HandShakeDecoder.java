package unet.jrtmp.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;

public class HandShakeDecoder {

    public static final int HANDSHAKE_LENGTH = 1536, VERSION_LENGTH = 1;

    public static boolean startHandshake(InputStream in, OutputStream out)throws IOException {
        if(in.available() < HANDSHAKE_LENGTH+VERSION_LENGTH){
            return false;
        }

        in.read();

        //C0&1
        byte[] clientHandshake = new byte[HANDSHAKE_LENGTH];
        in.read(clientHandshake);

        //S0&1
        byte[] serverHandshake = new byte[HANDSHAKE_LENGTH];
        serverHandshake[0] = ((byte) (0xff & (0 >> 24)));
        serverHandshake[1] = ((byte) (0xff & (0 >> 16)));
        serverHandshake[2] = ((byte) (0xff & (0 >> 8)));
        serverHandshake[3] = ((byte) (0xff & 0));

        serverHandshake[4] = ((byte) (0xff & (0 >> 24)));
        serverHandshake[5] = ((byte) (0xff & (0 >> 16)));
        serverHandshake[6] = ((byte) (0xff & (0 >> 8)));
        serverHandshake[7] = ((byte) (0xff & 0));

        byte[] random = generateRandomData(HANDSHAKE_LENGTH-8);
        System.arraycopy(random, 0, serverHandshake, 8, random.length);

        out.write(0x03);
        out.write(serverHandshake);
        out.write(clientHandshake);

        byte[] s1 = new byte[HANDSHAKE_LENGTH];
        in.read(s1);

        if(!Arrays.equals(serverHandshake, s1)){
            return false;
        }

        return true;
    }

    public static byte[] generateRandomData(int size){
        Random random = new Random();

        byte[] bytes = new byte[size];
        random.nextBytes(bytes);

        return bytes;
    }
}
