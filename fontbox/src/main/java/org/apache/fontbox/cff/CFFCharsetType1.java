package org.apache.fontbox.cff;

import java.util.HashMap;
import java.util.Map;
/**
 * A CFF charset. A charset is an array of CIDs for all glyphs in the font.
 *
 * @author Valery Bokov
 */
class CFFCharsetType1 extends CFFCharset
{
    private final Map<Integer, Integer> sidOrCidToGid = new HashMap<>(250);
    private final Map<Integer, Integer> gidToSid = new HashMap<>(250);
    private final Map<String, Integer> nameToSid = new HashMap<>(250);
    // inverse
    private final Map<Integer, String> gidToName = new HashMap<>(250);

    /**
     * Indicates if the charset belongs to a CID font.
     *
     * @return true for CID fonts
     */
    @Override
    public boolean isCIDFont()
    {
        return false;
    }

    /**
     * Adds a new GID/SID/name combination to the charset.
     *
     * @param gid GID
     * @param sid SID
     */
    @Override
    public void addSID(int gid, int sid, String name)
    {
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
    @Override
    public void addCID(int gid, int cid)
    {
        throw new IllegalStateException("Not a CIDFont");
    }

    /**
     * Returns the SID for a given GID. SIDs are internal to the font and are not public.
     *
     * @param sid SID
     * @return GID
     */
    @Override
    int getSIDForGID(int sid)
    {
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
    @Override
    int getGIDForSID(int sid)
    {
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
    @Override
    public int getGIDForCID(int cid)
    {
        throw new IllegalStateException("Not a CIDFont");
    }

    /**
     * Returns the SID for a given PostScript name, you would think this is not needed,
     * but some fonts have glyphs beyond their encoding with charset SID names.
     *
     * @param name PostScript glyph name
     * @return SID
     */
    @Override
    int getSID(String name)
    {
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
    @Override
    public String getNameForGID(int gid)
    {
        return gidToName.get(gid);
    }

    /**
     * Returns the CID for the given GID.
     *
     * @param gid GID
     * @return CID
     */
    @Override
    public int getCIDForGID(int gid)
    {
        throw new IllegalStateException("Not a CIDFont");
    }
}