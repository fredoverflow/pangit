import java.nio.charset.StandardCharsets;

public class Unicode {
    public static String decodeHeuristically(byte[] bytes, int start, int length) {
        String result = tryUtf8(bytes, start, length);
        if (result == null) {
            result = tryLatin1(bytes, start, length);
            if (result == null) {
                result = hexdump(bytes, start, length);
            }
        }
        return result;
    }

    private static String tryUtf8(byte[] bytes, int start, int length) {
        final int end = start + length;
        for (int i = start; i < end; ++i) {
            int b = bytes[i] & 255;
            switch (b >>> 3) {
                case 0:
                case 1:
                case 2:
                case 3:
                    if (((RETURN_NEWLINE_TAB >>> b) & 1) == 0) return null;
                    break;
                // https://en.wikipedia.org/wiki/UTF-8#Encoding
                case 0b11110:
                    // quadruplets
                    if (i + 1 == end || (bytes[++i] & 0b11000000) != 0b10000000) return null;
                    // intentional case fallthrough
                case 0b11100:
                case 0b11101:
                    // triplets
                    if (i + 1 == end || (bytes[++i] & 0b11000000) != 0b10000000) return null;
                    // intentional case fallthrough
                case 0b11000:
                case 0b11001:
                case 0b11010:
                case 0b11011:
                    // pairs
                    if (i + 1 == end || (bytes[++i] & 0b11000000) != 0b10000000) return null;
            }
        }
        return new String(bytes, start, length, StandardCharsets.UTF_8);
    }

    private static final int RETURN_NEWLINE_TAB = 1 << '\r' | 1 << '\n' | 1 << '\t';

    private static String tryLatin1(byte[] bytes, int start, int length) {
        final int end = start + length;
        for (int i = start; i < end; ++i) {
            if (!isValidCharacter(bytes[i] & 255, LATIN1)) {
                return null;
            }
        }
        return new String(bytes, start, length, StandardCharsets.ISO_8859_1);
    }

    private static boolean isValidCharacter(int b, int[] valid) {
        int outer = b >>> 5;
        int inner = b & 31;
        return ((valid[outer] >>> inner) & 1) != 0;
    }

    // https://en.wikipedia.org/wiki/ISO/IEC_8859-1#Code_page_layout
    private static final int[] LATIN1 = {RETURN_NEWLINE_TAB, -1, -1, 0x7fffffff, 0, -1, -1, -1};

    private static String hexdump(byte[] bytes, int start, int length) {
        final int end = start + length;
        int i = start;
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
                if (i + x < end) {
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

            for (int x = 0; x < 16 && i < end; ++x) {
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
