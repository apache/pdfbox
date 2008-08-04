/**
 * Copyright (c) 2003-2005, www.pdfbox.org
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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.StreamCorruptedException;

import org.pdfbox.cos.COSDictionary;

import org.pdfbox.io.NBitInputStream;
import org.pdfbox.io.NBitOutputStream;

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
    public void decode( InputStream compressedData, OutputStream result, COSDictionary options, int filterIndex ) throws IOException
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
    public void encode( InputStream rawData, OutputStream result, COSDictionary options, int filterIndex ) throws IOException
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