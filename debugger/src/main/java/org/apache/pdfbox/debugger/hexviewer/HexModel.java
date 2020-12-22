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

package org.apache.pdfbox.debugger.hexviewer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Khyrul Bashar
 *
 * A class that acts as a model for the hex viewer. It holds the data and provide the data as necessary.
 * It'll let listen for any underlying data changes.
 */
class HexModel implements HexChangeListener
{
    private final List<Byte> data;
    private final List<HexModelChangeListener> modelChangeListeners;

    /**
     * Constructor
     * @param bytes Byte array.
     */
    HexModel(final byte[] bytes)
    {
        data = new ArrayList<>(bytes.length);

        for (final byte b: bytes)
        {
            data.add(b);
        }

        modelChangeListeners = new ArrayList<>();
    }

    /**
     * provides the byte for a specific index of the byte array.
     * @param index int.
     * @return byte instance
     */
    public byte getByte(final int index)
    {
        return data.get(index);
    }

    /**
     * Provides a character array of 16 characters on availability.
     * @param lineNumber int. The line number of the characters. Line counting starts from 1.
     * @return A char array.
     */
    public char[] getLineChars(final int lineNumber)
    {
        int start = (lineNumber-1) * 16;
        final int length = data.size() - start < 16 ? data.size() - start:16;
        final char[] chars = new char[length];

        for (int i = 0; i < chars.length; i++)
        {
            char c = Character.toChars(data.get(start) & 0XFF)[0];
            if (!isAsciiPrintable(c))
            {
                c = '.';
            }
            chars[i] = c;
            start++;
        }
        return chars;
    }

    public byte[] getBytesForLine(final int lineNumber)
    {
        int index = (lineNumber-1) * 16;
        final int length = Math.min(data.size() - index, 16);
        final byte[] bytes = new byte[length];

        for (int i = 0; i < bytes.length; i++)
        {
            bytes[i] = data.get(index);
            index++;
        }
        return bytes;
    }

    /**
     * Provides the size of the model i.e. size of the input.
     * @return int value.
     */
    public int size()
    {
        return data.size();
    }

    /**
     *
     * @return
     */
    public int totalLine()
    {
        return size() % 16 != 0 ? size()/16 + 1 : size()/16;
    }

    public static int lineNumber(final int index)
    {
        final int elementNo = index + 1;
        return elementNo % 16 != 0 ? elementNo/16 + 1 : elementNo/16;
    }

    public static int elementIndexInLine(final int index)
    {
        return index%16;
    }

    private static boolean isAsciiPrintable(final char ch)
    {
        return ch >= 32 && ch < 127;
    }

    public void addHexModelChangeListener(final HexModelChangeListener listener)
    {
        modelChangeListeners.add(listener);
    }

    public void updateModel(final int index, final byte value)
    {
        if (!data.get(index).equals(value))
        {
            data.set(index, value);
            fireModelChanged(index);
        }
    }

    @Override
    public void hexChanged(final HexChangedEvent event)
    {
        final int index = event.getByteIndex();
        if (index != -1 && getByte(index) != event.getNewValue())
        {
            data.set(index, event.getNewValue());
        }
        fireModelChanged(index);
    }

    private void fireModelChanged(final int index)
    {
        modelChangeListeners.forEach(listener ->
                listener.hexModelChanged(new HexModelChangedEvent(index, HexModelChangedEvent.SINGLE_CHANGE)));
    }
}
