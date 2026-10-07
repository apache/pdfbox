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
package org.apache.pdfbox.pdmodel.interactive.annotation;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocumentState;
import org.apache.pdfbox.cos.COSName;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;

class PDAnnotationWidgetTest
{

    /**
     * PDFBOX-6270: check that a correct annotation doesn't get dirty.
     */
    @Test
    void testDontDirtyOnConstruction()
    {
        // setup - build a cosstream that will accept update flags:
        COSDictionary dict = new COSDictionary();
        COSDocumentState state = new COSDocumentState();
        state.setParsing(false);
        dict.getUpdateState().setOriginDocumentState(state);

        // set subtype correctly:
        // (this stands in for an annotation widget that was parsed from an existing document)
        dict.setItem(COSName.TYPE, COSName.ANNOT);
        dict.setName(COSName.SUBTYPE, PDAnnotationWidget.SUB_TYPE);
        dict.setNeedToBeUpdated(false);
        assertFalse(dict.isNeedToBeUpdated());

        // construct a PDAnnotationWidget wrapper around it:
        new PDAnnotationWidget(dict);
        
        assertFalse(dict.isNeedToBeUpdated());
    }
}
