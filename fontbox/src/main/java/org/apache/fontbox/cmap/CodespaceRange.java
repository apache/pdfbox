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
package org.apache.fontbox.cmap;

/**
 * This represents a single entry in the codespace range.
 *
 * @author Ben Litchfield (ben@benlitchfield.com)
 * @version $Revision: 1.1 $
 */
public class CodespaceRange
{

    private byte[] start;
    private byte[] end;

    /**
     * Creates a new instance of CodespaceRange.
     */
    public CodespaceRange()
    {
    }

    /** Getter for property end.
     * @return Value of property end.
     *
     */
    public byte[] getEnd()
    {
        return this.end;
    }

    /** Setter for property end.
     * @param endBytes New value of property end.
     *
     */
    public void setEnd(byte[] endBytes)
    {
        end = endBytes;
    }

    /** Getter for property start.
     * @return Value of property start.
     *
     */
    public byte[] getStart()
    {
        return this.start;
    }

    /** Setter for property start.
     * @param startBytes New value of property start.
     *
     */
    public void setStart(byte[] startBytes)
    {
        start = startBytes;
    }

    /**
     *  Check whether the given byte array is in this codespace range or ot.
     *  @param code The byte array to look for in the codespace range.
     *  @param offset The starting offset within the byte array.
     *  @param length The length of the part of the array.
     *  
     *  @return true if the given byte array is in the codespace range.
     */
    public boolean isInRange(byte[] code, int offset, int length)
    {
        if ( length < start.length || length > end.length ) 
        {
            return false;
        }

        if ( end.length == length ) 
        {
            for ( int i = 0; i < end.length; i++ ) 
            {
                int endInt = ((int)end[i]) & 0xFF;
                int codeInt = ((int)code[offset + i]) & 0xFF;
                if ( endInt < codeInt )
                {
                    return false;
                }
            }
        }
        if ( start.length == length ) 
        {
            for ( int i = 0; i < end.length; i++ ) 
            {
                int startInt = ((int)start[i]) & 0xFF;
                int codeInt = ((int)code[offset + i]) & 0xFF;
                if ( startInt > codeInt )
                {
                    return false;
                }
            }
        }
        return true;
    }
}