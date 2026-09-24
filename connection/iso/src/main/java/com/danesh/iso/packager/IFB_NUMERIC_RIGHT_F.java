package com.danesh.iso.packager;

import org.jpos.iso.BCDInterpreter;
import org.jpos.iso.ISOStringFieldPackager;
import org.jpos.iso.LeftPadder;
import org.jpos.iso.NullPrefixer;

public class IFB_NUMERIC_RIGHT_F extends ISOStringFieldPackager {

    public IFB_NUMERIC_RIGHT_F(int len, String description) {
        super(
                len,
                description,
                LeftPadder.ZERO_PADDER,
                BCDInterpreter.RIGHT_PADDED_F,
                NullPrefixer.INSTANCE
        );
    }
}