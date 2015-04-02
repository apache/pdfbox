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
package org.apache.pdfbox.pdfwriter;

import java.io.IOException;
import java.io.OutputStream;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSBoolean;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;

import org.apache.pdfbox.util.ImageParameters;
import org.apache.pdfbox.util.PDFOperator;

/**
 * A class that will take a list of tokens and write out a stream with them.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.8 $
 */
public class ContentStreamWriter
{
    private OutputStream output;
    /**
     * space character.
     */
    public static final byte[] SPACE = new byte[] { 32 };

    /**
     * standard line separator
     */
    public static final byte[] EOL = new byte[] { 0x0A };

    /**
     * This will create a new content stream writer.
     *
     * @param out The stream to write the data to.
     */
    public ContentStreamWriter( OutputStream out )
    {
        output = out;
    }

    /**
     * This will write out the list of tokens to the stream.
     *
     * @param tokens The tokens to write to the stream.
     * @param start The start index into the list of tokens.
     * @param end The end index into the list of tokens.
     * @throws IOException If there is an error writing to the stream.
     */
    public void writeTokens( List tokens, int start, int end ) throws IOException
    {
        for( int i=start; i<end; i++ )
        {
            Object o = tokens.get( i );
            writeObject( o );
            //write a space between each object.
            output.write( 32 );
        }
        output.flush();
    }

    private void writeObject( Object o ) throws IOException
    {
        if( o instanceof COSString )
        {
            ((COSString)o).writePDF( output );
        }
        else if( o instanceof COSFloat )
        {
            ((COSFloat)o).writePDF( output );
        }
        else if( o instanceof COSInteger )
        {
            ((COSInteger)o).writePDF( output );
        }
        else if( o instanceof COSBoolean )
        {
            ((COSBoolean)o).writePDF( output );
        }
        else if( o instanceof COSName )
        {
            ((COSName)o).writePDF( output );
        }
        else if( o instanceof COSArray )
        {
            COSArray array = (COSArray)o;
            output.write(COSWriter.ARRAY_OPEN);
            for( int i=0; i<array.size(); i++ )
            {
                writeObject( array.get( i ) );
                output.write( SPACE );
            }

            output.write( COSWriter.ARRAY_CLOSE );
        }
        else if( o instanceof COSDictionary )
        {
            COSDictionary obj = (COSDictionary)o;
            output.write( COSWriter.DICT_OPEN );
            for (Map.Entry<COSName, COSBase> entry : obj.entrySet())
            {
                if (entry.getValue() != null)
                {
                    writeObject( entry.getKey() );
                    output.write( SPACE );
                    writeObject( entry.getValue() );
                    output.write( SPACE );
                }
            }
            output.write( COSWriter.DICT_CLOSE );
            output.write( SPACE );
        }
        else if( o instanceof PDFOperator )
        {
            PDFOperator op = (PDFOperator)o;
            if( op.getOperation().equals( "BI" ) )
            {
                output.write( "BI".getBytes("ISO-8859-1") );
                ImageParameters params = op.getImageParameters();
                COSDictionary dic = params.getDictionary();
                for( COSName key : dic.keySet() )
                {
                    Object value = dic.getDictionaryObject( key );
                    key.writePDF( output );
                    output.write( SPACE );
                    writeObject( value );
                    output.write( EOL );
                }
                output.write( "ID".getBytes("ISO-8859-1") );
                output.write( EOL );
                output.write( op.getImageData() );
            }
            else
            {
                output.write( op.getOperation().getBytes("ISO-8859-1") );
                output.write( EOL );
            }
        }
        else
        {
            throw new IOException( "Error:Unknown type in content stream:" + o );
        }
    }

    /**
     * This will write out the list of tokens to the stream.
     *
     * @param tokens The tokens to write to the stream.
     * @throws IOException If there is an error writing to the stream.
     */
    public void writeTokens( List tokens ) throws IOException
    {
        writeTokens( tokens, 0, tokens.size() );
    }
}
