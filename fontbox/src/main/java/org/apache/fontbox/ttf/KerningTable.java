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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A 'kern' table in a true type font.
 *
 * @author Glenn Adams
 */
public class KerningTable extends TTFTable
{
    private static final Logger LOG = LogManager.getLogger(KerningTable.class);

    /**
     * Tag to identify this table.
     */
    public static final String TAG = "kern";

    private KerningSubtable[] subtables;

    KerningTable()
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
        int version = data.readUnsignedShort();
        if (version != 0)
        {
            version = (version << 16) | data.readUnsignedShort();
        }
        int numSubtables = 0;
        switch (version)
        {
            case 0:
                numSubtables = data.readUnsignedShort();
                break;
            case 1:
                numSubtables = (int) data.readUnsignedInt();
                break;
            default:
                LOG.debug("Skipped kerning table due to an unsupported kerning table version: {}",
                        version);
                break;
        }
        if (numSubtables > 0)
        {
            subtables = new KerningSubtable[numSubtables];
            for (int i = 0; i < numSubtables; ++i)
            {
                KerningSubtable subtable = new KerningSubtable();
                subtable.read(data, version);
                subtables[i] = subtable;
            }
        }
        initialized = true;
    }

    /**
     * Obtain first subtable that supports non-cross-stream horizontal kerning.
     *
     * @return first matching subtable or null if none found
     */
    public KerningSubtable getHorizontalKerningSubtable()
    {
        return getHorizontalKerningSubtable(false);
    }

    /**
     * Obtain first subtable that supports horizontal kerning with specified cross stream.
     *
     * @param cross true if requesting cross stream horizontal kerning
     * @return first matching subtable or null if none found
     */
    public KerningSubtable getHorizontalKerningSubtable(boolean cross)
    {
        if (subtables != null)
        {
            for (KerningSubtable s : subtables)
            {
                if (s.isHorizontalKerning(cross))
                {
                    return s;
                }
            }
        }
        return null;
    }
}
