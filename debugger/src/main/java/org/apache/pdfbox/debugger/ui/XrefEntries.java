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

package org.apache.pdfbox.debugger.ui;

import org.apache.pdfbox.pdmodel.PDDocument;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSObjectKey;

/**
 * Represents an abstract view of the cross references of a pdf.
 *
 */
public class XrefEntries
{
    public static final String PATH = "CRT";

    private final List<Entry<COSObjectKey, Long>> entries;
    private final COSDocument document;
    
    public XrefEntries(PDDocument document)
    {
        Map<COSObjectKey, Long> xrefTable = document.getDocument().getXrefTable();
        entries = xrefTable.entrySet().stream()
                .sorted(Comparator.comparingLong(e -> e.getKey().getNumber()))
                .collect(Collectors.toList());
        this.document = document.getDocument();
    }
    
    public int getXrefEntryCount()
    {
        return entries.size();
    }
    
    public XrefEntry getXrefEntry(int index)
    {
        Entry<COSObjectKey, Long> entry = entries.get(index);
        COSObject objectFromPool = document.getObjectFromPool(entry.getKey());
        return new XrefEntry(index, entry.getKey(), entry.getValue(), objectFromPool);
    }

    public int indexOf(XrefEntry xrefEntry)
    {
        COSObjectKey key = xrefEntry.getKey();
        Entry<COSObjectKey, Long> entry = entries.stream().filter(e -> key.equals(e.getKey()))
                .findFirst().orElse(null);
        return entry != null ? entries.indexOf(entry) : 0;
    }

    @Override
    public String toString()
    {
        return PATH;
    }

}
