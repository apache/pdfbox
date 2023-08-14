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
 * A CFF charset. A charset is an array of CIDs for all glyphs in the font.
 *
 * @author Valery Bokov
 */
class CFFCharsetType1 implements CFFCharset
{
    private static final String EXCEPTION_MESSAGE = "Not a CIDFont";

    private final Map<Integer, Integer> sidOrCidToGid = new HashMap<>(250);
    private final Map<Integer, Integer> gidToSid = new HashMap<>(250);
    private final Map<String, Integer> nameToSid = new HashMap<>(250);

    // inverse
    private final Map<Integer, String> gidToName = new HashMap<>(250);

    @Override
    public boolean isCIDFont()
    {
        return false;
    }
    
    @Override
    public void addSID(int gid, int sid, String name)
    {
        sidOrCidToGid.put(sid, gid);
        gidToSid.put(gid, sid);
        nameToSid.put(name, sid);
        gidToName.put(gid, name);
    }

    @Override
    public void addCID(int gid, int cid)
    {
        throw new IllegalStateException(EXCEPTION_MESSAGE);
    }

    @Override
    public int getSIDForGID(int gid)
    {
        Integer sid = gidToSid.get(gid);
        if (sid == null)
        {
            return 0;
        }
        return sid;
    }

    @Override
    public int getGIDForSID(int sid)
    {
        Integer gid = sidOrCidToGid.get(sid);
        if (gid == null)
        {
            return 0;
        }
        return gid;
    }

    @Override
    public int getGIDForCID(int cid)
    {
        throw new IllegalStateException(EXCEPTION_MESSAGE);
    }

    @Override
    public int getSID(String name)
    {
        Integer sid = nameToSid.get(name);
        if (sid == null)
        {
            return 0;
        }
        return sid;
    }

    @Override
    public String getNameForGID(int gid)
    {
        return gidToName.get(gid);
    }

    @Override
    public int getCIDForGID(int gid)
    {
        throw new IllegalStateException(EXCEPTION_MESSAGE);
    }
}
