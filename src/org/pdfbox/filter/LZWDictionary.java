/**
 * Copyright (c) 2003, www.pdfbox.org
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
 * 3. Neither the name of pdfbox; nor the names of its
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
 * http://www.pdfbox.org
 *
 */
package org.pdfbox.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

/**
 * This is the used for the LZWDecode filter.  This represents the dictionary mappings
 * between codes and their values.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.4 $
 */
class LZWDictionary
{
    private Map codeToData = new HashMap();
    private LZWNode root = new LZWNode();

    private ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private long nextCode = 258;
    private int codeSize = 9;

    /**
     * constructor.
     */
    public LZWDictionary()
    {
        for( long i=0; i<256; i++ )
        {
            LZWNode node = new LZWNode();
            node.setCode( i );
            root.setNode( (byte)i, node );
            codeToData.put( new Long( i ), new byte[]{ (byte)i } );
        }
    }

    /**
     * This will get the value for the code.  It will return null if the code is not
     * defined.
     *
     * @param code The key to the data.
     *
     * @return The data that is mapped to the code.
     */
    public byte[] getData( long code )
    {
        return (byte[])codeToData.get( new Long( code ) );
    }

    /**
     * This will take a visit from a byte[].  This will create new code entries as
     * necessary.
     *
     * @param data The byte to get a visit from.
     *
     * @throws IOException If there is an error visiting this data.
     */
    public void visit( byte[] data ) throws IOException
    {
        for( int i=0; i<data.length; i++ )
        {
            visit( data[i] );
        }
    }

    /**
     * This will take a visit from a byte.  This will create new code entries as
     * necessary.
     *
     * @param data The byte to get a visit from.
     *
     * @throws IOException If there is an error visiting this data.
     */
    public void visit( byte data ) throws IOException
    {
        buffer.write( data );
        byte[] curBuffer = buffer.toByteArray();
        LZWNode previous = null;
        LZWNode current = root;
        boolean createNewCode = false;
        for( int i=0; i<curBuffer.length && current != null; i++ )
        {
            previous = current;
            current = current.getNode( curBuffer[i] );
            if( current == null )
            {
                createNewCode = true;
                current = new LZWNode();
                previous.setNode( curBuffer[i], current );
            }
        }
        if( createNewCode )
        {
            long code = nextCode++;
            current.setCode( code );
            codeToData.put( new Long( code ), curBuffer );

            /**
            System.out.print( "Adding " + code + "='" );
            for( int i=0; i<curBuffer.length; i++ )
            {
                String hex = Integer.toHexString( ((curBuffer[i]+256)%256) );
                if( hex.length() <=1 )
                {
                    hex = "0" + hex;
                }
                if( i != curBuffer.length -1 )
                {
                    hex += " ";
                }
                System.out.print( hex.toUpperCase() );
            }
            System.out.println( "'" );
            **/
            buffer.reset();
            buffer.write( data );
            resetCodeSize();
        }
    }

    /**
     * This will get the next code that will be created.
     *
     * @return The next code to be created.
     */
    public long getNextCode()
    {
        return nextCode;
    }

    /**
     * This will get the size of the code in bits, 9, 10, or 11.
     *
     * @return The size of the code in bits.
     */
    public int getCodeSize()
    {
        return codeSize;
    }

    /**
     * This will determine the code size.
     */
    private void resetCodeSize()
    {
        if( nextCode >= 2048 )
        {
            codeSize = 12;
        }
        else if( nextCode >= 1024 )
        {
            codeSize = 11;
        }
        else if( nextCode >= 512 )
        {
            codeSize = 10;
        }
        else
        {
            codeSize = 9;
        }
    }

    /**
     * This will crear the internal buffer that the dictionary uses.
     */
    public void clear()
    {
        buffer.reset();
    }

    /**
     * This will folow the path to the data node.
     *
     * @param data The path to the node.
     *
     * @return The node that resides at that path.
     */
    public LZWNode getNode( byte[] data )
    {
        return root.getNode( data );
    }
}