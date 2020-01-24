package org.apache.pdfbox.util;

import org.apache.pdfbox.pdmodel.font.encoding.GlyphList;
import org.apache.pdfbox.pdmodel.font.encoding.WinAnsiEncoding;

public class AnsiEncodingUtil {
    public static boolean isWinAnsiEncoding(int unicode) {
        String name = GlyphList.getAdobeGlyphList().codePointToName(unicode);
        if (".notdef".equals(name)) {
            return false;
        }
        return WinAnsiEncoding.INSTANCE.contains(name);
    }
}
