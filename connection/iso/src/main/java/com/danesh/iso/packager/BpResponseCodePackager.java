package com.danesh.iso.packager;

import org.jpos.iso.IF_CHAR;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;

import java.nio.charset.StandardCharsets;


public class BpResponseCodePackager extends IF_CHAR {
    public BpResponseCodePackager() {
        super(3, "Response code");
    }

    @Override
    public int unpack(ISOComponent component, byte[] bytes, int offset) throws ISOException {
        int length = resolveLength(bytes, offset);
        component.setValue(new String(bytes, offset, length, StandardCharsets.ISO_8859_1));
        return length;
    }

    private static int resolveLength(byte[] bytes, int offset) {
        if (bytes.length >= offset + 4 &&
            isAsciiDigit(bytes[offset]) &&
            isAsciiDigit(bytes[offset + 1]) &&
            bytes[offset + 2] == 0x00 &&
            bytes[offset + 3] == 0x00) {
            return 2;
        }
        int remaining = bytes.length - offset;
        return Math.min(3, remaining);
    }

    private static boolean isAsciiDigit(byte value) {
        return value >= 0x30 && value <= 0x39;
    }
}
