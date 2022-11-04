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
package org.apache.fontbox.cff;

/**
 * A CFF charset. A charset is an array of SIDs/CIDs for all glyphs in the font.
 *
 * @author John Hewson
 */
public interface CFFCharset
{
    /**
     * Indicates if the charset belongs to a CID font.
     * 
     * @return true for CID fonts
     */
    public abstract boolean isCIDFont();
    
    /**
     * Adds a new GID/SID/name combination to the charset.
     *
     * @param gid GID
     * @param sid SID
     * @param name the postscript name of the glyph
     */
    public abstract void addSID(int gid, int sid, String name);

    /**
     * Adds a new GID/CID combination to the charset.
     *
     * @param gid GID
     * @param cid CID
     */
    public abstract void addCID(int gid, int cid);

    /**
     * Returns the SID for a given GID. SIDs are internal to the font and are not public.
     *
     * @param gid GID
     * @return SID
     */
    public abstract int getSIDForGID(int gid);

    /**
     * Returns the GID for the given SID. SIDs are internal to the font and are not public.
     *
     * @param sid SID
     * @return GID
     */
    public abstract int getGIDForSID(int sid);

    /**
     * Returns the GID for a given CID. Returns 0 if the CID is missing.
     *
     * @param cid CID
     * @return GID
     */
    public abstract int getGIDForCID(int cid);

    /**
     * Returns the SID for a given PostScript name, you would think this is not needed,
     * but some fonts have glyphs beyond their encoding with charset SID names.
     *
     * @param name PostScript glyph name
     * @return SID
     */
    public abstract int getSID(String name);

    /**
     * Returns the PostScript glyph name for the given GID.
     *
     * @param gid GID
     * @return PostScript glyph name
     */
    public abstract String getNameForGID(int gid);

    /**
     * Returns the CID for the given GID.
     *
     * @param gid GID
     * @return CID
     */
    public abstract int getCIDForGID(int gid);
}
