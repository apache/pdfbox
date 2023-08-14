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

package org.apache.fontbox.ttf.table.common;

/**
 * This class models the
 * <a href="https://docs.microsoft.com/en-us/typography/opentype/spec/chapter2#lookup-table">Lookup Table</a> in the
 * Open Type layout common tables.
 * 
 * @author Palash Ray
 *
 */
public class LookupTable
{
    private final int lookupType;
    private final int lookupFlag;
    private final int markFilteringSet;
    private final LookupSubTable[] subTables;

    public LookupTable(int lookupType, int lookupFlag, int markFilteringSet,
            LookupSubTable[] subTables)
    {
        this.lookupType = lookupType;
        this.lookupFlag = lookupFlag;
        this.markFilteringSet = markFilteringSet;
        this.subTables = subTables;
    }

    public int getLookupType()
    {
        return lookupType;
    }

    public int getLookupFlag()
    {
        return lookupFlag;
    }

    public int getMarkFilteringSet()
    {
        return markFilteringSet;
    }

    public LookupSubTable[] getSubTables()
    {
        return subTables;
    }

    @Override
    public String toString()
    {
        return String.format("LookupTable[lookupType=%d,lookupFlag=%d,markFilteringSet=%d]",
                lookupType, lookupFlag, markFilteringSet);
    }
}
