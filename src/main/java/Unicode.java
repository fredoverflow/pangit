import java.nio.charset.StandardCharsets;

public class Unicode {
    public static String decodeHeuristically(byte[] bytes, int start) {
        // https://en.wikipedia.org/wiki/UTF-8#Encoding
        // https://en.wikipedia.org/wiki/ISO/IEC_8859-1#Code_page_layout
        return utf8_latin1_binary(bytes, start);
        // TODO support UTF-16 https://en.wikipedia.org/wiki/Byte_order_mark
    }

    private static String utf8_latin1_binary(byte[] bytes, int start) {
        for (int i = start; i < bytes.length; ++i) {
            int b = bytes[i] & 255;
            switch (b >>> 3) {
                case 0b00001:
                    if (((RETURN_NEWLINE_TAB >>> b) & 1) != 0) continue;
                    // intentional case fallthrough
                case 0b00000:
                case 0b00010:
                case 0b00011:

                case 0b10000:
                case 0b10001:
                case 0b10010:
                case 0b10011:
                    return hexdump(bytes, start);

                case 0b10100:
                case 0b10101:
                case 0b10110:
                case 0b10111:

                case 0b11111:
                    return latin1_binary(bytes, start);

                case 0b11110:
                    // quadruplets
                    if (i + 1 >= bytes.length || (bytes[++i] & 0b11000000) != 0b10000000)
                        return latin1_binary(bytes, start);
                    // intentional case fallthrough
                case 0b11100:
                case 0b11101:
                    // triplets
                    if (i + 1 >= bytes.length || (bytes[++i] & 0b11000000) != 0b10000000)
                        return latin1_binary(bytes, start);
                    // intentional case fallthrough
                case 0b11000:
                case 0b11001:
                case 0b11010:
                case 0b11011:
                    // pairs
                    if (i + 1 >= bytes.length || (bytes[++i] & 0b11000000) != 0b10000000)
                        return latin1_binary(bytes, start);
            }
        }
        return new String(bytes, start, bytes.length - start, StandardCharsets.UTF_8);
    }

    private static final int RETURN_NEWLINE_TAB = 1 << '\r' | 1 << '\n' | 1 << '\t';

    private static String latin1_binary(byte[] bytes, int start) {
        for (int i = start; i < bytes.length; ++i) {
            if (!isValidCharacter(bytes[i] & 255, LATIN1)) {
                return hexdump(bytes, start);
            }
        }
        return new String(bytes, start, bytes.length - start, StandardCharsets.ISO_8859_1);
    }

    private static boolean isValidCharacter(int b, int[] valid) {
        int outer = b >>> 5;
        int inner = b & 31;
        return ((valid[outer] >>> inner) & 1) != 0;
    }

    private static final int[] LATIN1 = {RETURN_NEWLINE_TAB, -1, -1, 0x7fffffff, 0, -1, -1, -1};

    private static String hexdump(byte[] bytes, int start) {
        int i = start;
        final int length = bytes.length - start;
        final int lines = (length + 15) >>> 4;
        byte[] output = new byte[lines * 76];
        int o = 0;
        for (int address = 0; address < length; address += 16) {
            output[o++] = (byte) HEX_DIGITS.charAt(address >>> 28);
            output[o++] = (byte) HEX_DIGITS.charAt((address >>> 24) & 15);
            output[o++] = (byte) HEX_DIGITS.charAt((address >>> 20) & 15);
            output[o++] = (byte) HEX_DIGITS.charAt((address >>> 16) & 15);
            output[o++] = (byte) HEX_DIGITS.charAt((address >>> 12) & 15);
            output[o++] = (byte) HEX_DIGITS.charAt((address >>> 8) & 15);
            output[o++] = (byte) HEX_DIGITS.charAt((address >>> 4) & 15);
            output[o++] = (byte) HEX_DIGITS.charAt(address & 15);
            output[o++] = ' ';
            output[o++] = ' ';

            for (int x = 0; x < 16; ++x) {
                if (i + x < bytes.length) {
                    int b = bytes[i + x] & 255;
                    output[o++] = (byte) HEX_DIGITS.charAt(b >>> 4);
                    output[o++] = (byte) HEX_DIGITS.charAt(b & 15);
                } else {
                    output[o++] = ' ';
                    output[o++] = ' ';
                }
                output[o++] = ' ';
            }
            output[o++] = ' ';

            for (int x = 0; x < 16 && i < bytes.length; ++x) {
                int b = bytes[i++] & 255;
                output[o++] = (byte) (isValidCharacter(b, VISIBLE_LATIN1) ? b : '.');
            }
            output[o++] = '\n';
        }
        return new String(output, 0, o, StandardCharsets.ISO_8859_1);
    }

    private static final String HEX_DIGITS = "0123456789abcdef";
    private static final int[] VISIBLE_LATIN1 = {0, -1, -1, 0x7fffffff, 0, -1, -1, -1};
}
