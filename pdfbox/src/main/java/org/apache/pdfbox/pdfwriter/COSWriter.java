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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.cos.ICOSVisitor;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.exceptions.SignatureException;
import org.apache.pdfbox.pdfparser.PDFXRefStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.SecurityHandler;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.apache.pdfbox.persistence.util.COSObjectKey;
import org.apache.pdfbox.util.StringUtil;

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
    public static final byte[] DICT_OPEN = StringUtil.getBytes("<<");
    /**
     * The dictionary close token.
     */
    public static final byte[] DICT_CLOSE = StringUtil.getBytes(">>");
    /**
     * space character.
     */
    public static final byte[] SPACE = StringUtil.getBytes(" ");
    /**
     * The start to a PDF comment.
     */
    public static final byte[] COMMENT = StringUtil.getBytes("%");

    /**
     * The output version of the PDF.
     */
    public static final byte[] VERSION = StringUtil.getBytes("PDF-1.4");
    /**
     * Garbage bytes used to create the PDF header.
     */
    public static final byte[] GARBAGE = new byte[] {(byte)0xf6, (byte)0xe4, (byte)0xfc, (byte)0xdf};
    /**
     * The EOF constant.
     */
    public static final byte[] EOF = StringUtil.getBytes("%%EOF");
    // pdf tokens

    /**
     * The reference token.
     */
    public static final byte[] REFERENCE = StringUtil.getBytes("R");
    /**
     * The XREF token.
     */
    public static final byte[] XREF = StringUtil.getBytes("xref");
    /**
     * The xref free token.
     */
    public static final byte[] XREF_FREE = StringUtil.getBytes("f");
    /**
     * The xref used token.
     */
    public static final byte[] XREF_USED = StringUtil.getBytes("n");
    /**
     * The trailer token.
     */
    public static final byte[] TRAILER = StringUtil.getBytes("trailer");
    /**
     * The start xref token.
     */
    public static final byte[] STARTXREF = StringUtil.getBytes("startxref");
    /**
     * The starting object token.
     */
    public static final byte[] OBJ = StringUtil.getBytes("obj");
    /**
     * The end object token.
     */
    public static final byte[] ENDOBJ = StringUtil.getBytes("endobj");
    /**
     * The array open token.
     */
    public static final byte[] ARRAY_OPEN = StringUtil.getBytes("[");
    /**
     * The array close token.
     */
    public static final byte[] ARRAY_CLOSE = StringUtil.getBytes("]");
    /**
     * The open stream token.
     */
    public static final byte[] STREAM = StringUtil.getBytes("stream");
    /**
     * The close stream token.
     */
    public static final byte[] ENDSTREAM = StringUtil.getBytes("endstream");

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
    private Map<COSObjectKey,COSBase> keyObject = new Hashtable<COSObjectKey,COSBase>();

    // the list of x ref entries to be made so far
    private List<COSWriterXRefEntry> xRefEntries = new ArrayList<COSWriterXRefEntry>();
    private HashSet<COSBase> objectsToWriteSet = new HashSet<COSBase>();

    //A list of objects to write.
    private LinkedList<COSBase> objectsToWrite = new LinkedList<COSBase>();

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
    
    private boolean incrementalUpdate = false;
    
    private boolean reachedSignature = false;
    
    private int[] signaturePosition = new int[2];
    
    private int[] byterangePosition = new int[2];
    
    private FileInputStream in;

    /**
     * COSWriter constructor comment.
     *
     * @param os The wrapped output stream.
     */
    public COSWriter(OutputStream os)
    {
        super();
        setOutput(os);
        setStandardOutput(new COSStandardOutputStream(output));
        formatDecimal.setMaximumFractionDigits( 10 );
        formatDecimal.setGroupingUsed( false );
    }
    
    /**
     * COSWriter constructor for incremental updates. 
     *
     * @param os The wrapped output stream.
     * @param is input stream
     */
    public COSWriter(OutputStream os, FileInputStream is)
    {
      this(os);
      in = is;
      incrementalUpdate = true;
    }

    private void prepareIncrement(PDDocument doc)
    {
      try
      {
        if (doc != null)
        {
          COSDocument cosDoc = doc.getDocument();
          
          Map<COSObjectKey, Long> xrefTable = cosDoc.getXrefTable();
          Set<COSObjectKey> keySet = xrefTable.keySet();
          long highestNumber=0;
          for ( COSObjectKey cosObjectKey : keySet ) 
          {
            COSBase object = cosDoc.getObjectFromPool(cosObjectKey).getObject();
            if (object != null && cosObjectKey!= null && !(object instanceof COSNumber))
            {
            objectKeys.put(object, cosObjectKey);
            keyObject.put(cosObjectKey,object);
            }
            
            long num = cosObjectKey.getNumber();
            if (num > highestNumber)
            {
                highestNumber=num;
            }
          }
          setNumber(highestNumber);
          // xrefTable.clear();

        }
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
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
    public Map<COSBase,COSObjectKey> getObjectKeys()
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
    protected List<COSWriterXRefEntry> getXRefEntries()
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
            COSBase nextObject = objectsToWrite.removeFirst();
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
            COSBase nextObject = objectsToWrite.removeFirst();
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
            COSBase cosBase=null;
            COSObjectKey cosObjectKey = null;
            if(actual != null)
            {
                cosObjectKey= objectKeys.get(actual);
            }
            if(cosObjectKey!=null)
            {
                cosBase = keyObject.get(cosObjectKey);
            }
            if(actual != null && objectKeys.containsKey(actual) &&
                    !object.isNeedToBeUpdate() && (cosBase!= null &&
                    !cosBase.isNeedToBeUpdate()))
            {
                return;
            }
          
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
            if(obj instanceof COSDictionary) 
            {
                COSDictionary dict = (COSDictionary)obj;
                COSName item = (COSName)dict.getItem(COSName.TYPE);
                if (COSName.SIG.equals(item) || COSName.DOC_TIME_STAMP.equals(item))
                {
                    reachedSignature = true;
                }
            }
            
            // find the physical reference
            currentObjectKey = getObjectKey( obj );
            // add a x ref entry
            addXRefEntry( new COSWriterXRefEntry(getStandardOutput().getPos(), obj, currentObjectKey));
            // write the object
            getStandardOutput().write(String.valueOf(currentObjectKey.getNumber()).getBytes("ISO-8859-1"));
            getStandardOutput().write(SPACE);
            getStandardOutput().write(String.valueOf(currentObjectKey.getGeneration()).getBytes("ISO-8859-1"));
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
        getStandardOutput().write( doc.getHeaderString().getBytes("ISO-8859-1") );
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
        COSWriterXRefEntry lastEntry = getXRefEntries().get( getXRefEntries().size()-1);
        trailer.setInt(COSName.SIZE, (int)lastEntry.getKey().getNumber()+1);
        // Only need to stay, if an incremental update will be performed
        if (!incrementalUpdate) 
        {
          trailer.removeItem( COSName.PREV );
        }
        // Remove a checksum if present
        trailer.removeItem( COSName.DOC_CHECKSUM );
        
        trailer.accept(this);
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
        if (doc.isXRefStream())
        {
            // sort xref, needed only if object keys not regenerated
            Collections.sort(getXRefEntries());
            COSWriterXRefEntry lastEntry = getXRefEntries().get( getXRefEntries().size()-1 );
    
            // remember the position where x ref is written
            setStartxref(getStandardOutput().getPos());
            //
            getStandardOutput().write(XREF);
            getStandardOutput().writeEOL();
            // write start object number and object count for this x ref section
            // we assume starting from scratch
            writeXrefRange(0, lastEntry.getKey().getNumber() + 1);
            // write initial start object with ref to first deleted object and magic generation number
            writeXrefEntry(COSWriterXRefEntry.getNullEntry());
            // write entry for every object
            long lastObjectNumber = 0;
            for (Iterator<COSWriterXRefEntry> i = getXRefEntries().iterator(); i.hasNext();)
            {
                COSWriterXRefEntry entry = i.next();
                while( lastObjectNumber<entry.getKey().getNumber()-1 )
                {
                  writeXrefEntry(COSWriterXRefEntry.getNullEntry());
                }
                lastObjectNumber = entry.getKey().getNumber();
                writeXrefEntry(entry);
            }
        }
        else
        {

            COSDictionary trailer = doc.getTrailer();
            trailer.setLong(COSName.PREV, doc.getStartXref());
            addXRefEntry(COSWriterXRefEntry.getNullEntry());

            // sort xref, needed only if object keys not regenerated
            Collections.sort(getXRefEntries());

            // remember the position where x ref was written
            setStartxref(getStandardOutput().getPos());

            getStandardOutput().write(XREF);
            getStandardOutput().writeEOL();
            // write start object number and object count for this x ref section
            // we assume starting from scratch

            Integer[] xRefRanges = getXRefRanges(getXRefEntries());
            int xRefLength = xRefRanges.length;
            int x = 0;
            int j = 0;
            while (x < xRefLength && (xRefLength % 2) == 0)
            {
                writeXrefRange(xRefRanges[x], xRefRanges[x + 1]);

                for ( int i = 0; i < xRefRanges[x + 1]; ++i )
                {
                    writeXrefEntry(xRefEntries.get(j++));
                }
                x += 2;
            }
        }
    }

    private void doWriteXRefInc(COSDocument doc, long hybridPrev) throws IOException, COSVisitorException
    {
        if (doc.isXRefStream() || hybridPrev != -1)
        {
            // the file uses XrefStreams, so we need to update
            // it with an xref stream. We create a new one and fill it
            // with data available here
            // first set an entry for the null entry in the xref table
            // this is probably not necessary
            // addXRefEntry(COSWriterXRefEntry.getNullEntry());

            // create a new XRefStrema object
            PDFXRefStream pdfxRefStream = new PDFXRefStream();

            // add all entries from the incremental update.
            List<COSWriterXRefEntry> xRefEntries2 = getXRefEntries();
            for ( COSWriterXRefEntry cosWriterXRefEntry : xRefEntries2 )
            {
                pdfxRefStream.addEntry(cosWriterXRefEntry);
            }

            COSDictionary trailer = doc.getTrailer();
            //            trailer.setLong(COSName.PREV, hybridPrev == -1 ? prev : hybridPrev);
            trailer.setLong(COSName.PREV, doc.getStartXref());

            pdfxRefStream.addTrailerInfo(trailer);
            // the size is the highest object number+1. we add one more
            // for the xref stream object we are going to write
            pdfxRefStream.setSize(getNumber() + 2);

            setStartxref(getStandardOutput().getPos());
            COSStream stream2 = pdfxRefStream.getStream();
            doWriteObject(stream2);
        }

        if (!doc.isXRefStream() || hybridPrev != -1)
        {
            COSDictionary trailer = doc.getTrailer();
            trailer.setLong(COSName.PREV, doc.getStartXref());
            if (hybridPrev != -1)
            {
                COSName xrefStm = COSName.XREF_STM;
                trailer.removeItem(xrefStm);
                trailer.setLong(xrefStm, getStartxref());
            }
            addXRefEntry(COSWriterXRefEntry.getNullEntry());
    
            // sort xref, needed only if object keys not regenerated
            Collections.sort(getXRefEntries());
          
            // remember the position where x ref was written
            setStartxref(getStandardOutput().getPos());
    
            getStandardOutput().write(XREF);
            getStandardOutput().writeEOL();
            // write start object number and object count for this x ref section
            // we assume starting from scratch
    
            Integer[] xRefRanges = getXRefRanges(getXRefEntries());
            int xRefLength = xRefRanges.length;
            int x = 0;
            int j = 0;
            while(x < xRefLength && (xRefLength % 2) == 0)
            {
                writeXrefRange(xRefRanges[x], xRefRanges[x + 1]);
    
                for(int i = 0; i < xRefRanges[x + 1]; ++i)
                {
                    writeXrefEntry(xRefEntries.get(j++));
                }
                x += 2;
            }
        }
    }

    private void doWriteSignature(COSDocument doc) throws IOException, SignatureException
    {
        // need to calculate the ByteRange
        if (signaturePosition[0]>0 && byterangePosition[1] > 0)
        {
            int left = (int)getStandardOutput().getPos()-signaturePosition[1];
            String newByteRange = "0 "+signaturePosition[0]+" "+signaturePosition[1]+" "+left+"]";
            int leftByterange = byterangePosition[1]-byterangePosition[0]-newByteRange.length();
            if(leftByterange<0)
            {
                throw new IOException("Can't write new ByteRange, not enough space");
            }
            getStandardOutput().setPos(byterangePosition[0]);
            getStandardOutput().write(newByteRange.getBytes());
            for(int i=0;i<leftByterange;++i)
            {
                getStandardOutput().write(0x20);
            }
        
            getStandardOutput().setPos(0);
            // Begin - extracting document
            InputStream filterInputStream = new COSFilterInputStream(in, 
                    new int[] {0,signaturePosition[0],signaturePosition[1],left});
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try 
            {
                byte[] buffer = new byte[1024];
                int c;
                while((c = filterInputStream.read(buffer)) != -1)
                {
                    bytes.write(buffer, 0, c);
                }
            } 
            finally 
            {
                if(filterInputStream !=null)
                {
                    filterInputStream.close();
                }
            }

            byte[] pdfContent = bytes.toByteArray();
            // End - extracting document
        
            SignatureInterface signatureInterface = doc.getSignatureInterface();
            byte[] sign = signatureInterface.sign(new ByteArrayInputStream(pdfContent));
            String signature = new COSString(sign).getHexString();
            int leftSignaturerange = signaturePosition[1]-signaturePosition[0]-signature.length();
            if(leftSignaturerange<0)
            {
                throw new IOException("Can't write signature, not enough space");
            }
            getStandardOutput().setPos(signaturePosition[0]+1);
            getStandardOutput().write(signature.getBytes());
        }
    }
    
    private void writeXrefRange(long x, long y) throws IOException
    {
        getStandardOutput().write(String.valueOf(x).getBytes());
        getStandardOutput().write(SPACE);
        getStandardOutput().write(String.valueOf(y).getBytes());
        getStandardOutput().writeEOL();
    }

    private void writeXrefEntry(COSWriterXRefEntry entry) throws IOException
    {
        String offset = formatXrefOffset.format(entry.getOffset());
        String generation = formatXrefGeneration.format(entry.getKey().getGeneration());
        getStandardOutput().write(offset.getBytes("ISO-8859-1"));
        getStandardOutput().write(SPACE);
        getStandardOutput().write(generation.getBytes("ISO-8859-1"));
        getStandardOutput().write(SPACE);
        getStandardOutput().write(entry.isFree() ? XREF_FREE : XREF_USED);
        getStandardOutput().writeCRLF();
    }

    /**
     * check the xref entries and write out the ranges.  The format of the
     * returned array is exactly the same as the pdf specification.  See section
     * 7.5.4 of ISO32000-1:2008, example 1 (page 40) for reference.
     * <p>
     * example: 0 1 2 5 6 7 8 10
     * <p>
     * will create a array with follow ranges
     * <p>
     * 0 3 5 4 10 1
     * <p>
     * this mean that the element 0 is followed by two other related numbers 
     * that represent a cluster of the size 3. 5 is follow by three other
     * related numbers and create a cluster of size 4. etc.
     * 
     * @param xRefEntriesList list with the xRef entries that was written
     * @return a integer array with the ranges
     */
    protected Integer[] getXRefRanges(List<COSWriterXRefEntry> xRefEntriesList)
    {
        int nr = 0;
        int last = -2;
        int count = 1;

        ArrayList<Integer> list = new ArrayList<Integer>();
        for( Object object : xRefEntriesList )
        {
            nr = (int)((COSWriterXRefEntry)object).getKey().getNumber();
            if (nr == last + 1)
            {
                ++count;
                last = nr;
            }
            else if (last == -2)
            {
                last = nr;
            }
            else
            {
                list.add(last - count + 1);
                list.add(count);
                last = nr;
                count = 1;
            }
        }
        // If no new entry is found, we need to write out the last result
        if(xRefEntriesList.size() > 0)
        {
            list.add(last - count + 1);
            list.add(count);
        }
        return list.toArray(new Integer[list.size()]);
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
            key = objectKeys.get(actual);
        }
        if( key == null )
        {
            key = objectKeys.get(obj);
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
                    COSString copy = new COSString(true);
                    copy.append(((COSString)current).getBytes());
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
                        COSDictionary dict = (COSDictionary)value;
                        
                        // write all XObjects as direct objects, this will save some size
                        COSBase item = dict.getItem(COSName.XOBJECT);
                        if(item!=null)
                        {
                            item.setDirect(true);
                        }
                        item = dict.getItem(COSName.RESOURCES);
                        if(item!=null)
                        {
                            item.setDirect(true);
                        }

                        if(dict.isDirect()) 
                        {
                            // If the object should be written direct, we need
                            // to pass the dictionary to the visitor again.
                            visitFromDictionary(dict);
                        }
                        else 
                        {
                            addObjectToWrite( dict );
                            writeReference( dict );
                        }
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
                        // If we reach the pdf signature, we need to determinate the position of the
                        // content and byterange
                        if(reachedSignature && COSName.CONTENTS.equals(entry.getKey()))
                        {
                            signaturePosition = new int[2];
                            signaturePosition[0] = (int)getStandardOutput().getPos();
                            value.accept(this);
                            signaturePosition[1] = (int)getStandardOutput().getPos();
                        }
                        else if(reachedSignature && COSName.BYTERANGE.equals(entry.getKey()))
                        {
                            byterangePosition = new int[2];
                            byterangePosition[0] = (int)getStandardOutput().getPos()+1;
                            value.accept(this);
                            byterangePosition[1] = (int)getStandardOutput().getPos()-1;
                            reachedSignature = false;
                        }
                        else
                        {
                            value.accept(this);
                        }
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
            if(!incrementalUpdate)
            {
                doWriteHeader(doc);
            }
            doWriteBody(doc);
            
            // get the previous trailer
            COSDictionary trailer = doc.getTrailer();
            long hybridPrev = -1;

            if (trailer != null)
            {
                hybridPrev = trailer.getLong(COSName.XREF_STM);
            }
            
            if(incrementalUpdate)
            {
                doWriteXRefInc(doc, hybridPrev);
            }
            else
            {
                doWriteXRef(doc);
            }
            
            // the trailer section should only be used for xref tables not for xref streams
            if (!incrementalUpdate || !doc.isXRefStream() || hybridPrev != -1)
            {
                doWriteTrailer(doc);
            }
            
            // write endof
            getStandardOutput().write(STARTXREF);
            getStandardOutput().writeEOL();
            getStandardOutput().write(String.valueOf(getStartxref()).getBytes("ISO-8859-1"));
            getStandardOutput().writeEOL();
            getStandardOutput().write(EOF);
            getStandardOutput().writeEOL();
            
            if(incrementalUpdate)
            {
                doWriteSignature(doc);
            }
            
            return null;
        }
        catch (IOException e)
        {
            throw new COSVisitorException(e);
        }
        catch (SignatureException e)
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
            getStandardOutput().write(String.valueOf(key.getNumber()).getBytes("ISO-8859-1"));
            getStandardOutput().write(SPACE);
            getStandardOutput().write(String.valueOf(key.getGeneration()).getBytes("ISO-8859-1"));
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
        InputStream input = null;
        try
        {
            if (willEncrypt)
            {
                document.getSecurityHandler().encryptStream(obj, currentObjectKey.getNumber()
                        , currentObjectKey.getGeneration());
            }

            COSObject lengthObject = null;
            // check if the length object is required to be direct, like in
            // a cross reference stream dictionary
            COSBase lengthEntry = obj.getDictionaryObject(COSName.LENGTH);
            String type = obj.getNameAsString(COSName.TYPE);
            if (lengthEntry != null && lengthEntry.isDirect() || "XRef".equals(type))
            {
                // the length might be the non encoded length,
                // set the real one as direct object
                COSInteger cosInteger = COSInteger.get(obj.getFilteredLength());
                cosInteger.setDirect(true);
                obj.setItem(COSName.LENGTH, cosInteger);

            }
            else
            {
                // make the length an implicit indirect object
                // set the length of the stream and write stream dictionary
                lengthObject = new COSObject(null);

                obj.setItem(COSName.LENGTH, lengthObject);
            }
            input = obj.getFilteredStream();
            //obj.accept(this);
            // write the stream content
            visitFromDictionary(obj);
            getStandardOutput().write(STREAM);
            getStandardOutput().writeCRLF();
            byte[] buffer = new byte[1024];
            int amountRead = 0;
            int totalAmountWritten = 0;
            while ((amountRead = input.read(buffer, 0, 1024)) != -1)
            {
                getStandardOutput().write(buffer, 0, amountRead);
                totalAmountWritten += amountRead;
            }
            // set the length as an indirect object
            if (lengthObject != null)
            {
                lengthObject.setObject(COSInteger.get(totalAmountWritten));
            }
            getStandardOutput().writeCRLF();
            getStandardOutput().write(ENDSTREAM);
            getStandardOutput().writeEOL();
            return null;
        }
        catch (Exception e)
        {
            throw new COSVisitorException(e);
        }
        finally
        {
            if (input != null)
            {
                try
                {
                    input.close();
                }
                catch (IOException e)
                {
                    throw new COSVisitorException(e);
                }
            }
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
        if(incrementalUpdate)
        {
            prepareIncrement(doc);
        }
        
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
        if( idArray == null || incrementalUpdate)
        {
            try
            {

                //algorithm says to use time/path/size/values in doc to generate
                //the id.  We don't have path or size, so do the best we can
                MessageDigest md = MessageDigest.getInstance( "MD5" );
                md.update( Long.toString( System.currentTimeMillis()).getBytes("ISO-8859-1") );
                COSDictionary info = (COSDictionary)trailer.getDictionaryObject( COSName.INFO );
                if( info != null )
                {
                    Iterator<COSBase> values = info.getValues().iterator();
                    while( values.hasNext() )
                    {
                        md.update( values.next().toString().getBytes("ISO-8859-1") );
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
            catch( UnsupportedEncodingException e )
            {
                throw new COSVisitorException( e );
            }
        }
        cosDoc.accept(this);
    }
}

