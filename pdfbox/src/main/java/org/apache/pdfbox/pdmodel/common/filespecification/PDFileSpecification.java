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

import java.io.IOException;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSString;

import org.apache.pdfbox.pdmodel.common.COSObjectable;

/**
 * This represents a file specification.
 *
 * @author Ben Litchfield
 */
public abstract class PDFileSpecification implements COSObjectable
{

    /**
     * A file specfication can either be a COSString or a COSDictionary.  This
     * will create the file specification either way.
     *
     * @param base The cos object that describes the fs.
     *
     * @return The file specification for the COSBase object.
     *
     * @throws IOException If there is an error creating the file spec.
     */
    public static PDFileSpecification createFS( COSBase base ) throws IOException
    {
        PDFileSpecification retval = null;
        if( base == null )
        {
            //then simply return null
        }
        else if( base instanceof COSString )
        {
            retval = new PDSimpleFileSpecification( (COSString)base );
        }
        else if( base instanceof COSDictionary )
        {
            retval = new PDComplexFileSpecification( (COSDictionary)base );
        }
        else
        {
            throw new IOException( "Error: Unknown file specification " + base );
        }
        return retval;
    }

    /**
     * This will get the file name.
     *
     * @return The file name.
     */
    public abstract String getFile();

    /**
     * This will set the file name.
     *
     * @param file The name of the file.
     */
    public abstract void setFile( String file );
}
