/*
 *  Copyright 2010 adam.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.apache.pdfbox.pdfparser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.cos.COSUnread;
import org.apache.pdfbox.io.RandomAccess;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdmodel.ConformingPDDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.XrefEntry;
import org.apache.pdfbox.persistence.util.COSObjectKey;

/**
 * 
 * @author <a href="adam@apache.org">Adam Nichols</a>
 */
public class ConformingPDFParser extends BaseParser {
    protected RandomAccess inputFile;
    List<XrefEntry> xrefEntries;
    private long currentOffset;
    private ConformingPDDocument doc = null;
    private boolean throwNonConformingException = true;
    private boolean recursivlyRead = true;

    /**
     * Constructor.
     *
     * @param input The input stream that contains the PDF document.
     *
     * @throws IOException If there is an error initializing the stream.
     */
    public ConformingPDFParser(File inputFile) throws IOException {
        this.inputFile = new RandomAccessFile(inputFile, "r");
    }

    /**
     * This will parse the stream and populate the COSDocument object.  This will close
     * the stream when it is done parsing.
     *
     * @throws IOException If there is an error reading from the stream or corrupt data
     * is found.
     */
    public void parse() throws IOException {
        document = new COSDocument();
        doc = new ConformingPDDocument(document);
        currentOffset = inputFile.length()-1;
        long xRefTableLocation = parseTrailerInformation();
        currentOffset = xRefTableLocation;
        parseXrefTable();
        // now that we read the xref table and put null references in the doc,
        // we can deference those objects now.
        boolean oldValue = recursivlyRead;
        recursivlyRead = false;
        List<COSObjectKey> keys = doc.getObjectKeysFromPool();
        for(COSObjectKey key : keys) {
            // getObject will put it into the document's object pool for us
            getObject(key.getNumber(), key.getGeneration());
        }
        recursivlyRead = oldValue;
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
    public COSDocument getDocument() throws IOException {
        if( document == null ) {
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
    public PDDocument getPDDocument() throws IOException {
        return doc;
    }
    
    private boolean parseXrefTable() throws IOException {
        String currentLine = readLine();
        if(throwNonConformingException) {
            if(!"xref".equals(currentLine))
                throw new AssertionError("xref table not found.\nExpected: xref\nFound: "+currentLine);
        }

        int objectNumber = readInt();
        int entries = readInt();
        xrefEntries = new ArrayList<XrefEntry>(entries);
        for(int i=0; i<entries; i++)
            xrefEntries.add(new XrefEntry(objectNumber++, readInt(), readInt(), readLine()));
        
        return true;
    }

    protected long parseTrailerInformation() throws IOException, NumberFormatException {
        long xrefLocation = -1;
        consumeWhitespaceBackwards();
        String currentLine = readLineBackwards();
        if(throwNonConformingException) {
            if(!"%%EOF".equals(currentLine))
                throw new AssertionError("Invalid EOF marker.\nExpected: %%EOF\nFound: "+currentLine);
        }

        xrefLocation = readLongBackwards();
        currentLine = readLineBackwards();
        if(throwNonConformingException) {
            if(!"startxref".equals(currentLine))
                throw new AssertionError("Invalid trailer.\nExpected: startxref\nFound: "+currentLine);
        }

        document.setTrailer(readDictionaryBackwards());
        consumeWhitespaceBackwards();
        currentLine = readLineBackwards();
        if(throwNonConformingException) {
            if(!"trailer".equals(currentLine))
                throw new AssertionError("Invalid trailer.\nExpected: trailer\nFound: "+currentLine);
        }

        return xrefLocation;
    }
    
    protected byte readByteBackwards() throws IOException {
        inputFile.seek(currentOffset);
        byte singleByte = (byte)inputFile.read();
        currentOffset--;
        return singleByte;
    }

    protected byte readByte() throws IOException {
        inputFile.seek(currentOffset);
        byte singleByte = (byte)inputFile.read();
        currentOffset++;
        return singleByte;
    }

    protected String readBackwardUntilWhitespace() throws IOException {
        StringBuilder sb = new StringBuilder();
        byte singleByte = readByteBackwards();
        while(!isWhitespace(singleByte)) {
            sb.insert(0, (char)singleByte);
            singleByte = readByteBackwards();
        }
        return sb.toString();
    }

    /**
     * This will read all bytes (backwards) until a non-whitespace character is
     * found.  To save you an extra read, the non-whitespace character is
     * returned.  If the current character is not whitespace, this method will
     * just return the current char.
     * @return the first non-whitespace character found
     * @throws IOException if there is an error reading from the file
     */
    protected byte consumeWhitespaceBackwards() throws IOException {
        inputFile.seek(currentOffset);
        byte singleByte = (byte)inputFile.read();
        if(!isWhitespace(singleByte))
            return singleByte;

        // we have some whitespace, let's consume it
        while(isWhitespace(singleByte)) {
            singleByte = readByteBackwards();
        }
        // readByteBackwards will decrement the currentOffset to point the byte
        // before the one just read, so we increment it back to the current byte
        currentOffset++;
        return singleByte;
    }

    /**
     * This will read all bytes until a non-whitespace character is
     * found.  To save you an extra read, the non-whitespace character is
     * returned.  If the current character is not whitespace, this method will
     * just return the current char.
     * @return the first non-whitespace character found
     * @throws IOException if there is an error reading from the file
     */
    protected byte consumeWhitespace() throws IOException {
        inputFile.seek(currentOffset);
        byte singleByte = (byte)inputFile.read();
        if(!isWhitespace(singleByte))
            return singleByte;

        // we have some whitespace, let's consume it
        while(isWhitespace(singleByte)) {
            singleByte = readByte();
        }
        // readByte() will increment the currentOffset to point the byte
        // after the one just read, so we decrement it back to the current byte
        currentOffset--;
        return singleByte;
    }

    /**
     * This will consume any whitespace, read in bytes until whitespace is found
     * again and then parse the characters which have been read as a long.  The
     * current offset will then point at the first whitespace character which
     * preceeds the number.
     * @return the parsed number
     * @throws IOException if there is an error reading from the file
     * @throws NumberFormatException if the bytes read can not be converted to a number
     */
    protected long readLongBackwards() throws IOException, NumberFormatException {
        StringBuilder sb = new StringBuilder();
        consumeWhitespaceBackwards();
        byte singleByte = readByteBackwards();
        while(!isWhitespace(singleByte)) {
            sb.insert(0, (char)singleByte);
            singleByte = readByteBackwards();
        }
        if(sb.length() == 0)
            throw new AssertionError("Number not found.  Expected number at offset: " + currentOffset);
        return Long.parseLong(sb.toString());
    }

    @Override
    protected int readInt() throws IOException {
        StringBuilder sb = new StringBuilder();
        consumeWhitespace();
        byte singleByte = readByte();
        while(!isWhitespace(singleByte)) {
            sb.append((char)singleByte);
            singleByte = readByte();
        }
        if(sb.length() == 0)
            throw new AssertionError("Number not found.  Expected number at offset: " + currentOffset);
        return Integer.parseInt(sb.toString());
    }

    /**
     * This will read in a number and return the COS version of the number (be
     * it a COSInteger or a COSFloat).
     * @return the COSNumber which was read/parsed
     * @throws IOException
     */
    protected COSNumber readNumber() throws IOException {
        StringBuilder sb = new StringBuilder();
        consumeWhitespace();
        byte singleByte = readByte();
        while(!isWhitespace(singleByte)) {
            sb.append((char)singleByte);
            singleByte = readByte();
        }
        if(sb.length() == 0)
            throw new AssertionError("Number not found.  Expected number at offset: " + currentOffset);
        return parseNumber(sb.toString());
    }

    protected COSNumber parseNumber(String number) throws IOException {
        if(number.matches("^[0-9]+$"))
            return COSInteger.get(number);
        return new COSFloat(Float.parseFloat(number));
    }

    protected COSBase processCosObject(String string) throws IOException {
        if(string != null && string.endsWith(">")) {
            // string of hex codes
            return COSString.createFromHexString(string.replaceAll("^<", "").replaceAll(">$", ""));
        }
        return null;
    }

    protected COSBase readObjectBackwards() throws IOException {
        COSBase obj = null;
        consumeWhitespaceBackwards();
        String lastSection = readBackwardUntilWhitespace();
        if("R".equals(lastSection)) {
            // indirect reference
            long gen = readLongBackwards();
            long number = readLongBackwards();
            // We just put a placeholder in the pool for now, we'll read the data later
            doc.putObjectInPool(new COSUnread(), number, gen);
            obj = new COSUnread(number, gen, this);
        } else if(">>".equals(lastSection)) {
            // dictionary
            throw new RuntimeException("Not yet implemented");
        } else if(lastSection != null && lastSection.endsWith("]")) {
            // array
            COSArray array = new COSArray();
            lastSection = lastSection.replaceAll("]$", "");
            while(!lastSection.startsWith("[")) {
                if(lastSection.matches("^\\s*<.*>\\s*$")) // it's a hex string
                    array.add(COSString.createFromHexString(lastSection.replaceAll("^\\s*<", "").replaceAll(">\\s*$", "")));
                lastSection = readBackwardUntilWhitespace();
            }
            lastSection = lastSection.replaceAll("^\\[", "");
            if(lastSection.matches("^\\s*<.*>\\s*$")) // it's a hex string
                array.add(COSString.createFromHexString(lastSection.replaceAll("^\\s*<", "").replaceAll(">\\s*$", "")));
            obj = array;
        } else if(lastSection != null && lastSection.endsWith(">")) {
            // string of hex codes
            obj = processCosObject(lastSection);
        } else {
            // try a number, otherwise fall back on a string
            try {
                Long.parseLong(lastSection);
                obj = COSNumber.get(lastSection);
            } catch(NumberFormatException e) {
                throw new RuntimeException("Not yet implemented");
            }
        }

        return obj;
    }

    protected COSName readNameBackwards() throws IOException {
        String name = readBackwardUntilWhitespace();
        name = name.replaceAll("^/", "");
        return COSName.getPDFName(name);
    }

    public COSBase getObject(long objectNumber, long generation) throws IOException {
        // we could optionally, check to see if parse() have been called &
        // throw an exception here, but I don't think that's really necessary
        XrefEntry entry = xrefEntries.get((int)objectNumber);
        currentOffset = entry.getByteOffset();
        return readObject(objectNumber, generation);
    }

    /**
     * This will read an object from the inputFile at whatever our currentOffset
     * is.  If the object and generation are not the expected values and this
     * object is set to throw an exception for non-conforming documents, then an
     * exception will be thrown.
     * @param objectNumber the object number you expect to read
     * @param generation the generation you expect this object to be
     * @return
     */
    public COSBase readObject(long objectNumber, long generation) throws IOException {
        // when recursivly reading, we always pull the object from the filesystem
        if(document != null && recursivlyRead) {
            // check to see if it is in the document cache before hitting the filesystem
            COSBase obj = doc.getObjectFromPool(objectNumber, generation);
            if(obj != null)
                return obj;
        }

        int actualObjectNumber = readInt();
        if(objectNumber != actualObjectNumber)
            if(throwNonConformingException)
                throw new AssertionError("Object numer expected was " +
                        objectNumber + " but actual was " + actualObjectNumber);
        consumeWhitespace();

        int actualGeneration = readInt();
        if(generation != actualGeneration)
            if(throwNonConformingException)
                throw new AssertionError("Generation expected was " +
                        generation + " but actual was " + actualGeneration);
        consumeWhitespace();

        String obj = readWord();
        if(!"obj".equals(obj))
            if(throwNonConformingException)
                throw new AssertionError("Expected keyword 'obj' but found " + obj);
        
        // put placeholder object in doc to prevent infinite recursion
        // e.g. read Root -> dereference object -> read object which has /Parent -> GOTO read Root
        doc.putObjectInPool(new COSObject(null), objectNumber, generation);
        COSBase object = readObject();
        doc.putObjectInPool(object, objectNumber, generation);
        return object;
    }

    /**
     * This actually reads the object data.
     * @return the object which is read
     * @throws IOException
     */
    protected COSBase readObject() throws IOException {
        consumeWhitespace();
        String string = readWord();
        if(string.startsWith("<<")) {
            // this is a dictionary
            COSDictionary dictionary = new COSDictionary();
            boolean atEndOfDictionary = false;
            // remove the marker for the beginning of the dictionary
            string = string.replaceAll("^<<", "");
            
            if("".equals(string) || string.matches("^\\w$"))
                string = readWord().trim();
            while(!atEndOfDictionary) {
                COSName name = COSName.getPDFName(string);
                COSBase object = readObject();
                dictionary.setItem(name, object);

                byte singleByte = consumeWhitespace();
                if(singleByte == '>') {
                    readByte(); // get rid of the second '>'
                    atEndOfDictionary = true;
                }
                if(!atEndOfDictionary)
                    string = readWord().trim();
            }
            return dictionary;
        } else if(string.startsWith("/")) {
            // it's a dictionary label. i.e. /Type or /Pages or something similar
            COSBase name = COSName.getPDFName(string);
            return name;
        } else if(string.startsWith("-")) {
            // it's a negitive number
            return parseNumber(string);
        } else if(string.charAt(0) >= '0' && string.charAt(0) <= '9' ) {
            // it's a COSInt or COSFloat, or a weak reference (i.e. "3 0 R")
            // we'll have to peek ahead a little to see if it's a reference or not
            long tempOffset = this.currentOffset;
            consumeWhitespace();
            String tempString = readWord();
            if(tempString.matches("^[0-9]+$")) {
                // it is an int, might be a weak reference...
                tempString = readWord();
                if(!"R".equals(tempString)) {
                    // it's just a number, not a weak reference
                    this.currentOffset = tempOffset;
                    return parseNumber(string);
                }
            } else {
                // it's just a number, not a weak reference
                this.currentOffset = tempOffset;
                return parseNumber(string);
            }

            // it wasn't a number, so we need to parse the weak-reference
            this.currentOffset = tempOffset;
            int number = Integer.parseInt(string);
            int gen = readInt();
            String r = readWord();

            if(!"R".equals(r))
                if(throwNonConformingException)
                    throw new AssertionError("Expected keyword 'R' but found " + r);

            if(recursivlyRead) {
                // seek to the object, read it, seek back to current location
                long tempLocation = this.currentOffset;
                this.currentOffset = this.xrefEntries.get(number).getByteOffset();
                COSBase returnValue = readObject(number, gen);
                this.currentOffset = tempLocation;
                return returnValue;
            } else {
                // Put a COSUnknown there as a placeholder
                COSObject obj = new COSObject(new COSUnread());
                obj.setObjectNumber(COSInteger.get(number));
                obj.setGenerationNumber(COSInteger.get(gen));
                return obj;
            }
        } else if(string.startsWith("]")) {
            // end of an array, just return null
            if("]".equals(string))
                return null;
            int oldLength = string.length();
            this.currentOffset -= oldLength;
            return null;
        } else if(string.startsWith("[")) {
            // array of values
            // we'll just pay attention to the first part (this is in case there
            // is no whitespace between the "[" and the first element)
            int oldLength = string.length();
            string = "[";
            this.currentOffset -= (oldLength - string.length() + 1);

            COSArray array = new COSArray();
            COSBase object = readObject();
            while(object != null) {
                array.add(object);
                object = readObject();
            }
            return array;
        } else if(string.startsWith("(")) {
            // this is a string (not hex encoded), strip off the '(' and read until ')'
            StringBuilder sb = new StringBuilder(string.substring(1));
            byte singleByte = readByte();
            while(singleByte != ')') {
                sb.append((char)singleByte);
                singleByte = readByte();
            }
            return new COSString(sb.toString());
        } else {
            throw new RuntimeException("Not yet implemented: " + string
                    + " loation=" + this.currentOffset);
        }
    }

    /**
     * This will read the next string from the stream.
     * @return The string that was read from the stream.
     * @throws IOException If there is an error reading from the stream.
     */
    @Override
    protected String readString() throws IOException {
        consumeWhitespace();
        StringBuilder buffer = new StringBuilder();
        int c = pdfSource.read();
        while(!isEndOfName((char)c) && !isClosing(c) && c != -1) {
            buffer.append( (char)c );
            c = pdfSource.read();
        }
        if (c != -1) {
            pdfSource.unread(c);
        }
        return buffer.toString();
    }

    protected COSDictionary readDictionaryBackwards() throws IOException {
        COSDictionary dict = new COSDictionary();
        
        // consume the last two '>' chars which signify the end of the dictionary
        consumeWhitespaceBackwards();
        byte singleByte = readByteBackwards();
        if(throwNonConformingException) {
            if(singleByte != '>')
                throw new AssertionError("");
        }
        singleByte = readByteBackwards();
        if(throwNonConformingException) {
            if(singleByte != '>')
                throw new AssertionError("");
        }
        
        // check to see if we're at the end of the dictionary
        boolean atEndOfDictionary = false;
        singleByte = consumeWhitespaceBackwards();
        if(singleByte == '<') {
            inputFile.seek(currentOffset-1);
            atEndOfDictionary =  ((byte)inputFile.read()) == '<';
        }

        COSDictionary backwardsDictionary = new COSDictionary();
        // while we're not at the end of the dictionary, read in entries
        while(!atEndOfDictionary) {
            COSBase object = readObjectBackwards();
            COSName name = readNameBackwards();
            backwardsDictionary.setItem(name, object);
            
            singleByte = consumeWhitespaceBackwards();
            if(singleByte == '<') {
                inputFile.seek(currentOffset-1);
                atEndOfDictionary =  ((byte)inputFile.read()) == '<';
            }
        }

        // the dictionaries preserve the order keys were added, as such we shall
        // add them in the proper order, not the reverse order
        Set<COSName> backwardsKeys = backwardsDictionary.keySet();
        for(int i = backwardsKeys.size()-1; i >=0; i--)
            dict.setItem((COSName)backwardsKeys.toArray()[i], backwardsDictionary.getItem((COSName)backwardsKeys.toArray()[i]));
        
        // consume the last two '<' chars
        readByteBackwards();
        readByteBackwards();

        return dict;
    }

    /**
     * This will read a line starting with the byte at offset and going 
     * backwards until it finds a newline.  This should only be used if we are
     * certain that the data will only be text, and not binary data.
     * 
     * @param offset the location of the file where we should start reading
     * @return the string which was read
     * @throws IOException if there was an error reading data from the file
     */
    protected String readLineBackwards() throws IOException {
        StringBuilder sb = new StringBuilder();
        boolean endOfObject = false;
        
        do {
            // first we read the %%EOF marker
            byte singleByte = readByteBackwards();
            if(singleByte == '\n') {
                // if ther's a preceeding \r, we'll eat that as well
                inputFile.seek(currentOffset);
                if((byte)inputFile.read() == '\r')
                    currentOffset--;
                endOfObject = true;
            } else if(singleByte == '\r') {
                endOfObject = true;
            } else {
                sb.insert(0, (char)singleByte);
            }
        } while(!endOfObject);
        
        return sb.toString();
    }

    /**
     * This will read a line starting with the byte at offset and going
     * forward until it finds a newline.  This should only be used if we are
     * certain that the data will only be text, and not binary data.
     * @param offset the location of the file where we should start reading
     * @return the string which was read
     * @throws IOException if there was an error reading data from the file
     */
    @Override
    protected String readLine() throws IOException {
        StringBuilder sb = new StringBuilder();
        boolean endOfLine = false;

        do {
            // first we read the %%EOF marker
            byte singleByte = readByte();
            if(singleByte == '\n') {
                // if ther's a preceeding \r, we'll eat that as well
                inputFile.seek(currentOffset);
                if((byte)inputFile.read() == '\r')
                    currentOffset++;
                endOfLine = true;
            } else if(singleByte == '\r') {
                endOfLine = true;
            } else {
                sb.append((char)singleByte);
            }
        } while(!endOfLine);

        return sb.toString();
    }

    protected String readWord() throws IOException {
        StringBuilder sb = new StringBuilder();
        boolean stop = true;
        do {
            byte singleByte = readByte();
            stop = this.isWhitespace(singleByte);

            // there are some additional characters which indicate the next element/word has begun
            // ignore the first char we read, b/c the first char is the beginnging of this object, not the next one
            if(!stop && sb.length() > 0) {
                stop = singleByte == '/' || singleByte == '['
                        || singleByte == ']'
                        || (singleByte == '>' && !">".equals(sb.toString()));
                if(stop) // we're stopping on a non-whitespace char, decrement the
                    this.currentOffset--; // counter so we don't miss this character
            }
            if(!stop)
                sb.append((char)singleByte);
        } while(!stop);

        return sb.toString();
    }

    /**
     * @return the recursivlyRead
     */
    public boolean isRecursivlyRead() {
        return recursivlyRead;
    }

    /**
     * @param recursivlyRead the recursivlyRead to set
     */
    public void setRecursivlyRead(boolean recursivlyRead) {
        this.recursivlyRead = recursivlyRead;
    }
}
