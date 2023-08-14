/*
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.apache.pdfbox.debugger.flagbitspane;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;

/**
 * @author Tilman Hausherr
 *
 * A class that provides signature flag bits.
 */
public class SigFlag extends Flag
{
    private final PDDocument document;
    private final COSDictionary acroFormDictionary;

    /**
     * Constructor
     *
     * @param acroFormDictionary COSDictionary instance.
     */
    SigFlag(PDDocument document, COSDictionary acroFormDictionary)
    {
        this.document = document;
        this.acroFormDictionary = acroFormDictionary;
    }

    @Override
    String getFlagType()
    {
        return "Signature flag";
    }

    @Override
    String getFlagValue()
    {
        return "Flag value: " + acroFormDictionary.getInt(COSName.SIG_FLAGS);
    }

    @Override
    Object[][] getFlagBits()
    {
        PDAcroForm acroForm = new PDAcroForm(document, acroFormDictionary);
        return new Object[][]{
                new Object[]{1, "SignaturesExist", acroForm.isSignaturesExist()},
                new Object[]{2, "AppendOnly", acroForm.isAppendOnly()},
        };
    }
}
