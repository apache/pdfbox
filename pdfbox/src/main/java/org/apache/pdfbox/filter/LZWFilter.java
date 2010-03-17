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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.StreamCorruptedException;

import org.apache.pdfbox.cos.COSDictionary;

import org.apache.pdfbox.io.NBitInputStream;
import org.apache.pdfbox.io.NBitOutputStream;

/**
 * This is the used for the LZWDecode filter.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.15 $
 */
public class LZWFilter implements Filter
{

    /**
     * The LZW clear table code.
     */
    public static final long CLEAR_TABLE = 256;
    /**
     * The LZW end of data code.
     */
    public static final long EOD = 257;

    /**
     * {@inheritDoc}
     */
    public void decode( InputStream compressedData, OutputStream result, COSDictionary options, int filterIndex ) 
        throws IOException
    {
        //log.debug("decode( )");
        NBitInputStream in = null;
        in = new NBitInputStream( compressedData );
        in.setBitsInChunk( 9 );
        LZWDictionary dic = new LZWDictionary();
        byte firstByte = 0;
        long nextCommand = 0;
        while( (nextCommand = in.read() ) != EOD )
        {
            // log.debug( "decode - nextCommand=" + nextCommand + ", bitsInChunk: " + in.getBitsInChunk());

            if( nextCommand == CLEAR_TABLE )
            {
                in.setBitsInChunk( 9 );
                dic = new LZWDictionary();
            }
            else
            {
                byte[] data = dic.getData( nextCommand );
                if( data == null )
                {
                    dic.visit( firstByte );
                    data = dic.getData( nextCommand );
                    dic.clear();
                }
                if( data == null )
                {
                    throw new StreamCorruptedException( "Error: data is null" );
                }
                dic.visit(data);

                //log.debug( "decode - dic.getNextCode(): " + dic.getNextCode());

                if( dic.getNextCode() >= 2047 )
                {
                    in.setBitsInChunk( 12 );
                }
                else if( dic.getNextCode() >= 1023 )
                {
                    in.setBitsInChunk( 11 );
                }
                else if( dic.getNextCode() >= 511 )
                {
                    in.setBitsInChunk( 10 );
                }
                else
                {
                    in.setBitsInChunk( 9 );
                }
                /**
                if( in.getBitsInChunk() != dic.getCodeSize() )
                {
                    in.unread( nextCommand );
                    in.setBitsInChunk( dic.getCodeSize() );
                    System.out.print( "Switching " + nextCommand + " to " );
                    nextCommand = in.read();
                    System.out.println( "" +  nextCommand );
                    data = dic.getData( nextCommand );
                }**/
                firstByte = data[0];
                result.write( data );
            }
        }
        result.flush();
    }


    /**
     * {@inheritDoc}
     */
    public void encode( InputStream rawData, OutputStream result, COSDictionary options, int filterIndex ) 
        throws IOException
    {
        //log.debug("encode( )");
        PushbackInputStream input = new PushbackInputStream( rawData, 4096 );
        LZWDictionary dic = new LZWDictionary();
        NBitOutputStream out = new NBitOutputStream( result );
        out.setBitsInChunk( 9 ); //initially nine
        out.write( CLEAR_TABLE );
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int byteRead = 0;
        for( int i=0; (byteRead = input.read()) != -1; i++ )
        {
            //log.debug( "byteRead = '" + (char)byteRead + "' (0x" + Integer.toHexString(byteRead) + "), i=" + i);
            buffer.write( byteRead );
            dic.visit( (byte)byteRead );
            out.setBitsInChunk( dic.getCodeSize() );

            //log.debug( "Getting node '" + new String( buffer.toByteArray() ) + "', buffer.size = " + buffer.size() );
            LZWNode node = dic.getNode( buffer.toByteArray() );
            int nextByte = input.read();
            if( nextByte != -1 )
            {
                //log.debug( "nextByte = '" + (char)nextByte + "' (0x" + Integer.toHexString(nextByte) + ")");
                LZWNode next = node.getNode( (byte)nextByte );
                if( next == null )
                {
                    //log.debug("encode - No next node, writing node and resetting buffer (" +
                    //          " node.getCode: " + node.getCode() + ")" +
                    //          " bitsInChunk: " + out.getBitsInChunk() +
                    //          ")");
                    out.write( node.getCode() );
                    buffer.reset();
                }

                input.unread( nextByte );
            }
            else
            {
                //log.debug("encode - EOF on lookahead: writing node, resetting buffer, and terminating read loop (" +
                //          " node.getCode: " + node.getCode() + ")" +
                //          " bitsInChunk: " + out.getBitsInChunk() +
                //          ")");
                out.write( node.getCode() );
                buffer.reset();
                break;
            }

            if( dic.getNextCode() == 4096 )
            {
                //log.debug("encode - Clearing dictionary and unreading pending buffer data (" +
                //          " bitsInChunk: " + out.getBitsInChunk() +
                //          ")");
                out.write( CLEAR_TABLE );
                dic = new LZWDictionary();
                input.unread( buffer.toByteArray() );
                buffer.reset();
            }
        }

        // Fix the code size based on the fact that we are writing the EOD
        //
        if( dic.getNextCode() >= 2047 )
        {
            out.setBitsInChunk( 12 );
        }
        else if( dic.getNextCode() >= 1023 )
        {
            out.setBitsInChunk( 11 );
        }
        else if( dic.getNextCode() >= 511 )
        {
            out.setBitsInChunk( 10 );
        }
        else
        {
            out.setBitsInChunk( 9 );
        }

        //log.debug("encode - Writing EOD (" +
        //          " bitsInChunk: " + out.getBitsInChunk() +
        //          ")");
        out.write( EOD );
        out.close();
        result.flush();
    }
}
