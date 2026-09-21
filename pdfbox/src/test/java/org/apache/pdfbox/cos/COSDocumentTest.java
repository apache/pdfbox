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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;

class COSDocumentTest
{
    @Test
    void testPDFBox6132() throws IOException
    {
        try (COSDocument document = new COSDocument())
        {
            Map<COSObjectKey, Long> xrefTable = new HashMap<>();
            xrefTable.put(null, 10L);
            document.addXRefTable(xrefTable);
            document.getXrefTable().put(null, 11L);
            assertEquals(Collections.emptyList(), document.getObjectsByType(COSName.T));
            assertNull(document.getLinearizedDictionary());
            assertNull(document.getXrefKey(4, 0));
        }
    }

    @Test
    void testGetXrefKeyFollowsXrefTable() throws IOException
    {
        try (COSDocument document = new COSDocument())
        {
            Map<COSObjectKey, Long> xrefTable = document.getXrefTable();
            assertNull(document.getXrefKey(4, 0));

            COSObjectKey indexed = new COSObjectKey(4, 0, 2);
            xrefTable.put(indexed, 100L);
            assertSame(indexed, document.getXrefKey(4, 0));

            // like any HashMap, putting an equal key keeps the first instance
            xrefTable.put(new COSObjectKey(4, 0, 1), 200L);
            assertEquals(1, xrefTable.size());
            assertSame(indexed, xrefTable.keySet().iterator().next());
            assertSame(indexed, document.getXrefKey(4, 0));
            assertEquals(200L, xrefTable.get(new COSObjectKey(4, 0)));

            xrefTable.remove(new COSObjectKey(4, 0));
            assertNull(document.getXrefKey(4, 0));

            COSObjectKey reinserted = new COSObjectKey(4, 0, 9);
            xrefTable.put(reinserted, 300L);
            Map<COSObjectKey, Long> added = new HashMap<>();
            added.put(new COSObjectKey(4, 0, 2), 400L);
            added.put(new COSObjectKey(5, 0, 0), 500L);
            document.addXRefTable(added);
            assertSame(reinserted, document.getXrefKey(4, 0),
                    "present key keeps its instance on putAll");
            assertEquals(400L, xrefTable.get(reinserted));
            assertEquals(0, document.getXrefKey(5, 0).getStreamIndex(),
                    "new key indexed on putAll");

            COSObjectKey viaPutIfAbsent = new COSObjectKey(6, 0, 3);
            xrefTable.putIfAbsent(viaPutIfAbsent, 600L);
            xrefTable.putIfAbsent(new COSObjectKey(6, 0, 8), 601L);
            assertSame(viaPutIfAbsent, document.getXrefKey(6, 0));
            COSObjectKey viaCompute = new COSObjectKey(7, 0, 4);
            xrefTable.computeIfAbsent(viaCompute, k -> 700L);
            assertSame(viaCompute, document.getXrefKey(7, 0));
            xrefTable.compute(new COSObjectKey(8, 0, 5), (k, v) -> null);
            assertNull(document.getXrefKey(8, 0), "compute that stores nothing indexes nothing");
            COSObjectKey viaCompute2 = new COSObjectKey(8, 0, 6);
            xrefTable.compute(viaCompute2, (k, v) -> 800L);
            assertSame(viaCompute2, document.getXrefKey(8, 0));
            COSObjectKey viaMerge = new COSObjectKey(9, 0, 7);
            xrefTable.merge(viaMerge, 900L, Long::sum);
            assertSame(viaMerge, document.getXrefKey(9, 0));
            xrefTable.merge(new COSObjectKey(7, 0), 1L, (a, b) -> null);
            assertNull(document.getXrefKey(7, 0), "key removed by merge");

            xrefTable.clear();
            assertNull(document.getXrefKey(4, 0), "key 4 after clear");
            assertNull(document.getXrefKey(5, 0), "key 5 after clear");
        }
    }

    @Test
    void testViewRemovalsDropStaleKeys() throws IOException
    {
        try (COSDocument document = new COSDocument())
        {
            Map<COSObjectKey, Long> xrefTable = document.getXrefTable();
            for (int i = 1; i <= 6; i++)
            {
                xrefTable.put(new COSObjectKey(i, 0, i), i * 100L);
            }
            xrefTable.keySet().remove(new COSObjectKey(1, 0));
            xrefTable.values().remove(200L);
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
                assertNull(document.getXrefKey(i, 0), "entry " + i);
            }
            assertEquals(5, document.getXrefKey(5, 0).getStreamIndex());
            assertEquals(6, document.getXrefKey(6, 0).getStreamIndex());

            // a key inserted again after a view removal replaces the stale index entry
            COSObjectKey reinserted = new COSObjectKey(1, 0, 7);
            xrefTable.put(reinserted, 101L);
            assertSame(reinserted, document.getXrefKey(1, 0));
            COSObjectKey computed = new COSObjectKey(2, 0, 8);
            xrefTable.computeIfAbsent(computed, k -> 201L);
            assertSame(computed, document.getXrefKey(2, 0));

            xrefTable.entrySet().clear();
            assertEquals(0, xrefTable.size());
            assertNull(document.getXrefKey(5, 0));
        }
    }

    @Test
    void testXrefTableCloneHasOwnIndex() throws ReflectiveOperationException, IOException
    {
        try (COSDocument document = new COSDocument())
        {
            Map<COSObjectKey, Long> xrefTable = document.getXrefTable();
            COSObjectKey key = new COSObjectKey(4, 0, 2);
            xrefTable.put(key, 100L);
            @SuppressWarnings("unchecked")
            Map<COSObjectKey, Long> copy = (Map<COSObjectKey, Long>) xrefTable.getClass()
                    .getMethod("clone").invoke(xrefTable);
            assertEquals(xrefTable, copy);
            // the clone has its own index: mutating it must not affect the original
            copy.remove(key);
            assertSame(key, document.getXrefKey(4, 0));
            copy.put(new COSObjectKey(4, 0, 7), 101L);
            assertSame(key, document.getXrefKey(4, 0));
        }
    }
}
