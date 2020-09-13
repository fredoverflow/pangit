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
            switch (bytes[i] & 255) {
                case 0x00:
                case 0x01:
                case 0x02:
                case 0x03:
                case 0x04:
                case 0x05:
                case 0x06:
                case 0x07:
                case 0x08:
                    // \t
                    // \n
                case 0x0b:
                case 0x0c:
                    // \r
                case 0x0e:
                case 0x0f:

                case 0x10:
                case 0x11:
                case 0x12:
                case 0x13:
                case 0x14:
                case 0x15:
                case 0x16:
                case 0x17:
                case 0x18:
                case 0x19:
                case 0x1a:
                case 0x1b:
                case 0x1c:
                case 0x1d:
                case 0x1e:
                case 0x1f:

                case 0x7f:

                case 0x80:
                case 0x81:
                case 0x82:
                case 0x83:
                case 0x84:
                case 0x85:
                case 0x86:
                case 0x87:
                case 0x88:
                case 0x89:
                case 0x8a:
                case 0x8b:
                case 0x8c:
                case 0x8d:
                case 0x8e:
                case 0x8f:

                case 0x90:
                case 0x91:
                case 0x92:
                case 0x93:
                case 0x94:
                case 0x95:
                case 0x96:
                case 0x97:
                case 0x98:
                case 0x99:
                case 0x9a:
                case 0x9b:
                case 0x9c:
                case 0x9d:
                case 0x9e:
                case 0x9f:
                    return hexdump(bytes, start);

                case 0xa0:
                case 0xa1:
                case 0xa2:
                case 0xa3:
                case 0xa4:
                case 0xa5:
                case 0xa6:
                case 0xa7:
                case 0xa8:
                case 0xa9:
                case 0xaa:
                case 0xab:
                case 0xac:
                case 0xad:
                case 0xae:
                case 0xaf:

                case 0xb0:
                case 0xb1:
                case 0xb2:
                case 0xb3:
                case 0xb4:
                case 0xb5:
                case 0xb6:
                case 0xb7:
                case 0xb8:
                case 0xb9:
                case 0xba:
                case 0xbb:
                case 0xbc:
                case 0xbd:
                case 0xbe:
                case 0xbf:

                case 0xc0:
                case 0xc1:

                case 0xf5:
                case 0xf6:
                case 0xf7:
                case 0xf8:
                case 0xf9:
                case 0xfa:
                case 0xfb:
                case 0xfc:
                case 0xfd:
                case 0xfe:
                case 0xff:
                    return latin1_binary(bytes, start);

                case 0xf0:
                case 0xf1:
                case 0xf2:
                case 0xf3:
                case 0xf4:
                    // quadruplets
                    if (i + 1 >= bytes.length || (bytes[++i] & 0xc0) != 0x80)
                        return latin1_binary(bytes, start);
                    // intentional case fallthrough
                case 0xe0:
                case 0xe1:
                case 0xe2:
                case 0xe3:
                case 0xe4:
                case 0xe5:
                case 0xe6:
                case 0xe7:
                case 0xe8:
                case 0xe9:
                case 0xea:
                case 0xeb:
                case 0xec:
                case 0xed:
                case 0xee:
                case 0xef:
                    // triplets
                    if (i + 1 >= bytes.length || (bytes[++i] & 0xc0) != 0x80)
                        return latin1_binary(bytes, start);
                    // intentional case fallthrough
                case 0xc2:
                case 0xc3:
                case 0xc4:
                case 0xc5:
                case 0xc6:
                case 0xc7:
                case 0xc8:
                case 0xc9:
                case 0xca:
                case 0xcb:
                case 0xcc:
                case 0xcd:
                case 0xce:
                case 0xcf:

                case 0xd0:
                case 0xd1:
                case 0xd2:
                case 0xd3:
                case 0xd4:
                case 0xd5:
                case 0xd6:
                case 0xd7:
                case 0xd8:
                case 0xd9:
                case 0xda:
                case 0xdb:
                case 0xdc:
                case 0xdd:
                case 0xde:
                case 0xdf:
                    // pairs
                    if (i + 1 >= bytes.length || (bytes[++i] & 0xc0) != 0x80)
                        return latin1_binary(bytes, start);
            }
        }
        return new String(bytes, start, bytes.length - start, StandardCharsets.UTF_8);
    }

    private static String latin1_binary(byte[] bytes, int start) {
        for (int i = start; i < bytes.length; ++i) {
            switch (bytes[i] & 255) {
                case 0x00:
                case 0x01:
                case 0x02:
                case 0x03:
                case 0x04:
                case 0x05:
                case 0x06:
                case 0x07:
                case 0x08:
                    // \t
                    // \n
                case 0x0b:
                case 0x0c:
                    // \r
                case 0x0e:
                case 0x0f:

                case 0x10:
                case 0x11:
                case 0x12:
                case 0x13:
                case 0x14:
                case 0x15:
                case 0x16:
                case 0x17:
                case 0x18:
                case 0x19:
                case 0x1a:
                case 0x1b:
                case 0x1c:
                case 0x1d:
                case 0x1e:
                case 0x1f:

                case 0x7f:

                case 0x80:
                case 0x81:
                case 0x82:
                case 0x83:
                case 0x84:
                case 0x85:
                case 0x86:
                case 0x87:
                case 0x88:
                case 0x89:
                case 0x8a:
                case 0x8b:
                case 0x8c:
                case 0x8d:
                case 0x8e:
                case 0x8f:

                case 0x90:
                case 0x91:
                case 0x92:
                case 0x93:
                case 0x94:
                case 0x95:
                case 0x96:
                case 0x97:
                case 0x98:
                case 0x99:
                case 0x9a:
                case 0x9b:
                case 0x9c:
                case 0x9d:
                case 0x9e:
                case 0x9f:
                    return hexdump(bytes, start);
            }
        }
        return new String(bytes, start, bytes.length - start, StandardCharsets.ISO_8859_1);
    }

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
                byte b = bytes[i++];
                output[o++] = (32 <= b && b < 127 ? b : 46);
            }
            output[o++] = '\n';
        }
        return new String(output, 0, o, StandardCharsets.US_ASCII);
    }

    private static final String HEX_DIGITS = "0123456789abcdef";
}
