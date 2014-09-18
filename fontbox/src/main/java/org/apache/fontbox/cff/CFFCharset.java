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

import java.util.HashMap;
import java.util.Map;

/**
 * A CFF charset. A charset is an array of SIDs/CIDs for all glyphs in the font.
 *
 * todo: split this into two? CFFCharsetType1 and CFFCharsetCID ?
 *
 * @author John Hewson
 */
public abstract class CFFCharset
{
    private final boolean isCIDFont;
    private final Map<Integer, Integer> sidOrCidToGid = new HashMap<Integer, Integer>();
    private final Map<Integer, Integer> gidToSid = new HashMap<Integer, Integer>();
    private final Map<String, Integer> nameToSid = new HashMap<String, Integer>();

    // inverse
    private final Map<Integer, Integer> gidToCid = new HashMap<Integer, Integer>();
    private final Map<Integer, String> gidToName = new HashMap<Integer, String>();

    /**
     * Package-private constructor for use by subclasses.
     *
     * @param isCIDFont true if the parent font is a CIDFont
     */
    CFFCharset(boolean isCIDFont)
    {
        this.isCIDFont = isCIDFont;
    }

    /**
     * Adds a new GID/SID/name combination to the charset.
     *
     * @param gid GID
     * @param sid SID
     */
    public void addSID(int gid, int sid, String name)
    {
        if (isCIDFont)
        {
            throw new IllegalStateException("Not a Type 1-equivalent font");
        }
        sidOrCidToGid.put(sid, gid);
        gidToSid.put(gid, sid);
        nameToSid.put(name, sid);
        gidToName.put(gid, name);
    }

    /**
     * Adds a new GID/CID combination to the charset.
     *
     * @param gid GID
     * @param cid CID
     */
    public void addCID(int gid, int cid)
    {
        if (!isCIDFont)
        {
            throw new IllegalStateException("Not a CIDFont");
        }
        sidOrCidToGid.put(cid, gid);
        gidToCid.put(gid, cid);
    }

    /**
     * Returns the SID for a given GID. SIDs are internal to the font and are not public.
     *
     * @param sid SID
     * @return GID
     */
    int getSIDForGID(int sid)
    {
        if (isCIDFont)
        {
            throw new IllegalStateException("Not a Type 1-equivalent font");
        }
        Integer gid = gidToSid.get(sid);
        if (gid == null)
        {
            return 0;
        }
        return gid;
    }

    /**
     * Returns the GID for the given SID. SIDs are internal to the font and are not public.
     *
     * @param sid SID
     * @return GID
     */
    int getGIDForSID(int sid)
    {
        if (isCIDFont)
        {
            throw new IllegalStateException("Not a Type 1-equivalent font");
        }
        Integer gid = sidOrCidToGid.get(sid);
        if (gid == null)
        {
            return 0;
        }
        return gid;
    }

    /**
     * Returns the GID for a given CID. Returns 0 if the CID is missing.
     *
     * @param cid CID
     * @return GID
     */
    public int getGIDForCID(int cid)
    {
        if (!isCIDFont)
        {
            throw new IllegalStateException("Not a CIDFont");
        }
        Integer gid = sidOrCidToGid.get(cid);
        if (gid == null)
        {
            return 0;
        }
        return gid;
    }

    /**
     * Returns the SID for a given PostScript name, you would think this is not needed,
     * but some fonts have glyphs beyond their encoding with charset SID names.
     *
     * @param name PostScript glyph name
     * @return SID
     */
    int getSID(String name)
    {
        if (isCIDFont)
        {
            throw new IllegalStateException("Not a Type 1-equivalent font");
        }
        Integer sid = nameToSid.get(name);
        if (sid == null)
        {
            return 0;
        }
        return sid;
    }

    /**
     * Returns the PostScript glyph name for the given GID.
     *
     * @param gid GID
     * @return PostScript glyph name
     */
    public String getNameForGID(int gid)
    {
        if (isCIDFont)
        {
            throw new IllegalStateException("Not a Type 1-equivalent font");
        }
        return gidToName.get(gid);
    }

    /**
     * Returns the CID for the given GID.
     *
     * @param gid GID
     * @return CID
     */
    public int getCIDForGID(int gid)
    {
        if (!isCIDFont)
        {
            throw new IllegalStateException("Not a CIDFont");
        }

        Integer cid = gidToCid.get(gid);
        if (cid != null)
        {
            return cid;
        }
        return 0;
    }
}
