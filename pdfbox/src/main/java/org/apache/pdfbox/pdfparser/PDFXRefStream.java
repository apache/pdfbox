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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdfparser.xref.FreeXReference;
import org.apache.pdfbox.pdfparser.xref.XReferenceEntry;

/**
 * @author Alexander Funk
 */
public class PDFXRefStream
{

    private final List<XReferenceEntry> streamData = new ArrayList<>();

    private final Set<Long> objectNumbers = new TreeSet<>();

    private final COSStream stream;

    private long size = -1;

    /**
     * Create a fresh XRef stream like for a fresh file or an incremental update.
     * 
     * @param cosDocument
     */
    public PDFXRefStream(COSDocument cosDocument)
    {
        stream = cosDocument.createCOSStream();
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
        stream.setLong(COSName.SIZE, size);
    
        List<Long> indexEntry = getIndexEntry();
        COSArray indexAsArray = new COSArray();
        for ( Long i : indexEntry )
        {
            indexAsArray.add(COSInteger.get(i));
        }
        stream.setItem(COSName.INDEX, indexAsArray);

        int[] wEntry = getWEntry();
        COSArray wAsArray = new COSArray();
        for (int j : wEntry)
        {
            wAsArray.add(COSInteger.get(j));
        }
        stream.setItem(COSName.W, wAsArray);
        
        try (OutputStream outputStream = this.stream.createOutputStream(COSName.FLATE_DECODE))
        {
            writeStreamData(outputStream, wEntry);
            outputStream.flush();
        }
    
        Set<COSName> keySet = this.stream.keySet();
        for ( COSName cosName : keySet )
        {
            // "Other cross-reference stream entries not listed in Table 17 may be indirect; in fact, 
            // some (such as Root in Table 15) shall be indirect."
            if (COSName.ROOT.equals(cosName) || COSName.INFO.equals(cosName) || COSName.PREV.equals(cosName))
            {
                continue;
            }
            // this one too, because it has already been written in COSWriter.doWriteBody()
            if (COSName.ENCRYPT.equals(cosName))
            {
                continue;
            }
            COSBase dictionaryObject = this.stream.getDictionaryObject(cosName);
            dictionaryObject.setDirect(true);
        }
        return this.stream;
    }

    /**
     * Copy all Trailer Information to this file.
     * 
     * @param trailerDict dictionary to be added as trailer info
     */
    public void addTrailerInfo(COSDictionary trailerDict)
    {
        trailerDict.forEach((key, value) ->
        {
            if (COSName.INFO.equals(key) || COSName.ROOT.equals(key) || COSName.ENCRYPT.equals(key) 
                    || COSName.ID.equals(key) || COSName.PREV.equals(key))
            {
                stream.setItem(key, value);
            }
        });
    }

    /**
     * Add an new entry to the XRef stream.
     * 
     * @param entry new entry to be added
     */
    public void addEntry(XReferenceEntry entry)
    {
        if (objectNumbers.contains(entry.getReferencedKey().getNumber()))
        {
            return;
        }
        objectNumbers.add(entry.getReferencedKey().getNumber());
        streamData.add(entry);
    }

    /**
     * determines the minimal length required for all the lengths.
     * 
     * @return the length information
     */
    private int[] getWEntry()
    {
        long[] wMax = new long[3];
        for (XReferenceEntry entry : streamData)
        {
            wMax[0] = Math.max(wMax[0], entry.getFirstColumnValue());
            wMax[1] = Math.max(wMax[1], entry.getSecondColumnValue());
            wMax[2] = Math.max(wMax[2], entry.getThirdColumnValue());
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

    /**
     * Set the size of the XRef stream.
     * 
     * @param streamSize size to bet set as stream size
     */
    public void setSize(long streamSize)
    {
        this.size = streamSize;
    }

    private List<Long> getIndexEntry()
    {
        LinkedList<Long> linkedList = new LinkedList<>();
        Long first = null;
        Long length = null;
        Set<Long> objNumbers = new TreeSet<>();
        // add object number 0 to the set
        objNumbers.add(0L);
        objNumbers.addAll(objectNumbers);
        for ( Long objNumber : objNumbers )
        {
            if (first == null)
            {
                first = objNumber;
                length = 1L;
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
                length = 1L;
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
        Collections.sort(streamData);
        FreeXReference nullEntry = FreeXReference.NULL_ENTRY;
        writeNumber(os, nullEntry.getFirstColumnValue(), w[0]);
        writeNumber(os, nullEntry.getSecondColumnValue(), w[1]);
        writeNumber(os, nullEntry.getThirdColumnValue(), w[2]);
        // iterate over all streamData and write it in the required format
        for (XReferenceEntry entry : streamData)
        {
            writeNumber(os, entry.getFirstColumnValue(), w[0]);
            writeNumber(os, entry.getSecondColumnValue(), w[1]);
            writeNumber(os, entry.getThirdColumnValue(), w[2]);
        }
    }

}
