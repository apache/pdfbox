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
        long length;
        long version;
        int numGlyphs;
        if (subtableFormat < 8) {
            length = data.readUnsignedShort();
            version = data.readUnsignedShort();
            numGlyphs = ttf.getMaximumProfile().getNumGlyphs();
        } else {
            // read an other UnsignedShort to read a Fixed32
            data.readUnsignedShort();
            length = data.readUnsignedInt();
            version = data.readUnsignedInt();
            numGlyphs = ttf.getMaximumProfile().getNumGlyphs();         
        }
            
        switch (subtableFormat) {
        case 0:
            processSubtype0(ttf, data);
            break;
        case 2:
            processSubtype2(ttf, data, numGlyphs);
            break;
        case 4:
            processSubtype4(ttf, data, numGlyphs);
            break;
        case 6:
            processSubtype6(ttf, data, numGlyphs);
            break;
        case 8:
            processSubtype8(ttf, data, numGlyphs);
            break;
        case 10:
            processSubtype10(ttf, data, numGlyphs);
            break;
        case 12:
            processSubtype12(ttf, data, numGlyphs);
            break;
        case 13:
            processSubtype13(ttf, data, numGlyphs); 
            break;
        case 14:
            processSubtype14(ttf, data, numGlyphs);
            break;
        default:
            throw new IOException( "Unknown cmap format:" + subtableFormat );   
        }
    }
          
    /**
     * Reads a format 8 subtable.
     * @param ttf the TrueTypeFont instance holding the parsed data.
     * @param data the data stream of the to be parsed ttf font
     * @param numGlyphs number of glyphs to be read
     * @throws IOException If there is an error parsing the true type font.
     */
    protected void processSubtype8( TrueTypeFont ttf, TTFDataStream data, int numGlyphs ) 
    throws IOException {
        // --- is32 is a 65536 BITS array ( = 8192 BYTES) 
        int[] is32 = data.readUnsignedByteArray(8192);
        long nbGroups = data.readUnsignedInt();
            
        // --- nbGroups shouldn't be greater than 65536
        if (nbGroups > 65536) {
            throw new IOException("CMap ( Subtype8 ) is invalid");
        }
            
        glyphIdToCharacterCode = new int[numGlyphs];
        // -- Read all sub header
        for (long i = 0; i <= nbGroups ; ++i ) 
        {
            long firstCode = data.readUnsignedInt();
            long endCode = data.readUnsignedInt();
            long startGlyph = data.readUnsignedInt();
                
            // -- process simple validation
            if (firstCode > endCode || 0 > firstCode) {
                throw new IOException("Range invalid");
            }
                
            for (long j = firstCode; j <= endCode; ++j) {
                // -- Convert the Character code in decimal
                if (j > Integer.MAX_VALUE) {
                    throw new IOException("[Sub Format 8] Invalid Character code");
                }
                
                int currentCharCode;
                if ( (is32[ (int)j / 8 ] & (1 << ((int)j % 8 ))) == 0) {
                    currentCharCode = (int)j;
                } else {
                    // the character code uses a 32bits format 
                    // convert it in decimal : see http://www.unicode.org/faq//utf_bom.html#utf16-4
                    long LEAD_OFFSET = 0xD800 - (0x10000 >> 10);
                    long SURROGATE_OFFSET = 0x10000 - (0xD800 << 10) - 0xDC00;
                    long lead = LEAD_OFFSET + (j >> 10);
                    long trail = 0xDC00 + (j & 0x3FF);
                    
                    long codepoint = (lead << 10) + trail + SURROGATE_OFFSET;
                    if (codepoint > Integer.MAX_VALUE) {
                        throw new IOException("[Sub Format 8] Invalid Character code");
                    }
                    currentCharCode = (int)codepoint;
                }
                
                long glyphIndex = startGlyph + (j-firstCode);
                if (glyphIndex > numGlyphs || glyphIndex > Integer.MAX_VALUE) {
                    throw new IOException("CMap contains an invalid glyph index");
                }
                
                glyphIdToCharacterCode[(int)glyphIndex] = currentCharCode;
                characterCodeToGlyphId.put(currentCharCode, (int)glyphIndex);
            }
        }
    }
    
    /**
     * Reads a format 10 subtable.
     * @param ttf the TrueTypeFont instance holding the parsed data.
     * @param data the data stream of the to be parsed ttf font
     * @param numGlyphs number of glyphs to be read
     * @throws IOException If there is an error parsing the true type font.
     */
    protected void processSubtype10( TrueTypeFont ttf, TTFDataStream data, int numGlyphs ) 
    throws IOException {
        long startCode = data.readUnsignedInt();
        long numChars = data.readUnsignedInt();
        if (numChars > Integer.MAX_VALUE) {
            throw new IOException("Invalid number of Characters");
        }
        
        if ( startCode < 0 || startCode > 0x0010FFFF 
                || (startCode + numChars) > 0x0010FFFF
                || ((startCode + numChars) >= 0x0000D800 && (startCode + numChars) <= 0x0000DFFF)) {
            throw new IOException("Invalid Characters codes");
            
        }   
    }   
    
    /**
     * Reads a format 12 subtable.
     * @param ttf the TrueTypeFont instance holding the parsed data.
     * @param data the data stream of the to be parsed ttf font
     * @param numGlyphs number of glyphs to be read
     * @throws IOException If there is an error parsing the true type font.
     */
    protected void processSubtype12( TrueTypeFont ttf, TTFDataStream data, int numGlyphs ) 
    throws IOException {
        long nbGroups = data.readUnsignedInt();
        glyphIdToCharacterCode = new int[numGlyphs];
        for (long i = 0; i <= nbGroups ; ++i ) 
        {
            long firstCode = data.readUnsignedInt();
            long endCode = data.readUnsignedInt();
            long startGlyph = data.readUnsignedInt();
                
            if ( firstCode < 0 || firstCode > 0x0010FFFF 
                    || ( firstCode >= 0x0000D800 && firstCode <= 0x0000DFFF ) ) {
                throw new IOException("Invalid Characters codes");
            }
            
            if ( endCode > 0 && (endCode < firstCode || endCode > 0x0010FFFF 
                    || ( endCode >= 0x0000D800 && endCode <= 0x0000DFFF ) ) ) {
                throw new IOException("Invalid Characters codes");
            }
                       
            for (long j = 0; j <= (endCode - firstCode); ++j) {
                
                if ( (firstCode + j) > Integer.MAX_VALUE ) {
                    throw new IOException("Character Code greater than Integer.MAX_VALUE");                 
                }
                           
                long glyphIndex = (startGlyph + j);
                if (glyphIndex > numGlyphs || glyphIndex > Integer.MAX_VALUE) {
                    throw new IOException("CMap contains an invalid glyph index");
                }
                glyphIdToCharacterCode[(int)glyphIndex] = (int)(firstCode + j);
                characterCodeToGlyphId.put((int)(firstCode + j), (int)glyphIndex);
            }
        }
    }
            
    /**
     * Reads a format 13 subtable.
     * @param ttf the TrueTypeFont instance holding the parsed data.
     * @param data the data stream of the to be parsed ttf font
     * @param numGlyphs number of glyphs to be read
     * @throws IOException If there is an error parsing the true type font.
     */
    protected void processSubtype13( TrueTypeFont ttf, TTFDataStream data, int numGlyphs ) 
    throws IOException {
        long nbGroups = data.readUnsignedInt();
        for (long i = 0; i <= nbGroups ; ++i ) 
        {
            long firstCode = data.readUnsignedInt();
            long endCode = data.readUnsignedInt();
            long glyphId = data.readUnsignedInt();
                
            if (glyphId > numGlyphs) {
                throw new IOException("CMap contains an invalid glyph index");  
            }
            
            if ( firstCode < 0 || firstCode > 0x0010FFFF 
                    || ( firstCode >= 0x0000D800 && firstCode <= 0x0000DFFF ) ) {
                throw new IOException("Invalid Characters codes");
            }
            
            if ( endCode > 0 && (endCode < firstCode || endCode > 0x0010FFFF 
                    || ( endCode >= 0x0000D800 && endCode <= 0x0000DFFF )) ) {
                throw new IOException("Invalid Characters codes");
            }
            
            for (long j = 0; j <= (endCode - firstCode); ++j) {
                
                if ( (firstCode + j) > Integer.MAX_VALUE ) {
                    throw new IOException("Character Code greater than Integer.MAX_VALUE");                 
                }
                glyphIdToCharacterCode[(int)glyphId] = (int)(firstCode + j);
                characterCodeToGlyphId.put((int)(firstCode + j), (int)glyphId);
            }
        }
    }
            
    /**
     * Reads a format 14 subtable.
     * @param ttf the TrueTypeFont instance holding the parsed data.
     * @param data the data stream of the to be parsed ttf font
     * @param numGlyphs number of glyphs to be read
     * @throws IOException If there is an error parsing the true type font.
     */
    protected void processSubtype14( TrueTypeFont ttf, TTFDataStream data, int numGlyphs ) 
    throws IOException {
        throw new IOException("CMap subtype 14 not yet implemented");
    }
                
    /**
     * Reads a format 6 subtable.
     * @param ttf the TrueTypeFont instance holding the parsed data.
     * @param data the data stream of the to be parsed ttf font
     * @param numGlyphs number of glyphs to be read
     * @throws IOException If there is an error parsing the true type font.
     */
    protected void processSubtype6( TrueTypeFont ttf, TTFDataStream data, int numGlyphs ) 
    throws IOException {
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
    
    /**
     * Reads a format 4 subtable.
     * @param ttf the TrueTypeFont instance holding the parsed data.
     * @param data the data stream of the to be parsed ttf font
     * @param numGlyphs number of glyphs to be read
     * @throws IOException If there is an error parsing the true type font.
     */
    protected void processSubtype4( TrueTypeFont ttf, TTFDataStream data, int numGlyphs ) 
    throws IOException {
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
       
    /**
     * Read a format 2 subtable.
     * @param ttf the TrueTypeFont instance holding the parsed data.
     * @param data the data stream of the to be parsed ttf font
     * @param numGlyphs number of glyphs to be read
     * @throws IOException If there is an error parsing the true type font.
     */
    protected void processSubtype2( TrueTypeFont ttf, TTFDataStream data, int numGlyphs ) 
    throws IOException {
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
       
    /**
     * Initialize the CMapEntry when it is a subtype 0
     * 
     * @param ttf
     * @param data
     * @throws IOException
     */
    protected void processSubtype0( TrueTypeFont ttf, TTFDataStream data ) 
    throws IOException {
        byte[] glyphMapping = data.read( 256 );
        glyphIdToCharacterCode = new int[256];
        for( int i=0;i < glyphMapping.length; i++ )
        {
            int glyphIndex = (glyphMapping[i]+256)%256;
            glyphIdToCharacterCode[glyphIndex]=i;
            characterCodeToGlyphId.put(i, glyphIndex);
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
