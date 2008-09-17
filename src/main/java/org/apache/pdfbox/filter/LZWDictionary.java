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
package org.apache.pdfbox.filter;

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
