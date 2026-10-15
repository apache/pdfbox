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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link COSUpdateInfo}.
 */
class TestCOSUpdateInfo
{
    
    /**
     * Tests isNeedToBeUpdate() and setNeedToBeUpdate() - tests the getter/setter methods.
     */
    @Test
    void testIsSetNeedToBeUpdate()
    {
        COSDocumentState origin = new COSDocumentState();
        origin.setParsing(false);
        // COSDictionary
        COSUpdateInfo testCOSDictionary = new COSDictionary();
        testCOSDictionary.setNeedToBeUpdated(true);
        assertFalse(testCOSDictionary.isNeedToBeUpdated());
        testCOSDictionary.getUpdateState().setOriginDocumentState(origin);
        testCOSDictionary.setNeedToBeUpdated(true);
        assertTrue(testCOSDictionary.isNeedToBeUpdated());
        testCOSDictionary.setNeedToBeUpdated(false);
        assertFalse(testCOSDictionary.isNeedToBeUpdated());

        // COSObject
        COSUpdateInfo testCOSObject;
        testCOSObject = new COSObject(null);
        testCOSObject.setNeedToBeUpdated(true);
        assertFalse(testCOSObject.isNeedToBeUpdated());
        testCOSObject.getUpdateState().setOriginDocumentState(origin);
        testCOSObject.setNeedToBeUpdated(true);
        assertTrue(testCOSObject.isNeedToBeUpdated());
        testCOSObject.setNeedToBeUpdated(false);
        assertFalse(testCOSObject.isNeedToBeUpdated());
    }

    /**
     * PDFBOX-6200: linking a document state to a deeply nested structure must not overflow the
     * stack.
     */
    @Test
    void testSetOriginDocumentStateOfDeeplyNestedStructure()
    {
        COSDocumentState origin = new COSDocumentState();
        origin.setParsing(false);

        // a chain of dictionaries, arrays and dereferenced indirect objects, built from the end
        COSDictionary deepest = new COSDictionary();
        COSBase current = deepest;
        for (int i = 0; i < 50_000; i++)
        {
            switch (i % 3)
            {
                case 0:
                    COSDictionary dictionary = new COSDictionary();
                    dictionary.setItem(COSName.getPDFName("Next"), current);
                    current = dictionary;
                    break;
                case 1:
                    COSArray array = new COSArray();
                    array.add(current);
                    current = array;
                    break;
                default:
                    current = new COSObject(current);
                    break;
            }
        }
        COSUpdateInfo root = (COSUpdateInfo) current;

        root.getUpdateState().setOriginDocumentState(origin);

        assertSame(origin, deepest.getUpdateState().getOriginDocumentState());
        assertTrue(root.isNeedToBeUpdated());
        assertTrue(deepest.isNeedToBeUpdated());
    }
}
