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
class CFFCharsetCID implements CFFCharset
{

    private static final String EXCEPTION_MESSAGE = "Not a Type 1-equivalent font";

    private final Map<Integer, Integer> sidOrCidToGid = new HashMap<>(250);

    // inverse
    private final Map<Integer, Integer> gidToCid = new HashMap<>();

    @Override
    public boolean isCIDFont()
    {
        return true;
    }
    
    @Override
    public void addSID(int gid, int sid, String name)
    {
        throw new IllegalStateException(EXCEPTION_MESSAGE);
    }

    @Override
    public void addCID(int gid, int cid)
    {
        sidOrCidToGid.put(cid, gid);
        gidToCid.put(gid, cid);
    }

    @Override
    public int getSIDForGID(int sid)
    {
        throw new IllegalStateException(EXCEPTION_MESSAGE);
    }

    @Override
    public int getGIDForSID(int sid)
    {
        throw new IllegalStateException(EXCEPTION_MESSAGE);
    }

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

    @Override
    public int getSID(String name)
    {
        throw new IllegalStateException(EXCEPTION_MESSAGE);
    }

    @Override
    public String getNameForGID(int gid)
    {
        throw new IllegalStateException(EXCEPTION_MESSAGE);
    }

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
