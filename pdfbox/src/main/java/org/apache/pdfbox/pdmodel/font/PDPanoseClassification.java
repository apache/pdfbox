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

/**
 * Represents a 10-byte <a href="http://monotype.de/services/pan2">PANOSE classification</a>.
 *
 * @author John Hewson
 */
public class PDPanoseClassification
{
    private final byte[] bytes;

    PDPanoseClassification(byte[] bytes)
    {
        this.bytes = bytes;
    }
    
    public int getFamilyKind()
    {
        return bytes[0];
    }

    public int getSerifStyle()
    {
        return bytes[1];
    }

    public int getWeight()
    {
        return bytes[2];
    }

    public int getProportion()
    {
        return bytes[3];
    }

    public int getContrast()
    {
        return bytes[4];
    }

    public int getStrokeVariation()
    {
        return bytes[5];
    }

    public int getArmStyle()
    {
        return bytes[6];
    }
    
    public int getLetterform()
    {
        return bytes[7];
    }

    public int getMidline()
    {
        return bytes[8];
    }

    public int getXHeight()
    {
        return bytes[9];
    }

    public byte[] getBytes()
    {
        return bytes;
    }
    
    @Override
    public String toString()
    {
        return "{ FamilyType = " + getFamilyKind() + ", " +
                 "SerifStyle = " + getSerifStyle() + ", " +
                 "Weight = " + getWeight() + ", " +
                 "Proportion = " + getProportion() + ", " + 
                 "Contrast = " + getContrast() + ", " +
                 "StrokeVariation = " + getStrokeVariation() + ", " +
                 "ArmStyle = " + getArmStyle() + ", " +
                 "Letterform = " + getLetterform() + ", " +
                 "Midline = " + getMidline() + ", " +
                 "XHeight = " + getXHeight() + "}";
    }
}
