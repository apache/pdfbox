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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.interactive.action.PDPageAdditionalActions;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.pagenavigation.PDThreadBead;

/**
 * This represents a single page in a PDF document.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * 
 */
public class PDPage implements COSObjectable
{

    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(PDPage.class);

    /**
     * Default DPI in user space.
     */
    public static final int DEFAULT_USER_SPACE_UNIT_DPI = 72;

    private static final float MM_TO_UNITS = 1 / (10 * 2.54f) * DEFAULT_USER_SPACE_UNIT_DPI;

    private COSDictionary page;

    private PDResources pageResources;

    /**
     * A page size of LETTER or 8.5x11.
     */
    public static final PDRectangle PAGE_SIZE_LETTER = new PDRectangle(8.5f * DEFAULT_USER_SPACE_UNIT_DPI,
            11f * DEFAULT_USER_SPACE_UNIT_DPI);
    /**
     * A page size of A0 Paper.
     */
    public static final PDRectangle PAGE_SIZE_A0 = new PDRectangle(841 * MM_TO_UNITS, 1189 * MM_TO_UNITS);
    /**
     * A page size of A1 Paper.
     */
    public static final PDRectangle PAGE_SIZE_A1 = new PDRectangle(594 * MM_TO_UNITS, 841 * MM_TO_UNITS);
    /**
     * A page size of A2 Paper.
     */
    public static final PDRectangle PAGE_SIZE_A2 = new PDRectangle(420 * MM_TO_UNITS, 594 * MM_TO_UNITS);
    /**
     * A page size of A3 Paper.
     */
    public static final PDRectangle PAGE_SIZE_A3 = new PDRectangle(297 * MM_TO_UNITS, 420 * MM_TO_UNITS);
    /**
     * A page size of A4 Paper.
     */
    public static final PDRectangle PAGE_SIZE_A4 = new PDRectangle(210 * MM_TO_UNITS, 297 * MM_TO_UNITS);
    /**
     * A page size of A5 Paper.
     */
    public static final PDRectangle PAGE_SIZE_A5 = new PDRectangle(148 * MM_TO_UNITS, 210 * MM_TO_UNITS);
    /**
     * A page size of A6 Paper.
     */
    public static final PDRectangle PAGE_SIZE_A6 = new PDRectangle(105 * MM_TO_UNITS, 148 * MM_TO_UNITS);

    /**
     * Creates a new instance of PDPage with a size of 8.5x11.
     */
    public PDPage()
    {
        page = new COSDictionary();
        page.setItem(COSName.TYPE, COSName.PAGE);
        setMediaBox(PAGE_SIZE_LETTER);
    }

    /**
     * Creates a new instance of PDPage.
     * 
     * @param size The MediaBox or the page.
     */
    public PDPage(PDRectangle size)
    {
        page = new COSDictionary();
        page.setItem(COSName.TYPE, COSName.PAGE);
        setMediaBox(size);
    }

    /**
     * Creates a new instance of PDPage.
     * 
     * @param pageDic The existing page dictionary.
     */
    public PDPage(COSDictionary pageDic)
    {
        page = pageDic;
    }

    /**
     * Convert this standard java object to a COS object.
     * 
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return page;
    }

    /**
     * This will get the underlying dictionary that this class acts on.
     * 
     * @return The underlying dictionary for this class.
     */
    public COSDictionary getCOSDictionary()
    {
        return page;
    }

    /**
     * This is the parent page node. The parent is a required element of the page. This will be null until this page is
     * added to the document.
     * 
     * @return The parent to this page.
     */
    public PDPageNode getParent()
    {
        if (parent == null)
        {
            COSDictionary parentDic = (COSDictionary) page.getDictionaryObject(COSName.PARENT, COSName.P);
            if (parentDic != null)
            {
                parent = new PDPageNode(parentDic);
            }
        }
        return parent;
    }

    private PDPageNode parent = null;

    /**
     * This will set the parent of this page.
     * 
     * @param parentNode The parent to this page node.
     */
    public void setParent(PDPageNode parentNode)
    {
        parent = parentNode;
        page.setItem(COSName.PARENT, parent.getDictionary());
    }

    /**
     * This will update the last modified time for the page object.
     */
    public void updateLastModified()
    {
        page.setDate(COSName.LAST_MODIFIED, new GregorianCalendar());
    }

    /**
     * This will get the date that the content stream was last modified. This may return null.
     * 
     * @return The date the content stream was last modified.
     * 
     * @throws IOException If there is an error accessing the date information.
     */
    public Calendar getLastModified() throws IOException
    {
        return page.getDate(COSName.LAST_MODIFIED);
    }

    /**
     * This will get the resources at this page and not look up the hierarchy. This attribute is inheritable, and
     * findResources() should probably used. This will return null if no resources are available at this level.
     * 
     * @return The resources at this level in the hierarchy.
     */
    public PDResources getResources()
    {
        if (pageResources == null)
        {
            COSDictionary resources = (COSDictionary) page.getDictionaryObject(COSName.RESOURCES);
            if (resources != null)
            {
                pageResources = new PDResources(resources);
            }
        }
        return pageResources;
    }

    /**
     * This will find the resources for this page by looking up the hierarchy until it finds them.
     * 
     * @return The resources at this level in the hierarchy.
     */
    public PDResources findResources()
    {
        PDResources retval = getResources();
        PDPageNode parentNode = getParent();
        if (retval == null && parent != null)
        {
            retval = parentNode.findResources();
        }
        return retval;
    }

    /**
     * This will set the resources for this page.
     * 
     * @param resources The new resources for this page.
     */
    public void setResources(PDResources resources)
    {
        pageResources = resources;
        if (resources != null)
        {
            page.setItem(COSName.RESOURCES, resources);
        }
        else
        {
            page.removeItem(COSName.RESOURCES);
        }
    }

    /**
     * This will get the key of this Page in the structural parent tree.
     * 
     * @return the integer key of the page's entry in the structural parent tree
     */
    public int getStructParents()
    {
        return page.getInt(COSName.STRUCT_PARENTS, 0);
    }

    /**
     * This will set the key for this page in the structural parent tree.
     * 
     * @param structParents The new key for this page.
     */
    public void setStructParents(int structParents)
    {
        page.setInt(COSName.STRUCT_PARENTS, structParents);
    }

    /**
     * A rectangle, expressed in default user space units, defining the boundaries of the physical medium on which the
     * page is intended to be displayed or printed
     * 
     * This will get the MediaBox at this page and not look up the hierarchy. This attribute is inheritable, and
     * findMediaBox() should probably used. This will return null if no MediaBox are available at this level.
     * 
     * @return The MediaBox at this level in the hierarchy.
     */
    public PDRectangle getMediaBox()
    {
        if (mediaBox == null)
        {
            COSArray array = (COSArray) page.getDictionaryObject(COSName.MEDIA_BOX);
            if (array != null)
            {
                mediaBox = new PDRectangle(array);
            }
        }
        return mediaBox;
    }

    private PDRectangle mediaBox = null;

    /**
     * This will find the MediaBox for this page by looking up the hierarchy until it finds them.
     * 
     * @return The MediaBox at this level in the hierarchy.
     */
    public PDRectangle findMediaBox()
    {
        PDRectangle retval = getMediaBox();
        if (retval == null && getParent() != null)
        {
            retval = getParent().findMediaBox();
        }
        if (retval == null)
        {
            LOG.debug("Can't find MediaBox, using LETTER as default pagesize!");
            retval = PDPage.PAGE_SIZE_LETTER;
        }
        return retval;
    }

    /**
     * This will set the mediaBox for this page.
     * 
     * @param mediaBoxValue The new mediaBox for this page.
     */
    public void setMediaBox(PDRectangle mediaBoxValue)
    {
        this.mediaBox = mediaBoxValue;
        if (mediaBoxValue == null)
        {
            page.removeItem(COSName.MEDIA_BOX);
        }
        else
        {
            page.setItem(COSName.MEDIA_BOX, mediaBoxValue.getCOSArray());
        }
    }

    /**
     * A rectangle, expressed in default user space units, defining the visible region of default user space. When the
     * page is displayed or printed, its contents are to be clipped (cropped) to this rectangle and then imposed on the
     * output medium in some implementationdefined manner
     * 
     * This will get the CropBox at this page and not look up the hierarchy. This attribute is inheritable, and
     * findCropBox() should probably used. This will return null if no CropBox is available at this level.
     * 
     * @return The CropBox at this level in the hierarchy.
     */
    public PDRectangle getCropBox()
    {
        PDRectangle retval = null;
        COSArray array = (COSArray) page.getDictionaryObject(COSName.CROP_BOX);
        if (array != null)
        {
            retval = new PDRectangle(array);
        }
        return retval;
    }

    /**
     * This will find the CropBox for this page by looking up the hierarchy until it finds them.
     * 
     * @return The CropBox at this level in the hierarchy.
     */
    public PDRectangle findCropBox()
    {
        PDRectangle retval = getCropBox();
        PDPageNode parentNode = getParent();
        if (retval == null && parentNode != null)
        {
            retval = findParentCropBox(parentNode);
        }

        // default value for cropbox is the media box
        if (retval == null)
        {
            retval = findMediaBox();
        }
        return retval;
    }

    /**
     * This will search for a crop box in the parent and return null if it is not found. It will NOT default to the
     * media box if it cannot be found.
     * 
     * @param node The node
     */
    private PDRectangle findParentCropBox(PDPageNode node)
    {
        PDRectangle rect = node.getCropBox();
        PDPageNode parentNode = node.getParent();
        if (rect == null && parentNode != null)
        {
            rect = findParentCropBox(parentNode);
        }
        return rect;
    }

    /**
     * This will set the CropBox for this page.
     * 
     * @param cropBox The new CropBox for this page.
     */
    public void setCropBox(PDRectangle cropBox)
    {
        if (cropBox == null)
        {
            page.removeItem(COSName.CROP_BOX);
        }
        else
        {
            page.setItem(COSName.CROP_BOX, cropBox.getCOSArray());
        }
    }

    /**
     * A rectangle, expressed in default user space units, defining the region to which the contents of the page should
     * be clipped when output in a production environment. The default is the CropBox.
     * 
     * @return The BleedBox attribute.
     */
    public PDRectangle getBleedBox()
    {
        PDRectangle retval = null;
        COSArray array = (COSArray) page.getDictionaryObject(COSName.BLEED_BOX);
        if (array != null)
        {
            retval = new PDRectangle(array);
        }
        else
        {
            retval = findCropBox();
        }
        return retval;
    }

    /**
     * This will set the BleedBox for this page.
     * 
     * @param bleedBox The new BleedBox for this page.
     */
    public void setBleedBox(PDRectangle bleedBox)
    {
        if (bleedBox == null)
        {
            page.removeItem(COSName.BLEED_BOX);
        }
        else
        {
            page.setItem(COSName.BLEED_BOX, bleedBox.getCOSArray());
        }
    }

    /**
     * A rectangle, expressed in default user space units, defining the intended dimensions of the finished page after
     * trimming. The default is the CropBox.
     * 
     * @return The TrimBox attribute.
     */
    public PDRectangle getTrimBox()
    {
        PDRectangle retval = null;
        COSArray array = (COSArray) page.getDictionaryObject(COSName.TRIM_BOX);
        if (array != null)
        {
            retval = new PDRectangle(array);
        }
        else
        {
            retval = findCropBox();
        }
        return retval;
    }

    /**
     * This will set the TrimBox for this page.
     * 
     * @param trimBox The new TrimBox for this page.
     */
    public void setTrimBox(PDRectangle trimBox)
    {
        if (trimBox == null)
        {
            page.removeItem(COSName.TRIM_BOX);
        }
        else
        {
            page.setItem(COSName.TRIM_BOX, trimBox.getCOSArray());
        }
    }

    /**
     * A rectangle, expressed in default user space units, defining the extent of the page's meaningful content
     * (including potential white space) as intended by the page's creator The default isthe CropBox.
     * 
     * @return The ArtBox attribute.
     */
    public PDRectangle getArtBox()
    {
        PDRectangle retval = null;
        COSArray array = (COSArray) page.getDictionaryObject(COSName.ART_BOX);
        if (array != null)
        {
            retval = new PDRectangle(array);
        }
        else
        {
            retval = findCropBox();
        }
        return retval;
    }

    /**
     * This will set the ArtBox for this page.
     * 
     * @param artBox The new ArtBox for this page.
     */
    public void setArtBox(PDRectangle artBox)
    {
        if (artBox == null)
        {
            page.removeItem(COSName.ART_BOX);
        }
        else
        {
            page.setItem(COSName.ART_BOX, artBox.getCOSArray());
        }
    }

    // todo BoxColorInfo
    // todo Contents

    /**
     * A value representing the rotation. This will be null if not set at this level The number of degrees by which the
     * page should be rotated clockwise when displayed or printed. The value must be a multiple of 90.
     * 
     * This will get the rotation at this page and not look up the hierarchy. This attribute is inheritable, and
     * findRotation() should probably used. This will return null if no rotation is available at this level.
     * 
     * @return The rotation at this level in the hierarchy.
     */
    public Integer getRotation()
    {
        Integer retval = null;
        COSNumber value = (COSNumber) page.getDictionaryObject(COSName.ROTATE);
        if (value != null)
        {
            retval = new Integer(value.intValue());
        }
        return retval;
    }

    /**
     * This will find the rotation for this page by looking up the hierarchy until it finds them.
     * 
     * @return The rotation at this level in the hierarchy.
     */
    public int findRotation()
    {
        int retval = 0;
        Integer rotation = getRotation();
        if (rotation != null)
        {
            retval = rotation.intValue();
        }
        else
        {
            PDPageNode parentNode = getParent();
            if (parentNode != null)
            {
                retval = parentNode.findRotation();
            }
        }

        return retval;
    }

    /**
     * This will set the rotation for this page.
     * 
     * @param rotation The new rotation for this page.
     */
    public void setRotation(int rotation)
    {
        page.setInt(COSName.ROTATE, rotation);
    }

    /**
     * This will get the contents of the PDF Page, in the case that the contents of the page is an array then then the
     * entire array of streams will be be wrapped and appear as a single stream.
     * 
     * @return The page content stream.
     * 
     * @throws IOException If there is an error obtaining the stream.
     */
    public PDStream getContents() throws IOException
    {
        return PDStream.createFromCOS(page.getDictionaryObject(COSName.CONTENTS));
    }

    /**
     * This will set the contents of this page.
     * 
     * @param contents The new contents of the page.
     */
    public void setContents(PDStream contents)
    {
        page.setItem(COSName.CONTENTS, contents);
    }

    /**
     * This will get a list of PDThreadBead objects, which are article threads in the document. This will return an
     * empty list of there are no thread beads.
     * 
     * @return A list of article threads on this page.
     */
    public List<PDThreadBead> getThreadBeads()
    {
        COSArray beads = (COSArray) page.getDictionaryObject(COSName.B);
        if (beads == null)
        {
            beads = new COSArray();
        }
        List<PDThreadBead> pdObjects = new ArrayList<PDThreadBead>();
        for (int i = 0; i < beads.size(); i++)
        {
            COSDictionary beadDic = (COSDictionary) beads.getObject(i);
            PDThreadBead bead = null;
            // in some cases the bead is null
            if (beadDic != null)
            {
                bead = new PDThreadBead(beadDic);
            }
            pdObjects.add(bead);
        }
        return new COSArrayList<PDThreadBead>(pdObjects, beads);

    }

    /**
     * This will set the list of thread beads.
     * 
     * @param beads A list of PDThreadBead objects or null.
     */
    public void setThreadBeads(List<PDThreadBead> beads)
    {
        page.setItem(COSName.B, COSArrayList.converterToCOSArray(beads));
    }

    /**
     * Get the metadata that is part of the document catalog. This will return null if there is no meta data for this
     * object.
     * 
     * @return The metadata for this object.
     */
    public PDMetadata getMetadata()
    {
        PDMetadata retval = null;
        COSStream stream = (COSStream) page.getDictionaryObject(COSName.METADATA);
        if (stream != null)
        {
            retval = new PDMetadata(stream);
        }
        return retval;
    }

    /**
     * Set the metadata for this object. This can be null.
     * 
     * @param meta The meta data for this object.
     */
    public void setMetadata(PDMetadata meta)
    {
        page.setItem(COSName.METADATA, meta);
    }

    /**
     * Get the page actions.
     * 
     * @return The Actions for this Page
     */
    public PDPageAdditionalActions getActions()
    {
        COSDictionary addAct = (COSDictionary) page.getDictionaryObject(COSName.AA);
        if (addAct == null)
        {
            addAct = new COSDictionary();
            page.setItem(COSName.AA, addAct);
        }
        return new PDPageAdditionalActions(addAct);
    }

    /**
     * Set the page actions.
     * 
     * @param actions The actions for the page.
     */
    public void setActions(PDPageAdditionalActions actions)
    {
        page.setItem(COSName.AA, actions);
    }

    /**
     * This will return a list of the Annotations for this page.
     * 
     * @return List of the PDAnnotation objects.
     * 
     * @throws IOException If there is an error while creating the annotations.
     */
    public List<PDAnnotation> getAnnotations() throws IOException
    {
        COSArrayList<PDAnnotation> retval = null;
        COSArray annots = (COSArray) page.getDictionaryObject(COSName.ANNOTS);
        if (annots == null)
        {
            annots = new COSArray();
            page.setItem(COSName.ANNOTS, annots);
            retval = new COSArrayList<PDAnnotation>(new ArrayList<PDAnnotation>(), annots);
        }
        else
        {
            List<PDAnnotation> actuals = new ArrayList<PDAnnotation>();

            for (int i = 0; i < annots.size(); i++)
            {
                COSBase item = annots.getObject(i);
                actuals.add(PDAnnotation.createAnnotation(item));
            }
            retval = new COSArrayList<PDAnnotation>(actuals, annots);
        }
        return retval;
    }

    /**
     * This will set the list of annotations.
     * 
     * @param annots The new list of annotations.
     */
    public void setAnnotations(List<PDAnnotation> annots)
    {
        page.setItem(COSName.ANNOTS, COSArrayList.converterToCOSArray(annots));
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object other)
    {
        return other instanceof PDPage && ((PDPage) other).getCOSObject() == this.getCOSObject();
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode()
    {
        return this.getCOSDictionary().hashCode();
    }
}
