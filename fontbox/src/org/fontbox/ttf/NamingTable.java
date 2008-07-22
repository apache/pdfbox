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

import java.util.ArrayList;
import java.util.List;

/**
 * A table in a true type font.
 * 
 * @author Ben Litchfield (ben@benlitchfield.com)
 * @version $Revision: 1.1 $
 */
public class NamingTable extends TTFTable
{
    /**
     * A tag that identifies this table type.
     */
    public static final String TAG = "name";
    
    private List nameRecords = new ArrayList();
    
    /**
     * This will read the required data from the stream.
     * 
     * @param ttf The font that is being read.
     * @param data The stream to read the data from.
     * @throws IOException If there is an error reading the data.
     */
    public void initData( TrueTypeFont ttf, TTFDataStream data ) throws IOException
    {
        int formatSelector = data.readUnsignedShort();
        int numberOfNameRecords = data.readUnsignedShort();
        int offsetToStartOfStringStorage = data.readUnsignedShort();
        for( int i=0; i< numberOfNameRecords; i++ )
        {
            NameRecord nr = new NameRecord();
            nr.initData( ttf, data );
            nameRecords.add( nr );
        }
        for( int i=0; i<numberOfNameRecords; i++ )
        {
            NameRecord nr = (NameRecord)nameRecords.get( i );
            data.seek( getOffset() + (2*3)+numberOfNameRecords*2*6+nr.getStringOffset() );
            int platform = nr.getPlatformId();
            int encoding = nr.getPlatformEncodingId();
            String charset = "ISO-8859-1";
            if( platform == 3 && encoding == 1 )
            {
                charset = "UTF-16";
            }
            else if( platform == 2 )
            {
                if( encoding == 0 )
                {
                    charset = "US-ASCII";
                }
                else if( encoding == 1 )
                {
                    //not sure is this is correct??
                    charset = "ISO-10646-1";
                }
                else if( encoding == 2 )
                {
                    charset = "ISO-8859-1";
                }
            }
            String string = data.readString( nr.getStringLength(), charset );
            nr.setString( string );
        }
    }
    
    /**
     * This will get the name records for this naming table.
     * 
     * @return A list of NameRecord objects.
     */
    public List getNameRecords()
    {
        return nameRecords;
    }
}
