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
package org.apache.fontbox.cff.encoding;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the superclass for all CFFFont encodings.
 * 
 * @author Villu Ruusmann
 * @version $Revision$
 */
public abstract class CFFEncoding
{

    private List<Entry> entries = new ArrayList<Entry>();

    /**
     * Determines if the encoding is font specific or not.
     * @return if the encoding is font specific
     */
    public boolean isFontSpecific()
    {
        return false;
    }

    /**
     * Returns the code corresponding to the given SID.
     * @param sid the given SID
     * @return the corresponding code
     */
    public int getCode(int sid)
    {
        for(Entry entry : entries)
        {
            if(entry.entrySID == sid)
            {
                return entry.entryCode;
            }
        }
        return -1;
    }

    /**
     * Returns the SID corresponding to the given code.
     * @param code the given code
     * @return the corresponding SID
     */
    public int getSID(int code)
    {
        for(Entry entry : entries)
        {
            if(entry.entryCode == code)
            {
                return entry.entrySID;
            }
        }
        return -1;
    }

    /**
     * Adds a new code/SID combination to the encoding.
     * @param code the given code
     * @param sid the given SID
     */
    public void register(int code, int sid)
    {
        entries.add(new Entry(code, sid));
    }

    /**
     * Add a single entry.
     * @param entry the entry to be added
     */
    public void addEntry(Entry entry)
    {
        entries.add(entry);
    }

    /**
     * A list of all entries within this encoding.
     * @return a list of all entries
     */
    public List<Entry> getEntries()
    {
        return entries;
    }

    /**
     * This class represents a single code/SID mapping of the encoding.
     *
     */
    public static class Entry
    {
        private int entryCode;
        private int entrySID;

        /**
         * Create a new instance of Entry with the given values.
         * @param code the code
         * @param sid the SID
         */
        protected Entry(int code, int sid)
        {
            this.entryCode = code;
            this.entrySID = sid;
        }

        /**
         * The code of the entry.
         * @return the code
         */
        public int getCode()
        {
            return this.entryCode;
        }

        /**
         * The SID of the entry.
         * @return the SID
         */
        public int getSID()
        {
            return this.entrySID;
        }

        /**
         * {@inheritDoc}
         */
        public String toString()
        {
            return "[code=" + entryCode + ", sid=" + entrySID + "]";
        }
    }
}