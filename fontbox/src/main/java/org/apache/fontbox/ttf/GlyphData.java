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

import org.apache.fontbox.util.BoundingBox;

/**
 * A glyph data record in the glyf table.
 * 
 * @author Ben Litchfield (ben@benlitchfield.com)
 * @version $Revision: 1.1 $
 */
public class GlyphData
{
    private short xMin;
    private short yMin;
    private short xMax;
    private short yMax;
    private BoundingBox boundingBox = null;
    private short numberOfContours;
    private GlyfDescript glyphDescription = null;
    
    /**
     * This will read the required data from the stream.
     * 
     * @param ttf The font that is being read.
     * @param data The stream to read the data from.
     * @throws IOException If there is an error reading the data.
     */
    public void initData( TrueTypeFont ttf, TTFDataStream data ) throws IOException
    {
        numberOfContours = data.readSignedShort();
        xMin = data.readSignedShort();
        yMin = data.readSignedShort();
        xMax = data.readSignedShort();
        yMax = data.readSignedShort();
        boundingBox = new BoundingBox(xMin, yMin, xMax, yMax);

        if (numberOfContours >= 0) 
        {
            // create a simple glyph
            glyphDescription = new GlyfSimpleDescript(numberOfContours, data);
        }
        else 
        {
            // create a composite glyph
            glyphDescription = new GlyfCompositeDescript(data, ttf.getGlyph());
        }
    }
    
    /**
     * @return Returns the boundingBox.
     */
    public BoundingBox getBoundingBox()
    {
        return boundingBox;
    }
    /**
     * @param boundingBoxValue The boundingBox to set.
     */
    public void setBoundingBox(BoundingBox boundingBoxValue)
    {
        this.boundingBox = boundingBoxValue;
    }
    /**
     * @return Returns the numberOfContours.
     */
    public short getNumberOfContours()
    {
        return numberOfContours;
    }
    /**
     * @param numberOfContoursValue The numberOfContours to set.
     */
    public void setNumberOfContours(short numberOfContoursValue)
    {
        this.numberOfContours = numberOfContoursValue;
    }
   
    /**
     * Returns the description of the glyph.
     * @return the glyph description
     */
    public GlyphDescription getDescription()
    {
        return glyphDescription;
    }
    
    /**
     * Returns the xMax value.
     * @return the XMax value
     */
    public short getXMaximum() 
    {
        return xMax;
    }

    /**
     * Returns the xMin value.
     * @return the xMin value
     */
    public short getXMinimum() 
    {
        return xMin;
    }

    /**
     * Returns the yMax value.
     * @return the yMax value
     */
    public short getYMaximum() 
    {
        return yMax;
    }

    /**
     * Returns the yMin value.
     * @return the yMin value
     */
    public short getYMinimum() 
    {
        return yMin;
    }

}
