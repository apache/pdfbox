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
package org.apache.pdfbox.filter;

import java.util.HashMap;
import java.util.Map;

/**
 * This is the used for the LZWDecode filter.
 *
 * @author Ben Litchfield
 */
final class LZWNode
{
    private final long code;
    private final Map<Byte, LZWNode> subNodes = new HashMap<Byte, LZWNode>();

    public LZWNode(long codeValue) 
    {
        code = codeValue;
    }

    /*
     * This will get the number of children.
     */
    public int childCount()
    {
        return subNodes.size();
    }

    /*
     * This will set the node for a particular byte.
     */
    public void setNode(byte b, LZWNode node)
    {
        subNodes.put(b, node);
    }

    /*
     * This will get the node that is a direct sub node of this node.
     */
    public LZWNode getNode(byte data)
    {
        return subNodes.get(data);
    }

    /*
     * This will traverse the tree until it gets to the sub node.
     * This will return null if the node does not exist.
     */
    public LZWNode getNode(byte[] data)
    {
        LZWNode current = this;
        for (int i = 0; i < data.length && current != null; i++)
        {
            current = current.getNode(data[i]);
        }
        return current;
    }

    /*
     * Returns the property code
     */
    public long getCode()
    {
        return code;
    }
}
