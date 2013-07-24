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
import java.awt.geom.Point2D;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.cmap.CMap;
import org.apache.fontbox.ttf.CMAPEncodingEntry;
import org.apache.fontbox.ttf.CMAPTable;
import org.apache.fontbox.ttf.GlyfDescript;
import org.apache.fontbox.ttf.GlyphData;
import org.apache.fontbox.ttf.GlyphDescription;
import org.apache.fontbox.ttf.HeaderTable;
import org.apache.fontbox.ttf.TrueTypeFont;

/**
 * This class provides a glyph to GeneralPath conversion for true type fonts.
 * 
 * This class is based on code from Apache Batik a subproject of Apache XMLGraphics. see
 * http://xmlgraphics.apache.org/batik/ for further details.
 */
public class TTFGlyph2D implements Glyph2D
{

    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(TTFGlyph2D.class);

    private TrueTypeFont font;
    private String name;
    private float scale = 0.001f;
    private CMAPEncodingEntry cmapMiscUnicode = null;
    private CMAPEncodingEntry cmapWinUnicode = null;
    private CMAPEncodingEntry cmapWinSymbol = null;
    private CMAPEncodingEntry cmapMacintoshSymbol = null;
    private boolean isSymbol = false;
    private HashMap<Integer, GeneralPath> glyphs = new HashMap<Integer, GeneralPath>();
    private CMap toUnicode = null;
    private int[] cid2gid = null;

    /**
     * Constructor.
     * 
     * @param trueTypeFont the true type font containing the glyphs
     * @param fontname the name of the given font
     * @param symbolFont indicates if the font is a symbolic font
     * 
     */
    public TTFGlyph2D(TrueTypeFont trueTypeFont, String fontname, boolean symbolFont)
    {
        this(trueTypeFont, fontname, symbolFont, null);
    }

    /**
     * Constructor.
     * 
     * @param trueTypeFont the true type font containing the glyphs
     * @param fontname the name of the given font
     * @param symbolFont indicates if the font is a symbolic font
     * @param toUnicodeCMap an optional toUnicode mapping
     * 
     */
    public TTFGlyph2D(TrueTypeFont trueTypeFont, String fontname, boolean symbolFont, CMap toUnicodeCMap)
    {
        this(trueTypeFont, fontname, symbolFont, toUnicodeCMap, null);
    }

    /**
     * Constructor.
     * 
     * @param trueTypeFont the true type font containing the glyphs
     * @param fontname the name of the given font
     * @param symbolFont indicates if the font is a symbolic font
     * @param toUnicodeCMap an optional toUnicode mapping
     * @param cid2gidMapping an optional CID2GIC mapping
     * 
     */
    public TTFGlyph2D(TrueTypeFont trueTypeFont, String fontname, boolean symbolFont, CMap toUnicodeCMap,
            int[] cid2gidMapping)
    {
        font = trueTypeFont;
        isSymbol = symbolFont;
        name = fontname;
        toUnicode = toUnicodeCMap;
        cid2gid = cid2gidMapping;
        // get units per em, which is used as scaling factor
        HeaderTable header = font.getHeader();
        if (header != null)
        {
            scale = 1f / header.getUnitsPerEm();
        }
        CMAPTable cmapTable = font.getCMAP();
        if (cmapTable != null)
        {
            // get all relevant CMaps
            CMAPEncodingEntry[] cmaps = cmapTable.getCmaps();
            for (int i = 0; i < cmaps.length; i++)
            {
                if (CMAPTable.PLATFORM_WINDOWS == cmaps[i].getPlatformId())
                {
                    if (CMAPTable.ENCODING_UNICODE == cmaps[i].getPlatformEncodingId())
                    {
                        cmapWinUnicode = cmaps[i];
                    }
                    else if (CMAPTable.ENCODING_SYMBOL == cmaps[i].getPlatformEncodingId())
                    {
                        cmapWinSymbol = cmaps[i];
                    }
                }
                else if (CMAPTable.PLATFORM_MACINTOSH == cmaps[i].getPlatformId())
                {
                    if (CMAPTable.ENCODING_SYMBOL == cmaps[i].getPlatformEncodingId())
                    {
                        cmapMacintoshSymbol = cmaps[i];
                    }
                }
                else if (CMAPTable.PLATFORM_MISC == cmaps[i].getPlatformId())
                {
                    if (CMAPTable.ENCODING_UNICODE == cmaps[i].getPlatformEncodingId())
                    {
                        cmapMiscUnicode = cmaps[i];
                    }
                }
            }
        }
    }

    /**
     * Set the points of a glyph from the GlyphDescription.
     */
    private Point[] describe(GlyphDescription gd)
    {
        int endPtIndex = 0;
        Point[] points = new Point[gd.getPointCount()];
        for (int i = 0; i < gd.getPointCount(); i++)
        {
            boolean endPt = gd.getEndPtOfContours(endPtIndex) == i;
            if (endPt)
            {
                endPtIndex++;
            }
            points[i] = new Point(gd.getXCoordinate(i), -gd.getYCoordinate(i),
                    (gd.getFlags(i) & GlyfDescript.ON_CURVE) != 0, endPt);
        }
        return points;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GeneralPath getPathForGlyphId(int glyphId)
    {
        GeneralPath glyphPath = null;
        if (glyphs.containsKey(glyphId))
        {
            glyphPath = glyphs.get(glyphId);
        }
        else
        {
            GlyphData[] glyphData = font.getGlyph().getGlyphs();
            if (glyphId < glyphData.length && glyphData[glyphId] != null)
            {
                GlyphData glyph = glyphData[glyphId];
                GlyphDescription gd = glyph.getDescription();
                Point[] points = describe(gd);
                glyphPath = calculatePath(points);
                AffineTransform atScale = AffineTransform.getScaleInstance(scale, scale);
                glyphPath.transform(atScale);
                glyphs.put(glyphId, glyphPath);
            }
            else
            {
                LOG.debug(name + ": Glyph not found:" + glyphId);
            }
        }
        return glyphPath != null ? (GeneralPath) glyphPath.clone() : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GeneralPath getPathForCharactercode(int code)
    {
        if (isSymbol)
        {
            // symbol fonts
            if (cmapWinSymbol != null)
            {
                int glyphId = cmapWinSymbol.getGlyphId(code);
                // microsoft sometimes uses PUA unicode values for symbol fonts
                // the range 0x0020 - 0x00FF maps to 0xF020 - 0xF0FF
                if (glyphId == 0 && code >= 0x0020 && code <= 0x00FF)
                {
                    glyphId = cmapWinSymbol.getGlyphId(code + 0xF000);
                }
                return getPathForGlyphId(glyphId);
            }
        }
        else
        {
            // non symbol fonts
            // Unicode mapping
            if (cmapWinUnicode != null)
            {
                return getPathForGlyphId(cmapWinUnicode.getGlyphId(code));
            }
            // some fonts provide a custom CMap
            if (cmapMiscUnicode != null)
            {
                int unicode = code;
                // map the given code to a valid unicode value, if necessary
                if (toUnicode != null)
                {
                    String unicodeStr = toUnicode.lookup(code, 1);
                    if (unicodeStr != null)
                    {
                        unicode = unicodeStr.codePointAt(0);
                    }
                }
                return getPathForGlyphId(cmapMiscUnicode.getGlyphId(unicode));
            }
            // use a mac related mapping
            if (cmapMacintoshSymbol != null)
            {
                return getPathForGlyphId(cmapMacintoshSymbol.getGlyphId(code));
            }
        }
        // there isn't any mpping, but propably an optional CID2GID mapping
        if (cid2gid != null && code <= cid2gid.length)
        {
            code = cid2gid[code];
        }
        return getPathForGlyphId(code);
    }

    /**
     * Use the given points to calculate a GeneralPath.
     * 
     * @param points the points to be used to generate the GeneralPath
     * 
     * @return the calculated GeneralPath
     */
    private GeneralPath calculatePath(Point[] points)
    {
        GeneralPath path = new GeneralPath();
        int numberOfPoints = points.length;
        int i = 0;
        boolean endOfContour = true;
        Point startingPoint = null;
        Point lastCtrlPoint = null;
        while (i < numberOfPoints)
        {
            Point point = points[i % numberOfPoints];
            Point nextPoint1 = points[(i + 1) % numberOfPoints];
            Point nextPoint2 = points[(i + 2) % numberOfPoints];
            // new contour
            if (endOfContour)
            {
                // skip endOfContour points
                if (point.endOfContour)
                {
                    i++;
                    continue;
                }
                // move to the starting point
                path.moveTo(point.x, point.y);
                endOfContour = false;
                startingPoint = point;
            }
            // lineTo
            if (point.onCurve && nextPoint1.onCurve)
            {
                path.lineTo(nextPoint1.x, nextPoint1.y);
                i++;
                if (point.endOfContour || nextPoint1.endOfContour)
                {
                    endOfContour = true;
                    path.closePath();
                }
                continue;
            }
            // quadratic bezier
            if (point.onCurve && !nextPoint1.onCurve && nextPoint2.onCurve)
            {
                if (nextPoint1.endOfContour)
                {
                    // use the starting point as end point
                    path.quadTo(nextPoint1.x, nextPoint1.y, startingPoint.x, startingPoint.y);
                }
                else
                {
                    path.quadTo(nextPoint1.x, nextPoint1.y, nextPoint2.x, nextPoint2.y);
                }
                if (nextPoint1.endOfContour || nextPoint2.endOfContour)
                {
                    endOfContour = true;
                    path.closePath();
                }
                i += 2;
                lastCtrlPoint = nextPoint1;
                continue;
            }
            if (point.onCurve && !nextPoint1.onCurve && !nextPoint2.onCurve)
            {
                // interpolate endPoint
                int endPointX = midValue(nextPoint1.x, nextPoint2.x);
                int endPointY = midValue(nextPoint1.y, nextPoint2.y);
                path.quadTo(nextPoint1.x, nextPoint1.y, endPointX, endPointY);
                if (point.endOfContour || nextPoint1.endOfContour || nextPoint2.endOfContour)
                {
                    path.quadTo(nextPoint2.x, nextPoint2.y, startingPoint.x, startingPoint.y);
                    endOfContour = true;
                    path.closePath();
                }
                i += 2;
                lastCtrlPoint = nextPoint1;
                continue;
            }
            if (!point.onCurve && !nextPoint1.onCurve)
            {
                Point2D lastEndPoint = path.getCurrentPoint();
                // calculate new control point using the previous control point
                lastCtrlPoint = new Point(midValue(lastCtrlPoint.x, (int) lastEndPoint.getX()), midValue(
                        lastCtrlPoint.y, (int) lastEndPoint.getY()));
                // interpolate endPoint
                int endPointX = midValue((int) lastEndPoint.getX(), nextPoint1.x);
                int endPointY = midValue((int) lastEndPoint.getY(), nextPoint1.y);
                path.quadTo(lastCtrlPoint.x, lastCtrlPoint.y, endPointX, endPointY);
                if (point.endOfContour || nextPoint1.endOfContour)
                {
                    endOfContour = true;
                    path.closePath();
                }
                i++;
                continue;
            }
            if (!point.onCurve && nextPoint1.onCurve)
            {
                path.quadTo(point.x, point.y, nextPoint1.x, nextPoint1.y);
                if (point.endOfContour || nextPoint1.endOfContour)
                {
                    endOfContour = true;
                    path.closePath();
                }
                i++;
                lastCtrlPoint = point;
                continue;
            }
            System.err.println("Unknown glyph command!!");
            break;
        }
        return path;
    }

    private int midValue(int a, int b)
    {
        return a + (b - a) / 2;
    }

    /**
     * This class represents one point of a glyph.
     * 
     */
    private class Point
    {

        public int x = 0;
        public int y = 0;
        public boolean onCurve = true;
        public boolean endOfContour = false;

        public Point(int xValue, int yValue, boolean onCurveValue, boolean endOfContourValue)
        {
            x = xValue;
            y = yValue;
            onCurve = onCurveValue;
            endOfContour = endOfContourValue;
        }

        public Point(int xValue, int yValue)
        {
            this(xValue, yValue, false, false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfGlyphs()
    {
        return font != null ? font.getGlyph().getGlyphs().length : 0;
    }

    @Override
    public void dispose()
    {
        cid2gid = null;
        cmapMacintoshSymbol = null;
        cmapMiscUnicode = null;
        cmapWinSymbol = null;
        cmapWinUnicode = null;
        font = null;
        toUnicode = null;
        if (glyphs != null)
        {
            glyphs.clear();
        }
    }
}
