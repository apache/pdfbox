package org.apache.fontbox.ttf;

import org.apache.fontbox.encoding.Encoding;

import java.awt.geom.GeneralPath;
import java.io.IOException;

/**
 * A Type 1-equivalent font, i.e. a font which can access glyphs by their PostScript name.
 * This is currently a minimal interface and could be expanded if needed.
 *
 * @author John Hewson
 */
public interface Type1Equivalent
{
    /**
     * The PostScript name of the font.
     */
    public String getFullName() throws IOException;

    /**
     * Returns the Type 1 CharString for the character with the given name.
     *
     * @return glyph path
     * @throws IOException if the path could not be read
     */
    public GeneralPath getPath(String name) throws IOException;

    /**
     * Returns the advance width for the character with the given name.
     *
     * @return glyph advance width
     * @throws IOException if the path could not be read
     */
    public float getWidth(String name) throws IOException;

    /**
     * Returns true if the font contains the given glyph.
     * @param name PostScript glyph name
     */
    public boolean hasGlyph(String name) throws IOException;

    /**
     * Returns the PostScript Encoding vector for the font.
     */
    public Encoding getEncoding();
}
