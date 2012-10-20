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
package org.apache.pdfbox.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;


/**
 * A FileFilter that will only accept files of a certain extension.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class ExtensionFileFilter extends FileFilter
{
    private String[] extensions = null;
    private String desc;

    /**
     * Constructor.
     *
     * @param ext A list of filename extensions, ie new String[] { "PDF"}.
     * @param description A description of the files.
     */
    public ExtensionFileFilter( String[] ext, String description )
    {
        extensions = ext;
        desc = description;
    }

    /**
     * {@inheritDoc}
     */
    public boolean accept(File pathname)
    {
        if (pathname.isDirectory())
        {
            return true;
        }
        boolean acceptable = false;
        String name = pathname.getName().toUpperCase();
        for( int i=0; !acceptable && i<extensions.length; i++ )
        {
            if( name.endsWith( extensions[i].toUpperCase() ) )
            {
                acceptable = true;
            }
        }
        return acceptable;
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription()
    {
        return desc;
    }
}
