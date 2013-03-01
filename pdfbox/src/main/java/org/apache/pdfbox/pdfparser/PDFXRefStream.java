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
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.pdfwriter.COSWriterXRefEntry;

/**
 * @author Alexander Funk
 * @version $Revision: $
 */
public class PDFXRefStream implements PDFXRef
{

    private static final int ENTRY_OBJSTREAM = 2;

    private static final int ENTRY_NORMAL = 1;

    private static final int ENTRY_FREE = 0;

    private Map<Integer, Object> streamData;

    private Set<Integer> objectNumbers;

    private COSStream stream;

    private long size = -1;

    /**
     * Create a fresh XRef stream like for a fresh file or an incremental update.
     */
    public PDFXRefStream()
    {
        this.stream = new COSStream(new COSDictionary(), new RandomAccessBuffer());
        streamData = new TreeMap<Integer, Object>();
        objectNumbers = new TreeSet<Integer>();
    }

    /**
     * Returns the stream of the XRef.
     * @return the XRef stream
     * @throws IOException if something went wrong
     */
    public COSStream getStream() throws IOException
    {
        stream.setItem(COSName.TYPE, COSName.XREF);
        if (size == -1)
        {
            throw new IllegalArgumentException("size is not set in xrefstream");
        }
        stream.setLong(COSName.SIZE, getSizeEntry());
        stream.setFilters(COSName.FLATE_DECODE);

        {
            List<Integer> indexEntry = getIndexEntry();
            COSArray indexAsArray = new COSArray();
            for ( Integer i : indexEntry )
            {
                indexAsArray.add(COSInteger.get(i));
            }
            stream.setItem(COSName.INDEX, indexAsArray);
        }
        {
            int[] wEntry = getWEntry();
            COSArray wAsArray = new COSArray();
            for ( int i = 0; i < wEntry.length; i++ )
            {
                int j = wEntry[i];
                wAsArray.add(COSInteger.get(j));
            }
            stream.setItem(COSName.W, wAsArray);
            OutputStream unfilteredStream = stream.createUnfilteredStream();
            writeStreamData(unfilteredStream, wEntry);
        }
        Set<COSName> keySet = stream.keySet();
        for ( COSName cosName : keySet )
        {
            COSBase dictionaryObject = stream.getDictionaryObject(cosName);
            dictionaryObject.setDirect(true);
        }
        return stream;
    }

    /**
     * Copy all Trailer Information to this file.
     * 
     * @param trailerDict dictionary to be added as trailer info
     */
    public void addTrailerInfo(COSDictionary trailerDict)
    {
        Set<Entry<COSName, COSBase>> entrySet = trailerDict.entrySet();
        for ( Entry<COSName, COSBase> entry : entrySet )
        {
            COSName key = entry.getKey();
            if (COSName.INFO.equals(key) || COSName.ROOT.equals(key) || COSName.ENCRYPT.equals(key) 
                    || COSName.ID.equals(key) || COSName.PREV.equals(key))
            {
                stream.setItem(key, entry.getValue());
            }
        }
    }

    /**
     * Add an new entry to the XRef stream.
     * 
     * @param entry new entry to be added
     */
    public void addEntry(COSWriterXRefEntry entry)
    {
        objectNumbers.add((int)entry.getKey().getNumber());
        if (entry.isFree())
        {
            // what would be a f-Entry in the xref table
            FreeReference value = new FreeReference();
            value.nextGenNumber = entry.getKey().getGeneration();
            value.nextFree = entry.getKey().getNumber();
            streamData.put((int)value.nextFree, value);
        }
        else
        {
            // we don't care for ObjectStreamReferences for now and only handle
            // normal references that would be f-Entrys in the xref table.
            NormalReference value = new NormalReference();
            value.genNumber = entry.getKey().getGeneration();
            value.offset = entry.getOffset();
            streamData.put((int)entry.getKey().getNumber(), value);
        }
    }

    /**
     * determines the minimal length required for all the lengths.
     * 
     * @return
     */
    private int[] getWEntry()
    {
        long[] wMax = new long[3];
        for ( Object entry : streamData.values() )
        {
            if (entry instanceof FreeReference)
            {
                FreeReference free = (FreeReference)entry;
                wMax[0] = Math.max(wMax[0], ENTRY_FREE); // the type field for a free reference
                wMax[1] = Math.max(wMax[1], free.nextFree);
                wMax[2] = Math.max(wMax[2], free.nextGenNumber);
            }
            else if (entry instanceof NormalReference)
            {
                NormalReference ref = (NormalReference)entry;
                wMax[0] = Math.max(wMax[0], ENTRY_NORMAL); // the type field for a normal reference
                wMax[1] = Math.max(wMax[1], ref.offset);
                wMax[2] = Math.max(wMax[2], ref.genNumber);
            }
            else if (entry instanceof ObjectStreamReference)
            {
                ObjectStreamReference objStream = (ObjectStreamReference)entry;
                wMax[0] = Math.max(wMax[0], ENTRY_OBJSTREAM); // the type field for a objstm reference
                wMax[1] = Math.max(wMax[1], objStream.offset);
                wMax[2] = Math.max(wMax[2], objStream.objectNumberOfObjectStream);
            }
            // TODO add here if new standard versions define new types
            else
            {
                throw new RuntimeException("unexpected reference type");
            }
        }
        // find the max bytes needed to display that column
        int[] w = new int[3];
        for ( int i = 0; i < w.length; i++ )
        {
            while (wMax[i] > 0)
            {
                w[i]++;
                wMax[i] >>= 8;
            }
        }
        return w;
    }

    private long getSizeEntry()
    {
        return size;
    }

    /**
     * Set the size of the XRef stream.
     * 
     * @param streamSize size to bet set as stream size
     */
    public void setSize(long streamSize)
    {
        this.size = streamSize;
    }

    private List<Integer> getIndexEntry()
    {
        LinkedList<Integer> linkedList = new LinkedList<Integer>();
        Integer first = null;
        Integer length = null;

        for ( Integer objNumber : objectNumbers )
        {
            if (first == null)
            {
                first = objNumber;
                length = 1;
            }
            if (first + length == objNumber)
            {
                length += 1;
            }
            if (first + length < objNumber)
            {
                linkedList.add(first);
                linkedList.add(length);
                first = objNumber;
                length = 1;
            }
        }
        linkedList.add(first);
        linkedList.add(length);

        return linkedList;
    }

    private void writeNumber(OutputStream os, long number, int bytes) throws IOException
    {
        byte[] buffer = new byte[bytes];
        for ( int i = 0; i < bytes; i++ )
        {
            buffer[i] = (byte)(number & 0xff);
            number >>= 8;
        }

        for ( int i = 0; i < bytes; i++ )
        {
            os.write(buffer[bytes-i-1]);
        }
    }

    private void writeStreamData(OutputStream os, int[] w) throws IOException
    {
        // iterate over all streamData and write it in the required format
        for ( Object entry : streamData.values() )
        {
            if (entry instanceof FreeReference)
            {
                FreeReference free = (FreeReference)entry;
                writeNumber(os, ENTRY_FREE, w[0]);
                writeNumber(os, free.nextFree, w[1]);
                writeNumber(os, free.nextGenNumber, w[2]);
            }
            else if (entry instanceof NormalReference)
            {
                NormalReference ref = (NormalReference)entry;
                writeNumber(os, ENTRY_NORMAL, w[0]);
                writeNumber(os, ref.offset, w[1]);
                writeNumber(os, ref.genNumber, w[2]);
            }
            else if (entry instanceof ObjectStreamReference)
            {
                ObjectStreamReference objStream = (ObjectStreamReference)entry;
                writeNumber(os, ENTRY_OBJSTREAM, w[0]);
                writeNumber(os, objStream.offset, w[1]);
                writeNumber(os, objStream.objectNumberOfObjectStream, w[2]);
            }
            // TODO add here if new standard versions define new types
            else
            {
                throw new RuntimeException("unexpected reference type");
            }
        }
        os.flush();
        os.close();
    }

    /**
     * A class representing an object stream reference. 
     *
     */
    class ObjectStreamReference
    {
        long objectNumberOfObjectStream;
        long offset;
    }

    /**
     * A class representing a normal reference. 
     *
     */
    class NormalReference
    {
        long genNumber;
        long offset;
    }

    /**
     * A class representing a free reference. 
     *
     */
    class FreeReference
    {
        long nextGenNumber;
        long nextFree;
    }

    /**
     * {@inheritDoc}
     */
    public COSObject getObject(int objectNumber)
    {
        return null;
    }
}
