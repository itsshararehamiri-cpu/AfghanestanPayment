package com.danesh.iso.packager;

import org.jpos.iso.IFB_LLLCHAR;

/**
 * Packager پاسخ 1314 پیکربندی ترمینال همراه‌پی (درخواست 1304 با Function Code 305).
 *
 * سوییچ کارن در این پاسخ DE43 را با طول دو بایتی BCD (LLL) می‌فرستد؛ در حالی که
 * {@link HpIso93BPackager} برای DE43 طول یک بایتی (LL) دارد و unpack با خطا متوقف می‌شود.
 * فقط همین فیلد override شده تا سایر تراکنش‌ها بدون تغییر بمانند.
 */
public class HpTerminalConfigResponsePackager extends HpIso93BPackager {
    public HpTerminalConfigResponsePackager() {
        super();
        fld[43] = new IFB_LLLCHAR(99, "Card acceptor name/location");
        setFieldPackager(fld);
    }
}
