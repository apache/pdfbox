package org.apache.pdfbox.pdmodel.font;

import java.awt.geom.GeneralPath;
import java.io.IOException;

/**
 * A Type 1-equivalent font in a PDF, i.e. a font which can access glyphs by their PostScript name.
 * May be a PFB, CFF, or TTF.
 *
 * @author John Hewson
 */
public interface PDType1Equivalent
{
    /**
     * Returns the name of this font.
     */
    public String getName();

    /**
     * Returns true if the font contains a glyph with the given name.
     */
    public boolean hasGlyph(String name) throws IOException;

    /**
     * Returns the glyph name for the given character code.
     *
     * @param code character code
     * @return PostScript glyph name
     */
    public String codeToName(int code);

    /**
     * Returns the glyph path for the given character code.
     * @param name PostScript glyph name
     * @throws java.io.IOException if the font could not be read
     */
    public GeneralPath getPath(String name) throws IOException;
}
