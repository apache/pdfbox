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
    private static final int FLAG_ON_CURVE = 1;
    private static final int FLAG_SHORT_X = 1<<1;
    private static final int FLAG_SHORT_Y = 1<<2;
    private static final int FLAG_X_MAGIC = 1<<3;
    private static final int FLAG_Y_MAGIC = 1<<4;
   
    private BoundingBox boundingBox = new BoundingBox();
    private short numberOfContours;
    private int[] endPointsOfContours;
    private byte[] instructions;
    private int[] flags;
    private short[] xCoordinates;
    private short[] yCoordinates;
    
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
        boundingBox.setLowerLeftX( data.readSignedShort() );
        boundingBox.setLowerLeftY( data.readSignedShort() );
        boundingBox.setUpperRightX( data.readSignedShort() );
        boundingBox.setUpperRightY( data.readSignedShort() );
        /**if( numberOfContours > 0 )
        {
            endPointsOfContours = new int[ numberOfContours ];
            for( int i=0; i<numberOfContours; i++ )
            {
                endPointsOfContours[i] = data.readUnsignedShort();
            }
            int instructionLength = data.readUnsignedShort();
            instructions = data.read( instructionLength );
            
            //BJL It is possible to read some more information here but PDFBox
            //does not need it at this time so just ignore it.
            
            //not sure if the length of the flags is the number of contours??
            //flags = new int[numberOfContours];
            //first read the flags, and just so the TTF can save a couples bytes
            //we need to check some bit masks to see if there are more bytes or not.
            //int currentFlagIndex = 0;
            //int currentFlag = 
            
            
        }*/
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
}
