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
package org.apache.pdfbox.pdmodel.graphics.form;

import org.apache.pdfbox.cos.COSDocumentState;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;

public class PDFormXObjectTest
{

    /**
     * PDFBOX-6271: check that a correct form xobject doesn't get dirty.
     */
    @Test
    void testDontDirtyOnConstruction()
    {
        // setup - build a cosstream that will accept update flags:
        COSStream s = new COSStream();
        COSDocumentState state = new COSDocumentState();
        state.setParsing(false);
        s.getUpdateState().setOriginDocumentState(state);

        // set type/subtype correctly for an XObject:
        // (this stands in for an XObject that was parsed from an existing document)
        s.setItem(COSName.TYPE, COSName.XOBJECT);
        s.setItem(COSName.SUBTYPE, COSName.FORM);
        s.setNeedToBeUpdated(false);

        // construct a PDFormXObject wrapper around it:
        new PDFormXObject(s);

        assertFalse(s.isNeedToBeUpdated());
    }
}
