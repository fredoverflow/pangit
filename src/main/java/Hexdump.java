import java.nio.charset.StandardCharsets;

public class Hexdump {
    private static final int BYTES_PER_LINE = "12345678  41 42 43 44 45 46 47 48 49 4a 4b 4c 4d 4e 4f 50  ABCDEFGHIJKLMNOP\n".length();

    public static String hexdump(byte[] bytes, int start) {
        int i = start;
        final int length = bytes.length - start;
        final int lines = (length + 0xF) >>> 4;
        byte[] output = new byte[lines * BYTES_PER_LINE];
        int o = 0;
        for (int address = 0; address < length; address += 0x10) {
            output[o++] = hexDigit(address, 28);
            output[o++] = hexDigit(address, 24);
            output[o++] = hexDigit(address, 20);
            output[o++] = hexDigit(address, 16);
            output[o++] = hexDigit(address, 12);
            output[o++] = hexDigit(address, 8);
            output[o++] = hexDigit(address, 4);
            output[o++] = hexDigit(address, 0);
            output[o++] = ' ';
            output[o++] = ' ';

            for (int x = 0; x < 0x10; ++x) {
                if (i + x < bytes.length) {
                    int b = bytes[i + x] & 0xFF;
                    output[o++] = hexDigit(b, 4);
                    output[o++] = hexDigit(b, 0);
                } else {
                    output[o++] = ' ';
                    output[o++] = ' ';
                }
                output[o++] = ' ';
            }
            output[o++] = ' ';

            for (int x = 0; x < 0x10 && i < bytes.length; ++x) {
                byte b = bytes[i++];
                output[o++] = (0x20 <= b && b < 0x7F ? b : 0x2E);
            }
            output[o++] = '\n';
        }
        return new String(output, 0, o, StandardCharsets.US_ASCII);
    }

    private static byte hexDigit(int x, int shift) {
        return (byte) "0123456789abcdef".charAt((x >>> shift) & 0xF);
    }
}
