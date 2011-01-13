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
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSBoolean;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNull;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.cos.ICOSVisitor;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.SecurityHandler;
import org.apache.pdfbox.persistence.util.COSObjectKey;

/**
 * this class acts on a in-memory representation of a pdf document.
 *
 * todo no support for incremental updates
 * todo single xref section only
 * todo no linearization
 *
 * @author Michael Traut
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.36 $
 */
public class COSWriter implements ICOSVisitor
{
    /**
     * The dictionary open token.
     */
    public static final byte[] DICT_OPEN = "<<".getBytes();
    /**
     * The dictionary close token.
     */
    public static final byte[] DICT_CLOSE = ">>".getBytes();
    /**
     * space character.
     */
    public static final byte[] SPACE = " ".getBytes();
    /**
     * The start to a PDF comment.
     */
    public static final byte[] COMMENT = "%".getBytes();

    /**
     * The output version of the PDF.
     */
    public static final byte[] VERSION = "PDF-1.4".getBytes();
    /**
     * Garbage bytes used to create the PDF header.
     */
    public static final byte[] GARBAGE = new byte[] {(byte)0xf6, (byte)0xe4, (byte)0xfc, (byte)0xdf};
    /**
     * The EOF constant.
     */
    public static final byte[] EOF = "%%EOF".getBytes();
    // pdf tokens

    /**
     * The reference token.
     */
    public static final byte[] REFERENCE = "R".getBytes();
    /**
     * The XREF token.
     */
    public static final byte[] XREF = "xref".getBytes();
    /**
     * The xref free token.
     */
    public static final byte[] XREF_FREE = "f".getBytes();
    /**
     * The xref used token.
     */
    public static final byte[] XREF_USED = "n".getBytes();
    /**
     * The trailer token.
     */
    public static final byte[] TRAILER = "trailer".getBytes();
    /**
     * The start xref token.
     */
    public static final byte[] STARTXREF = "startxref".getBytes();
    /**
     * The starting object token.
     */
    public static final byte[] OBJ = "obj".getBytes();
    /**
     * The end object token.
     */
    public static final byte[] ENDOBJ = "endobj".getBytes();
    /**
     * The array open token.
     */
    public static final byte[] ARRAY_OPEN = "[".getBytes();
    /**
     * The array close token.
     */
    public static final byte[] ARRAY_CLOSE = "]".getBytes();
    /**
     * The open stream token.
     */
    public static final byte[] STREAM = "stream".getBytes();
    /**
     * The close stream token.
     */
    public static final byte[] ENDSTREAM = "endstream".getBytes();

    private NumberFormat formatXrefOffset = new DecimalFormat("0000000000");
    /**
     * The decimal format for the xref object generation number data.
     */
    private NumberFormat formatXrefGeneration = new DecimalFormat("00000");

    private NumberFormat formatDecimal = NumberFormat.getNumberInstance( Locale.US );

    // the stream where we create the pdf output
    private OutputStream output;
    // the stream used to write standard cos data
    private COSStandardOutputStream standardOutput;

    // the start position of the x ref section
    private long startxref = 0;

    // the current object number
    private long number = 0;

    // maps the object to the keys generated in the writer
    // these are used for indirect references in other objects
    //A hashtable is used on purpose over a hashmap
    //so that null entries will not get added.
    private Map<COSBase,COSObjectKey> objectKeys = new Hashtable<COSBase,COSObjectKey>();

    // the list of x ref entries to be made so far
    private LinkedList xRefEntries = new LinkedList();

    //A list of objects to write.
    private LinkedList<COSBase> objectsToWrite = new LinkedList<COSBase>();
    private HashSet<COSBase> objectsToWriteSet = new HashSet<COSBase>();

    //a list of objects already written
    private Set<COSBase> writtenObjects = new HashSet<COSBase>();
    //An 'actual' is any COSBase that is not a COSObject.
    //need to keep a list of the actuals that are added
    //as well as the objects because there is a problem
    //when adding a COSObject and then later adding
    //the actual for that object, so we will track
    //actuals separately.
    private Set<COSBase> actualsAdded = new HashSet<COSBase>();

    private COSObjectKey currentObjectKey = null;

    private PDDocument document = null;

    private boolean willEncrypt = false;

    /**
     * COSWriter constructor comment.
     *
     * @param os The wrapped output stream.
     */
    public COSWriter(OutputStream os)
    {
        super();
        setOutput(os);
        setStandardOutput(new COSStandardOutputStream(getOutput()));
        formatDecimal.setMaximumFractionDigits( 10 );
        formatDecimal.setGroupingUsed( false );
    }
    /**
     * add an entry in the x ref table for later dump.
     *
     * @param entry The new entry to add.
     */
    protected void addXRefEntry(COSWriterXRefEntry entry)
    {
        getXRefEntries().add(entry);
    }

    /**
     * This will close the stream.
     *
     * @throws IOException If the underlying stream throws an exception.
     */
    public void close() throws IOException
    {
        if (getStandardOutput() != null)
        {
            getStandardOutput().close();
        }
        if (getOutput() != null)
        {
            getOutput().close();
        }
    }

    /**
     * This will get the current object number.
     *
     * @return The current object number.
     */
    protected long getNumber()
    {
        return number;
    }

    /**
     * This will get all available object keys.
     *
     * @return A map of all object keys.
     */
    public java.util.Map<COSBase,COSObjectKey> getObjectKeys()
    {
        return objectKeys;
    }

    /**
     * This will get the output stream.
     *
     * @return The output stream.
     */
    protected java.io.OutputStream getOutput()
    {
        return output;
    }

    /**
     * This will get the standard output stream.
     *
     * @return The standard output stream.
     */
    protected COSStandardOutputStream getStandardOutput()
    {
        return standardOutput;
    }

    /**
     * This will get the current start xref.
     *
     * @return The current start xref.
     */
    protected long getStartxref()
    {
        return startxref;
    }
    /**
     * This will get the xref entries.
     *
     * @return All available xref entries.
     */
    protected java.util.List getXRefEntries()
    {
        return xRefEntries;
    }

    /**
     * This will set the current object number.
     *
     * @param newNumber The new object number.
     */
    protected void setNumber(long newNumber)
    {
        number = newNumber;
    }

    /**
     * This will set the output stream.
     *
     * @param newOutput The new output stream.
     */
    private void setOutput( OutputStream newOutput )
    {
        output = newOutput;
    }

    /**
     * This will set the standard output stream.
     *
     * @param newStandardOutput The new standard output stream.
     */
    private void setStandardOutput(COSStandardOutputStream newStandardOutput)
    {
        standardOutput = newStandardOutput;
    }

    /**
     * This will set the start xref.
     *
     * @param newStartxref The new start xref attribute.
     */
    protected void setStartxref(long newStartxref)
    {
        startxref = newStartxref;
    }

    /**
     * This will write the body of the document.
     *
     * @param doc The document to write the body for.
     *
     * @throws IOException If there is an error writing the data.
     * @throws COSVisitorException If there is an error generating the data.
     */
    protected void doWriteBody(COSDocument doc) throws IOException, COSVisitorException
    {
        COSDictionary trailer = doc.getTrailer();
        COSDictionary root = (COSDictionary)trailer.getDictionaryObject( COSName.ROOT );
        COSDictionary info = (COSDictionary)trailer.getDictionaryObject( COSName.INFO );
        COSDictionary encrypt = (COSDictionary)trailer.getDictionaryObject( COSName.ENCRYPT );
        if( root != null )
        {
            addObjectToWrite( root );
        }
        if( info != null )
        {
            addObjectToWrite( info );
        }


        while( objectsToWrite.size() > 0 )
        {
            COSBase nextObject = (COSBase)objectsToWrite.removeFirst();
            objectsToWriteSet.remove(nextObject);
            doWriteObject( nextObject );
        }


        willEncrypt = false;

        if( encrypt != null )
        {
            addObjectToWrite( encrypt );
        }

        while( objectsToWrite.size() > 0 )
        {
            COSBase nextObject = (COSBase)objectsToWrite.removeFirst();
            objectsToWriteSet.remove(nextObject);
            doWriteObject( nextObject );
        }

    }

    private void addObjectToWrite( COSBase object )
    {
        COSBase actual = object;
        if( actual instanceof COSObject )
        {
            actual = ((COSObject)actual).getObject();
        }

        if( !writtenObjects.contains( object ) &&
            !objectsToWriteSet.contains( object ) &&
            !actualsAdded.contains( actual ) )
        {
            objectsToWrite.add( object );
            objectsToWriteSet.add( object );
            if( actual != null )
            {
                actualsAdded.add( actual );
            }
        }
    }

    /**
     * This will write a COS object.
     *
     * @param obj The object to write.
     *
     * @throws COSVisitorException If there is an error visiting objects.
     */
    public void doWriteObject( COSBase obj ) throws COSVisitorException
    {
        try
        {
            writtenObjects.add( obj );
            // find the physical reference
            currentObjectKey = getObjectKey( obj );
            // add a x ref entry
            addXRefEntry( new COSWriterXRefEntry(getStandardOutput().getPos(), obj, currentObjectKey));
            // write the object
            getStandardOutput().write(String.valueOf(currentObjectKey.getNumber()).getBytes());
            getStandardOutput().write(SPACE);
            getStandardOutput().write(String.valueOf(currentObjectKey.getGeneration()).getBytes());
            getStandardOutput().write(SPACE);
            getStandardOutput().write(OBJ);
            getStandardOutput().writeEOL();
            obj.accept( this );
            getStandardOutput().writeEOL();
            getStandardOutput().write(ENDOBJ);
            getStandardOutput().writeEOL();
        }
        catch (IOException e)
        {
            throw new COSVisitorException(e);
        }
    }

    /**
     * This will write the header to the PDF document.
     *
     * @param doc The document to get the data from.
     *
     * @throws IOException If there is an error writing to the stream.
     */
    protected void doWriteHeader(COSDocument doc) throws IOException
    {
        getStandardOutput().write( doc.getHeaderString().getBytes() );
        getStandardOutput().writeEOL();
        getStandardOutput().write(COMMENT);
        getStandardOutput().write(GARBAGE);
        getStandardOutput().writeEOL();
    }


    /**
     * This will write the trailer to the PDF document.
     *
     * @param doc The document to create the trailer for.
     *
     * @throws IOException If there is an IOError while writing the document.
     * @throws COSVisitorException If there is an error while generating the data.
     */
    protected void doWriteTrailer(COSDocument doc) throws IOException, COSVisitorException
    {
        getStandardOutput().write(TRAILER);
        getStandardOutput().writeEOL();

        COSDictionary trailer = doc.getTrailer();
        //sort xref, needed only if object keys not regenerated
        Collections.sort(getXRefEntries());
        COSWriterXRefEntry lastEntry = (COSWriterXRefEntry)getXRefEntries().get( getXRefEntries().size()-1);
        trailer.setInt(COSName.SIZE, (int)lastEntry.getKey().getNumber()+1);
        trailer.removeItem( COSName.PREV );
        /**
        COSObject catalog = doc.getCatalog();
        if (catalog != null)
        {
            trailer.setItem(COSName.getPDFName("Root"), catalog);
        }
        */
        trailer.accept(this);

        getStandardOutput().write(STARTXREF);
        getStandardOutput().writeEOL();
        getStandardOutput().write(String.valueOf(getStartxref()).getBytes());
        getStandardOutput().writeEOL();
        getStandardOutput().write(EOF);
    }

    /**
     * write the x ref section for the pdf file
     *
     * currently, the pdf is reconstructed from the scratch, so we write a single section
     *
     * todo support for incremental writing?
     *
     * @param doc The document to write the xref from.
     *
     * @throws IOException If there is an error writing the data to the stream.
     */
    protected void doWriteXRef(COSDocument doc) throws IOException
    {
        String offset;
        String generation;

        // sort xref, needed only if object keys not regenerated
        Collections.sort(getXRefEntries());
        COSWriterXRefEntry lastEntry = (COSWriterXRefEntry)getXRefEntries().get( getXRefEntries().size()-1 );

        // remember the position where x ref is written
        setStartxref(getStandardOutput().getPos());
        //
        getStandardOutput().write(XREF);
        getStandardOutput().writeEOL();
        // write start object number and object count for this x ref section
        // we assume starting from scratch
        getStandardOutput().write(String.valueOf(0).getBytes());
        getStandardOutput().write(SPACE);
        getStandardOutput().write(String.valueOf(lastEntry.getKey().getNumber() + 1).getBytes());
        getStandardOutput().writeEOL();
        // write initial start object with ref to first deleted object and magic generation number
        offset = formatXrefOffset.format(0);
        generation = formatXrefGeneration.format(65535);
        getStandardOutput().write(offset.getBytes());
        getStandardOutput().write(SPACE);
        getStandardOutput().write(generation.getBytes());
        getStandardOutput().write(SPACE);
        getStandardOutput().write(XREF_FREE);
        getStandardOutput().writeCRLF();
        // write entry for every object
        long lastObjectNumber = 0;
        for (Iterator i = getXRefEntries().iterator(); i.hasNext();)
        {
            COSWriterXRefEntry entry = (COSWriterXRefEntry) i.next();
            while( lastObjectNumber<entry.getKey().getNumber()-1 )
            {
                offset = formatXrefOffset.format(0);
                generation = formatXrefGeneration.format(65535);
                getStandardOutput().write(offset.getBytes());
                getStandardOutput().write(SPACE);
                getStandardOutput().write(generation.getBytes());
                getStandardOutput().write(SPACE);
                getStandardOutput().write(XREF_FREE);
                getStandardOutput().writeCRLF();
                lastObjectNumber++;
            }
            lastObjectNumber = entry.getKey().getNumber();
            offset = formatXrefOffset.format(entry.getOffset());
            generation = formatXrefGeneration.format(entry.getKey().getGeneration());
            getStandardOutput().write(offset.getBytes());
            getStandardOutput().write(SPACE);
            getStandardOutput().write(generation.getBytes());
            getStandardOutput().write(SPACE);
            getStandardOutput().write(entry.isFree() ? XREF_FREE : XREF_USED);
            getStandardOutput().writeCRLF();
        }
    }

    /**
     * This will get the object key for the object.
     *
     * @param obj The object to get the key for.
     *
     * @return The object key for the object.
     */
    private COSObjectKey getObjectKey( COSBase obj )
    {
        COSBase actual = obj;
        if( actual instanceof COSObject )
        {
            actual = ((COSObject)obj).getObject();
        }
        COSObjectKey key = null;
        if( actual != null )
        {
            key = (COSObjectKey)objectKeys.get(actual);
        }
        if( key == null )
        {
            key = (COSObjectKey)objectKeys.get(obj);
        }
        if (key == null)
        {
            setNumber(getNumber()+1);
            key = new COSObjectKey(getNumber(),0);
            objectKeys.put(obj, key);
            if( actual != null )
            {
                objectKeys.put(actual, key);
            }
        }
        return key;
    }

    /**
     * visitFromArray method comment.
     *
     * @param obj The object that is being visited.
     *
     * @throws COSVisitorException If there is an exception while visiting this object.
     *
     * @return null
     */
    public Object visitFromArray( COSArray obj ) throws COSVisitorException
    {
        try
        {
            int count = 0;
            getStandardOutput().write(ARRAY_OPEN);
            for (Iterator<COSBase> i = obj.iterator(); i.hasNext();)
            {
                COSBase current = i.next();
                if( current instanceof COSDictionary )
                {
                    addObjectToWrite( current );
                    writeReference( current );
                }
                else if( current instanceof COSObject )
                {
                    COSBase subValue = ((COSObject)current).getObject();
                    if( subValue instanceof COSDictionary || subValue == null )
                    {
                        addObjectToWrite( current );
                        writeReference( current );
                    }
                    else
                    {
                        subValue.accept( this );
                    }
                }
                else if( current == null )
                {
                    COSNull.NULL.accept( this );
                }
                else if( current instanceof COSString )
                {
                    COSString copy = new COSString(((COSString)current).getString());
                    copy.accept(this);
                }
                else
                {
                    current.accept(this);
                }
                count++;
                if (i.hasNext())
                {
                    if (count % 10 == 0)
                    {
                        getStandardOutput().writeEOL();
                    }
                    else
                    {
                        getStandardOutput().write(SPACE);
                    }
                }
            }
            getStandardOutput().write(ARRAY_CLOSE);
            getStandardOutput().writeEOL();
            return null;
        }
        catch (IOException e)
        {
            throw new COSVisitorException(e);
        }
    }

    /**
     * visitFromBoolean method comment.
     *
     * @param obj The object that is being visited.
     *
     * @throws COSVisitorException If there is an exception while visiting this object.
     *
     * @return null
     */
    public Object visitFromBoolean(COSBoolean obj) throws COSVisitorException
    {

        try
        {
            obj.writePDF( getStandardOutput() );
            return null;
        }
        catch (IOException e)
        {
            throw new COSVisitorException(e);
        }
    }

    /**
     * visitFromDictionary method comment.
     *
     * @param obj The object that is being visited.
     *
     * @throws COSVisitorException If there is an exception while visiting this object.
     *
     * @return null
     */
    public Object visitFromDictionary(COSDictionary obj) throws COSVisitorException
    {
        try
        {
            getStandardOutput().write(DICT_OPEN);
            getStandardOutput().writeEOL();
            for (Map.Entry<COSName, COSBase> entry : obj.entrySet())
            {
                COSBase value = entry.getValue();
                if (value != null)
                {
                    entry.getKey().accept(this);
                    getStandardOutput().write(SPACE);
                    if( value instanceof COSDictionary )
                    {
                        addObjectToWrite( value );
                        writeReference( value );
                    }
                    else if( value instanceof COSObject )
                    {
                        COSBase subValue = ((COSObject)value).getObject();
                        if( subValue instanceof COSDictionary || subValue == null )
                        {
                            addObjectToWrite( value );
                            writeReference( value );
                        }
                        else
                        {
                            subValue.accept( this );
                        }
                    }
                    else
                    {
                        value.accept(this);
                    }
                    getStandardOutput().writeEOL();

                }
                else
                {
                    //then we won't write anything, there are a couple cases
                    //were the value of an entry in the COSDictionary will
                    //be a dangling reference that points to nothing
                    //so we will just not write out the entry if that is the case
                }
            }
            getStandardOutput().write(DICT_CLOSE);
            getStandardOutput().writeEOL();
            return null;
        }
        catch( IOException e )
        {
            throw new COSVisitorException(e);
        }
    }

    /**
     * The visit from document method.
     *
     * @param doc The object that is being visited.
     *
     * @throws COSVisitorException If there is an exception while visiting this object.
     *
     * @return null
     */
    public Object visitFromDocument(COSDocument doc) throws COSVisitorException
    {
        try
        {
            doWriteHeader(doc);
            doWriteBody(doc);
            doWriteXRef(doc);
            doWriteTrailer(doc);
            return null;
        }
        catch (IOException e)
        {
            throw new COSVisitorException(e);
        }
    }

    /**
     * visitFromFloat method comment.
     *
     * @param obj The object that is being visited.
     *
     * @throws COSVisitorException If there is an exception while visiting this object.
     *
     * @return null
     */
    public Object visitFromFloat(COSFloat obj) throws COSVisitorException
    {

        try
        {
            obj.writePDF( getStandardOutput() );
            return null;
        }
        catch (IOException e)
        {
            throw new COSVisitorException(e);
        }
    }

    /**
     * visitFromFloat method comment.
     *
     * @param obj The object that is being visited.
     *
     * @throws COSVisitorException If there is an exception while visiting this object.
     *
     * @return null
     */
    public Object visitFromInt(COSInteger obj) throws COSVisitorException
    {
        try
        {
            obj.writePDF( getStandardOutput() );
            return null;
        }
        catch (IOException e)
        {
            throw new COSVisitorException(e);
        }
    }

    /**
     * visitFromName method comment.
     *
     * @param obj The object that is being visited.
     *
     * @throws COSVisitorException If there is an exception while visiting this object.
     *
     * @return null
     */
    public Object visitFromName(COSName obj) throws COSVisitorException
    {
        try
        {
            obj.writePDF( getStandardOutput() );
            return null;
        }
        catch (IOException e)
        {
            throw new COSVisitorException(e);
        }
    }

    /**
     * visitFromNull method comment.
     *
     * @param obj The object that is being visited.
     *
     * @throws COSVisitorException If there is an exception while visiting this object.
     *
     * @return null
     */
    public Object visitFromNull(COSNull obj) throws COSVisitorException
    {
        try
        {
            obj.writePDF( getStandardOutput() );
            return null;
        }
        catch (IOException e)
        {
            throw new COSVisitorException(e);
        }
    }

    /**
     * visitFromObjRef method comment.
     *
     * @param obj The object that is being visited.
     *
     * @throws COSVisitorException If there is an exception while visiting this object.
     */
    public void writeReference(COSBase obj) throws COSVisitorException
    {
        try
        {
            COSObjectKey  key = getObjectKey(obj);
            getStandardOutput().write(String.valueOf(key.getNumber()).getBytes());
            getStandardOutput().write(SPACE);
            getStandardOutput().write(String.valueOf(key.getGeneration()).getBytes());
            getStandardOutput().write(SPACE);
            getStandardOutput().write(REFERENCE);
        }
        catch (IOException e)
        {
            throw new COSVisitorException(e);
        }
    }

    /**
     * visitFromStream method comment.
     *
     * @param obj The object that is being visited.
     *
     * @throws COSVisitorException If there is an exception while visiting this object.
     *
     * @return null
     */
    public Object visitFromStream(COSStream obj) throws COSVisitorException
    {
        try
        {
            if(willEncrypt)
            {
                document.getSecurityHandler().encryptStream(
                    obj,
                    currentObjectKey.getNumber(),
                    currentObjectKey.getGeneration());
            }

            InputStream input = obj.getFilteredStream();
            // set the length of the stream and write stream dictionary
            COSObject lengthObject = new COSObject( null );

            obj.setItem(COSName.LENGTH, lengthObject);
            //obj.accept(this);
            // write the stream content
            visitFromDictionary( obj );
            getStandardOutput().write(STREAM);
            getStandardOutput().writeCRLF();
            byte[] buffer = new byte[1024];
            int amountRead = 0;
            int totalAmountWritten = 0;
            while( (amountRead = input.read(buffer,0,1024)) != -1 )
            {
                getStandardOutput().write( buffer, 0, amountRead );
                totalAmountWritten += amountRead;
            }
            lengthObject.setObject( COSInteger.get( totalAmountWritten ) );
            getStandardOutput().writeCRLF();
            getStandardOutput().write(ENDSTREAM);
            getStandardOutput().writeEOL();
            return null;
        }
        catch( Exception e )
        {
            throw new COSVisitorException(e);
        }
    }

    /**
     * visitFromString method comment.
     *
     * @param obj The object that is being visited.
     *
     * @return null
     *
     * @throws COSVisitorException If there is an exception while visiting this object.
     */
    public Object visitFromString(COSString obj) throws COSVisitorException
    {
        try
        {
            if(willEncrypt)
            {
                document.getSecurityHandler().decryptString(
                    obj,
                    currentObjectKey.getNumber(),
                    currentObjectKey.getGeneration());
            }

            obj.writePDF( getStandardOutput() );
        }
        catch (Exception e)
        {
            throw new COSVisitorException(e);
        }
        return null;
    }

    /**
     * This will write the pdf document.
     *
     * @param doc The document to write.
     *
     * @throws COSVisitorException If an error occurs while generating the data.
     */
    public void write(COSDocument doc) throws COSVisitorException
    {
        PDDocument pdDoc = new PDDocument( doc );
        write( pdDoc );
    }

    /**
     * This will write the pdf document.
     *
     * @param doc The document to write.
     *
     * @throws COSVisitorException If an error occurs while generating the data.
     */
    public void write(PDDocument doc) throws COSVisitorException
    {
        document = doc;
        
        // if the document says we should remove encryption, then we shouldn't encrypt
        if(doc.isAllSecurityToBeRemoved())
        {
            this.willEncrypt = false;
            // also need to get rid of the "Encrypt" in the trailer so readers 
            // don't try to decrypt a document which is not encrypted
            COSDocument cosDoc = doc.getDocument();
            COSDictionary trailer = cosDoc.getTrailer();
            trailer.removeItem(COSName.ENCRYPT);
        }
        else
        {
            SecurityHandler securityHandler = document.getSecurityHandler();
            if(securityHandler != null)
            {
                try
                {
                    securityHandler.prepareDocumentForEncryption(document);
                    this.willEncrypt = true;
                }
                catch(IOException e)
                {
                    throw new COSVisitorException( e );
                }
                catch(CryptographyException e)
                {
                    throw new COSVisitorException( e );
                }
            }
            else
            {
                    this.willEncrypt = false;
            }        
        }

        COSDocument cosDoc = document.getDocument();
        COSDictionary trailer = cosDoc.getTrailer();
        COSArray idArray = (COSArray)trailer.getDictionaryObject( COSName.ID );
        if( idArray == null )
        {
            try
            {

                //algorithm says to use time/path/size/values in doc to generate
                //the id.  We don't have path or size, so do the best we can
                MessageDigest md = MessageDigest.getInstance( "MD5" );
                md.update( Long.toString( System.currentTimeMillis()).getBytes() );
                COSDictionary info = (COSDictionary)trailer.getDictionaryObject( COSName.INFO );
                if( info != null )
                {
                    Iterator<COSBase> values = info.getValues().iterator();
                    while( values.hasNext() )
                    {
                        md.update( values.next().toString().getBytes() );
                    }
                }
                idArray = new COSArray();
                COSString id = new COSString( md.digest() );
                idArray.add( id );
                idArray.add( id );
                trailer.setItem( COSName.ID, idArray );
            }
            catch( NoSuchAlgorithmException e )
            {
                throw new COSVisitorException( e );
            }
        }
        cosDoc.accept(this);
    }
}
