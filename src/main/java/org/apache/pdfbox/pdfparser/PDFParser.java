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

import java.io.File;
import java.io.InputStream;
import java.io.IOException;

import java.util.Iterator;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.exceptions.WrappedIOException;
import org.apache.pdfbox.io.RandomAccess;

import org.apache.pdfbox.pdmodel.PDDocument;

import org.apache.pdfbox.pdmodel.fdf.FDFDocument;

import org.apache.pdfbox.persistence.util.COSObjectKey;

/**
 * This class will handle the parsing of the PDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.53 $
 */
public class PDFParser extends BaseParser
{
    private static final int SPACE_BYTE = 32;

    private static final String PDF_HEADER = "%PDF-";
    private COSDocument document;

    /**
     * Temp file directory.
     */
    private File tempDirectory = null;

    private RandomAccess raf = null;

    /**
     * Constructor.
     *
     * @param input The input stream that contains the PDF document.
     *
     * @throws IOException If there is an error initializing the stream.
     */
    public PDFParser( InputStream input ) throws IOException
    {
        this(input, null);
    }

    /**
     * Constructor to allow control over RandomAccessFile.
     * @param input The input stream that contains the PDF document.
     * @param rafi The RandomAccessFile to be used in internal COSDocument
     *
     * @throws IOException If there is an error initializing the stream.
     */
    public PDFParser(InputStream input, RandomAccess rafi)
        throws IOException
    {
        super(input);
        this.raf = rafi;
    }

    /**
     * This is the directory where pdfbox will create a temporary file
     * for storing pdf document stream in.  By default this directory will
     * be the value of the system property java.io.tmpdir.
     *
     * @param tmpDir The directory to create scratch files needed to store
     *        pdf document streams.
     */
    public void setTempDirectory( File tmpDir )
    {
        tempDirectory = tmpDir;
    }

    /**
     * This will parse the stream and populate the COSDocument object.  This will close
     * the stream when it is done parsing.
     *
     * @throws IOException If there is an error reading from the stream or corrupt data
     * is found.
     */
    public void parse() throws IOException
    {
        try
        {
            if ( raf == null )
            {
                if( tempDirectory != null )
                {
                    document = new COSDocument( tempDirectory );
                }
                else
                {
                    document = new COSDocument();
                }
            }
            else
            {
                document = new COSDocument( raf );
            }
            setDocument( document );

            parseHeader();
            
            skipHeaderFillBytes();

            boolean wasLastParsedObjectEOF = false;
            try{
                while(true){
                    if(pdfSource.isEOF()){
                        break;
                    }
                    wasLastParsedObjectEOF = parseObject();
                    
                    skipSpaces();
                }
                //Test if we saw a trailer section. If not, look for an XRef Stream (Cross-Reference Stream) 
                //For PDF 1.5 and above 
                if( document.getTrailer() == null ){
                    COSDictionary trailer = new COSDictionary();
                    Iterator xrefIter = document.getObjectsByType( "XRef" ).iterator();
                    while( xrefIter.hasNext() )
                    {
                        COSStream next = (COSStream)((COSObject)xrefIter.next()).getObject();
                        trailer.addAll( next );
                    }
                    document.setTrailer( trailer );
                }
                if( !document.isEncrypted() )
                {
                    document.dereferenceObjectStreams();
                }
            }
            catch( IOException e ){
                /*
                 * PDF files may have random data after the EOF marker. Ignore errors if
                 * last object processed is EOF. 
                 */
                if( !wasLastParsedObjectEOF ){
                    throw e;
                } 
            }
        }
        catch( Throwable t )
        {
            //so if the PDF is corrupt then close the document and clear
            //all resources to it
            if( document != null )
            {
                document.close();
            }
            if( t instanceof IOException )
            {
                throw (IOException)t;
            }
            else
            {
                throw new WrappedIOException( t );
            }
        }
        finally
        {
            pdfSource.close();
        }
    }

    private   void  parseHeader()  throws  IOException
    {
    	// read first line
    	String header = readLine();
    	// some pdf-documents are broken and the pdf-version is in one of the following lines
        if (header.indexOf( PDF_HEADER ) == -1)
        {
                header = readLine();
                while (header.indexOf( PDF_HEADER ) == -1)
                {
                	// if a line starts with a digit, it has to be the first one with data in it
                	if (Character. isDigit (header.charAt(0)))
                		break ;
                	header = readLine();
                }
        }

        // nothing found
        if (header.indexOf( PDF_HEADER ) == -1)
        {
            throw new IOException( "Error: Header doesn't contain versioninfo" );
        }

        document .setHeaderString( header );

        //sometimes there are some garbage bytes in the header before the header
        //actually starts, so lets try to find the header first.
        int headerStart = header.indexOf( PDF_HEADER );

        //greater than zero because if it is zero then
        //there is no point of trimming
        if ( headerStart > 0 )
        {
            //trim off any leading characters
            header = header.substring( headerStart, header.length() );
        }

        try
        {
            float pdfVersion = Float. parseFloat (
                header.substring( PDF_HEADER .length(), Math. min ( header.length(), PDF_HEADER .length()+3) ) );
            document .setVersion( pdfVersion );
        }
        catch ( NumberFormatException e )
        {
            throw new IOException( "Error getting pdf version:" + e );
        }
    } 
    /**
     * This will skip a header's binary fill bytes.  This is in accordance to
     * PDF Specification 1.5 pg 68 section 3.4.1 "Syntax.File Structure.File Header"
     *
     * @throws IOException If there is an error reading from the stream.
    */
    protected void skipHeaderFillBytes() throws IOException
    {
        skipSpaces();
        int c = pdfSource.peek();

        if( !Character.isDigit( (char)c ))
        {
            // Fill bytes conform with PDF reference (but without comment sign)
            // => skip until EOL
            readLine();
        }
        // else: no fill bytes
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
        return new PDDocument( getDocument() );
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
     * This will parse the next object from the stream and add it to 
     * the local state. 
     *
     * @return Returns true if the processed object had an endOfFile marker
     *
     * @throws IOException If an IO error occurs.
     */
    private boolean parseObject() throws IOException{
        boolean isEndOfFile = false; 
        skipSpaces();
        //peek at the next character to determine the type of object we are parsing
        char peekedChar = (char)pdfSource.peek();
        
        //ignore endobj and endstream sections.
        while( peekedChar == 'e' ){
            //there are times when there are multiple endobj, so lets
            //just read them and move on.
            readString();
            skipSpaces();
            peekedChar = (char)pdfSource.peek();
        }
        if( pdfSource.isEOF()){
            //"Skipping because of EOF" );
            //end of file we will return a false and call it a day.
        }
        //xref table. Note: The contents of the Xref table are currently ignored
        else if( peekedChar == 'x') {
            parseXrefTable();
        }
        // Note: startxref can occur in either a trailer section or by itself 
        else if (peekedChar == 't' || peekedChar == 's') {
            if(peekedChar == 't'){
                parseTrailer();
                peekedChar = (char)pdfSource.peek(); 
            }
            if (peekedChar == 's'){  
                parseStartXref();
                //verify that EOF exists 
                String eof = readExpectedString( "%%EOF" );
                if( eof.indexOf( "%%EOF" )== -1 && !pdfSource.isEOF() )
                {
                    throw new IOException( "expected='%%EOF' actual='" + eof + "' next=" + readString() +
                            " next=" +readString() );
                }
                isEndOfFile = true; 
            }
        }
        //we are going to parse an normal object
        else
        {
            int number = -1;
            int genNum = -1;
            String objectKey = null;
            boolean missingObjectNumber = false;
            try
            {
                char peeked = (char)pdfSource.peek();
                if( peeked == '<' )
                {
                    missingObjectNumber = true;
                }
                else
                {
                    number = readInt();
                }
            }
            catch( IOException e )
            {
                //ok for some reason "GNU Ghostscript 5.10" puts two endobj
                //statements after an object, of course this is nonsense
                //but because we want to support as many PDFs as possible
                //we will simply try again
                number = readInt();
            }
            if( !missingObjectNumber )
            {
                skipSpaces();
                genNum = readInt();

                objectKey = readString( 3 );
                //System.out.println( "parseObject() num=" + number +
                //" genNumber=" + genNum + " key='" + objectKey + "'" );
                if( !objectKey.equals( "obj" ) )
                {
                    throw new IOException("expected='obj' actual='" + objectKey + "' " + pdfSource);
                }
            }
            else
            {
                number = -1;
                genNum = -1;
            }

            skipSpaces();
            COSBase pb = parseDirObject();
            String endObjectKey = readString();
            
            if( endObjectKey.equals( "stream" ) )
            {
                pdfSource.unread( endObjectKey.getBytes() );
                pdfSource.unread( ' ' );
                if( pb instanceof COSDictionary )
                {
                    pb = parseCOSStream( (COSDictionary)pb, getDocument().getScratchFile() );
                }
                else
                {
                    // this is not legal
                    // the combination of a dict and the stream/endstream forms a complete stream object
                    throw new IOException("stream not preceded by dictionary");
                }
                endObjectKey = readString();
            }
            COSObjectKey key = new COSObjectKey( number, genNum );
            COSObject pdfObject = document.getObjectFromPool( key );
            pdfObject.setObject(pb);

            if( !endObjectKey.equals( "endobj" ) )
            {
                if( !pdfSource.isEOF() )
                {
                    try{
                        //It is possible that the endobj  is missing, there
                        //are several PDFs out there that do that so skip it and move on.
                        Float.parseFloat( endObjectKey );
                        pdfSource.unread( SPACE_BYTE );
                        pdfSource.unread( endObjectKey.getBytes() );
                    }
                    catch( NumberFormatException e )
                    {
                        //we will try again incase there was some garbage which
                        //some writers will leave behind.
                        String secondEndObjectKey = readString();
                        if( !secondEndObjectKey.equals( "endobj" ) )
                        {
                            if( isClosing() )
                            {
                                //found a case with 17506.pdf object 41 that was like this
                                //41 0 obj [/Pattern /DeviceGray] ] endobj
                                //notice the second array close, here we are reading it
                                //and ignoring and attempting to continue
                                pdfSource.read();
                            }
                            skipSpaces();
                            String thirdPossibleEndObj = readString();
                            if( !thirdPossibleEndObj.equals( "endobj" ) )
                            {
                                throw new IOException("expected='endobj' firstReadAttempt='" + endObjectKey + "' " +
                                    "secondReadAttempt='" + secondEndObjectKey + "' " + pdfSource);
                            }
                        }
                    }
                }
            }
            skipSpaces();
        }
        return isEndOfFile;
    }

    /**
     * This will parse the startxref section from the stream.
     * The startxref value is ignored.
     *            
     * @return false on parsing error 
     * @throws IOException If an IO error occurs.
     */
    private boolean parseStartXref() throws IOException{
        if(pdfSource.peek() != 's'){
            return false; 
        }
        String nextLine = readLine();
        if( !nextLine.equals( "startxref" ) ) {
            return false;
        }
        skipSpaces();
        /* This integer is the byte offset of the first object referenced by the xref or xref stream
         * Not needed for PDFbox
         */
        readInt();
        return true;
    }


    /**
     * This will parse the xref table from the stream and add it to the state
     * The XrefTable contents are ignored.
     *            
     * @return false on parsing error 
     * @throws IOException If an IO error occurs.
     */
    private boolean parseXrefTable() throws IOException{
        if(pdfSource.peek() != 'x'){
            return false;
        }
        String nextLine = readLine();
        if( !nextLine.equals( "xref" ) ) {
            return false;
        }
        /*
         * Xref tables can have multiple sections. 
         * Each starts with a starting object id and a count.
         */
        while(true){
            int start = readInt(); // first obj id
            int count = readInt(); // the number of objects in the xref table
            skipSpaces();
            for(int i = 0; i < count; i++){
                if(pdfSource.isEOF() || isEndOfName((char)pdfSource.peek())){
                    break;
                }
                if(pdfSource.peek() == 't'){
                    break;
                }
                //Ignore table contents
                readLine();
                skipSpaces();
            }
            addXref(new PDFXref(start, count));
            skipSpaces();
            char c = (char)pdfSource.peek();
            if(c < '0' || c > '9'){
                break;
            }
        }
        return true;
    }

    /**
     * This will parse the trailer from the stream and add it to the state
     *            
     * @return false on parsing error
     * @throws IOException If an IO error occurs.
     */
    private boolean parseTrailer() throws IOException
    {
        if(pdfSource.peek() != 't'){
            return false;
        }
        //read "trailer"
        String nextLine = readLine();
        if( !nextLine.equals( "trailer" ) ) {
            return false;
        }
        COSDictionary parsedTrailer = parseCOSDictionary();
        COSDictionary docTrailer = document.getTrailer();
        if( docTrailer == null )
        {
            document.setTrailer( parsedTrailer );
        }
        else
        {
            docTrailer.addAll( parsedTrailer );
        }
        skipSpaces();
        return true;
    }
}
