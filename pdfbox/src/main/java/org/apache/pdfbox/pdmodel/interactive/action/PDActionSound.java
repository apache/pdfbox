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

package org.apache.pdfbox.pdmodel.interactive.action;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSBoolean;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSStream;

/**
 * This represents a Sound action that can be executed in a PDF document.
 *
 * @author Timur Kamalov
 * @author Tilman Hausherr
 */
public class PDActionSound extends PDAction
{

    /**
     * This type of action this object represents.
     */
    public static final String SUB_TYPE = "Sound";

    /**
     * Default constructor.
     */
    public PDActionSound()
    {
        action = new COSDictionary();
        setSubType(SUB_TYPE);
    }

    /**
     * Constructor.
     *
     * @param a The action dictionary.
     */
    public PDActionSound(COSDictionary a)
    {
        super(a);
    }

    /**
     * Sets the sound object.
     * 
     * @param sound the sound object defining the sound that shall be played.
     */
    public void setSound(COSStream sound)
    {
        action.setItem(COSName.SOUND, sound);
    }

    /**
     * Gets the sound object.
     * 
     * @return The sound object defining the sound that shall be played.
     */
    public COSStream getSound()
    {
        COSBase base = action.getDictionaryObject(COSName.SOUND);
        if (base instanceof COSStream)
        {
            return (COSStream) base;
        }
        return null;
    }
    
    /**
     * Gets the volume at which to play the sound, in the range −1.0 to 1.0.
     *
     * @param volume The volume at which to play the sound, in the range −1.0 to 1.0.
     * 
     * @throws IllegalArgumentException if the volume parameter is outside of the range −1.0 to 1.0.
     */
    public void setVolume(float volume)
    {
        if (volume < -1 || volume > 1)
        {
            throw new IllegalArgumentException("volume outside of the range −1.0 to 1.0");
        }
        action.setFloat(COSName.VOLUME, volume);
    }

    /**
     * Sets the volume.
     *
     * @return The volume at which to play the sound, in the range −1.0 to 1.0. Default value: 1.0.
     */
    public float getVolume()
    {
        COSBase base = action.getDictionaryObject(COSName.VOLUME);
        if (base instanceof COSNumber)
        {
            float volume = ((COSNumber) base).floatValue();
            if (volume < -1 || volume > 1)
            {
                volume = 1;
            }
            return volume;
        }
        return 1;
    }
    
    /**
     * A flag specifying whether to play the sound synchronously or asynchronously. When true, the
     * reader allows no further user interaction other than canceling the sound until the sound has
     * been completely played.
     *
     * @param synchronous Whether to play the sound synchronously (true) or asynchronously (false).
     */
    public void setSynchronous(boolean synchronous)
    {
        action.setBoolean(COSName.SYNCHRONOUS, synchronous);
    }

    /**
     * Gets the synchronous flag. It specifyes whether to play the sound synchronously or
     * asynchronously. When true, the reader allows no further user interaction other than canceling
     * the sound until the sound has been completely played.
     *
     * @return Whether to play the sound synchronously (true) or asynchronously (false, also the
     * default).
     */
    public boolean getSynchronous()
    {
        COSBase base = action.getDictionaryObject(COSName.SYNCHRONOUS);
        if (base instanceof COSBoolean)
        {
            return ((COSBoolean) base).getValue();
        }
        return false;
    }
    
    /**
     * A flag specifying whether to repeat the sound indefinitely.
     *
     * @param repeat Whether to repeat the sound indefinitely.
     */
    public void setRepeat(boolean repeat)
    {
        action.setBoolean(COSName.REPEAT, repeat);
    }

    /**
     * Gets whether to repeat the sound indefinitely.
     *
     * @return Whether to repeat the sound indefinitely (default: false).
     */
    public boolean getRepeat()
    {
        COSBase base = action.getDictionaryObject(COSName.REPEAT);
        if (base instanceof COSBoolean)
        {
            return ((COSBoolean) base).getValue();
        }
        return false;
    }

    /**
     * The flag specifying whether to mix this sound with any other sound already playing. If this
     * flag is false, any previously playing sound shall be stopped before starting this sound; this
     * can be used to stop a repeating sound (see Repeat). Default value: false.
     *
     * @param mix whether to mix this sound with any other sound already playing.
     * (false).
     */
    public void setMix(boolean mix)
    {
        action.setBoolean(COSName.MIX, mix);
    }

    /**
     * Gets the flag specifying whether to mix this sound with any other sound already playing. If
     * this flag is false, any previously playing sound shall be stopped before starting this sound;
     * this can be used to stop a repeating sound (see Repeat).
     *
     * @return whether to mix this sound with any other sound already playing (default: false).
     */
    public boolean getMix()
    {
        COSBase base = action.getDictionaryObject(COSName.MIX);
        if (base instanceof COSBoolean)
        {
            return ((COSBoolean) base).getValue();
        }
        return false;
    }
}
