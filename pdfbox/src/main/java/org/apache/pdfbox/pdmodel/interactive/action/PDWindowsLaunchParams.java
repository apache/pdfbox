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
package org.apache.pdfbox.pdmodel.interactive.action;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

import org.apache.pdfbox.pdmodel.common.COSObjectable;

/**
 * Launch paramaters for the windows OS.
 *
 * @author Ben Litchfield
 */
public class PDWindowsLaunchParams implements COSObjectable
{
    /**
     * The open operation for the launch.
     */
    public static final String OPERATION_OPEN = "open";
    /**
     * The print operation for the lanuch.
     */
    public static final String OPERATION_PRINT = "print";

    /**
     * The params dictionary.
     */
    protected COSDictionary params;

    /**
     * Default constructor.
     */
    public PDWindowsLaunchParams()
    {
        params = new COSDictionary();
    }

    /**
     * Constructor.
     *
     * @param p The params dictionary.
     */
    public PDWindowsLaunchParams( COSDictionary p )
    {
        params = p;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    @Override
    public COSDictionary getCOSObject()
    {
        return params;
    }

    /**
     * The file to launch.
     *
     * @return The executable/document to launch.
     */
    public String getFilename()
    {
        return params.getString( COSName.F );
    }

    /**
     * Set the file to launch.
     *
     * @param file The executable/document to launch.
     */
    public void setFilename( String file )
    {
        params.setString( COSName.F, file );
    }

    /**
     * The dir to launch from.
     *
     * @return The dir of the executable/document to launch.
     */
    public String getDirectory()
    {
        return params.getString( COSName.D );
    }

    /**
     * Set the dir to launch from.
     *
     * @param dir The dir of the executable/document to launch.
     */
    public void setDirectory( String dir )
    {
        params.setString( COSName.D, dir );
    }

    /**
     * Get the operation to perform on the file.  This method will not return null,
     * OPERATION_OPEN is the default.
     *
     * @return The operation to perform for the file.
     * @see PDWindowsLaunchParams#OPERATION_OPEN
     * @see PDWindowsLaunchParams#OPERATION_PRINT
     */
    public String getOperation()
    {
        return params.getString(COSName.O, OPERATION_OPEN);
    }

    /**
     * Set the operation to perform..
     *
     * @param op The operation to perform on the file.
     */
    public void setOperation( String op )
    {
        params.setString( COSName.D, op );
    }

    /**
     * A parameter to pass the executable.
     *
     * @return The parameter to pass the executable.
     */
    public String getExecuteParam()
    {
        return params.getString( COSName.P );
    }

    /**
     * Set the parameter to pass the executable.
     *
     * @param param The parameter for the executable.
     */
    public void setExecuteParam( String param )
    {
        params.setString( COSName.P, param );
    }
}
