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
package org.apache.pdfbox.pdmodel.common;

import java.io.IOException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSObject;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * A test case for PDNameTreeNode.
 * 
 * @author Koch
 */
public class TestPDNameTreeNode extends TestCase
{

    private PDNameTreeNode node1;
    private PDNameTreeNode node2;
    private PDNameTreeNode node4;
    private PDNameTreeNode node5;
    private PDNameTreeNode node24;

    @Override
    protected void setUp() throws Exception
    {
        this.node5 = new PDNameTreeNode(COSObject.class);
        SortedMap<String, COSObject> names = new TreeMap<String, COSObject>();
        COSInteger i = COSInteger.get(89);
        names.put("Actinium", new COSObject(i));
        names.put("Aluminum", new COSObject(COSInteger.get(13)));
        names.put("Americium", new COSObject(COSInteger.get(95)));
        names.put("Antimony", new COSObject(COSInteger.get(51)));
        names.put("Argon", new COSObject(COSInteger.get(18)));
        names.put("Arsenic", new COSObject(COSInteger.get(33)));
        names.put("Astatine", new COSObject(COSInteger.get(85)));
        this.node5.setNames(names);

        this.node24 = new PDNameTreeNode(COSObject.class);
        names = new TreeMap<String, COSObject>();
        names.put("Xenon", new COSObject(COSInteger.get(54)));
        names.put("Ytterbium", new COSObject(COSInteger.get(70)));
        names.put("Yttrium", new COSObject(COSInteger.get(39)));
        names.put("Zinc", new COSObject(COSInteger.get(30)));
        names.put("Zirconium", new COSObject(COSInteger.get(40)));
        this.node24.setNames(names);

        this.node2 = new PDNameTreeNode(COSObject.class);
        List<PDNameTreeNode> kids = this.node2.getKids();
        if (kids == null)
        {
            kids = new COSArrayList();
        }
        kids.add(this.node5);
        this.node2.setKids(kids);

        this.node4 = new PDNameTreeNode(COSObject.class);
        kids = this.node4.getKids();
        if (kids == null)
        {
            kids = new COSArrayList();
        }
        kids.add(this.node24);
        this.node4.setKids(kids);

        this.node1 = new PDNameTreeNode(COSObject.class);
        kids = this.node1.getKids();
        if (kids == null)
        {
            kids = new COSArrayList();
        }
        kids.add(this.node2);
        kids.add(this.node4);
        this.node1.setKids(kids);
    }


    public void testUpperLimit() throws IOException
    {
        Assert.assertEquals("Astatine", this.node5.getUpperLimit());
        Assert.assertEquals("Astatine", this.node2.getUpperLimit());

        Assert.assertEquals("Zirconium", this.node24.getUpperLimit());
        Assert.assertEquals("Zirconium", this.node4.getUpperLimit());

        Assert.assertEquals("Zirconium", this.node1.getUpperLimit());
    }

    public void testLowerLimit() throws IOException
    {
        Assert.assertEquals("Actinium", this.node5.getLowerLimit());
        Assert.assertEquals("Actinium", this.node2.getLowerLimit());

        Assert.assertEquals("Xenon", this.node24.getLowerLimit());
        Assert.assertEquals("Xenon", this.node4.getLowerLimit());

        Assert.assertEquals("Actinium", this.node1.getLowerLimit());
    }

}
