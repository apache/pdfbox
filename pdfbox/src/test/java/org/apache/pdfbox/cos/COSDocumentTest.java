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
package org.apache.pdfbox.cos;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;

class COSDocumentTest
{
    @Test
    void testPDFBox6132()
    {
        COSDocument document = new COSDocument();
        Map<COSObjectKey, Long> xrefTable = new HashMap<>();
        xrefTable.put(null, 10L);
        document.addXRefTable(xrefTable);
        assertEquals(Collections.emptyList(), document.getObjectsByType(COSName.T));
        assertNull(document.getLinearizedDictionary());
    }

    @Test
    void testGetObjectKeyFollowsXrefTable()
    {
        COSDocument document = new COSDocument();
        Map<COSObjectKey, Long> xrefTable = document.getXrefTable();
        COSObjectKey missing = document.getObjectKey(4, 0);
        assertEquals(new COSObjectKey(4, 0), missing);
        assertEquals(-1, missing.getStreamIndex());

        COSObjectKey indexed = new COSObjectKey(4, 0, 2);
        xrefTable.put(indexed, -1L);
        assertSame(indexed, document.getObjectKey(4, 0));

        // a replaced entry must hand back the new key, not the one the map first stored
        COSObjectKey replaced = new COSObjectKey(4, 0, 1);
        xrefTable.put(replaced, -1L);
        assertSame(replaced, document.getObjectKey(4, 0));
        assertEquals(1, xrefTable.size());

        xrefTable.remove(new COSObjectKey(4, 0));
        assertNotSame(replaced, document.getObjectKey(4, 0));

        Map<COSObjectKey, Long> added = new HashMap<>();
        added.put(indexed, 7L);
        added.put(new COSObjectKey(5, 0, 0), 7L);
        document.addXRefTable(added);
        assertSame(indexed, document.getObjectKey(4, 0));
        assertEquals(0, document.getObjectKey(5, 0).getStreamIndex());

        xrefTable.clear();
        assertEquals(-1, document.getObjectKey(4, 0).getStreamIndex());
        assertEquals(-1, document.getObjectKey(5, 0).getStreamIndex());
    }

    @Test
    void testXrefTableViewsAreLive()
    {
        COSDocument document = new COSDocument();
        Map<COSObjectKey, Long> xrefTable = document.getXrefTable();
        for (int i = 1; i <= 6; i++)
        {
            xrefTable.put(new COSObjectKey(i, 0, i), (long) i);
        }
        xrefTable.keySet().remove(new COSObjectKey(1, 0));
        xrefTable.values().remove(2L);
        xrefTable.entrySet().removeIf(e -> e.getKey().getNumber() == 3);
        Iterator<Entry<COSObjectKey, Long>> it = xrefTable.entrySet().iterator();
        while (it.hasNext())
        {
            if (it.next().getKey().getNumber() == 4)
            {
                it.remove();
            }
        }
        assertEquals(2, xrefTable.size());
        for (int i = 1; i <= 4; i++)
        {
            assertEquals(-1, document.getObjectKey(i, 0).getStreamIndex(), "entry " + i);
        }
        assertEquals(5, document.getObjectKey(5, 0).getStreamIndex());
        assertEquals(6, document.getObjectKey(6, 0).getStreamIndex());

        xrefTable.entrySet().clear();
        assertEquals(0, xrefTable.size());
        assertEquals(-1, document.getObjectKey(5, 0).getStreamIndex());
    }
}
