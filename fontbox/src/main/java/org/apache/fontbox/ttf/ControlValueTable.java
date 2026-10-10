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
package org.apache.fontbox.ttf;

import java.io.IOException;

/**
 * The 'cvt ' (Control Value) table. It holds an array of reference values - stem widths, heights and
 * similar control measurements - used by the TrueType hinting bytecode. The values are stored here in
 * raw font units (signed FWords); they are scaled to the active ppem by the interpreter, not at parse
 * time.
 *
 * @author Apache PDFBox
 */
public class ControlValueTable extends TTFTable
{
    /**
     * A tag that identifies this table type.
     */
    public static final String TAG = "cvt ";

    private int[] values;

    ControlValueTable()
    {
    }

    /**
     * This will read the required data from the stream.
     *
     * @param ttf The font that is being read.
     * @param data The stream to read the data from.
     * @throws IOException If there is an error reading the data.
     */
    @Override
    void read(TrueTypeFont ttf, TTFDataStream data) throws IOException
    {
        int count = (int) (getLength() / 2);
        int[] cvt = new int[count];
        for (int i = 0; i < count; i++)
        {
            cvt[i] = data.readSignedShort();
        }
        values = cvt;
        initialized = true;
    }

    /**
     * Returns the raw control values in font units (FWords). The interpreter scales these to the
     * active ppem.
     *
     * @return the control values in font units
     */
    public int[] getValues()
    {
        return values;
    }

    /**
     * Returns the number of control values in this table.
     *
     * @return the entry count
     */
    public int getValueCount()
    {
        return values != null ? values.length : 0;
    }
}