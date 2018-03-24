/*
 * Copyright 2018 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
 * Font finder for OS/400 systems.
 */
public class OS400FontDirFinder extends NativeFontDirFinder
{
    @Override
    protected String[] getSearchableDirectories()
    {
        return new String[] { System.getProperty("user.home") + "/.fonts", // user
            "/QIBM/ProdData/OS400/Fonts"
        };
    }
}
