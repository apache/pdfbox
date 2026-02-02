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
package org.apache.pdfbox.pdfparser;

import java.io.IOException;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdmodel.fdf.FDFDocument;

public class FDFParser extends COSParser
{
    /**
     * Constructs parser for given file using memory buffer.
     * 
     * @param source the source of the pdf to be parsed
     * 
     * @throws IOException If something went wrong.
     */
    public FDFParser(RandomAccessRead source) throws IOException
    {
        super(source);
    }

    /**
     * The initial parse will first parse only the trailer, the xrefstart and all xref tables to have a pointer (offset)
     * to all the pdf's objects. It can handle linearized pdfs, which will have an xref at the end pointing to an xref
     * at the beginning of the file. Last the root object is parsed.
     * 
     * @throws IOException If something went wrong.
     */
    private void initialParse() throws IOException
    {
        COSDictionary trailer = retrieveTrailer();
    
        COSDictionary root = trailer.getCOSDictionary(COSName.ROOT);
        if (root == null)
        {
            throw new IOException("Missing root object specification in trailer.");
        }
        initialParseDone = true;
    }

    /**
     * This will parse the stream and populate the FDFDocument object.
     *
     * @return the parsed FDFDocument
     * @throws IOException If there is an error reading from the stream or corrupt data is found.
     */
    public FDFDocument parse() throws IOException
    {
         // set to false if all is processed
         boolean exceptionOccurred = true; 
         try
         {
            if (!parseFDFHeader())
            {
                throw new IOException( "Error: Header doesn't contain versioninfo" );
            }
            initialParse();
            exceptionOccurred = false;
            return new FDFDocument(document, source);
        }
        finally
        {
            if (exceptionOccurred && document != null)
            {
                IOUtils.closeQuietly(document);
                document = null;
            }
        }
    }
}
