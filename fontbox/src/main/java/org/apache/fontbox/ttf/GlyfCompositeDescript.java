/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.fontbox.ttf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
/**
 * Glyph description for composite glyphs. Composite glyphs are made up of one
 * or more simple glyphs, usually with some sort of transformation applied to
 * each.
 *
 * This class is based on code from Apache Batik a subproject of Apache
 * XMLGraphics. see http://xmlgraphics.apache.org/batik/ for further details.
 */
public class GlyfCompositeDescript extends GlyfDescript
{
    /**
     * Log instance.
     */
    private static final Logger LOG = LogManager.getLogger(GlyfCompositeDescript.class);

    private final List<GlyfCompositeComp> components = new ArrayList<>();
    private final Map<Integer,GlyphDescription> descriptions = new HashMap<>();
    private GlyphTable glyphTable = null;
    private boolean beingResolved = false;
    private boolean resolved = false;
    private int pointCount = -1;
    private int contourCount = -1;

    /**
     * Constructor.
     * 
     * @param bais the stream to be read
     * @param glyphTable the Glyphtable containing all glyphs
     * @param level current level
     * @throws IOException is thrown if something went wrong
     */
    GlyfCompositeDescript(TTFDataStream bais, GlyphTable glyphTable, int level) throws IOException
    {
        super((short) -1);

        this.glyphTable = glyphTable;

        // Get all of the composite components
        GlyfCompositeComp comp;
        do
        {
            comp = new GlyfCompositeComp(bais);
            components.add(comp);
        } 
        while ((comp.getFlags() & GlyfCompositeComp.MORE_COMPONENTS) != 0);

        // Are there hinting instructions to read?
        if ((comp.getFlags() & GlyfCompositeComp.WE_HAVE_INSTRUCTIONS) != 0)
        {
            readInstructions(bais, (bais.readUnsignedShort()));
        }
        initDescriptions(level);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resolve()
    {
        if (resolved)
        {
            return;
        }
        if (beingResolved)
        {
            LOG.error("Circular reference in GlyfCompositeDesc");
            return;
        }
        beingResolved = true;

        int firstIndex = 0;
        int firstContour = 0;

        for (GlyfCompositeComp comp : components)
        {
            comp.setFirstIndex(firstIndex);
            comp.setFirstContour(firstContour);

            GlyphDescription desc = descriptions.get(comp.getGlyphIndex());
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
    @Override
    public int getEndPtOfContours(int i)
    {
        GlyfCompositeComp c = getCompositeCompEndPt(i);
        if (c != null)
        {
            GlyphDescription gd = descriptions.get(c.getGlyphIndex());
            return gd.getEndPtOfContours(i - c.getFirstContour()) + c.getFirstIndex();
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte getFlags(int i)
    {
        GlyfCompositeComp c = getCompositeComp(i);
        if (c != null)
        {
            GlyphDescription gd = descriptions.get(c.getGlyphIndex());
            return gd.getFlags(i - c.getFirstIndex());
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short getXCoordinate(int i)
    {
        GlyfCompositeComp c = getCompositeComp(i);
        if (c != null)
        {
            GlyphDescription gd = descriptions.get(c.getGlyphIndex());
            int n = i - c.getFirstIndex();
            int x = gd.getXCoordinate(n);
            int y = gd.getYCoordinate(n);
            return (short) (c.scaleX(x, y) + c.getXTranslate());
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short getYCoordinate(int i)
    {
        GlyfCompositeComp c = getCompositeComp(i);
        if (c != null)
        {
            GlyphDescription gd = descriptions.get(c.getGlyphIndex());
            int n = i - c.getFirstIndex();
            int x = gd.getXCoordinate(n);
            int y = gd.getYCoordinate(n);
            return (short) (c.scaleY(x, y) + c.getYTranslate());
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isComposite()
    {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPointCount()
    {
        if (!resolved)
        {
            LOG.error("getPointCount called on unresolved GlyfCompositeDescript");
        }
        if (pointCount < 0)
        {
            GlyfCompositeComp c = components.get(components.size() - 1);
            GlyphDescription gd = descriptions.get(c.getGlyphIndex());
            if (gd == null)
            {
                LOG.error("GlyphDescription for index {} is null, returning 0", c.getGlyphIndex());
                pointCount = 0;
            }
            else
            {
                pointCount = c.getFirstIndex() + gd.getPointCount();
            }
        }   
        return pointCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getContourCount()
    {
        if (!resolved)
        {
            LOG.error("getContourCount called on unresolved GlyfCompositeDescript");
        }
        if (contourCount < 0)
        {
            GlyfCompositeComp c = components.get(components.size() - 1);
            GlyphDescription gd = descriptions.get(c.getGlyphIndex());
            if (gd == null)
            {
                LOG.error("missing glyph description for index {}", c.getGlyphIndex());
                contourCount = 0;
            }
            else
            {
                contourCount = c.getFirstContour() + gd.getContourCount();
            }
        }
        return contourCount;
    }

    /**
     * Get number of components.
     * 
     * @return the number of components
     */
    public int getComponentCount()
    {
        return components.size();
    }

    /**
     * Gets a view to the composite components.
     * 
     * @return unmodifiable list of this composite glyph's {@linkplain GlyfCompositeComp components}
     */
    public List<GlyfCompositeComp> getComponents()
    {
        return Collections.unmodifiableList(components);
    }

    private GlyfCompositeComp getCompositeComp(int i)
    {
        for (GlyfCompositeComp c : components)
        {
            GlyphDescription gd = descriptions.get(c.getGlyphIndex());
            if (c.getFirstIndex() <= i && gd != null && i < (c.getFirstIndex() + gd.getPointCount()))
            {
                return c;
            }
        }
        return null;
    }

    private GlyfCompositeComp getCompositeCompEndPt(int i)
    {
        for (GlyfCompositeComp c : components)
        {
            GlyphDescription gd = descriptions.get(c.getGlyphIndex());
            if (c.getFirstContour() <= i && gd != null && i < (c.getFirstContour() + gd.getContourCount()))
            {
                return c;
            }
        }
        return null;
    }

    private void initDescriptions(int level)
    {
        for (GlyfCompositeComp component : components)
        {
            try
            {
                int index = component.getGlyphIndex();
                GlyphData glyph = glyphTable.getGlyph(index, level);
                if (glyph != null)
                {
                    descriptions.put(index, glyph.getDescription());
                }
            }
            catch (IOException e)
            {
                LOG.error(e);
            }            
        }
    }
}
