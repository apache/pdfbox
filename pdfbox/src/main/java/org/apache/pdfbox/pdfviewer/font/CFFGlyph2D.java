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
package org.apache.pdfbox.pdfviewer.font;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.cff.CFFFont;
import org.apache.fontbox.cff.CharStringRenderer;
import org.apache.pdfbox.encoding.Encoding;

/**
 * This class provides a glyph to GeneralPath conversion for CFF fonts.
 * 
 */
public class CFFGlyph2D implements Glyph2D
{

    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(CFFGlyph2D.class);

    private float scale = 0.001f;
    private HashMap<Integer, GeneralPath> glyphs = new HashMap<Integer, GeneralPath>();
    private HashMap<Integer, Integer> codeToGlyph = new HashMap<Integer, Integer>();
    private String fontname = null;

    /**
     * Constructor.
     * 
     */
    public CFFGlyph2D(CFFFont cffFont, Encoding encoding)
    {
        fontname = cffFont.getName();
        Map<String, Integer> nameToCode = encoding != null ? encoding.getNameToCodeMap() : null;
        Collection<CFFFont.Mapping> mappings = cffFont.getMappings();
        Map<Integer, String> codeToNameMap = new LinkedHashMap<Integer, String>();
        for (CFFFont.Mapping mapping : mappings)
        {
            codeToNameMap.put(mapping.getCode(), mapping.getName());
        }

        CharStringRenderer renderer = cffFont.createRenderer();
        int glyphId = 0;
        for (CFFFont.Mapping mapping : mappings)
        {
            GeneralPath glyph = null;
            try
            {
                glyph = renderer.render(mapping.toType1Sequence());
            }
            catch (IOException exception)
            {
                LOG.error("CFF glyph rendering fails!", exception);
            }
            if (glyph != null)
            {
                AffineTransform atPath = AffineTransform.getScaleInstance(scale, scale);
                glyph.transform(atPath);
                glyphs.put(glyphId, transformGlyph(glyph));
                int code = mapping.getSID();
                String name = mapping.getName();
                if (nameToCode != null && nameToCode.containsKey(name))
                {
                    code = nameToCode.get(name);
                }
                codeToGlyph.put(code, glyphId);
                glyphId++;
            }
        }
    }

    private GeneralPath transformGlyph(GeneralPath glyph)
    {
        // we have to invert all y-coordinates due to the moved 0,0-reference
        PathIterator iter = glyph.getPathIterator(null);
        float[] currentSegment = new float[6];
        Path2D.Float path = new Path2D.Float(iter.getWindingRule());
        boolean glyphTransformed = false;
        while (!iter.isDone())
        {
            glyphTransformed = true;
            int type = iter.currentSegment(currentSegment);
            switch (type)
            {
            case PathIterator.SEG_MOVETO:
                path.moveTo(currentSegment[0], -currentSegment[1]);
                break;
            case PathIterator.SEG_LINETO:
                path.lineTo(currentSegment[0], -currentSegment[1]);
                break;
            case PathIterator.SEG_QUADTO:
                path.quadTo(currentSegment[0], -currentSegment[1], currentSegment[2], -currentSegment[3]);
                break;
            case PathIterator.SEG_CUBICTO:
                path.curveTo(currentSegment[0], -currentSegment[1], currentSegment[2], -currentSegment[3],
                        currentSegment[4], -currentSegment[5]);
                break;
            case PathIterator.SEG_CLOSE:
                path.closePath();
                break;
            }
            iter.next();
        }
        if (glyphTransformed)
        {
            return new GeneralPath(path);
        }
        else
        {
            return glyph;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GeneralPath getPathForGlyphId(int glyphId)
    {
        if (glyphs.containsKey(glyphId))
        {
            return glyphs.get(glyphId);
        }
        else
        {
            LOG.debug(fontname + ": glyph " + glyphId + " not found!");
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GeneralPath getPathForCharactercode(int code)
    {
        if (codeToGlyph.containsKey(code))
        {
            return getPathForGlyphId(codeToGlyph.get(code));
        }
        else
        {
            LOG.debug(fontname + ": glyphmapping for " + code + " not found!");
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfGlyphs()
    {
        if (glyphs != null)
        {
            return glyphs.size();
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose()
    {
        if (glyphs != null)
        {
            glyphs.clear();
        }
        if (codeToGlyph != null)
        {
            codeToGlyph.clear();
        }
    }
}
