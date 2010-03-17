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
package org.apache.pdfbox.examples;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.pdfbox.cos.COSDocument;

import org.apache.pdfbox.pdmodel.PDDocument;

import org.apache.pdfbox.pdfwriter.COSWriter;

/**
 * A simple class which has some methods used by all examples.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.4 $
 */
public abstract class AbstractExample
{
    /**
     * Close the stream.
     *
     * @param stream The stream to close.
     *
     * @throws IOException If there is an error closing the stream.
     */
    public void close( InputStream stream ) throws IOException
    {
        if( stream != null )
        {
            stream.close();
        }
    }

    /**
     * Close the stream.
     *
     * @param stream The stream to close.
     *
     * @throws IOException If there is an error closing the stream.
     */
    public void close( OutputStream stream ) throws IOException
    {
        if( stream != null )
        {
            stream.close();
        }
    }

    /**
     * Close the document.
     *
     * @param doc The doc to close.
     *
     * @throws IOException If there is an error closing the document.
     */
    public void close( COSDocument doc ) throws IOException
    {
        if( doc != null )
        {
            doc.close();
        }
    }

    /**
     * Close the document.
     *
     * @param doc The doc to close.
     *
     * @throws IOException If there is an error closing the document.
     */
    public void close( PDDocument doc ) throws IOException
    {
        if( doc != null )
        {
            doc.close();
        }
    }

    /**
     * Close the writer.
     *
     * @param writer The writer to close.
     *
     * @throws IOException If there is an error closing the writer.
     */
    public static void close( COSWriter writer ) throws IOException
    {
        if( writer != null )
        {
            writer.close();
        }
    }
}
