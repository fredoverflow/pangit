import java.nio.charset.StandardCharsets;

public enum CharacterEncoding {
    UTF8 {
        @Override
        public String decode(byte[] bytes, int start) {
            // https://en.wikipedia.org/wiki/Byte_order_mark#UTF-8
            if (start + 2 < bytes.length &&
                    bytes[start] == (byte) 0xEF &&
                    bytes[start + 1] == (byte) 0xBB &&
                    bytes[start + 2] == (byte) 0xBF) {
                start += 3;
            }
            return new String(bytes, start, bytes.length - start, StandardCharsets.UTF_8);
        }
    }, LATIN1 {
        @Override
        public String decode(byte[] bytes, int start) {
            return new String(bytes, start, bytes.length - start, StandardCharsets.ISO_8859_1);
        }
    }, BINARY {
        @Override
        public String decode(byte[] bytes, int start) {
            return Hexdump.hexdump(bytes, start);
        }
    };

    public abstract String decode(byte[] bytes, int start);

    public static CharacterEncoding guess(byte[] bytes, int start) {
        // https://en.wikipedia.org/wiki/UTF-8#Encoding
        // https://en.wikipedia.org/wiki/ISO/IEC_8859-1#Code_page_layout
        return utf8_latin1_binary(bytes, start);
    }

    private static CharacterEncoding utf8_latin1_binary(byte[] bytes, int start) {
        for (int i = start; i < bytes.length; ++i) {
            switch (bytes[i] & 0xFF) {
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
                case 0x0B:
                case 0x0C:
                    // \r
                case 0x0E:
                case 0x0F:

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
                case 0x1A:
                case 0x1B:
                case 0x1C:
                case 0x1D:
                case 0x1E:
                case 0x1F:

                case 0x7F:

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
                case 0x8A:
                case 0x8B:
                case 0x8C:
                case 0x8D:
                case 0x8E:
                case 0x8F:

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
                case 0x9A:
                case 0x9B:
                case 0x9C:
                case 0x9D:
                case 0x9E:
                case 0x9F:
                    return BINARY;

                case 0xA0:
                case 0xA1:
                case 0xA2:
                case 0xA3:
                case 0xA4:
                case 0xA5:
                case 0xA6:
                case 0xA7:
                case 0xA8:
                case 0xA9:
                case 0xAA:
                case 0xAB:
                case 0xAC:
                case 0xAD:
                case 0xAE:
                case 0xAF:

                case 0xB0:
                case 0xB1:
                case 0xB2:
                case 0xB3:
                case 0xB4:
                case 0xB5:
                case 0xB6:
                case 0xB7:
                case 0xB8:
                case 0xB9:
                case 0xBA:
                case 0xBB:
                case 0xBC:
                case 0xBD:
                case 0xBE:
                case 0xBF:

                case 0xC0:
                case 0xC1:

                case 0xF5:
                case 0xF6:
                case 0xF7:
                case 0xF8:
                case 0xF9:
                case 0xFA:
                case 0xFB:
                case 0xFC:
                case 0xFD:
                case 0xFE:
                case 0xFF:
                    return latin1_binary(bytes, i + 1);

                case 0xC2:
                case 0xC3:
                case 0xC4:
                case 0xC5:
                case 0xC6:
                case 0xC7:
                case 0xC8:
                case 0xC9:
                case 0xCA:
                case 0xCB:
                case 0xCC:
                case 0xCD:
                case 0xCE:
                case 0xCF:

                case 0xD0:
                case 0xD1:
                case 0xD2:
                case 0xD3:
                case 0xD4:
                case 0xD5:
                case 0xD6:
                case 0xD7:
                case 0xD8:
                case 0xD9:
                case 0xDA:
                case 0xDB:
                case 0xDC:
                case 0xDD:
                case 0xDE:
                case 0xDF:
                    // pair
                    if (i + 1 >= bytes.length ||
                            (bytes[i + 1] & 0xC0) != 0x80)
                        return latin1_binary(bytes, i + 1);
                    i += 1;
                    break;

                case 0xE1:
                case 0xE2:
                case 0xE3:
                case 0xE4:
                case 0xE5:
                case 0xE6:
                case 0xE7:
                case 0xE8:
                case 0xE9:
                case 0xEA:
                case 0xEB:
                case 0xEC:
                case 0xEE:
                case 0xEF:
                    // triplet
                    if (i + 2 >= bytes.length ||
                            (bytes[i + 1] & 0xC0) != 0x80 ||
                            (bytes[i + 2] & 0xC0) != 0x80)
                        return latin1_binary(bytes, i + 1);
                    i += 2;
                    break;
                case 0xE0:
                    // triplet, possibly overlong encoding
                    if (i + 2 >= bytes.length ||
                            (bytes[i + 1] & 0xC0) != 0x80 ||
                            (bytes[i + 2] & 0xC0) != 0x80 ||
                            // 0800 = 0000 100000 000000
                            //           & 1
                            (bytes[i + 1] & 0x20) == 0) // overlong encoding
                        return latin1_binary(bytes, i + 1);
                    i += 2;
                    break;
                case 0xED:
                    // triplet, possibly surrogate half
                    if (i + 2 >= bytes.length ||
                            (bytes[i + 1] & 0xC0) != 0x80 ||
                            (bytes[i + 2] & 0xC0) != 0x80 ||
                            // D800 = 1101 100000 000000
                            //           & 1
                            (bytes[i + 1] & 0x20) != 0) // surrogate half
                        return latin1_binary(bytes, i + 1);
                    i += 2;
                    break;

                case 0xF1:
                case 0xF2:
                case 0xF3:
                    // quadruplet
                    if (i + 3 >= bytes.length ||
                            (bytes[i + 1] & 0xC0) != 0x80 ||
                            (bytes[i + 2] & 0xC0) != 0x80 ||
                            (bytes[i + 3] & 0xC0) != 0x80)
                        return latin1_binary(bytes, i + 1);
                    i += 3;
                    break;
                case 0xF0:
                    // quadruplet, possibly overlong encoding
                    if (i + 3 >= bytes.length ||
                            (bytes[i + 1] & 0xC0) != 0x80 ||
                            (bytes[i + 2] & 0xC0) != 0x80 ||
                            (bytes[i + 3] & 0xC0) != 0x80 ||
                            // 10000 = 000 010000 000000 000000
                            //           & 11
                            (bytes[i + 1] & 0x30) == 0) // overlong encoding
                        return latin1_binary(bytes, i + 1);
                    i += 3;
                    break;
                case 0xF4:
                    // quadruplet, possibly overflow
                    if (i + 3 >= bytes.length ||
                            (bytes[i + 1] & 0xC0) != 0x80 ||
                            (bytes[i + 2] & 0xC0) != 0x80 ||
                            (bytes[i + 3] & 0xC0) != 0x80 ||
                            // 10FFFF = 100 001111 111111 111111
                            //            & 11
                            (bytes[i + 1] & 0x30) != 0) // overflow
                        return latin1_binary(bytes, i + 1);
                    i += 3;
                    break;
            }
        }
        return UTF8;
    }

    private static CharacterEncoding latin1_binary(byte[] bytes, int start) {
        for (int i = start; i < bytes.length; ++i) {
            switch (bytes[i] & 0xFF) {
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
                case 0x0B:
                case 0x0C:
                    // \r
                case 0x0E:
                case 0x0F:

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
                case 0x1A:
                case 0x1B:
                case 0x1C:
                case 0x1D:
                case 0x1E:
                case 0x1F:

                case 0x7F:

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
                case 0x8A:
                case 0x8B:
                case 0x8C:
                case 0x8D:
                case 0x8E:
                case 0x8F:

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
                case 0x9A:
                case 0x9B:
                case 0x9C:
                case 0x9D:
                case 0x9E:
                case 0x9F:
                    return BINARY;
            }
        }
        return LATIN1;
    }
}
