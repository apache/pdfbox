package org.apache.fontbox.cff;

class EmbeddedCharset extends CFFCharset
{
    private final CFFCharset font;

    EmbeddedCharset(boolean isCIDFont)
    {
        font = isCIDFont ? new CFFCharsetCID() : new CFFCharsetType1();
    }

    @Override
    public int getCIDForGID(int gid)
    {
        return font.getCIDForGID(gid);
    }

    @Override
    public boolean isCIDFont()
    {
        return font.isCIDFont();
    }

    @Override
    public void addSID(int gid, int sid, String name)
    {
        font.addSID(gid, sid, name);
    }

    @Override
    public void addCID(int gid, int cid)
    {
        font.addCID(gid, cid);
    }

    @Override
    int getSIDForGID(int sid)
    {
        return font.getSIDForGID(sid);
    }

    @Override
    int getGIDForSID(int sid)
    {
        return font.getGIDForSID(sid);
    }

    @Override
    public int getGIDForCID(int cid)
    {
        return font.getGIDForCID(cid);
    }

    @Override
    int getSID(String name)
    {
        return font.getSID(name);
    }

    @Override
    public String getNameForGID(int gid)
    {
        return font.getNameForGID(gid);
    }
}
