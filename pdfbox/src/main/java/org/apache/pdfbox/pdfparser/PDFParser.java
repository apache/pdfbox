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
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdfparser.XrefTrailerResolver.XRefType;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.fdf.FDFDocument;
import org.apache.pdfbox.persistence.util.COSObjectKey;

/**
 * This class will handle the parsing of the PDF document.
 *
 * @author Ben Litchfield
 */
public class PDFParser extends BaseParser
{

    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(PDFParser.class);

    private static final String PDF_HEADER = "%PDF-";
    private static final String FDF_HEADER = "%FDF-";
    
    protected boolean isFDFDocment = false;
    
    private static final String PDF_DEFAULT_VERSION = "1.4";
    private static final String FDF_DEFAULT_VERSION = "1.0";

    /** 
     * Collects all Xref/trailer objects and resolves them into single
     * object using startxref reference. 
     */
    protected XrefTrailerResolver xrefTrailerResolver = new XrefTrailerResolver();

    /**
     * Constructor.
     *
     * @param input The input stream that contains the PDF document.
     *
     * @throws IOException If there is an error initializing the stream.
     */
    public PDFParser( InputStream input ) throws IOException
    {
        super(input);
    }

    protected void parseHeader() throws IOException
    {
        // read first line
        String header = readLine();
        // some pdf-documents are broken and the pdf-version is in one of the following lines
        if (!header.contains(PDF_HEADER) && !header.contains(FDF_HEADER))
        {
            header = readLine();
            while (!header.contains(PDF_HEADER) && !header.contains(FDF_HEADER))
            {
                // if a line starts with a digit, it has to be the first one with data in it
                if ((header.length() > 0) && (Character.isDigit(header.charAt(0))))
                {
                    break;
                }
                header = readLine();
            }
        }

        // nothing found
        if (!header.contains(PDF_HEADER) && !header.contains(FDF_HEADER))
        {
            throw new IOException( "Error: Header doesn't contain versioninfo" );
        }

        //sometimes there is some garbage in the header before the header
        //actually starts, so lets try to find the header first.
        int headerStart = header.indexOf( PDF_HEADER );
        if (headerStart == -1)
        {
            headerStart = header.indexOf(FDF_HEADER);
        }

        //greater than zero because if it is zero then
        //there is no point of trimming
        if ( headerStart > 0 )
        {
            //trim off any leading characters
            header = header.substring( headerStart, header.length() );
        }

        /*
         * This is used if there is garbage after the header on the same line
         */
        if (header.startsWith(PDF_HEADER))
        {
            if (!header.matches(PDF_HEADER + "\\d.\\d"))
            {

                if (header.length() < PDF_HEADER.length() + 3)
                {
                    // No version number at all, set to 1.4 as default
                    header = PDF_HEADER + PDF_DEFAULT_VERSION;
                    LOG.debug("No pdf version found, set to " + PDF_DEFAULT_VERSION + " as default.");
                }
                else
                {
                    String headerGarbage = header.substring(PDF_HEADER.length() + 3, header.length()) + "\n";
                    header = header.substring(0, PDF_HEADER.length() + 3);
                    pdfSource.unread(headerGarbage.getBytes(ISO_8859_1));
                }
            }
        }
        else
        {
            isFDFDocment = true;
            if (!header.matches(FDF_HEADER + "\\d.\\d"))
            {
                if (header.length() < FDF_HEADER.length() + 3)
                {
                    // No version number at all, set to 1.0 as default
                    header = FDF_HEADER + FDF_DEFAULT_VERSION;
                    LOG.debug("No fdf version found, set to " + FDF_DEFAULT_VERSION + " as default.");
                }
                else
                {
                    String headerGarbage = header.substring(FDF_HEADER.length() + 3, header.length()) + "\n";
                    header = header.substring(0, FDF_HEADER.length() + 3);
                    pdfSource.unread(headerGarbage.getBytes(ISO_8859_1));
                }
            }
        }
        document.setHeaderString(header);

        try
        {
            if (header.startsWith( PDF_HEADER ))
            {
                float pdfVersion = Float. parseFloat(
                        header.substring( PDF_HEADER.length(), Math.min( header.length(), PDF_HEADER .length()+3) ) );
                document.setVersion( pdfVersion );
            }
            else
            {
                float pdfVersion = Float. parseFloat(
                        header.substring( FDF_HEADER.length(), Math.min( header.length(), FDF_HEADER.length()+3) ) );
                document.setVersion( pdfVersion );
            }
        }
        catch ( NumberFormatException e )
        {
            throw new IOException( "Error getting pdf version: " + e.getMessage(), e );
        }
    }

    /**
     * This will get the document that was parsed.  parse() must be called before this is called.
     * When you are done with this document you must call close() on it to release
     * resources.
     *
     * @return The document that was parsed.
     *
     * @throws IOException If there is an error getting the document.
     */
    public COSDocument getDocument() throws IOException
    {
        if( document == null )
        {
            throw new IOException( "You must call parse() before calling getDocument()" );
        }
        return document;
    }

    /**
     * This will get the PD document that was parsed.  When you are done with
     * this document you must call close() on it to release resources.
     *
     * @return The document at the PD layer.
     *
     * @throws IOException If there is an error getting the document.
     */
    public PDDocument getPDDocument() throws IOException
    {
        return new PDDocument( getDocument(), this );
    }

    /**
     * This will get the FDF document that was parsed.  When you are done with
     * this document you must call close() on it to release resources.
     *
     * @return The document at the PD layer.
     *
     * @throws IOException If there is an error getting the document.
     */
    public FDFDocument getFDFDocument() throws IOException
    {
        return new FDFDocument( getDocument() );
    }

    /**
     * This will parse the startxref section from the stream.
     * The startxref value is ignored.
     *
     * @return false on parsing error
     * @throws IOException If an IO error occurs.
     */
    protected boolean parseStartXref() throws IOException
    {
        if(pdfSource.peek() != 's')
        {
            return false;
        }
        String startXRef = readString();
        if( !startXRef.trim().equals( "startxref" ) )
        {
            return false;
        }
        skipSpaces();
        /* This integer is the byte offset of the first object referenced by the xref or xref stream
         * Needed for the incremental update (PREV)
         */
        getDocument().setStartXref(readLong());
        return true;
    }


    /**
     * This will parse the xref table from the stream and add it to the state
     * The XrefTable contents are ignored.
     * @param startByteOffset the offset to start at
     * @return false on parsing error
     * @throws IOException If an IO error occurs.
     */
    protected boolean parseXrefTable( long startByteOffset ) throws IOException
    {
        if(pdfSource.peek() != 'x')
        {
            return false;
        }
        String xref = readString();
        if( !xref.trim().equals( "xref" ) )
        {
            return false;
        }
        
        // check for trailer after xref
        String str = readString();
        byte[] b = str.getBytes(ISO_8859_1);
        pdfSource.unread(b, 0, b.length);
        
        // signal start of new XRef
        xrefTrailerResolver.nextXrefObj( startByteOffset, XRefType.TABLE );

        if (str.startsWith("trailer"))
        {
            LOG.warn("skipping empty xref table");
            return false;
        }
        
        /*
         * Xref tables can have multiple sections.
         * Each starts with a starting object id and a count.
         */
        while(true)
        {
            long currObjID = readObjectNumber(); // first obj id
            long count = readLong(); // the number of objects in the xref table
            skipSpaces();
            for(int i = 0; i < count; i++)
            {
                if(pdfSource.isEOF() || isEndOfName((char)pdfSource.peek()))
                {
                    break;
                }
                if(pdfSource.peek() == 't')
                {
                    break;
                }
                //Ignore table contents
                String currentLine = readLine();
                String[] splitString = currentLine.split("\\s");
                if (splitString.length < 3)
                {
                    LOG.warn("invalid xref line: " + currentLine);
                    break;
                }
                /* This supports the corrupt table as reported in
                 * PDFBOX-474 (XXXX XXX XX n) */
                if(splitString[splitString.length-1].equals("n"))
                {
                    try
                    {
                        long currOffset = Long.parseLong(splitString[0]);
                        int currGenID = Integer.parseInt(splitString[1]);
                        COSObjectKey objKey = new COSObjectKey(currObjID, currGenID);
                        xrefTrailerResolver.setXRef(objKey, currOffset);
                    }
                    catch(NumberFormatException e)
                    {
                        throw new IOException(e);
                    }
                }
                else if(!splitString[2].equals("f"))
                {
                    throw new IOException("Corrupt XRefTable Entry - ObjID:" + currObjID);
                }
                currObjID++;
                skipSpaces();
            }
            skipSpaces();
            char c = (char)pdfSource.peek();
            if(c < '0' || c > '9')
            {
                break;
            }
        }
        return true;
    }

    /**
     * This will parse the trailer from the stream and add it to the state.
     *
     * @return false on parsing error
     * @throws IOException If an IO error occurs.
     */
    protected boolean parseTrailer() throws IOException
    {
        if(pdfSource.peek() != 't')
        {
            return false;
        }
        //read "trailer"
        String nextLine = readLine();
        if( !nextLine.trim().equals( "trailer" ) )
        {
            // in some cases the EOL is missing and the trailer immediately
            // continues with "<<" or with a blank character
            // even if this does not comply with PDF reference we want to support as many PDFs as possible
            // Acrobat reader can also deal with this.
            if (nextLine.startsWith("trailer"))
            {
                byte[] b = nextLine.getBytes(ISO_8859_1);
                int len = "trailer".length();
                pdfSource.unread('\n');
                pdfSource.unread(b, len, b.length-len);
            }
            else
            {
                return false;
            }
        }

        // in some cases the EOL is missing and the trailer continues with " <<"
        // even if this does not comply with PDF reference we want to support as many PDFs as possible
        // Acrobat reader can also deal with this.
        skipSpaces();

        COSDictionary parsedTrailer = parseCOSDictionary();
        xrefTrailerResolver.setTrailer( parsedTrailer );

        // The version can also be specified within the document /Catalog
        readVersionInTrailer(parsedTrailer);

        skipSpaces();
        return true;
    }

    /**
     * The document catalog can also have a /Version parameter which overrides the version specified
     * in the header if, and only if it is greater.
     *
     * @param parsedTrailer the parsed catalog in the trailer
     */
    protected void readVersionInTrailer(COSDictionary parsedTrailer)
    {
        COSObject root = (COSObject) parsedTrailer.getItem(COSName.ROOT);
        if (root != null)
        {
            COSBase item = root.getItem(COSName.VERSION);
            if (item instanceof COSName)
            {
                COSName version = (COSName) item;
                float trailerVersion = Float.valueOf(version.getName());
                if (trailerVersion > document.getVersion())
                {
                    document.setVersion(trailerVersion);
                }
            }
            else if (item != null)
            {
                LOG.warn("Incorrect /Version entry is ignored: " + item);
            }
        }
    }

    /**
     * Fills XRefTrailerResolver with data of given stream.
     * Stream must be of type XRef.
     * @param stream the stream to be read
     * @param objByteOffset the offset to start at
     * @param isStandalone should be set to true if the stream is not part of a hybrid xref table
     * @throws IOException if there is an error parsing the stream
     */
    public void parseXrefStream( COSStream stream, long objByteOffset, boolean isStandalone ) throws IOException
    {
        // the cross reference stream of a hybrid xref table will be added to the existing one
        // and we must not override the offset and the trailer
        if ( isStandalone )
        {
            xrefTrailerResolver.nextXrefObj( objByteOffset, XRefType.STREAM );
            xrefTrailerResolver.setTrailer( stream );
        }        
        PDFXrefStreamParser parser =
                new PDFXrefStreamParser( stream, document, xrefTrailerResolver );
        parser.parse();
    }

}
