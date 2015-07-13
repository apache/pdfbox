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

package org.apache.pdfbox.pdmodel.font;

import org.apache.fontbox.FontBoxFont;

/**
 * Information about a font on the system.
 *
 * @author John Hewson
 */
public abstract class FontInfo
{
    /**
     * Returns the PostScript name of the font.
     */
    public abstract String getPostScriptName();

    /**
     * Returns the font's format.
     */
    public abstract FontFormat getFormat();

    /**
     * Returns the CIDSystemInfo associated with the font, if any.
     */
    public abstract CIDSystemInfo getCIDSystemInfo();

    /**
     * Returns a new FontBox font instance for the font. Implementors of this method must not
     * cache the return value of this method unless doing so via the current {@link FontCache}.
     */
    public abstract FontBoxFont getFont();

    /**
     * Returns the sFamilyClass field of the "OS/2" table, or -1.
     */
    public abstract int getFamilyClass();

    /**
     * Returns the usWeightClass field of the "OS/2" table, or -1.
     */
    public abstract int getWeightClass();

    /**
     * Returns the usWeightClass field as a Panose Weight.
     */
    final int getWeightClassAsPanose()
    {
        int usWeightClass = getWeightClass();
        switch (usWeightClass)
        {
            case -1: return 0;
            case 0: return 0;
            case 100: return 2;
            case 200: return 3;
            case 300: return 4;
            case 400: return 5;
            case 500: return 6;
            case 600: return 7;
            case 700: return 8;
            case 800: return 9;
            case 900: return 10;
            default: return 0;
        }
    }

    /**
     * Returns the ulCodePageRange1 field of the "OS/2" table, or 0.
     */
    public abstract int getCodePageRange1();

    /**
     * Returns the ulCodePageRange2 field of the "OS/2" table, or 0.
     */
    public abstract int getCodePageRange2();

    /**
     * Returns the ulCodePageRange1 and ulCodePageRange1 field of the "OS/2" table, or 0.
     */
    final long getCodePageRange()
    {
        long range1 = getCodePageRange1() & 0x00000000ffffffffL;
        long range2 = getCodePageRange2() & 0x00000000ffffffffL;
        return range2 << 32 | range1;
    }

    /**
     * Returns the macStyle field of the "head" table, or -1.
     */
    public abstract int getMacStyle();

    /**
     * Returns the Panose classification of the font, if any.
     */
    public abstract PDPanoseClassification getPanose();
    
    // todo: 'post' table for Italic. Also: OS/2 fsSelection for italic/bold.
    // todo: ulUnicodeRange too?
    
    @Override
    public String toString()
    {
        return getPostScriptName() + " (" + getFormat() +
                ", mac: 0x" + Integer.toHexString(getMacStyle()) +
                ", os/2: 0x" + Integer.toHexString(getFamilyClass()) +
                ", cid: " + getCIDSystemInfo() + ")";
    }
}
