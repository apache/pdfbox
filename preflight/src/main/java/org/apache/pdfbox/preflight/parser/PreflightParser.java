/*****************************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 ****************************************************************************/

package org.apache.pdfbox.preflight.parser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNull;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSObjectKey;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdfparser.XrefTrailerResolver.XRefType;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.preflight.Format;
import org.apache.pdfbox.preflight.PreflightConfiguration;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.PreflightDocument;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.SyntaxValidationException;


import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_ARRAY_TOO_LONG;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_CROSS_REF;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_HEXA_STRING_EVEN_NUMBER;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_HEXA_STRING_INVALID;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_HEXA_STRING_TOO_LONG;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_INVALID_OFFSET;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_MISSING_OFFSET;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_NAME_TOO_LONG;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_NUMERIC_RANGE;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_OBJ_DELIMITER;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_STREAM_DELIMITER;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_STREAM_LENGTH_INVALID;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_TOO_MANY_ENTRIES;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_TRAILER_EOF;
import static org.apache.pdfbox.preflight.PreflightConstants.MAX_ARRAY_ELEMENTS;
import static org.apache.pdfbox.preflight.PreflightConstants.MAX_DICT_ENTRIES;
import static org.apache.pdfbox.preflight.PreflightConstants.MAX_NAME_SIZE;
import static org.apache.pdfbox.preflight.PreflightConstants.MAX_NEGATIVE_FLOAT;
import static org.apache.pdfbox.preflight.PreflightConstants.MAX_POSITIVE_FLOAT;
import static org.apache.pdfbox.preflight.PreflightConstants.MAX_STRING_LENGTH;

public class PreflightParser extends PDFParser
{
    /**
     * Define a one byte encoding that hasn't specific encoding in UTF-8 charset. Avoid unexpected
     * error when the encoding is Cp5816
     */
    private static final Charset ENCODING = StandardCharsets.ISO_8859_1;

    private Format format = null;

    private PreflightConfiguration config = null;

    private PreflightDocument preflightDocument;

    private ValidationResult validationResult;

    /**
     * Constructor.
     *
     * @param file
     * @throws IOException if there is a reading error.
     */
    public PreflightParser(File file) throws IOException
    {
        super(new RandomAccessReadBufferedFile(file));
    }

    /**
     * Constructor.
     *
     * @param filename
     * @throws IOException if there is a reading error.
     */
    public PreflightParser(String filename) throws IOException
    {
        this(new File(filename));
    }

    /**
     * Add a validation error to the ValidationResult.
     * 
     * @param error the validation error to be added
     */
    private void addValidationError(ValidationError error)
    {
        validationResult.addError(error);
    }

    @Override
    public PDDocument parse() throws IOException
    {
        return parse(Format.PDF_A1B);
    }

    /**
     * Parse the given file and check if it is a confirming file according to the given format.
     * 
     * @param format
     *            format that the document should follow (default {@link Format#PDF_A1B})
     * @throws IOException
     */
    public PDDocument parse(Format format) throws IOException
    {
        return parse(format, null);
    }

    /**
     * Parse the given file and check if it is a confirming file according to the given format.
     * 
     * @param format
     *            format that the document should follow (default {@link Format#PDF_A1B})
     * @param config
     *            Configuration bean that will be used by the PreflightDocument. If null the format is used to determine
     *            the default configuration.
     * @throws IOException
     */
    public PDDocument parse(Format format, PreflightConfiguration config) throws IOException
    {
        this.format = format == null ? Format.PDF_A1B : format;
        this.config = config;
        validationResult = new ValidationResult(true);
        PDDocument pdDocument = null;
        checkPdfHeader();
        try
        {
            // disable leniency
            pdDocument = super.parse(false);
        }
        catch (IOException e)
        {
            addValidationError(new ValidationError(PreflightConstants.ERROR_SYNTAX_COMMON, e.getMessage()));
            throw new SyntaxValidationException(e, validationResult);
        }
        // create context
        PreflightContext context = new PreflightContext();
        context.setDocument(preflightDocument);
        preflightDocument.setContext(context);
        context.setXrefTrailerResolver(xrefTrailerResolver);
        context.setFileLen(fileLen);
        preflightDocument.addValidationErrors(validationResult.getErrorsList());
        return pdDocument;
    }

    @Override
    protected PDDocument createDocument() throws IOException
    {
        preflightDocument = new PreflightDocument(document, format, config);
        return preflightDocument;
    }

    // --------------------------------------------------------
    // - Below All methods that adds controls on the PDF syntax
    // --------------------------------------------------------

    @Override
    protected void initialParse() throws IOException
    {
        super.initialParse();
        // Dereference each object to trigger parser validation errors
        Set<COSObjectKey> objectKeys = document.getXrefTable().keySet();
        objectKeys.forEach(key -> document.getObjectFromPool(key).getObject());
    }

    @Override
    protected boolean resetTrailerResolver()
    {
        // Don't reset the xref trailer resolver after parsing as it is needed for validation
        return false;
    }

    /**
     * Check that the PDF header match rules of the PDF/A specification. First line (offset 0) must
     * be a comment with the PDF version (version 1.0 isn't conform to the PDF/A specification)
     * Second line is a comment with at least 4 bytes greater than 0x80
     */
    private void checkPdfHeader()
    {
        try
        {
            source.seek(0);
            String firstLine = readLine();
            if (firstLine == null || !firstLine.matches("%PDF-1\\.[1-9]"))
            {
                addValidationError(new ValidationError(PreflightConstants.ERROR_SYNTAX_HEADER,
                        "First line must match %PDF-1.\\d"));
            }
            String secondLine = readLine();
            if (secondLine != null)
            {
                boolean secondLineFails = false;
                byte[] secondLineAsBytes = secondLine.getBytes(ENCODING);
                if (secondLineAsBytes.length == 0 || secondLineAsBytes[0] != '%')
                {
                    secondLineFails = true;
                }
                else if (secondLine.length() >= 5)
                {
                    for (int i = 1; i < 5; ++i)
                    {
                        secondLineFails |= (secondLineAsBytes[i] & 0xFF) < 0x80;
                    }
                }
                if (secondLineFails)
                {
                    addValidationError(new ValidationError(PreflightConstants.ERROR_SYNTAX_HEADER,
                            "Second line must begin with '%' followed by at least 4 bytes greater than 127"));
                }
            }
            source.seek(0);
        }
        catch (IOException e)
        {
            addValidationError(new ValidationError(PreflightConstants.ERROR_SYNTAX_HEADER,
                    "Unable to read the PDF file : " + e.getMessage(), e));
        }
    }

    /**
     * Same method than the {@linkplain PDFParser#parseXrefTable(long)} with additional controls : -
     * EOL mandatory after the 'xref' keyword - Cross reference subsection header uses single white
     * space as separator - and so on
     *
     * @param startByteOffset the offset to start at
     * @return false on parsing error
     * @throws IOException If an IO error occurs.
     */
    @Override
    protected boolean parseXrefTable(long startByteOffset) throws IOException
    {
        if (source.peek() != 'x')
        {
            return false;
        }
        String xref = readString();
        if (!xref.equals("xref"))
        {
            addValidationError(new ValidationError(PreflightConstants.ERROR_SYNTAX_CROSS_REF,
                    "xref must be followed by a EOL character"));
            return false;
        }
        if (!nextIsEOL())
        {
            addValidationError(new ValidationError(PreflightConstants.ERROR_SYNTAX_CROSS_REF,
                    "xref must be followed by EOL"));
        }

        // signal start of new XRef
        xrefTrailerResolver.nextXrefObj(startByteOffset,XRefType.TABLE);

        Pattern pattern = Pattern.compile("(\\d+)\\s(\\d+)(\\s*)");

        // Xref tables can have multiple sections. Each starts with a starting object id and a count.
        while (true)
        {
            // just after the xref<EOL> there are an integer
            // first obj id
            long currObjID;
            // the number of objects in the xref table
            int count; 

            long offset = source.getPosition();
            String line = readLine();
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches())
            {
                currObjID = Long.parseLong(matcher.group(1));
                count = Integer.parseInt(matcher.group(2));
            }
            else
            {
                addValidationError(new ValidationError(ERROR_SYNTAX_CROSS_REF,
                        "Cross reference subsection header is invalid: '" + line + "' at position "
                                + source.getPosition()));
                // reset source cursor to read xref information
                source.seek(offset);
                // first obj id
                currObjID = readObjectNumber();
                // the number of objects in the xref table
                count = readInt();
            }

            skipSpaces();
            for (int i = 0; i < count; i++)
            {
                if (source.isEOF() || isEndOfName((char) source.peek()))
                {
                    break;
                }
                if (source.peek() == 't')
                {
                    addValidationError(new ValidationError(PreflightConstants.ERROR_SYNTAX_CROSS_REF,
                            "Expected xref line but 't' found"));
                    break;
                }
                // Ignore table contents
                String currentLine = readLine();
                String[] splitString = currentLine.split(" ");
                if (splitString.length < 3)
                {
                    addValidationError(new ValidationError(PreflightConstants.ERROR_SYNTAX_CROSS_REF,
                            "invalid xref line: " + currentLine));
                    break;
                }
                // This supports the corrupt table as reported in PDFBOX-474 (XXXX XXX XX n)
                if (splitString[splitString.length - 1].equals("n"))
                {
                    try
                    {
                        long currOffset = Long.parseLong(splitString[0]);
                        int currGenID = Integer.parseInt(splitString[1]);
                        COSObjectKey objKey = new COSObjectKey(currObjID, currGenID);
                        xrefTrailerResolver.setXRef(objKey, currOffset);
                    }
                    catch (NumberFormatException e)
                    {
                        addValidationError(new ValidationError(PreflightConstants.ERROR_SYNTAX_CROSS_REF,
                                "offset or genid can't be read as number " + e.getMessage(), e));
                    }
                }
                else if (!splitString[2].equals("f"))
                {
                    addValidationError(new ValidationError(PreflightConstants.ERROR_SYNTAX_CROSS_REF,
                            "Corrupt XRefTable Entry - ObjID:" + currObjID));
                }
                currObjID++;
                skipSpaces();
            }
            skipSpaces();
            if (!isDigit())
            {
                break;
            }
        }
        return true;
    }

    /**
     * Wraps the {@link PDFParser#parseCOSStream} to check rules on 'stream' and 'endstream'
     * keywords. {@link #checkStreamKeyWord()} and {@link #checkEndstreamKeyWord(org.apache.pdfbox.cos.COSDictionary, long)}
     *
     * @param dic dictionary that goes with this stream.
     *
     * @return parsed pdf stream.
     *
     * @throws IOException if an error occurred reading the stream, like problems with reading
     * length attribute, stream does not end with 'endstream' after data read, stream too short etc.
     */
    @Override
    protected COSStream parseCOSStream(COSDictionary dic) throws IOException
    {
        long startOffset = checkStreamKeyWord();
        COSStream result = super.parseCOSStream(dic);
        checkEndstreamKeyWord(dic, startOffset);
        return result;
    }

    /**
     * 'stream' must be followed by &lt;CR&gt;&lt;LF&gt; or only &lt;LF&gt;
     * 
     * @throws IOException
     */
    private long checkStreamKeyWord() throws IOException
    {
        String streamV = readString();
        if (!streamV.equals("stream"))
        {
            addValidationError(new ValidationError(ERROR_SYNTAX_STREAM_DELIMITER,
                    "Expected 'stream' keyword but found '" + streamV + "' at offset "+source.getPosition()));
        }
        long startOffset = source.getPosition();
        int nextChar = source.read();
        if (nextChar == ASCII_CR && source.peek() == ASCII_LF)
        {
            startOffset += 2;
        }
        else if (nextChar == ASCII_LF)
        {
            startOffset++;
        }
        else
        {
            addValidationError(new ValidationError(ERROR_SYNTAX_STREAM_DELIMITER,
                    "Expected 'EOL' after the stream keyword at offset "+source.getPosition()));
        }
        // set the offset before stream
        source.seek(source.getPosition() - 7);
        return startOffset;
    }

    /**
     * 'endstream' must be preceded by an EOL
     * 
     * @throws IOException
     */
    private void checkEndstreamKeyWord(COSDictionary dic, long startOffset)
            throws IOException
    {
        source.seek(source.getPosition() - 10);
        long endOffset = source.getPosition();
        int nextChar = source.read();
        boolean eolFound = false;
        boolean crlfFound = false;
        // LF found
        if (nextChar == ASCII_LF)
        {
            eolFound = true;
            // check if the LF is part of a CRLF
            source.rewind(2);
            if (source.read() == ASCII_CR)
            {
                endOffset--;
                crlfFound = true;
            }
            source.read();
        }
        boolean addStreamLengthErrorMessage = false;
        long actualLength = endOffset - startOffset;
        if (!eolFound)
        {
            addValidationError(new ValidationError(ERROR_SYNTAX_STREAM_DELIMITER,
                    "Expected 'EOL' before the endstream keyword at offset "+source.getPosition()+" but found '"+source.peek()+"'"));
            addStreamLengthErrorMessage = true;
        }
        String endstreamV = readString();
        if (!endstreamV.equals("endstream"))
        {
            addValidationError(new ValidationError(ERROR_SYNTAX_STREAM_DELIMITER,
                    "Expected 'endstream' keyword at offset "+source.getPosition()+" but found '" + endstreamV + "'"));
            addStreamLengthErrorMessage = true;
        }

        int length = dic.getInt(COSName.LENGTH);
        if (addStreamLengthErrorMessage || //
                (length > -1 && ((!crlfFound && length - actualLength != 0)
                        || (crlfFound && length - actualLength > 1))))
        {
            addValidationError(new ValidationError(ERROR_SYNTAX_STREAM_LENGTH_INVALID,
                    "Stream length is invalid [dic=" + dic + "; defined length=" + length
                            + "; actual length=" + actualLength + ", starting offset="
                            + startOffset));
        }
    }

    private boolean nextIsEOL() throws IOException
    {
        boolean succeed = false;
        int nextChar = source.read();
        if (ASCII_CR == nextChar && ASCII_LF == source.peek())
        {
            source.read();
            succeed = true;
        }
        else if (ASCII_CR == nextChar || ASCII_LF == nextChar)
        {
            succeed = true;
        }
        return succeed;
    }

    @Override
    /**
     * Call {@link BaseParser#parseCOSArray()} and check the number of element in the array
     */
    protected COSArray parseCOSArray() throws IOException
    {
        COSArray result = super.parseCOSArray();
        if (result != null && result.size() > MAX_ARRAY_ELEMENTS)
        {
            addValidationError(new ValidationError(ERROR_SYNTAX_ARRAY_TOO_LONG, "Array too long : " + result.size()));
        }
        return result;
    }

    @Override
    /**
     * Call {@link BaseParser#parseCOSName()} and check the length of the name
     */
    protected COSName parseCOSName() throws IOException
    {
        COSName result = super.parseCOSName();
        if (result != null && result.getName().getBytes().length > MAX_NAME_SIZE)
        {
            addValidationError(new ValidationError(ERROR_SYNTAX_NAME_TOO_LONG, "Name too long: " + result.getName()));
        }
        return result;
    }

    /**
     * Check that the hexa string contains only an even number of
     * Hexadecimal characters. Once it is done, reset the offset at the beginning of the string and
     * call {@link PDFParser#parseCOSString()}
     *
     * @return The parsed PDF string.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    @Override
    protected COSString parseCOSString() throws IOException
    {
        // offset reminder
        long offset = source.getPosition();
        char nextChar = (char) source.read();
        int count = 0;
        if (nextChar == '<')
        {
            do
            {
                nextChar = (char) source.read();
                if (!isWhitespace(nextChar) && nextChar != '>')
                {
                    if (Character.digit(nextChar, 16) >= 0)
                    {
                        count++;
                    }
                    else
                    {
                        addValidationError(new ValidationError(ERROR_SYNTAX_HEXA_STRING_INVALID,
                                "Hexa String must have only Hexadecimal Characters (found '" + nextChar + "') at offset " + source.getPosition()));
                        break;
                    }
                }
            } 
            while (nextChar != '>');
        }

        if (count % 2 != 0)
        {
            addValidationError(new ValidationError(ERROR_SYNTAX_HEXA_STRING_EVEN_NUMBER,
                    "Hexa string shall contain even number of non white space char at offset " + source.getPosition()));
        }

        // reset the offset to parse the COSString
        source.seek(offset);
        COSString result = super.parseCOSString();

        if (result.getString().length() > MAX_STRING_LENGTH)
        {
            addValidationError(new ValidationError(ERROR_SYNTAX_HEXA_STRING_TOO_LONG, "Hexa string is too long at offset "+source.getPosition()));
        }
        return result;
    }

    /**
     * Call {@link PDFParser#parseDirObject()} check limit range for Float, Integer and number of
     * Dictionary entries.
     *
     * @return The parsed object.
     * @throws java.io.IOException if there is an error during parsing.
     */
    @Override
    protected COSBase parseDirObject() throws IOException
    {
        COSBase result = super.parseDirObject();

        if (result instanceof COSNumber)
        {
            COSNumber number = (COSNumber) result;
            if (number instanceof COSFloat)
            {
                float real = number.floatValue();
                if (real > MAX_POSITIVE_FLOAT || real < MAX_NEGATIVE_FLOAT)
                {
                    addValidationError(new ValidationError(ERROR_SYNTAX_NUMERIC_RANGE,
                            "Float is too long or too small: " + real+"  at offset "+source.getPosition()));
                }
            }
            else
            {
                long numAsLong = number.longValue();
                if (numAsLong > Integer.MAX_VALUE || numAsLong < Integer.MIN_VALUE)
                {
                    addValidationError(new ValidationError(ERROR_SYNTAX_NUMERIC_RANGE,
                            "Numeric is too long or too small: " + numAsLong+"  at offset "+source.getPosition()));
                }
            }
        }

        if (result instanceof COSDictionary)
        {
            COSDictionary dic = (COSDictionary) result;
            if (dic.size() > MAX_DICT_ENTRIES)
            {
                addValidationError(new ValidationError(ERROR_SYNTAX_TOO_MANY_ENTRIES, "Too Many Entries In Dictionary at offset "+source.getPosition()));
            }
        }
        return result;
    }

    @Override
    protected synchronized COSBase parseObjectDynamically(COSObjectKey objKey,
            boolean requireExistingNotCompressedObj)
            throws IOException
    {
        COSObject pdfObject = document.getObjectFromPool(objKey);
        if (!pdfObject.isObjectNull())
        {
            return pdfObject.getObject();
        }
        COSBase referencedObject = null;

        // read offset or object stream object number from xref table
        Long offsetOrObjstmObNr = document.getXrefTable().get(objKey);

        // test to circumvent loops with broken documents
        if (requireExistingNotCompressedObj && offsetOrObjstmObNr == null)
        {
            addValidationError(new ValidationError(ERROR_SYNTAX_MISSING_OFFSET,
                    "Object must be defined and must not be compressed object: "
                            + objKey.getNumber() + ":" + objKey.getGeneration()));
            throw new SyntaxValidationException(
                    "Object must be defined and must not be compressed object: "
                            + objKey.getNumber() + ":" + objKey.getGeneration(),
                    validationResult);
        }

        if (offsetOrObjstmObNr != null)
        {
            if (offsetOrObjstmObNr == 0)
            {
                addValidationError(new ValidationError(ERROR_SYNTAX_INVALID_OFFSET,
                        "Object {" + objKey.getNumber() + ":" + objKey.getGeneration()
                                + "} has an offset of 0"));
            }
            else if (offsetOrObjstmObNr > 0)
            {
                // offset of indirect object in file
                referencedObject = parseFileObject(offsetOrObjstmObNr, objKey);
            }
            else
            {
                // xref value is object nr of object stream containing object to be parsed
                // since our object was not found it means object stream was not parsed so far
                referencedObject = parseObjectStreamObject(-offsetOrObjstmObNr, objKey);
            }
        }
        if (referencedObject == null || referencedObject instanceof COSNull)
        {
            // not defined object -> NULL object (Spec. 1.7, chap. 3.2.9)
            // or some other issue with dereferencing
            // remove parser to avoid endless recursion
            pdfObject.setToNull();
        }
        return referencedObject;
    }

    private COSBase parseFileObject(Long offsetOrObjstmObNr, final COSObjectKey objKey)
            throws IOException
    {
        // offset of indirect object in file
        // ---- go to object start
        source.seek(offsetOrObjstmObNr);
        // ---- we must have an indirect object
        long readObjNr;
        int readObjGen;

        long offset = source.getPosition();
        String line = readLine();
        Pattern pattern = Pattern.compile("(\\d+)\\s(\\d+)\\sobj");
        Matcher matcher = pattern.matcher(line);
        if (matcher.matches())
        {
            readObjNr = Long.parseLong(matcher.group(1));
            readObjGen = Integer.parseInt(matcher.group(2));
        }
        else
        {

            addValidationError(new ValidationError(ERROR_SYNTAX_OBJ_DELIMITER,
                    "Single space expected [offset=" + offset + "; key="
                            + offsetOrObjstmObNr.toString() + "; line=" + line + "; object="
                            + objKey.getNumber() + " " + objKey.getGeneration() + "]"));

            // reset source cursor to read object information
            source.seek(offset);
            readObjNr = readObjectNumber();
            readObjGen = readGenerationNumber();
            skipSpaces(); // skip spaces between Object Generation number and the 'obj' keyword
            for (char c : OBJ_MARKER)
            {
                if (source.read() != c)
                {
                    addValidationError(new ValidationError(ERROR_SYNTAX_OBJ_DELIMITER,
                            "Expected pattern '" + new String(OBJ_MARKER)
                                    + " but missed at character '" + c + "'"));
                    throw new SyntaxValidationException("Expected pattern '"
                            + new String(OBJ_MARKER) + " but missed at character '" + c + "'",
                            validationResult);
                }
            }
        }

        // ---- consistency check
        if ((readObjNr != objKey.getNumber()) || (readObjGen != objKey.getGeneration()))
        {
            throw new IOException("XREF for " + objKey.getNumber() + ":" + objKey.getGeneration()
                    + " points to wrong object: " + readObjNr + ":" + readObjGen);
        }

        skipSpaces();
        COSBase referencedObject = parseDirObject();
        skipSpaces();
        long endObjectOffset = source.getPosition();
        String endObjectKey = readString();

        if (endObjectKey.equals("stream"))
        {
            source.seek(endObjectOffset);
            if (referencedObject instanceof COSDictionary)
            {
                COSStream stream = parseCOSStream((COSDictionary) referencedObject);
                if (securityHandler != null)
                {
                    securityHandler.decryptStream(stream, readObjNr, readObjGen);
                }
                referencedObject = stream;
            }
            else
            {
                // this is not legal
                // the combination of a dict and the stream/endstream forms a complete stream object
                throw new IOException(
                        "Stream not preceded by dictionary (offset: " + offsetOrObjstmObNr + ").");
            }
            skipSpaces();
            endObjectOffset = source.getPosition();
            endObjectKey = readString();

            // we have case with a second 'endstream' before endobj
            if (!endObjectKey.startsWith("endobj") && endObjectKey.startsWith("endstream"))
            {
                endObjectKey = endObjectKey.substring(9).trim();
                if (endObjectKey.length() == 0)
                {
                    // no other characters in extra endstream line
                    endObjectKey = readString(); // read next line
                }
            }
        }
        else if (securityHandler != null)
        {
            securityHandler.decrypt(referencedObject, readObjNr, readObjGen);
        }
        if (!endObjectKey.startsWith("endobj"))
        {
            throw new IOException("Object (" + readObjNr + ":" + readObjGen + ") at offset "
                    + offsetOrObjstmObNr + " does not end with 'endobj'.");
        }
        else
        {
            offset = source.getPosition();
            source.seek(endObjectOffset - 1);
            if (!nextIsEOL())
            {
                addValidationError(
                        new ValidationError(PreflightConstants.ERROR_SYNTAX_OBJ_DELIMITER,
                                "EOL expected before the 'endobj' keyword at offset "
                                        + source.getPosition()));
            }
            source.seek(offset);
        }

        if (!nextIsEOL())
        {
            addValidationError(new ValidationError(PreflightConstants.ERROR_SYNTAX_OBJ_DELIMITER,
                    "EOL expected after the 'endobj' keyword at offset " + source.getPosition()));
        }
        return referencedObject;
    }

    @Override
    protected int lastIndexOf(final char[] pattern, final byte[] buf, final int endOff)
    {
        int offset = super.lastIndexOf(pattern, buf, endOff);
        if (offset > 0 && Arrays.equals(pattern, EOF_MARKER))
        {
            // this is the offset of the last %%EOF sequence.
            // nothing should be present after this sequence.
            int tmpOffset = offset + pattern.length;
            int offsetDiff = buf.length - tmpOffset;
            // EOL is authorized
            if (offsetDiff > 2
                    || (offsetDiff == 2
                            && (buf[tmpOffset] != ASCII_CR || buf[tmpOffset + 1] != ASCII_LF))
                    || (offsetDiff == 1
                            && (buf[tmpOffset] != ASCII_CR && buf[tmpOffset] != ASCII_LF)))
            {
                long position;
                try
                {
                    position = source.getPosition();
                }
                catch (IOException ex)
                {
                    position = Long.MIN_VALUE;
                }
                addValidationError(new ValidationError(ERROR_SYNTAX_TRAILER_EOF,
                        "File contains data after the last %%EOF sequence at offset " + position));
            }
        }
        return offset;
    }

    /**
     * Load and validate the given file. Returns the validation result and closes the read pdf.
     * 
     * @param file thew file to be read and validated
     * @return the validation result
     * @throws IOException in case of a file reading or parsing error
     */
    public static ValidationResult validate(File file) throws IOException
    {
        ValidationResult result;
        PreflightParser parser = new PreflightParser(file);
        try (PreflightDocument document = (PreflightDocument) parser.parse())
        {
            result = document.validate();
        }
        catch (SyntaxValidationException e)
        {
            result = e.getResult();
        }
        return result;
    }
}
