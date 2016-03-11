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
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;

/**
 * @author Tilman Hausherr
 *
 * A class that provides Encrypt flag bits.
 */
public class EncryptFlag extends Flag
{
    private final COSDictionary encryptDictionary;

    /**
     * Constructor
     * @param encryptDict COSDictionary instance.
     */
    EncryptFlag(COSDictionary encryptDict)
    {
        encryptDictionary = encryptDict;
    }

    @Override
    String getFlagType()
    {
        return "Encrypt flag";
    }

    @Override
    String getFlagValue()
    {
        return "Flag value:" + encryptDictionary.getInt(COSName.P);
    }

    @Override
    Object[][] getFlagBits()
    {
        AccessPermission ap = new AccessPermission(encryptDictionary.getInt(COSName.P));
        return new Object[][]{
                new Object[]{3, "can print", ap.canPrint()},
                new Object[]{4, "can modify", ap.canModify()},
                new Object[]{5, "can extract content", ap.canExtractContent()},
                new Object[]{6, "can modify annotations", ap.canModifyAnnotations()},
                new Object[]{9, "can fill in form fields", ap.canFillInForm()},
                new Object[]{10, "can extract for accessibility", ap.canExtractForAccessibility()},
                new Object[]{11, "can assemble document", ap.canAssembleDocument()},
                new Object[]{12, "can print degraded", ap.canPrintDegraded()},
        };
    }
}
