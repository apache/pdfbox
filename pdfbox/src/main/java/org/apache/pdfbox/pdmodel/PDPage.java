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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.contentstream.PDContentStream;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
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
import org.apache.pdfbox.pdmodel.interactive.pagenavigation.PDTransition;
import org.apache.pdfbox.util.Matrix;

/**
 * A page in a PDF document.
 * 
 * @author Ben Litchfield
 */
public class PDPage implements COSObjectable, PDContentStream
{
    /**
     * Log instance
     */
    private static final Log LOG = LogFactory.getLog(PDPage.class);
    
    private final COSDictionary page;
    private PDResources pageResources;
    private ResourceCache resourceCache;
    private PDRectangle mediaBox;

    /**
     * Creates a new PDPage instance for embedding, with a size of U.S. Letter (8.5 x 11 inches).
     */
    public PDPage()
    {
        this(PDRectangle.LETTER);
    }

    /**
     * Creates a new instance of PDPage for embedding.
     * 
     * @param mediaBox The MediaBox of the page.
     */
    public PDPage(PDRectangle mediaBox)
    {
        page = new COSDictionary();
        page.setItem(COSName.TYPE, COSName.PAGE);
        page.setItem(COSName.MEDIA_BOX, mediaBox);
    }

    /**
     * Creates a new instance of PDPage for reading.
     * 
     * @param pageDictionary A page dictionary in a PDF document.
     */
    public PDPage(COSDictionary pageDictionary)
    {
        page = pageDictionary;
    }

    /**
     * Creates a new instance of PDPage for reading.
     *
     * @param pageDictionary A page dictionary in a PDF document.
     */
    PDPage(COSDictionary pageDictionary, ResourceCache resourceCache)
    {
        page = pageDictionary;
        this.resourceCache = resourceCache;
    }

    /**
     * Convert this standard java object to a COS object.
     * 
     * @return The cos object that matches this Java object.
     */
    @Override
    public COSDictionary getCOSObject()
    {
        return page;
    }

    /**
     * Returns the content streams which make up this page.
     * 
     * @return content stream iterator
     */
    public Iterator<PDStream> getContentStreams()
    {
        List<PDStream> streams = new ArrayList<PDStream>();
        COSBase base = page.getDictionaryObject(COSName.CONTENTS);
        if (base instanceof COSStream)
        {
            streams.add(new PDStream((COSStream) base));
        }
        else if (base instanceof COSArray && ((COSArray) base).size() > 0)
        {
            COSArray array = (COSArray)base;
            for (int i = 0; i < array.size(); i++)
            {
                COSStream stream = (COSStream) array.getObject(i);
                streams.add(new PDStream(stream));
            }
        }
        return streams.iterator();
    }
    
    @Override
    public InputStream getContents() throws IOException
    {
        COSBase base = page.getDictionaryObject(COSName.CONTENTS);
        if (base instanceof COSStream)
        {
            return ((COSStream)base).createInputStream();
        }
        else if (base instanceof COSArray && ((COSArray) base).size() > 0)
        {
            COSArray streams = (COSArray)base;
            byte[] delimiter = new byte[] { '\n' };
            List<InputStream> inputStreams = new ArrayList<InputStream>();
            for (int i = 0; i < streams.size(); i++)
            {
                COSStream stream = (COSStream)streams.getObject(i);
                inputStreams.add(stream.createInputStream());
                inputStreams.add(new ByteArrayInputStream(delimiter));
            }
            return new SequenceInputStream(Collections.enumeration(inputStreams));
        }
        return new ByteArrayInputStream(new byte[0]);
    }

    /**
     * Returns true if this page has contents.
     */
    public boolean hasContents()
    {
        COSBase contents = page.getDictionaryObject(COSName.CONTENTS);
        if (contents instanceof COSStream)
        {
            return ((COSStream) contents).size() > 0;
        }
        else if (contents instanceof COSArray)
        {
            return ((COSArray) contents).size() > 0;
        }
        return false;
    }

    /**
     * A dictionary containing any resources required by the page.
     */
    @Override
    public PDResources getResources()
    {
        if (pageResources == null)
        {
            COSDictionary resources = (COSDictionary)
                    PDPageTree.getInheritableAttribute(page, COSName.RESOURCES);

            // note: it's an error for resources to not be present
            if (resources != null)
            {
                pageResources = new PDResources(resources, resourceCache);
            }
        }
        return pageResources;
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

    @Override
    public PDRectangle getBBox()
    {
        return getCropBox();
    }

    @Override
    public Matrix getMatrix()
    {
        // todo: take into account user-space unit redefinition as scale?
        return new Matrix();
    }

    /**
     * A rectangle, expressed in default user space units, defining the boundaries of the physical
     * medium on which the page is intended to be displayed or printed.
     */
    public PDRectangle getMediaBox()
    {
        if (mediaBox == null)
        {
            COSArray array = (COSArray) PDPageTree.getInheritableAttribute(page, COSName.MEDIA_BOX);
            if (array != null)
            {
                mediaBox = new PDRectangle(array);
            }
        }
        if (mediaBox == null)
        {
            LOG.debug("Can't find MediaBox, will use U.S. Letter");
            mediaBox = PDRectangle.LETTER;
        }
        return mediaBox;
    }

    /**
     * This will set the mediaBox for this page.
     * 
     * @param mediaBox The new mediaBox for this page.
     */
    public void setMediaBox(PDRectangle mediaBox)
    {
        this.mediaBox = mediaBox;
        if (mediaBox == null)
        {
            page.removeItem(COSName.MEDIA_BOX);
        }
        else
        {
            page.setItem(COSName.MEDIA_BOX, mediaBox);
        }
    }

    /**
     * A rectangle, expressed in default user space units, defining the visible region of default
     * user space. When the page is displayed or printed, its contents are to be clipped (cropped)
     * to this rectangle.
     */
    public PDRectangle getCropBox()
    {
        COSArray array = (COSArray) PDPageTree.getInheritableAttribute(page, COSName.CROP_BOX);
        if (array != null)
        {
            return clipToMediaBox(new PDRectangle(array));
        }
        else
        {
            return getMediaBox();
        }
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
     * A rectangle, expressed in default user space units, defining the region to which the contents
     * of the page should be clipped when output in a production environment. The default is the
     * CropBox.
     * 
     * @return The BleedBox attribute.
     */
    public PDRectangle getBleedBox()
    {
        PDRectangle retval;
        COSArray array = (COSArray) page.getDictionaryObject(COSName.BLEED_BOX);
        if (array != null)
        {
            retval = clipToMediaBox(new PDRectangle(array));
        }
        else
        {
            retval = getCropBox();
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
            page.setItem(COSName.BLEED_BOX, bleedBox);
        }
    }

    /**
     * A rectangle, expressed in default user space units, defining the intended dimensions of the
     * finished page after trimming. The default is the CropBox.
     * 
     * @return The TrimBox attribute.
     */
    public PDRectangle getTrimBox()
    {
        PDRectangle retval;
        COSArray array = (COSArray) page.getDictionaryObject(COSName.TRIM_BOX);
        if (array != null)
        {
            retval = clipToMediaBox(new PDRectangle(array));
        }
        else
        {
            retval = getCropBox();
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
            page.setItem(COSName.TRIM_BOX, trimBox);
        }
    }

    /**
     * A rectangle, expressed in default user space units, defining the extent of the page's
     * meaningful content (including potential white space) as intended by the page's creator The
     * default is the CropBox.
     * 
     * @return The ArtBox attribute.
     */
    public PDRectangle getArtBox()
    {
        PDRectangle retval;
        COSArray array = (COSArray) page.getDictionaryObject(COSName.ART_BOX);
        if (array != null)
        {
            retval = clipToMediaBox(new PDRectangle(array));
        }
        else
        {
            retval = getCropBox();
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
            page.setItem(COSName.ART_BOX, artBox);
        }
    }
    
    /**
     * Clips the given box to the bounds of the media box.
     */
    private PDRectangle clipToMediaBox(PDRectangle box)
    {
        PDRectangle mediaBox = getMediaBox();
        PDRectangle result = new PDRectangle();
        result.setLowerLeftX(Math.max(mediaBox.getLowerLeftX(), box.getLowerLeftX()));
        result.setLowerLeftY(Math.max(mediaBox.getLowerLeftY(), box.getLowerLeftY()));
        result.setUpperRightX(Math.min(mediaBox.getUpperRightX(), box.getUpperRightX()));
        result.setUpperRightY(Math.min(mediaBox.getUpperRightY(), box.getUpperRightY()));
        return result;
    }

    /**
     * Returns the rotation angle in degrees by which the page should be rotated
     * clockwise when displayed or printed. Valid values in a PDF must be a
     * multiple of 90.
     *
     * @return The rotation angle in degrees in normalized form (0, 90, 180 or
     * 270) or 0 if invalid or not set at this level.
     */
    public int getRotation()
    {
        COSBase obj = PDPageTree.getInheritableAttribute(page, COSName.ROTATE);
        if (obj instanceof COSNumber)
        {
            int rotationAngle = ((COSNumber) obj).intValue();
            if (rotationAngle % 90 == 0)
            {
                return (rotationAngle % 360 + 360) % 360;
            }
        }
        return 0;
    }

    /**
     * This will set the rotation for this page.
     * 
     * @param rotation The new rotation for this page in degrees.
     */
    public void setRotation(int rotation)
    {
        page.setInt(COSName.ROTATE, rotation);
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
     * This will set the contents of this page.
     *
     * @param contents Array of new contents of the page.
     */
    public void setContents(List<PDStream> contents)
    {
        COSArray array = new COSArray();
        for (PDStream stream : contents)
        {
            array.add(stream);
        }
        page.setItem(COSName.CONTENTS, array);
    }

    /**
     * This will get a list of PDThreadBead objects, which are article threads in the document. This
     * will return an empty list if there are no thread beads.
     *
     * @return A list of article threads on this page, never null. The returned list is backed by
     * the beads COSArray, so any adding or deleting in this list will change the document too.
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
     * Get the metadata that is part of the document catalog. This will return null if there is
     * no meta data for this object.
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
     * @return The page transition associated with this page or null if no transition is defined
     */
    public PDTransition getTransition()
    {
        COSDictionary transitionDictionary = (COSDictionary) page.getDictionaryObject(COSName.TRANS);
        return transitionDictionary == null ? null : new PDTransition(transitionDictionary);
    }

    /**
     * @param transition The new transition to set on this page.
     */
    public void setTransition(PDTransition transition)
    {
        page.setItem(COSName.TRANS, transition);
    }

    /**
     * Convenient method to set a transition and the display duration
     * 
     * @param transition The new transition to set on this page.
     * @param duration The maximum length of time, in seconds, that the page shall be displayed during presentations
     * before the viewer application shall automatically advance to the next page.
     */
    public void setTransition(PDTransition transition, float duration)
    {
        page.setItem(COSName.TRANS, transition);
        page.setItem(COSName.DUR, new COSFloat(duration));
    }

    /**
     * This will return a list of the annotations for this page.
     *
     * @return List of the PDAnnotation objects, never null. The returned list is backed by the
     * annotations COSArray, so any adding or deleting in this list will change the document too.
     * 
     * @throws IOException If there is an error while creating the annotation list.
     */
    public List<PDAnnotation> getAnnotations() throws IOException
    {
        COSArrayList<PDAnnotation> retval;
        COSArray annots = (COSArray) page.getDictionaryObject(COSName.ANNOTS);
        if (annots == null)
        {
            return new COSArrayList<PDAnnotation>(page, COSName.ANNOTS);
        }
        else
        {
            List<PDAnnotation> actuals = new ArrayList<PDAnnotation>();
            for (int i = 0; i < annots.size(); i++)
            {
                COSBase item = annots.getObject(i);
                if (item == null)
                {
                    continue;
                }
                actuals.add(PDAnnotation.createAnnotation(item));
            }
            retval = new COSArrayList<PDAnnotation>(actuals, annots);
        }
        return retval;
    }

    /**
     * This will set the list of annotations.
     * 
     * @param annotations The new list of annotations.
     */
    public void setAnnotations(List<PDAnnotation> annotations)
    {
        page.setItem(COSName.ANNOTS, COSArrayList.converterToCOSArray(annotations));
    }

    @Override
    public boolean equals(Object other)
    {
        return other instanceof PDPage && ((PDPage) other).getCOSObject() == this.getCOSObject();
    }

    @Override
    public int hashCode()
    {
        return page.hashCode();
    }

    /**
     * Returns the resource cache associated with this page, or null if there is none.
     */
    public ResourceCache getResourceCache()
    {
        return resourceCache;
    }
}
