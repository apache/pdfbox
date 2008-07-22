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

/**
 * A table in a true type font.
 * 
 * @author Ben Litchfield (ben@benlitchfield.com)
 * @version $Revision: 1.1 $
 */
public class CMAPTable extends TTFTable
{
    /**
     * A tag used to identify this table.
     */
    public static final String TAG = "cmap";
    
    /**
     * A constant for the platform.
     */
    public static final int PLATFORM_WINDOWS = 3;
    
    /**
     * An encoding constant.
     */
    public static final int ENCODING_SYMBOL = 0;
    /**
     * An encoding constant.
     */
    public static final int ENCODING_UNICODE = 1;
    /**
     * An encoding constant.
     */
    public static final int ENCODING_SHIFT_JIS = 2;
    /**
     * An encoding constant.
     */
    public static final int ENCODING_BIG5 = 3;
    /**
     * An encoding constant.
     */
    public static final int ENCODING_PRC = 4;
    /**
     * An encoding constant.
     */
    public static final int ENCODING_WANSUNG = 5;
    /**
     * An encoding constant.
     */
    public static final int ENCODING_JOHAB = 6;
    
    private CMAPEncodingEntry[] cmaps;
    
    /**
     * This will read the required data from the stream.
     * 
     * @param ttf The font that is being read.
     * @param data The stream to read the data from.
     * @throws IOException If there is an error reading the data.
     */
    public void initData( TrueTypeFont ttf, TTFDataStream data ) throws IOException
    {
        int version = data.readUnsignedShort();
        int numberOfTables = data.readUnsignedShort();
        cmaps = new CMAPEncodingEntry[ numberOfTables ];
        for( int i=0; i< numberOfTables; i++ )
        {
            CMAPEncodingEntry cmap = new CMAPEncodingEntry();
            cmap.initData( ttf, data );
            cmaps[i]=cmap;
        }
        for( int i=0; i< numberOfTables; i++ )
        {
            cmaps[i].initSubtable( ttf, data );
        }
        
    }
    /**
     * @return Returns the cmaps.
     */
    public CMAPEncodingEntry[] getCmaps()
    {
        return cmaps;
    }
    /**
     * @param cmapsValue The cmaps to set.
     */
    public void setCmaps(CMAPEncodingEntry[] cmapsValue)
    {
        this.cmaps = cmapsValue;
    }
}
