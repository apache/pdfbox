/*

 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.

 */

package org.apache.pdfbox.preflight;

import java.util.Stack;

/**
 * Contains a stack of objects to follow the validation path. Examples:
 * <ul>
 * <li>If the ValidationProcess computes a Type1Font, this object could contain a path like
 * PDPage|PDResources|PDFont.
 * <li>If the ValidationProcess computes an XObject, this object could contain a path like
 * PDPage|PDResources|PDFontType3|PDResource|PDXObject.
 * </ul>
 */
public class PreflightPath
{

    @SuppressWarnings("rawtypes")
    private final Stack objectPath = new Stack();

    @SuppressWarnings("rawtypes")
    private final Stack<Class> classObjPath = new Stack<>();

    @SuppressWarnings("unchecked")
    public boolean pushObject(final Object pathElement)
    {
        boolean pushed = false;
        if (pathElement != null)
        {
            this.objectPath.push(pathElement);
            this.classObjPath.push(pathElement.getClass());
            pushed = true;
        }
        return pushed;
    }

    /**
     * Return the object at the given position. The object must be an instance of the given class.
     * 
     * @param position
     * @param expectedType
     * @return the object at the given position.
     */
    @SuppressWarnings("unchecked")
    public <T> T getPathElement(final int position, final Class<T> expectedType)
    {
        if (position < 0 || position >= this.objectPath.size())
        {
            return null;
        }
        return (T) this.objectPath.get(position);
    }

    /**
     * Return the index of the first object that have the given type.
     * 
     * @param type
     * @return the object position, -1 if the type doesn't exist in the stack.
     */
    public <T> int getClosestTypePosition(final Class<T> type)
    {
        for (int i = this.objectPath.size(); i-- > 0;)
        {
            if (this.classObjPath.get(i).equals(type))
            {
                return i;
            }
        }
        return -1;
    }

    public <T> T getClosestPathElement(final Class<T> type)
    {
        return getPathElement(getClosestTypePosition(type), type);
    }

    /**
     * Looks at the object at the top of this stack without removing it from the stack.
     * 
     * @return the object at the top of the stack.
     */
    public Object peek()
    {
        return this.objectPath.peek();
    }

    public Object pop()
    {
        this.classObjPath.pop();
        return this.objectPath.pop();
    }

    public void clear()
    {
        this.classObjPath.clear();
        this.objectPath.clear();
    }

    public int size()
    {
        return this.objectPath.size();
    }

    public boolean isEmpty()
    {
        return this.objectPath.isEmpty();
    }

    public boolean isExpectedType(final Class<?> type)
    {
        @SuppressWarnings("rawtypes") final Class knownType = this.classObjPath.peek();
        return (knownType != null && (type.equals(knownType) || type.isAssignableFrom(knownType)));
    }
}
