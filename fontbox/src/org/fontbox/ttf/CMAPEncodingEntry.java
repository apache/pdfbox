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
package org.fontbox.ttf;

import java.io.IOException;

/**
 * An encoding entry for a cmap.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class CMAPEncodingEntry
{

    private int platformId;
    private int platformEncodingId;
    private long subTableOffset;
    private int[] glyphIdToCharacterCode;
    /**
     * This will read the required data from the stream.
     * 
     * @param ttf The font that is being read.
     * @param data The stream to read the data from.
     * @throws IOException If there is an error reading the data.
     */
    public void initData( TrueTypeFont ttf, TTFDataStream data ) throws IOException
    {
        platformId = data.readUnsignedShort();
        platformEncodingId = data.readUnsignedShort();
        subTableOffset = data.readUnsignedInt();
    }
    
    /**
     * This will read the required data from the stream.
     * 
     * @param ttf The font that is being read.
     * @param data The stream to read the data from.
     * @throws IOException If there is an error reading the data.
     */
    public void initSubtable( TrueTypeFont ttf, TTFDataStream data ) throws IOException
    {
        data.seek( ttf.getCMAP().getOffset() + subTableOffset );
        int subtableFormat = data.readUnsignedShort();
        int length = data.readUnsignedShort();
        int version = data.readUnsignedShort();
        int numGlyphs = ttf.getMaximumProfile().getNumGlyphs();
        if( subtableFormat == 0 )
        {
            byte[] glyphMapping = data.read( 256 );
            glyphIdToCharacterCode = new int[256];
            for( int i=0;i<glyphMapping.length; i++ )
            {
                glyphIdToCharacterCode[i]=(glyphMapping[i]+256)%256;
            }
        }
        else if( subtableFormat == 2 )
        {
            int[] subHeaderKeys = new int[256];
            for( int i=0; i<256; i++)
            {
                subHeaderKeys[i] = data.readUnsignedShort();
            }
            int firstCode = data.readUnsignedShort();
            int entryCount = data.readUnsignedShort();
            short idDelta = data.readSignedShort();
            int idRangeOffset = data.readUnsignedShort();
            //BJL
            //HMM the TTF spec is not very clear about what is suppose to
            //happen here.  If you know please submit a patch or point
            //me to some better documentation.
            throw new IOException( "Not yet implemented:" + subtableFormat );
        }
        else if( subtableFormat == 4 )
        {
            int segCountX2 = data.readUnsignedShort();
            int segCount = segCountX2/2;
            int searchRange = data.readUnsignedShort();
            int entrySelector = data.readUnsignedShort();
            int rangeShift = data.readUnsignedShort();
            int[] endCount = data.readUnsignedShortArray( segCount );
            int reservedPad = data.readUnsignedShort();
            int[] startCount = data.readUnsignedShortArray( segCount );
            int[] idDelta = data.readUnsignedShortArray( segCount );
            int[] idRangeOffset = data.readUnsignedShortArray( segCount );
            
            //this is the final result
            //key=glyphId, value is character codes
            glyphIdToCharacterCode = new int[numGlyphs];
            
            long currentPosition = data.getCurrentPosition();
            
            for( int i=0; i<segCount; i++ )
            {
                int start = startCount[i];
                int end = endCount[i];
                int delta = idDelta[i];
                int rangeOffset = idRangeOffset[i];
                if( start != 65535 && end != 65535 )
                {
                    for( int j=start; j<=end; j++ )
                    {
                        if( rangeOffset == 0 )
                        {
                            glyphIdToCharacterCode[ ((j+delta)%65536) ]=j;
                        }
                        else
                        {
                            long glyphOffset = currentPosition +
                                ((rangeOffset/2) + //idRangeOffset[i]/2 
                                (j-start) + //(c - startCount[i])                                   
                                (i-segCount))*2; //&idRangeOffset[i]); 
                            data.seek( glyphOffset );
                            int glyphIndex = data.readUnsignedShort();
                            if( glyphIndex != 0 )
                            {
                                glyphIndex += delta;
                                glyphIndex = glyphIndex % 65536;
                                if( glyphIdToCharacterCode[glyphIndex] == 0 )
                                {
                                    glyphIdToCharacterCode[glyphIndex] = j;
                                }
                            }
                            
                        }
                    }
                }
            }
        }
        else if( subtableFormat == 6 )
        {
            int firstCode = data.readUnsignedShort();
            int entryCount = data.readUnsignedShort();
            glyphIdToCharacterCode = new int[numGlyphs];
            int[] glyphIdArray = data.readUnsignedShortArray( entryCount );
            for( int i=0; i<entryCount; i++)
            {
                glyphIdToCharacterCode[glyphIdArray[i]] = firstCode+i;
            }
        }
        else
        {
            throw new IOException( "Unknown cmap format:" + subtableFormat );
        }
    }
    

    /**
     * @return Returns the glyphIdToCharacterCode.
     */
    public int[] getGlyphIdToCharacterCode()
    {
        return glyphIdToCharacterCode;
    }
    /**
     * @param glyphIdToCharacterCodeValue The glyphIdToCharacterCode to set.
     */
    public void setGlyphIdToCharacterCode(int[] glyphIdToCharacterCodeValue)
    {
        this.glyphIdToCharacterCode = glyphIdToCharacterCodeValue;
    }
    
    /**
     * @return Returns the platformEncodingId.
     */
    public int getPlatformEncodingId()
    {
        return platformEncodingId;
    }
    /**
     * @param platformEncodingIdValue The platformEncodingId to set.
     */
    public void setPlatformEncodingId(int platformEncodingIdValue)
    {
        this.platformEncodingId = platformEncodingIdValue;
    }
    /**
     * @return Returns the platformId.
     */
    public int getPlatformId()
    {
        return platformId;
    }
    /**
     * @param platformIdValue The platformId to set.
     */
    public void setPlatformId(int platformIdValue)
    {
        this.platformId = platformIdValue;
    }
}
