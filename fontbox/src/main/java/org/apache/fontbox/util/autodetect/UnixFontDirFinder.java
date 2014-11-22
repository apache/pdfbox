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

package org.apache.fontbox.util.autodetect;

/**
 * Unix font directory finder. This class is based on a class provided by Apache FOP. see
 * org.apache.fop.fonts.autodetect.UnixFontDirFinder
 */
public class UnixFontDirFinder extends NativeFontDirFinder
{

    /**
     * Some guesses at possible unix font directory locations.
     * 
     * @return a list of possible font locations
     */
    protected String[] getSearchableDirectories()
    {
        return new String[] { System.getProperty("user.home") + "/.fonts", // user
                "/usr/local/fonts", // local
                "/usr/local/share/fonts", // local shared
                "/usr/share/fonts", // system
                "/usr/X11R6/lib/X11/fonts" // X
        };
    }
}
