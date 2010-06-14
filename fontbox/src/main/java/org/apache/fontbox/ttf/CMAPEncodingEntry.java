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
import java.util.Map;
import java.util.HashMap;
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
    private Map<Integer, Integer> characterCodeToGlyphId = new HashMap<Integer, Integer>();

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
            for( int i=0;i < glyphMapping.length; i++ )
            {
                int glyphIndex = (glyphMapping[i]+256)%256;
                glyphIdToCharacterCode[glyphIndex]=i;
            }
        }
        else if( subtableFormat == 2 )
        {
            int[] subHeaderKeys = new int[256];
            // ---- keep the Max Index of the SubHeader array to know its length
            int maxSubHeaderIndex = 0;
            for( int i=0; i<256; i++)
            {
                subHeaderKeys[i] = data.readUnsignedShort();
                maxSubHeaderIndex = Math.max(maxSubHeaderIndex, (int)(subHeaderKeys[i]/8));
            }
                
            // ---- Read all SubHeaders to avoid useless seek on DataSource
            SubHeader[] subHeaders = new SubHeader[maxSubHeaderIndex + 1]; 
            for (int i = 0; i <= maxSubHeaderIndex ; ++i ) 
            {
                int firstCode = data.readUnsignedShort();
                int entryCount = data.readUnsignedShort();
                short idDelta = data.readSignedShort();
                int idRangeOffset = data.readUnsignedShort();
                subHeaders[i] = new SubHeader(firstCode, entryCount, idDelta, idRangeOffset);
            }
                
            long startGlyphIndexOffset = data.getCurrentPosition();
            glyphIdToCharacterCode = new int[numGlyphs];
            for ( int i = 0; i <= maxSubHeaderIndex ; ++i )
            {
                SubHeader sh = subHeaders[i];
                int firstCode = sh.getFirstCode();
                for ( int j = 0 ; j < sh.getEntryCount() ; ++j)
                {
                    // ---- compute the Character Code
                    int charCode = ( i * 8 );
                    charCode = (charCode << 8 ) + (firstCode + j);
                    
                    // ---- Go to the CharacterCOde position in the Sub Array 
                    //      of the glyphIndexArray 
                    //      glyphIndexArray contains Unsigned Short so add (j * 2) bytes 
                    //      at the index position
                    data.seek(startGlyphIndexOffset + sh.getIdRangeOffset() + (j*2));
                    int p = data.readUnsignedShort();
                    // ---- compute the glyphIndex 
                    p = p + sh.getIdDelta() % 65536;
                    
                    glyphIdToCharacterCode[p] = charCode;
                    characterCodeToGlyphId.put(charCode, p);
                }
            }        
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
                            characterCodeToGlyphId.put(j, ((j+delta)%65536));
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
                                    characterCodeToGlyphId.put(j, glyphIndex);
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
                characterCodeToGlyphId.put((firstCode+i), glyphIdArray[i]);
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

    /**
     * Returns the GlyphId linked with the given character code. 
     * @param characterCode
     * @return glyphId
     */
    public int getGlyphId(int characterCode) {
    	if (this.characterCodeToGlyphId.containsKey(characterCode)) 
    	{
    		return this.characterCodeToGlyphId.get(characterCode);
    	} 
    	else 
    	{
    		return 0;
    	}
    }
    
    /**
     * Class used to manage CMap - Format 2
     */
    private class SubHeader {
        
        private int firstCode;
        private int entryCount;
        /**
         * used to compute the GlyphIndex :
         * P = glyphIndexArray.SubArray[pos]
         * GlyphIndex = P + idDelta % 65536
         */
        private short idDelta;
        /**
         * Number of bytes to skip to reach the firstCode in the 
         * glyphIndexArray 
         */
        private int idRangeOffset;
        
        private SubHeader(int firstCode, int entryCount, short idDelta, int idRangeOffset) 
        {
            this.firstCode = firstCode;
            this.entryCount = entryCount;
            this.idDelta = idDelta;
            this.idRangeOffset = idRangeOffset;
        }    
    
        /**
         * @return the firstCode
         */
        private int getFirstCode() 
        {
            return firstCode;
        }
    
        /**
         * @return the entryCount
         */
        private int getEntryCount() 
        {
            return entryCount;
        }    
    
        /**
         * @return the idDelta
         */
        private short getIdDelta() 
        {
            return idDelta;
        }
    
        /**
         * @return the idRangeOffset
         */
        private int getIdRangeOffset() 
        {
            return idRangeOffset;
        }
    }

}
