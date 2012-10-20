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
package org.apache.fontbox.cff.charset;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the superclass for all CFFFont charsets.
 * 
 * @author Villu Ruusmann
 * @version $Revision$
 */
public abstract class CFFCharset
{
    private List<Entry> entries = new ArrayList<Entry>();

    /**
     * Determines if the charset is font specific or not.
     * @return if the charset is font specific
     */
    public boolean isFontSpecific()
    {
        return false;
    }

    /**
     * Returns the SID corresponding to the given name.
     * @param name the given SID
     * @return the corresponding SID
     */
    public int getSID(String name)
    {
        for(Entry entry : this.entries)
        {
            if((entry.entryName).equals(name))
            {
                return entry.entrySID;
            }
        }
        return -1;
    }

    /**
     * Returns the name corresponding to the given SID.
     * @param sid the given SID
     * @return the corresponding name
     */
    public String getName(int sid)
    {
        for(Entry entry : this.entries)
        {
            if(entry.entrySID == sid)
            {
                return entry.entryName;
            }
        }
        return null;
    }

    /**
     * Adds a new SID/name combination to the charset.
     * @param sid the given SID
     * @param name the given name
     */
    public void register(int sid, String name)
    {
        entries.add(new Entry(sid,name));
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
     * A list of all entries within this charset.
     * @return a list of all entries
     */
    public List<Entry> getEntries()
    {
        return entries;
    }

    /**
     * This class represents a single SID/name mapping of the charset.
     *
     */
    public static class Entry
    {
        private int entrySID;
        private String entryName;

        /**
         * Create a new instance of Entry with the given values.
         * @param sid the SID
         * @param name the Name
         */
        protected Entry(int sid, String name) 
        {
            this.entrySID = sid;
            this.entryName = name;
        }

        /**
         * The SID of this entry.
         * @return the SID
         */
        public int getSID()
        {
            return entrySID;
        }

        /**
         * The Name of this entry.
         * @return the name
         */
        public String getName()
        {
            return entryName;
        }
        
        /**
         * {@inheritDoc}
         */
        public String toString()
        {
            return "[sid=" + entrySID + ", name=" + entryName + "]";
        }
    }
}