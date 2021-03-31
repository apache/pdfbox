package org.apache.fontbox.cff;

import java.util.HashMap;
import java.util.Map;

/**
 * A CFF charset. A charset is an array of CIDs for all glyphs in the font.
 *
 * @author Valery Bokov
 */
class CFFCharsetCID extends CFFCharset
{
    private final Map<Integer, Integer> sidOrCidToGid = new HashMap<>(250);

    // inverse
    private final Map<Integer, Integer> gidToCid = new HashMap<>();

    /**
     * Indicates if the charset belongs to a CID font.
     *
     * @return true for CID fonts
     */
    @Override
    public boolean isCIDFont()
    {
        return true;
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
        throw new IllegalStateException("Not a Type 1-equivalent font");
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
        sidOrCidToGid.put(cid, gid);
        gidToCid.put(gid, cid);
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
        throw new IllegalStateException("Not a Type 1-equivalent font");
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
        throw new IllegalStateException("Not a Type 1-equivalent font");
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
    @Override
    int getSID(String name)
    {
        throw new IllegalStateException("Not a Type 1-equivalent font");
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
        throw new IllegalStateException("Not a Type 1-equivalent font");
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
        Integer cid = gidToCid.get(gid);
        if (cid != null)
        {
            return cid;
        }
        return 0;
    }
}
