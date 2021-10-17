package org.apache.fontbox.ttf.advanced;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.fontbox.ttf.advanced.api.GlyphVector;

/**
 * TODO
 */
public class GlyphVectorAdvanced implements GlyphVector {
    private final int[] glyphs;
    private final float width;
    private final int[][] adjustments;

    /** TODO */
    public GlyphVectorAdvanced(int[] glyphs, float width, int[][] adjustments)
    {
        this.adjustments = adjustments;
        this.width = width;
        this.glyphs = glyphs;
    }

    /** {@inheritDoc} */
    @Override
    public float getWidth() 
    {
        return width;
    }

    /** TODO */
    public Set<Integer> getGlyphs()
    {
        Set<Integer> gset = new LinkedHashSet<Integer>(glyphs.length);
        for (int i = 0; i < glyphs.length; i++) {
            gset.add(glyphs[i]);
        }
        return gset;
    }

    /**
     * TODO
     */
    public int[] getGlyphArray() {
        return glyphs;
    }

    /** TODO */
    public int[][] getAdjustments()
    {
        return adjustments;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{ width: ")
          .append(width)
          .append(", gids: [")
          .append(glyphs)
          .append("], adjustments: [")
          .append(Arrays.deepToString(adjustments))
          .append("] }");
        return sb.toString();
    }

}
