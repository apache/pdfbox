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
package org.apache.pdfbox.util.filetypedetector;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Drew Noakes
 *
 * code taken from https://github.com/drewnoakes/metadata-extractor
 *
 * 2016-01-04
 *
 * latest commit number 73f1a48
 *
 * Stores values using a prefix tree (aka 'trie', i.e. reTRIEval data structure).
 *
 * @param <T> the type of value to store for byte sequences
 */
class ByteTrie<T>
{
    /**
     * A node in the trie. Has children and may have an associated value.
     */
    static class ByteTrieNode<T>
    {
        private final Map<Byte, ByteTrieNode<T>> children = new HashMap<Byte, ByteTrieNode<T>>();
        private T value = null;

        public void setValue(T value)
        {
            if (this.value != null)
            {
                throw new IllegalStateException("Value already set for this trie node");
            }
            this.value = value;
        }

        public T getValue()
        {
            return value;
        }
    }

    private final ByteTrieNode<T> root = new ByteTrieNode<T>();
    private int maxDepth;

    /**
     * Return the most specific value stored for this byte sequence. If not found, returns
     * <code>null</code> or a default values as specified by calling
     * {@link ByteTrie#setDefaultValue}.
     * @param bytes
     * @return 
     */
    public T find(byte[] bytes)
    {
        ByteTrieNode<T> node = root;
        T val = node.getValue();
        for (byte b : bytes)
        {
            ByteTrieNode<T> child = node.children.get(b);
            if (child == null)
            {
                break;
            }
            node = child;
            if (node.getValue() != null)
            {
                val = node.getValue();
            }
        }
        return val;
    }

    /**
     * Store the given value at the specified path.
     * @param value
     * @param parts
     */
    public void addPath(T value, byte[]... parts)
    {
        int depth = 0;
        ByteTrieNode<T> node = root;
        for (byte[] part : parts)
        {
            for (byte b : part)
            {
                ByteTrieNode<T> child = node.children.get(b);
                if (child == null)
                {
                    child = new ByteTrieNode<T>();
                    node.children.put(b, child);
                }
                node = child;
                depth++;
            }
        }
        node.setValue(value);
        maxDepth = Math.max(maxDepth, depth);
    }

    /**
     * Sets the default value to use in {@link ByteTrie#find(byte[])} when no path matches.
     * @param defaultValue
     */
    public void setDefaultValue(T defaultValue)
    {
        root.setValue(defaultValue);
    }

    /**
     * Gets the maximum depth stored in this trie.
     * @return 
     */
    public int getMaxDepth()
    {
        return maxDepth;
    }
}
