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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSObject;
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

    /**
     * Log instance.
     */
    private static final Log log = LogFactory.getLog(PDFParser.class);

    private static final int SPACE_BYTE = 32;

    private static final String PDF_HEADER = "%PDF-";
    private static final String FDF_HEADER = "%FDF-";
    private boolean forceParsing = false; 
    
    /**
     * A list of duplicate objects found when Parsing the PDF
     * File. 
     */
    private List conflictList = new ArrayList();
   
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
     * Constructor to allow control over RandomAccessFile.
     * Also enables parser to skip corrupt objects to try and force parsing
     * @param input The input stream that contains the PDF document.
     * @param rafi The RandomAccessFile to be used in internal COSDocument
     * @param force When true, the parser will skip corrupt pdf objects and 
     * will continue parsing at the next object in the file
     *
     * @throws IOException If there is an error initializing the stream.
     */
    public PDFParser(InputStream input, RandomAccess rafi, boolean force)
        throws IOException
    {
        super(input);
        this.raf = rafi;
        this.forceParsing = force;
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
            
            //Some PDF files have garbage between the header and the
            //first object
            skipToNextObj();

            boolean wasLastParsedObjectEOF = false;
            try
            {
                while(true)
                {
                    if(pdfSource.isEOF())
                    {
                        break;
                    }
                    try
                    {
                        wasLastParsedObjectEOF = parseObject();
                    }
                    catch(IOException e)
                    {
                        if(forceParsing)
                        {
                            /*
                             * Warning is sent to the PDFBox.log and to the Console that
                             * we skipped over an object
                             */
                            log.warn("Parsing Error, Skipping Object", e);
                            skipToNextObj();
                        }
                        else
                        { 
                            throw e;
                        }
                    }
                    skipSpaces();
                }
                //Test if we saw a trailer section. If not, look for an XRef Stream (Cross-Reference Stream) 
                //to populate the trailer and xref information. For PDF 1.5 and above 
                if( document.getTrailer() == null )
                {
                    document.parseXrefStreams();
                }
                if( !document.isEncrypted() )
                {
                    document.dereferenceObjectStreams();
                }
                ConflictObj.resolveConflicts(document, conflictList);     
            }
            catch( IOException e )
            {
                /*
                 * PDF files may have random data after the EOF marker. Ignore errors if
                 * last object processed is EOF. 
                 */
                if( !wasLastParsedObjectEOF )
                {
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
    
    /**
     * Skip to the start of the next object.  This is used to recover
     * from a corrupt object. This should handle all cases that parseObject
     * supports. This assumes that the next object will
     * start on its own line.
     * 
     * @throws IOException 
     */
    private void skipToNextObj() throws IOException 
    {
        byte[] b = new byte[16];
        Pattern p = Pattern.compile("\\d+\\s+\\d+\\s+obj.*", Pattern.DOTALL);
        /* Read a buffer of data each time to see if it starts with a
         * known keyword. This is not the most efficient design, but we should
         * rarely be needing this function. We could update this to use the 
         * circular buffer, like in readUntilEndStream().
         */
        while(!pdfSource.isEOF())
        {
             int l = pdfSource.read(b);
             if(l < 1)
             {
                 break;
             }
             String s = new String(b, "US-ASCII");  
             if(s.startsWith("trailer") ||
                     s.startsWith("xref") || 
                     s.startsWith("startxref") ||
                     s.startsWith("stream") ||
                     p.matcher(s).matches())
             {
                 pdfSource.unread(b);
                 break;
             }
             else
             {
                 pdfSource.unread(b, 1, l-1);
             }
        }   
    }

    private void parseHeader() throws IOException
    {
        // read first line
        String header = readLine();
        // some pdf-documents are broken and the pdf-version is in one of the following lines
        if ((header.indexOf( PDF_HEADER ) == -1) && (header.indexOf( FDF_HEADER ) == -1))
        {
            header = readLine();
            while ((header.indexOf( PDF_HEADER ) == -1) && (header.indexOf( FDF_HEADER ) == -1))
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
        if ((header.indexOf( PDF_HEADER ) == -1) && (header.indexOf( FDF_HEADER ) == -1))
        {
            throw new IOException( "Error: Header doesn't contain versioninfo" );
        }
        
        //sometimes there are some garbage bytes in the header before the header
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
            if(!header.matches(PDF_HEADER + "\\d.\\d")) 
            {
                String headerGarbage = header.substring(PDF_HEADER.length()+3, header.length()) + "\n";
                header = header.substring(0, PDF_HEADER.length()+3);
                pdfSource.unread(headerGarbage.getBytes());
            }
        }
        else 
        {
            if(!header.matches(FDF_HEADER + "\\d.\\d")) 
            {
                String headerGarbage = header.substring(FDF_HEADER.length()+3, header.length()) + "\n";
                header = header.substring(0, FDF_HEADER.length()+3);
                pdfSource.unread(headerGarbage.getBytes());
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
            throw new IOException( "Error getting pdf version:" + e );
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
    private boolean parseObject() throws IOException
    {
        int currentObjByteOffset = pdfSource.getOffset();
        boolean isEndOfFile = false; 
        skipSpaces();
        //peek at the next character to determine the type of object we are parsing
        char peekedChar = (char)pdfSource.peek();
        
        //ignore endobj and endstream sections.
        while( peekedChar == 'e' )
        {
            //there are times when there are multiple endobj, so lets
            //just read them and move on.
            readString();
            skipSpaces();
            peekedChar = (char)pdfSource.peek();
        }
        if( pdfSource.isEOF())
        {
            //"Skipping because of EOF" );
            //end of file we will return a false and call it a day.
        }
        //xref table. Note: The contents of the Xref table are currently ignored
        else if( peekedChar == 'x') 
        {
            parseXrefTable();
        }
        // Note: startxref can occur in either a trailer section or by itself 
        else if (peekedChar == 't' || peekedChar == 's') 
        {
            if(peekedChar == 't')
            {
                parseTrailer();
                peekedChar = (char)pdfSource.peek(); 
            }
            if (peekedChar == 's')
            {  
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
            if(pdfObject.getObject() == null)
            {
                pdfObject.setObject(pb);
            }
            /*
             * If the object we returned already has a baseobject, then we have a conflict
             * which we will resolve using information after we parse the xref table.
             */
            else
            {
                addObjectToConflicts(currentObjByteOffset, key, pb); 
            }
            
            if( !endObjectKey.equals( "endobj" ) )
            {
                               if (endObjectKey.startsWith( "endobj" ) ) 
                               {
                                       /*
                                         * Some PDF files don't contain a new line after endobj so we 
                                         * need to make sure that the next object number is getting read separately
                                         * and not part of the endobj keyword. Ex. Some files would have "endobj28"
                                         * instead of "endobj"
                                         */
                                        pdfSource.unread( endObjectKey.substring( 6 ).getBytes() );
                                    } 
                                    else if( !pdfSource.isEOF() )                
                                    {
                    try
                    {
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
    * Adds a new ConflictObj to the conflictList.
    * @param offset the offset of the ConflictObj
    * @param key The COSObjectKey of this object
    * @param pb The COSBase of this conflictObj
    * @throws IOException
    */
    private void addObjectToConflicts(int offset, COSObjectKey key, COSBase pb) throws IOException
    {
        COSObject obj = new COSObject(null);
        obj.setObjectNumber( new COSInteger( key.getNumber() ) );
        obj.setGenerationNumber( new COSInteger( key.getGeneration() ) );
        obj.setObject(pb);
        ConflictObj conflictObj = new ConflictObj(offset, key, obj);
        conflictList.add(conflictObj);   
    }

    /**
     * This will parse the startxref section from the stream.
     * The startxref value is ignored.
     *            
     * @return false on parsing error 
     * @throws IOException If an IO error occurs.
     */
    private boolean parseStartXref() throws IOException
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
    private boolean parseXrefTable() throws IOException
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
        /*
         * Xref tables can have multiple sections. 
         * Each starts with a starting object id and a count.
         */
        while(true)
        {
            int currObjID = readInt(); // first obj id
            int count = readInt(); // the number of objects in the xref table
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
                String[] splitString = currentLine.split(" ");
                if (splitString.length < 3)
                {
                    log.warn("invalid xref line: " + currentLine);
                    break;
                }
                /* This supports the corrupt table as reported in 
                 * PDFBOX-474 (XXXX XXX XX n) */
                if(splitString[splitString.length-1].equals("n"))
                {
                    try
                    {
                        int currOffset = Integer.parseInt(splitString[0]);
                        int currGenID = Integer.parseInt(splitString[1]);
                        COSObjectKey objKey = new COSObjectKey(currObjID, currGenID);
                        document.setXRef(objKey, currOffset);
                    }
                    catch(NumberFormatException e)
                    {
                        throw new IOException(e.getMessage());
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
    private boolean parseTrailer() throws IOException
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
                byte[] b = nextLine.getBytes();
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
    
    /**
     * Used to resolve conflicts when a PDF Document has multiple objects with
     * the same id number. Ideally, we could use the Xref table when parsing
     * the document to be able to determine which of the objects with the same ID
     * is correct, but we do not have access to the Xref Table during parsing.
     * Instead, we queue up the conflicts and resolve them after the Xref has
     * been parsed. The Objects listed in the Xref Table are kept and the 
     * others are ignored. 
     */
    private static class ConflictObj
    {

        private int offset;
        private COSObjectKey objectKey;
        private COSObject object;
        
        public ConflictObj(int offsetValue, COSObjectKey key, COSObject pdfObject) 
        {
            this.offset = offsetValue;
            this.objectKey = key;
            this.object = pdfObject;
        }
        public String toString()
        {
            return "Object(" + offset + ", " + objectKey + ")";
        }
        
        /**
         * Sometimes pdf files have objects with the same ID number yet are
         * not referenced by the Xref table and therefore should be excluded.             
         * This method goes through the conflicts list and replaces the object stored
         * in the objects array with this one if it is referenced by the xref
         * table. 
         * @throws IOException
         */
        private static void resolveConflicts(COSDocument document, List conflictList) throws IOException
        {
            Iterator conflicts = conflictList.iterator();
            while(conflicts.hasNext())
            {
                ConflictObj o = (ConflictObj)conflicts.next();
                Integer offset = new Integer(o.offset);
                if(document.getXrefTable().containsValue(offset))
                {
                    COSObject pdfObject = document.getObjectFromPool(o.objectKey);
                    pdfObject.setObject(o.object.getObject());
                }
            }
        }
    }
}
