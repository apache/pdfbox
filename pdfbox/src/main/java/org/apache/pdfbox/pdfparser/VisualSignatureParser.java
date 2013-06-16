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
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdfwriter.COSWriter;
import org.apache.pdfbox.persistence.util.COSObjectKey;

public class VisualSignatureParser extends BaseParser 
{

    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(PDFParser.class);

    /**
     * Constructor.
     * 
     * @param input the inputstream to be read.
     * 
     * @throws IOException If something went wrong
     */
    public VisualSignatureParser(InputStream input) throws IOException 
    {
        super(input);
    }

    /**
     * {@inheritDoc}
     */
    public void parse() throws IOException 
    {
        document = new COSDocument();
        skipToNextObj();

        boolean wasLastParsedObjectEOF = false;
        try 
        {
            while(!wasLastParsedObjectEOF) 
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
                    /*
                     * Warning is sent to the PDFBox.log and to the Console that
                     * we skipped over an object
                     */
                    LOG.warn("Parsing Error, Skipping Object", e);
                    skipToNextObj();
                }
                skipSpaces();
            }
        } 
        catch(IOException e) 
        {
            /*
             * PDF files may have random data after the EOF marker. Ignore errors if
             * last object processed is EOF.
             */
            if(!wasLastParsedObjectEOF) 
            {
                throw e;
            }
        }
    }

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
            if(s.startsWith("trailer")
                    || s.startsWith("xref")
                    || s.startsWith("startxref")
                    || s.startsWith("stream")
                    || p.matcher(s).matches()) 
            {
                pdfSource.unread(b);
                break;
            } 
            else 
            {
                pdfSource.unread(b, 1, l - 1);
            }
        }
    }

    private boolean parseObject() throws IOException 
    {
        boolean isEndOfFile = false;
        skipSpaces();
        //peek at the next character to determine the type of object we are parsing
        char peekedChar = (char) pdfSource.peek();

        //ignore endobj and endstream sections.
        while(peekedChar == 'e') 
        {
            //there are times when there are multiple endobj, so lets
            //just read them and move on.
            readString();
            skipSpaces();
            peekedChar = (char) pdfSource.peek();
        }
        if(pdfSource.isEOF()) 
        {
            // end of file we will return a false and call it a day.
        } 
        else if(peekedChar == 'x') 
        {
            //xref table. Note: The contents of the Xref table are currently ignored
            return true;
        } 
        else if(peekedChar == 't' || peekedChar == 's') 
        {
            // Note: startxref can occur in either a trailer section or by itself
            if(peekedChar == 't') 
            {
                return true;
            }
            if(peekedChar == 's') 
            {
                skipToNextObj();
                //verify that EOF exists
                String eof = readExpectedString("%%EOF");
                if(eof.indexOf("%%EOF") == -1 && !pdfSource.isEOF()) 
                {
                    throw new IOException("expected='%%EOF' actual='" + eof + "' next=" + readString()
                            + " next=" + readString());
                }
                isEndOfFile = true;
            }
        } 
        else 
        {
            //we are going to parse an normal object
            long number = -1;
            int genNum = -1;
            String objectKey = null;
            boolean missingObjectNumber = false;
            try 
            {
                char peeked = (char) pdfSource.peek();
                if(peeked == '<') 
                {
                    missingObjectNumber = true;
                } 
                else 
                {
                    number = readObjectNumber();
                }
            } 
            catch(IOException e) 
            {
                //ok for some reason "GNU Ghostscript 5.10" puts two endobj
                //statements after an object, of course this is nonsense
                //but because we want to support as many PDFs as possible
                //we will simply try again
                number = readObjectNumber();
            }
            if(!missingObjectNumber)
            {
                skipSpaces();
                genNum = readGenerationNumber();

                objectKey = readString(3);
                //System.out.println( "parseObject() num=" + number +
                //" genNumber=" + genNum + " key='" + objectKey + "'" );
                if(!objectKey.equals("obj")) 
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

            if(endObjectKey.equals("stream")) 
            {
                pdfSource.unread(endObjectKey.getBytes());
                pdfSource.unread(' ');
                if(pb instanceof COSDictionary) 
                {
                    pb = parseCOSStream((COSDictionary) pb, getDocument().getScratchFile());

                } 
                else 
                {
                    // this is not legal
                    // the combination of a dict and the stream/endstream forms a complete stream object
                    throw new IOException("stream not preceded by dictionary");
                }
                endObjectKey = readString();
            }

            COSObjectKey key = new COSObjectKey(number, genNum);
            COSObject pdfObject = document.getObjectFromPool(key);
            pb.setNeedToBeUpdate(true);
            pdfObject.setObject(pb);

            if(!endObjectKey.equals("endobj")) 
            {
                if(endObjectKey.startsWith("endobj")) 
                {
                    /*
                     * Some PDF files don't contain a new line after endobj so we
                     * need to make sure that the next object number is getting read separately
                     * and not part of the endobj keyword. Ex. Some files would have "endobj28"
                     * instead of "endobj"
                     */
                    pdfSource.unread(endObjectKey.substring(6).getBytes());
                } 
                else if(!pdfSource.isEOF()) 
                {
                    try 
                    {
                        //It is possible that the endobj  is missing, there
                        //are several PDFs out there that do that so skip it and move on.
                        Float.parseFloat(endObjectKey);
                        pdfSource.unread(COSWriter.SPACE);
                        pdfSource.unread(endObjectKey.getBytes());
                    } 
                    catch(NumberFormatException e) 
                    {
                        //we will try again incase there was some garbage which
                        //some writers will leave behind.
                        String secondEndObjectKey = readString();
                        if(!secondEndObjectKey.equals("endobj")) 
                        {
                            if(isClosing()) 
                            {
                                //found a case with 17506.pdf object 41 that was like this
                                //41 0 obj [/Pattern /DeviceGray] ] endobj
                                //notice the second array close, here we are reading it
                                //and ignoring and attempting to continue
                                pdfSource.read();
                            }
                            skipSpaces();
                            String thirdPossibleEndObj = readString();
                            if(!thirdPossibleEndObj.equals("endobj")) 
                            {
                                throw new IOException("expected='endobj' firstReadAttempt='" + endObjectKey + "' "
                                        + "secondReadAttempt='" + secondEndObjectKey + "' " + pdfSource);
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
     * Returns the underlying COSDocument.
     * 
     * @return the COSDocument
     * 
     * @throws IOException If something went wrong
     */
    public COSDocument getDocument() throws IOException 
    {
        if(document == null) 
        {
            throw new IOException("You must call parse() before calling getDocument()");
        }
        return document;
    }
}
