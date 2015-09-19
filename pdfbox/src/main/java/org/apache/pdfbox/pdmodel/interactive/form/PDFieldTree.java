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
package org.apache.pdfbox.pdmodel.interactive.form;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * The field tree.
 */
public class PDFieldTree implements Iterable<PDField>
{
    private final PDAcroForm acroForm;

    /**
     * Constructor for reading.
     *
     * @param acroForm the AcroForm containing the fields.
     */
    public PDFieldTree(PDAcroForm acroForm)
    {
        if (acroForm == null)
        {
            throw new IllegalArgumentException("root cannot be null");
        }
        this.acroForm = acroForm;
    }

    /**
     * Returns an iterator which walks all fields in the tree, in order.
     */
    @Override
    public Iterator<PDField> iterator()
    {
        return new FieldIterator(acroForm);
    }

    /**
     * Iterator which walks all fields in the tree, in order.
     */
    private static final class FieldIterator implements Iterator<PDField>
    {
        private final Queue<PDField> queue = new ArrayDeque<PDField>();
        
        private FieldIterator(PDAcroForm form)
        {
            List<PDField> fields = form.getFields();
            for (PDField field : fields)
            {
                enqueueKids(field);
            }
        }

        @Override
        public boolean hasNext()
        {
            return !queue.isEmpty();
        }

        @Override
        public PDField next()
        {
            if(!hasNext()){
                throw new NoSuchElementException();
            }
            
            return queue.poll();
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
        
        private void enqueueKids(PDField node)
        {
            queue.add(node);
            if (node instanceof PDNonTerminalField)
            {
                List<PDField> kids = ((PDNonTerminalField) node).getChildren();
                for (PDField kid : kids)
                {
                    enqueueKids(kid);
                }
            }
        }
    }
}
