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
package org.apache.pdfbox.pdmodel.common.filespecification;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSString;

/**
 * A file specification that is just a string.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class PDSimpleFileSpecification extends PDFileSpecification
{
    private COSString file;

    /**
     * Constructor.
     *
     */
    public PDSimpleFileSpecification()
    {
        file = new COSString( "" );
    }

    /**
     * Constructor.
     *
     * @param fileName The file that this spec represents.
     */
    public PDSimpleFileSpecification( COSString fileName )
    {
        file = fileName;
    }

    /**
     * This will get the file name.
     *
     * @return The file name.
     */
    public String getFile()
    {
    return file.getString();
    }

    /**
     * This will set the file name.
     *
     * @param fileName The name of the file.
     */
    public void setFile( String fileName )
    {
    file = new COSString( fileName );
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return file;
    }

}
