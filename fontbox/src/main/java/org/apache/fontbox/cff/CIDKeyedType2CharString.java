package org.apache.fontbox.cff;

import org.apache.fontbox.type1.Type1CharStringReader;

import java.util.List;

/**
 * A CID-Keyed Type 2 CharString.
 *
 * @author John Hewson
 */
public class CIDKeyedType2CharString extends Type2CharString
{
    private final int cid;

    /**
     * Constructor.
     *
     * @param font Parent CFF font
     * @param fontName font name
     * @param cid CID
     * @param gid GID
     * @param sequence Type 2 char string sequence
     * @param defaultWidthX default width
     * @param nomWidthX nominal width
     */
    public CIDKeyedType2CharString(Type1CharStringReader font, String fontName, int cid, int gid, List<Object> sequence, int defaultWidthX, int nomWidthX)
    {
        // glyph name is for debugging only
        super(font, fontName, String.format("%04x", cid), gid, sequence, defaultWidthX, nomWidthX);
        this.cid = cid;
    }

    /**
     * Returns the CID (character id) of this charstring.
     */
    public int getCID()
    {
        return cid;
    }
}
