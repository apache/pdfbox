/**
 * Copyright (c) 2003, www.pdfbox.org
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
package org.pdfbox.filter;

import java.util.HashMap;
import java.util.Map;

/**
 * This is the used for the LZWDecode filter.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.4 $
 */
class LZWNode
{
    private long code;
    private Map subNodes = new HashMap();

    /**
     * This will get the number of children.
     *
     * @return The number of children.
     */
    public int childCount()
    {
        return subNodes.size();
    }

    /**
     * This will set the node for a particular byte.
     *
     * @param b The byte for that node.
     * @param node The node to add.
     */
    public void setNode( byte b, LZWNode node )
    {
        subNodes.put( new Byte( b ), node );
    }

    /**
     * This will get the node that is a direct sub node of this node.
     *
     * @param data The byte code to the node.
     *
     * @return The node at that value if it exists.
     */
    public LZWNode getNode( byte data )
    {
        return (LZWNode)subNodes.get( new Byte( data ) );
    }


    /**
     * This will traverse the tree until it gets to the sub node.
     * This will return null if the node does not exist.
     *
     * @param data The path to the node.
     *
     * @return The node that resides at the data path.
     */
    public LZWNode getNode( byte[] data )
    {
        LZWNode current = this;
        for( int i=0; i<data.length && current != null; i++ )
        {
            current = current.getNode( data[i] );
        }
        return current;
    }

    /** Getter for property code.
     * @return Value of property code.
     */
    public long getCode()
    {
        return code;
    }

    /** Setter for property code.
     * @param codeValue New value of property code.
     */
    public void setCode(long codeValue)
    {
        code = codeValue;
    }

}