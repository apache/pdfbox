/**
 * Copyright (c) 2005, www.fontbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of fontbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.fontbox.org
 *
 */
package org.fontbox.ttf;

import java.io.IOException;

import org.fontbox.util.BoundingBox;

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
