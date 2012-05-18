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
final class LZWDictionary
{
    private Map<Long,byte[]> codeToData = new HashMap<Long,byte[]>();
    private LZWNode root = new LZWNode( 0 );

    private byte[] buffer = new byte[8];
    private int bufferNextWrite = 0;
    private long nextCode = 258;
    private int codeSize = 9;

    private LZWNode previous = null;
    private LZWNode current = root;

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
        byte[] result = codeToData.get( code );
        if (result == null && code < 256) 
        {
            addRootNode( (byte) code );
            result = codeToData.get( code );
        }
        return result;
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
        if (buffer.length == bufferNextWrite) 
        {
            final byte[] nextBuffer = new byte[2*buffer.length];
            System.arraycopy(buffer, 0, nextBuffer, 0, buffer.length);
            buffer = nextBuffer;
        }
        buffer[bufferNextWrite++] = data;
        previous = current;
        current = current.getNode( data );
        if (current == null) 
        {
            final long code;
            if ( previous == root ) 
            {
                code = data & 0xFF;
            } 
            else 
            {
                code = nextCode++;
            }
            current = new LZWNode( code );
            previous.setNode( data, current );
            byte[] sav = new byte[bufferNextWrite];
            System.arraycopy(buffer, 0, sav, 0, bufferNextWrite);
            codeToData.put( code,  sav);

            /**
            System.out.print( "Adding " + code + "='" );
            for( int i=0; i<bufferNextWrite; i++ )
            {
                String hex = Integer.toHexString( ((buffer[i]&0xFF );
                if( hex.length() <=1 )
                {
                    hex = "0" + hex;
                }
                if( i != bufferNextWrite -1 )
                {
                    hex += " ";
                }
                System.out.print( hex.toUpperCase() );
            }
            System.out.println( "'" );
            **/
            bufferNextWrite = 0;
            current = root;
            visit(data);
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
        if ( nextCode < 512) 
        {
            codeSize = 9;
        } 
        else if ( nextCode < 1024 ) 
        {
            codeSize = 10;
        } 
        else if ( nextCode < 2048 ) 
        {
            codeSize = 11;
        } 
        else 
        {
            codeSize = 12;
        }
    }

    /**
     * This will clear the internal buffer that the dictionary uses.
     */
    public void clear()
    {
        bufferNextWrite = 0;
        current = root;
        previous = null;
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
        LZWNode result = root.getNode( data );
        if (result == null && data.length == 1) 
        {
            result = addRootNode(data[0]);
        }
        return result;
    }

    private LZWNode addRootNode( byte b) 
    {
        long code = b & 0xFF;
        LZWNode result = new LZWNode( code );
        root.setNode( b, result );
        codeToData.put( code, new byte[] { b } );
        return result;
    }
}
