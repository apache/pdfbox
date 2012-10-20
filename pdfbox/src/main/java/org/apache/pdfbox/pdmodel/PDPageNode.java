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
package org.apache.pdfbox.pdmodel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;

import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This represents a page node in a pdf document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.8 $
 */
public class PDPageNode implements COSObjectable
{
    private COSDictionary page;

    /**
     * Log instance.
     */
    private static final Log log = LogFactory.getLog(PDPageNode.class);

    /**
     * Creates a new instance of PDPage.
     */
    public PDPageNode()
    {
        page = new COSDictionary();
        page.setItem( COSName.TYPE, COSName.PAGES );
        page.setItem( COSName.KIDS, new COSArray() );
        page.setItem( COSName.COUNT, COSInteger.ZERO );
    }

    /**
     * Creates a new instance of PDPage.
     *
     * @param pages The dictionary pages.
     */
    public PDPageNode( COSDictionary pages )
    {
        page = pages;
    }

    /**
     * This will update the count attribute of the page node.  This only needs to
     * be called if you add or remove pages.  The PDDocument will call this for you
     * when you use the PDDocumnet persistence methods.  So, basically most clients
     * will never need to call this.
     *
     * @return The update count for this node.
     */
    public long updateCount()
    {
        long totalCount = 0;
        List kids = getKids();
        Iterator kidIter = kids.iterator();
        while( kidIter.hasNext() )
        {
            Object next = kidIter.next();
            if( next instanceof PDPage )
            {
                totalCount++;
            }
            else
            {
                PDPageNode node = (PDPageNode)next;
                totalCount += node.updateCount();
            }
        }
        page.setLong( COSName.COUNT, totalCount );
        return totalCount;
    }

    /**
     * This will get the count of descendent page objects.
     *
     * @return The total number of descendent page objects.
     */
    public long getCount()
    {
        if(page == null)
        {
            return 0L;
        }
        COSBase num = page.getDictionaryObject(COSName.COUNT);
        if(num == null)
        {
            return 0L;
        }
        return ((COSNumber) num).intValue();
    }

    /**
     * This will get the underlying dictionary that this class acts on.
     *
     * @return The underlying dictionary for this class.
     */
    public COSDictionary getDictionary()
    {
        return page;
    }

    /**
     * This is the parent page node.
     *
     * @return The parent to this page.
     */
    public PDPageNode getParent()
    {
        PDPageNode parent = null;
        COSDictionary parentDic = (COSDictionary)page.getDictionaryObject(COSName.PARENT, COSName.P);
        if( parentDic != null )
        {
            parent = new PDPageNode( parentDic );
        }
        return parent;
    }

    /**
     * This will set the parent of this page.
     *
     * @param parent The parent to this page node.
     */
    public void setParent( PDPageNode parent )
    {
        page.setItem( COSName.PARENT, parent.getDictionary() );
    }

    /**
     * {@inheritDoc}
     */
    public COSBase getCOSObject()
    {
        return page;
    }

    /**
     * This will return all kids of this node, either PDPageNode or PDPage.
     *
     * @return All direct descendents of this node.
     */
    public List getKids()
    {
        List actuals = new ArrayList();
        COSArray kids = getAllKids(actuals, page, false);
        return new COSArrayList( actuals, kids );
    }

    /**
     * This will return all kids of this node as PDPage.
     *
     * @param result All direct and indirect descendents of this node are added to this list.
     */
    public void getAllKids(List result)
    {
        getAllKids(result, page, true);
    }

    /**
     * This will return all kids of the given page node as PDPage.
     *
     * @param result All direct and optionally indirect descendents of this node are added to this list.
     * @param page Page dictionary of a page node.
     * @param recurse if true indirect descendents are processed recursively
     */
    private static COSArray getAllKids(List result, COSDictionary page, boolean recurse)
    {
        if(page == null)
            return null;
        COSArray kids = (COSArray)page.getDictionaryObject( COSName.KIDS );
        if ( kids == null)
        {
            log.error("No Kids found in getAllKids(). Probably a malformed pdf.");
            return null;
        }
        for( int i=0; i<kids.size(); i++ )
        {
            COSBase obj = kids.getObject( i );
            if (obj instanceof COSDictionary)
            {
                COSDictionary kid = (COSDictionary)obj;
                if( COSName.PAGE.equals( kid.getDictionaryObject( COSName.TYPE ) ) )
                {
                    result.add( new PDPage( kid ) );
                }
                else
                {
                    if (recurse)
                    {
                        getAllKids(result, kid, recurse);
                    }
                    else
                    {
                        result.add( new PDPageNode( kid ) );
                    }
                }
            }
        }
        return kids;
    }

    /**
     * This will get the resources at this page node and not look up the hierarchy.
     * This attribute is inheritable, and findResources() should probably used.
     * This will return null if no resources are available at this level.
     *
     * @return The resources at this level in the hierarchy.
     */
    public PDResources getResources()
    {
        PDResources retval = null;
        COSDictionary resources = (COSDictionary)page.getDictionaryObject( COSName.RESOURCES );
        if( resources != null )
        {
            retval = new PDResources( resources );
        }
        return retval;
    }

    /**
     * This will find the resources for this page by looking up the hierarchy until
     * it finds them.
     *
     * @return The resources at this level in the hierarchy.
     */
    public PDResources findResources()
    {
        PDResources retval = getResources();
        PDPageNode parent = getParent();
        if( retval == null && parent != null )
        {
            retval = parent.findResources();
        }
        return retval;
    }

    /**
     * This will set the resources for this page.
     *
     * @param resources The new resources for this page.
     */
    public void setResources( PDResources resources )
    {
        if( resources == null )
        {
            page.removeItem( COSName.RESOURCES );
        }
        else
        {
            page.setItem( COSName.RESOURCES, resources.getCOSDictionary() );
        }
    }

    /**
     * This will get the MediaBox at this page and not look up the hierarchy.
     * This attribute is inheritable, and findMediaBox() should probably used.
     * This will return null if no MediaBox are available at this level.
     *
     * @return The MediaBox at this level in the hierarchy.
     */
    public PDRectangle getMediaBox()
    {
        PDRectangle retval = null;
        COSArray array = (COSArray)page.getDictionaryObject( COSName.MEDIA_BOX );
        if( array != null )
        {
            retval = new PDRectangle( array );
        }
        return retval;
    }

    /**
     * This will find the MediaBox for this page by looking up the hierarchy until
     * it finds them.
     *
     * @return The MediaBox at this level in the hierarchy.
     */
    public PDRectangle findMediaBox()
    {
        PDRectangle retval = getMediaBox();
        PDPageNode parent = getParent();
        if( retval == null && parent != null )
        {
            retval = parent.findMediaBox();
        }
        return retval;
    }

    /**
     * This will set the mediaBox for this page.
     *
     * @param mediaBox The new mediaBox for this page.
     */
    public void setMediaBox( PDRectangle mediaBox )
    {
        if( mediaBox == null )
        {
            page.removeItem( COSName.MEDIA_BOX  );
        }
        else
        {
            page.setItem( COSName.MEDIA_BOX , mediaBox.getCOSArray() );
        }
    }

    /**
     * This will get the CropBox at this page and not look up the hierarchy.
     * This attribute is inheritable, and findCropBox() should probably used.
     * This will return null if no CropBox is available at this level.
     *
     * @return The CropBox at this level in the hierarchy.
     */
    public PDRectangle getCropBox()
    {
        PDRectangle retval = null;
        COSArray array = (COSArray)page.getDictionaryObject( COSName.CROP_BOX );
        if( array != null )
        {
            retval = new PDRectangle( array );
        }
        return retval;
    }

    /**
     * This will find the CropBox for this page by looking up the hierarchy until
     * it finds them.
     *
     * @return The CropBox at this level in the hierarchy.
     */
    public PDRectangle findCropBox()
    {
        PDRectangle retval = getCropBox();
        PDPageNode parent = getParent();
        if( retval == null && parent != null )
        {
            retval = findParentCropBox( parent );
        }

        //default value for cropbox is the media box
        if( retval == null )
        {
            retval = findMediaBox();
        }
        return retval;
    }

    /**
     * This will search for a crop box in the parent and return null if it is not
     * found.  It will NOT default to the media box if it cannot be found.
     *
     * @param node The node
     */
    private PDRectangle findParentCropBox( PDPageNode node )
    {
        PDRectangle rect = node.getCropBox();
        PDPageNode parent = node.getParent();
        if( rect == null && parent != null )
        {
            rect = findParentCropBox( node );
        }
        return rect;
    }

    /**
     * This will set the CropBox for this page.
     *
     * @param cropBox The new CropBox for this page.
     */
    public void setCropBox( PDRectangle cropBox )
    {
        if( cropBox == null )
        {
            page.removeItem( COSName.CROP_BOX );
        }
        else
        {
            page.setItem( COSName.CROP_BOX, cropBox.getCOSArray() );
        }
    }

    /**
     * A value representing the rotation.  This will be null if not set at this level
     * The number of degrees by which the page should
     * be rotated clockwise when displayed or printed. The value must be a multiple
     * of 90.
     *
     * This will get the rotation at this page and not look up the hierarchy.
     * This attribute is inheritable, and findRotation() should probably used.
     * This will return null if no rotation is available at this level.
     *
     * @return The rotation at this level in the hierarchy.
     */
    public Integer getRotation()
    {
        Integer retval = null;
        COSNumber value = (COSNumber)page.getDictionaryObject( COSName.ROTATE );
        if( value != null )
        {
            retval = new Integer( value.intValue() );
        }
        return retval;
    }

    /**
     * This will find the rotation for this page by looking up the hierarchy until
     * it finds them.
     *
     * @return The rotation at this level in the hierarchy.
     */
    public int findRotation()
    {
        int retval = 0;
        Integer rotation = getRotation();
        if( rotation != null )
        {
            retval = rotation.intValue();
        }
        else
        {
            PDPageNode parent = getParent();
            if( parent != null )
            {
                retval = parent.findRotation();
            }
        }

        return retval;
    }

    /**
     * This will set the rotation for this page.
     *
     * @param rotation The new rotation for this page.
     */
    public void setRotation( int rotation )
    {
        page.setInt( COSName.ROTATE, rotation );
    }
}
