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
package org.apache.fontbox.cff;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.fontbox.cff.encoding.CFFEncoding;

/**
 * This class creates all needed AFM font metric data from a CFFFont ready to be read from a AFMPaser.
 * 
 * @author Villu Ruusmann
 * 
 */
public class AFMFormatter
{

    private AFMFormatter()
    {
    }

    /**
     * Create font metric data for the given CFFFont.
     * @param font the CFFFont
     * @return the created font metric data
     * @throws IOException if an error occurs during reading
     */
    public static byte[] format(CFFFont font) throws IOException
    {
        DataOutput output = new DataOutput();
        printFont(font, output);
        return output.getBytes();
    }

    private static void printFont(CFFFont font, DataOutput output)
            throws IOException
    {
        printFontMetrics(font, output);
    }

    @SuppressWarnings(value = { "unchecked" })
    private static void printFontMetrics(CFFFont font, DataOutput output)
            throws IOException
    {
        List<CharMetric> metrics = renderFont(font);
        output.println("StartFontMetrics 2.0");
        output.println("FontName " + font.getName());
        output.println("FullName " + font.getProperty("FullName"));
        output.println("FamilyName " + font.getProperty("FamilyName"));
        output.println("Weight " + font.getProperty("Weight"));
        CFFEncoding encoding = font.getEncoding();
        if (encoding.isFontSpecific())
        {
            output.println("EncodingScheme FontSpecific");
        }
        Rectangle2D bounds = getBounds(metrics);
        output.println("FontBBox " + (int)bounds.getX() + " " + (int)bounds.getY()
                + " " + (int)bounds.getMaxX() + " " + (int)bounds.getMaxY());
        printDirectionMetrics(font, output);
        printCharMetrics(font, metrics, output);
        output.println("EndFontMetrics");
    }

    private static void printDirectionMetrics(CFFFont font, DataOutput output)
            throws IOException
    {
        output.println("UnderlinePosition "
                + font.getProperty("UnderlinePosition"));
        output.println("UnderlineThickness "
                + font.getProperty("UnderlineThickness"));
        output.println("ItalicAngle " + font.getProperty("ItalicAngle"));
        output.println("IsFixedPitch " + font.getProperty("isFixedPitch"));
    }

    private static void printCharMetrics(CFFFont font, List<CharMetric> metrics, DataOutput output)
            throws IOException
    {
        output.println("StartCharMetrics " + metrics.size());
        Collections.sort(metrics);
        for (CharMetric metric : metrics)
        {
            output.print("C " + metric.code + " ;");
            output.print(" ");
            output.print("WX " + metric.width + " ;");
            output.print(" ");
            output.print("N " + metric.name + " ;");
            output.print(" ");
            output.print("B " + (int) metric.bounds.getX() + " "
                    + (int) metric.bounds.getY() + " "
                    + (int) metric.bounds.getMaxX() + " "
                    + (int) metric.bounds.getMaxY() + " ;");
            output.println();
        }
        output.println("EndCharMetrics");
    }

    private static List<CharMetric> renderFont(CFFFont font) throws IOException
    {
        List<CharMetric> metrics = new ArrayList<CharMetric>();
        Collection<CFFFont.Mapping> mappings = font.getMappings();
        for (CFFFont.Mapping mapping : mappings)
        {
            CharMetric metric = new CharMetric();
            metric.code = mapping.getCode();
            metric.name = mapping.getName();
            metric.width = mapping.getType1CharString().getWidth();
            metric.bounds = mapping.getType1CharString().getBounds();
            metrics.add(metric);
        }
        return metrics;
    }

    private static Rectangle2D getBounds(List<CharMetric> metrics)
    {
        Rectangle2D bounds = null;
        for(CharMetric metric : metrics)
        {
            if(bounds == null)
            {
                bounds = new Rectangle2D.Double();
                bounds.setFrame(metric.bounds);
            }
            else
            {
                Rectangle2D.union(bounds, metric.bounds, bounds);
            }
        }
        return bounds;
    }

    /**
     * This class represents the metric of one single character. 
     *
     */
    private static class CharMetric implements Comparable<CharMetric>
    {
        private int code;
        private String name;
        private int width;
        private Rectangle2D bounds;

        public int compareTo(CharMetric that)
        {
            return code - that.code;
        }
    }
}