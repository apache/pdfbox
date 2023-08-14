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
 * Class representing an embedded CFF charset.
 *
 */
class EmbeddedCharset implements CFFCharset
{
    private final CFFCharset charset;

    EmbeddedCharset(boolean isCIDFont)
    {
        charset = isCIDFont ? new CFFCharsetCID() : new CFFCharsetType1();
    }

    @Override
    public int getCIDForGID(int gid)
    {
        return charset.getCIDForGID(gid);
    }

    @Override
    public boolean isCIDFont()
    {
        return charset.isCIDFont();
    }

    @Override
    public void addSID(int gid, int sid, String name)
    {
        charset.addSID(gid, sid, name);
    }

    @Override
    public void addCID(int gid, int cid)
    {
        charset.addCID(gid, cid);
    }

    @Override
    public int getSIDForGID(int sid)
    {
        return charset.getSIDForGID(sid);
    }

    @Override
    public int getGIDForSID(int sid)
    {
        return charset.getGIDForSID(sid);
    }

    @Override
    public int getGIDForCID(int cid)
    {
        return charset.getGIDForCID(cid);
    }

    @Override
    public int getSID(String name)
    {
        return charset.getSID(name);
    }

    @Override
    public String getNameForGID(int gid)
    {
        return charset.getNameForGID(gid);
    }
}
