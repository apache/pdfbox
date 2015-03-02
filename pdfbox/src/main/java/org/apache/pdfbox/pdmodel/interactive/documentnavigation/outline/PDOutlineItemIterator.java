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
package org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline;

import java.util.Iterator;

/**
 * Iterator over the linked list of {@link PDOutlineItem} siblings.
 * 
 * @author Andrea Vacondio
 *
 */
class PDOutlineItemIterator implements Iterator<PDOutlineItem>
{
    private PDOutlineItem currentItem;
    private final PDOutlineItem startingItem;

    PDOutlineItemIterator(PDOutlineItem startingItem)
    {
        this.startingItem = startingItem;
    }

    @Override
    public boolean hasNext()
    {
        return startingItem != null
                && (currentItem == null || (currentItem.getNextSibling() != null && !startingItem
                        .equals(currentItem.getNextSibling())));
    }

    @Override
    public PDOutlineItem next()
    {
        if (currentItem == null)
        {
            currentItem = startingItem;
        }
        else
        {
            currentItem = currentItem.getNextSibling();
        }
        return currentItem;
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
