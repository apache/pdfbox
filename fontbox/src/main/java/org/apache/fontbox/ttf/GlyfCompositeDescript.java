/*

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.apache.fontbox.ttf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Glyph description for composite glyphs. Composite glyphs are made up of one or more simple glyphs, usually with some
 * sort of transformation applied to each.
 * 
 * This class is based on code from Apache Batik a subproject of Apache XMLGraphics. see
 * http://xmlgraphics.apache.org/batik/ for further details.
 */
public class GlyfCompositeDescript extends GlyfDescript
{

    private List<GlyfCompositeComp> components = new ArrayList<GlyfCompositeComp>();
    private GlyphData[] glyphs = null;
    private boolean beingResolved = false;
    private boolean resolved = false;

    /**
     * Constructor.
     * 
     * @param bais the stream to be read
     * @param glyphTable the Glyphtable containing all glyphs
     * @throws IOException is thrown if something went wrong
     */
    public GlyfCompositeDescript(TTFDataStream bais, GlyphTable glyphTable) throws IOException
    {
        super((short) -1, bais);

        glyphs = glyphTable.getGlyphs();

        // Get all of the composite components
        GlyfCompositeComp comp;
        do
        {
            comp = new GlyfCompositeComp(bais);
            components.add(comp);
        } while ((comp.getFlags() & GlyfCompositeComp.MORE_COMPONENTS) != 0);

        // Are there hinting instructions to read?
        if ((comp.getFlags() & GlyfCompositeComp.WE_HAVE_INSTRUCTIONS) != 0)
        {
            readInstructions(bais, (bais.readUnsignedShort()));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void resolve()
    {
        if (resolved)
        {
            return;
        }
        if (beingResolved)
        {
            System.err.println("Circular reference in GlyfCompositeDesc");
            return;
        }
        beingResolved = true;

        int firstIndex = 0;
        int firstContour = 0;

        Iterator<GlyfCompositeComp> i = components.iterator();
        while (i.hasNext())
        {
            GlyfCompositeComp comp = (GlyfCompositeComp) i.next();
            comp.setFirstIndex(firstIndex);
            comp.setFirstContour(firstContour);

            GlyphDescription desc;
            desc = getGlypDescription(comp.getGlyphIndex());
            if (desc != null)
            {
                desc.resolve();
                firstIndex += desc.getPointCount();
                firstContour += desc.getContourCount();
            }
        }
        resolved = true;
        beingResolved = false;
    }

    /**
     * {@inheritDoc}
     */
    public int getEndPtOfContours(int i)
    {
        GlyfCompositeComp c = getCompositeCompEndPt(i);
        if (c != null)
        {
            GlyphDescription gd = getGlypDescription(c.getGlyphIndex());
            return gd.getEndPtOfContours(i - c.getFirstContour()) + c.getFirstIndex();
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public byte getFlags(int i)
    {
        GlyfCompositeComp c = getCompositeComp(i);
        if (c != null)
        {
            GlyphDescription gd = getGlypDescription(c.getGlyphIndex());
            return gd.getFlags(i - c.getFirstIndex());
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public short getXCoordinate(int i)
    {
        GlyfCompositeComp c = getCompositeComp(i);
        if (c != null)
        {
            GlyphDescription gd = getGlypDescription(c.getGlyphIndex());
            int n = i - c.getFirstIndex();
            int x = gd.getXCoordinate(n);
            int y = gd.getYCoordinate(n);
            short x1 = (short) c.scaleX(x, y);
            x1 += c.getXTranslate();
            return x1;
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public short getYCoordinate(int i)
    {
        GlyfCompositeComp c = getCompositeComp(i);
        if (c != null)
        {
            GlyphDescription gd = getGlypDescription(c.getGlyphIndex());
            int n = i - c.getFirstIndex();
            int x = gd.getXCoordinate(n);
            int y = gd.getYCoordinate(n);
            short y1 = (short) c.scaleY(x, y);
            y1 += c.getYTranslate();
            return y1;
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isComposite()
    {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public int getPointCount()
    {
        if (!resolved)
        {
            System.err.println("getPointCount called on unresolved GlyfCompositeDescript");
        }
        GlyfCompositeComp c = (GlyfCompositeComp) components.get(components.size() - 1);
        return c.getFirstIndex() + getGlypDescription(c.getGlyphIndex()).getPointCount();
    }

    /**
     * {@inheritDoc}
     */
    public int getContourCount()
    {
        if (!resolved)
        {
            System.err.println("getContourCount called on unresolved GlyfCompositeDescript");
        }
        GlyfCompositeComp c = (GlyfCompositeComp) components.get(components.size() - 1);
        return c.getFirstContour() + getGlypDescription(c.getGlyphIndex()).getContourCount();
    }

    /**
     * {@inheritDoc}
     */
    public int getComponentCount()
    {
        return components.size();
    }

    private GlyfCompositeComp getCompositeComp(int i)
    {
        GlyfCompositeComp c;
        for (int n = 0; n < components.size(); n++)
        {
            c = (GlyfCompositeComp) components.get(n);
            GlyphDescription gd = getGlypDescription(c.getGlyphIndex());
            if (c.getFirstIndex() <= i && i < (c.getFirstIndex() + gd.getPointCount()))
            {
                return c;
            }
        }
        return null;
    }

    private GlyfCompositeComp getCompositeCompEndPt(int i)
    {
        GlyfCompositeComp c;
        for (int j = 0; j < components.size(); j++)
        {
            c = (GlyfCompositeComp) components.get(j);
            GlyphDescription gd = getGlypDescription(c.getGlyphIndex());
            if (c.getFirstContour() <= i && i < (c.getFirstContour() + gd.getContourCount()))
            {
                return c;
            }
        }
        return null;
    }

    private GlyphDescription getGlypDescription(int index)
    {
        if (glyphs != null && index < glyphs.length)
        {
            GlyphData glyph = glyphs[index];
            if (glyph != null)
            {
                return glyph.getDescription();
            }
        }
        return null;
    }
}
