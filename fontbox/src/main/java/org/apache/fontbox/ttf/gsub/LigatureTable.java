package org.apache.fontbox.ttf.gsub;

class LigatureTable
{
    int ligatureGlyph;
    int componentCount;
    int[] componentGlyphIDs;

    @Override
    public String toString()
    {
        return String.format("%s[ligatureGlyph=%d, componentCount=%d]",
                LigatureTable.class.getSimpleName(), ligatureGlyph, componentCount);
    }
}