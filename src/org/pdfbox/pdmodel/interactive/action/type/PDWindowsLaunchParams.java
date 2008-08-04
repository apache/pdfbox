/**
 * Copyright (c) 2004, www.pdfbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.pdfbox.org
 *
 */
package org.pdfbox.pdmodel.interactive.action.type;

import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDictionary;

import org.pdfbox.pdmodel.common.COSObjectable;

/**
 * Launch paramaters for the windows OS.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
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
    public COSBase getCOSObject()
    {
        return params;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSDictionary getCOSDictionary()
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
        return params.getString( "F" );
    }
    
    /**
     * Set the file to launch.
     * 
     * @param file The executable/document to launch.
     */
    public void setFilename( String file )
    {
        params.setString( "F", file );
    }
    
    /**
     * The dir to launch from.
     * 
     * @return The dir of the executable/document to launch.
     */
    public String getDirectory()
    {
        return params.getString( "D" );
    }
    
    /**
     * Set the dir to launch from.
     * 
     * @param dir The dir of the executable/document to launch.
     */
    public void setDirectory( String dir )
    {
        params.setString( "D", dir );
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
        return params.getString( "O", OPERATION_OPEN );
    }
    
    /**
     * Set the operation to perform..
     * 
     * @param op The operation to perform on the file.
     */
    public void setOperation( String op )
    {
        params.setString( "D", op );
    }
    
    /**
     * A parameter to pass the executable.
     * 
     * @return The parameter to pass the executable.
     */
    public String getExecuteParam()
    {
        return params.getString( "P" );
    }
    
    /**
     * Set the parameter to pass the executable.
     * 
     * @param param The parameter for the executable.
     */
    public void setExecuteParam( String param )
    {
        params.setString( "P", param );
    }
}