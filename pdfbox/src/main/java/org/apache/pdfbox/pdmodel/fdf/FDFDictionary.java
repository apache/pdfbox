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
package org.apache.pdfbox.pdmodel.fdf;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.common.filespecification.PDFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDSimpleFileSpecification;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This represents an FDF dictionary that is part of the FDF document.
 *
 * @author Ben Litchfield
 */
public class FDFDictionary implements COSObjectable
{

    private static final Log LOG = LogFactory.getLog(FDFDictionary.class);

    private final COSDictionary fdf;

    /**
     * Default constructor.
     */
    public FDFDictionary()
    {
        fdf = new COSDictionary();
    }

    /**
     * Constructor.
     *
     * @param fdfDictionary The FDF documents catalog.
     */
    public FDFDictionary(final COSDictionary fdfDictionary)
    {
        fdf = fdfDictionary;
    }

    /**
     * This will create an FDF dictionary from an XFDF XML document.
     *
     * @param fdfXML The XML document that contains the XFDF data.
     */
    public FDFDictionary(final Element fdfXML)
    {
        this();
        final NodeList nodeList = fdfXML.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++)
        {
            final Node node = nodeList.item(i);
            if (node instanceof Element)
            {
                final Element child = (Element) node;
                switch (child.getTagName())
                {
                    case "f":
                        final PDSimpleFileSpecification fs = new PDSimpleFileSpecification();
                        fs.setFile(child.getAttribute("href"));
                        setFile(fs);
                        break;
                    case "ids":
                        final COSArray ids = new COSArray();
                        final String original = child.getAttribute("original");
                        final String modified = child.getAttribute("modified");
                        try
                        {
                            ids.add(COSString.parseHex(original));
                        }
                        catch (final IOException e)
                        {
                            LOG.warn("Error parsing ID entry for attribute 'original' [" + original +
                                    "]. ID entry ignored.", e);
                        }
                        try
                        {
                            ids.add(COSString.parseHex(modified));
                        }
                        catch (final IOException e)
                        {
                            LOG.warn("Error parsing ID entry for attribute 'modified' [" + modified +
                                    "]. ID entry ignored.", e);
                        }
                        setID(ids);
                        break;
                    case "fields":
                        final NodeList fields = child.getChildNodes();
                        final List<FDFField> fieldList = new ArrayList<>();
                        for (int f = 0; f < fields.getLength(); f++)
                        {
                            final Node currentNode = fields.item(f);
                            if (currentNode instanceof Element
                                    && ((Element) currentNode).getTagName().equals("field"))
                            {
                                try
                                {
                                    fieldList.add(new FDFField((Element) fields.item(f)));
                                }
                                catch (final IOException e)
                                {
                                    LOG.warn("Error parsing field entry [" + currentNode.getNodeValue() +
                                            "]. Field ignored.", e);
                                }
                            }
                        }
                        setFields(fieldList);
                        break;
                    case "annots":
                        final NodeList annots = child.getChildNodes();
                        final List<FDFAnnotation> annotList = new ArrayList<>();
                        for (int j = 0; j < annots.getLength(); j++)
                        {
                            final Node annotNode = annots.item(j);
                            if (annotNode instanceof Element)
                            {
                                // the node name defines the annotation type
                                final Element annot = (Element) annotNode;
                                final String annotationName = annot.getNodeName();
                                try
                                {
                                    switch (annotationName)
                                    {
                                        case "text":
                                            annotList.add(new FDFAnnotationText(annot));
                                            break;
                                        case "caret":
                                            annotList.add(new FDFAnnotationCaret(annot));
                                            break;
                                        case "freetext":
                                            annotList.add(new FDFAnnotationFreeText(annot));
                                            break;
                                        case "fileattachment":
                                            annotList.add(new FDFAnnotationFileAttachment(annot));
                                            break;
                                        case "highlight":
                                            annotList.add(new FDFAnnotationHighlight(annot));
                                            break;
                                        case "ink":
                                            annotList.add(new FDFAnnotationInk(annot));
                                            break;
                                        case "line":
                                            annotList.add(new FDFAnnotationLine(annot));
                                            break;
                                        case "link":
                                            annotList.add(new FDFAnnotationLink(annot));
                                            break;
                                        case "circle":
                                            annotList.add(new FDFAnnotationCircle(annot));
                                            break;
                                        case "square":
                                            annotList.add(new FDFAnnotationSquare(annot));
                                            break;
                                        case "polygon":
                                            annotList.add(new FDFAnnotationPolygon(annot));
                                            break;
                                        case "polyline":
                                            annotList.add(new FDFAnnotationPolyline(annot));
                                            break;
                                        case "sound":
                                            annotList.add(new FDFAnnotationSound(annot));
                                            break;
                                        case "squiggly":
                                            annotList.add(new FDFAnnotationSquiggly(annot));
                                            break;
                                        case "stamp":
                                            annotList.add(new FDFAnnotationStamp(annot));
                                            break;
                                        case "strikeout":
                                            annotList.add(new FDFAnnotationStrikeOut(annot));
                                            break;
                                        case "underline":
                                            annotList.add(new FDFAnnotationUnderline(annot));
                                            break;
                                        default:
                                            LOG.warn("Unknown or unsupported annotation type '" +
                                                    annotationName + "'");
                                            break;
                                    }
                                }
                                catch (final IOException e)
                                {
                                    LOG.warn("Error parsing annotation information [" +
                                            annot.getNodeValue() + "]. Annotation ignored", e);
                                }
                            }
                        }
                        setAnnotations(annotList);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * This will write this element as an XML document.
     *
     * @param output The stream to write the xml to.
     *
     * @throws IOException If there is an error writing the XML.
     */
    public void writeXML(final Writer output) throws IOException
    {
        final PDFileSpecification fs = this.getFile();
        if (fs != null)
        {
            output.write("<f href=\"" + fs.getFile() + "\" />\n");
        }
        final COSArray ids = this.getID();
        if (ids != null)
        {
            final COSString original = (COSString) ids.getObject(0);
            final COSString modified = (COSString) ids.getObject(1);
            output.write("<ids original=\"" + original.toHexString() + "\" ");
            output.write("modified=\"" + modified.toHexString() + "\" />\n");
        }
        final List<FDFField> fields = getFields();
        if (fields != null && fields.size() > 0)
        {
            output.write("<fields>\n");
            for (final FDFField field : fields)
            {
                field.writeXML(output);
            }
            output.write("</fields>\n");
        }
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    @Override
    public COSDictionary getCOSObject()
    {
        return fdf;
    }

    /**
     * The source file or target file: the PDF document file that this FDF file was exported from or is intended to be
     * imported into.
     *
     * @return The F entry of the FDF dictionary.
     *
     * @throws IOException If there is an error creating the file spec.
     */
    public PDFileSpecification getFile() throws IOException
    {
        return PDFileSpecification.createFS(fdf.getDictionaryObject(COSName.F));
    }

    /**
     * This will set the file specification.
     *
     * @param fs The file specification.
     */
    public final void setFile(final PDFileSpecification fs)
    {
        fdf.setItem(COSName.F, fs);
    }

    /**
     * This is the FDF id.
     *
     * @return The FDF ID.
     */
    public COSArray getID()
    {
        return (COSArray) fdf.getDictionaryObject(COSName.ID);
    }

    /**
     * This will set the FDF id.
     *
     * @param id The new id for the FDF.
     */
    public final void setID(final COSArray id)
    {
        fdf.setItem(COSName.ID, id);
    }

    /**
     * This will get the list of FDF Fields. This will return a list of FDFField objects.
     *
     * @return A list of FDF fields.
     */
    public List<FDFField> getFields()
    {
        List<FDFField> retval = null;
        final COSArray fieldArray = (COSArray) fdf.getDictionaryObject(COSName.FIELDS);
        if (fieldArray != null)
        {
            final List<FDFField> fields = new ArrayList<>();
            for (int i = 0; i < fieldArray.size(); i++)
            {
                fields.add(new FDFField((COSDictionary) fieldArray.getObject(i)));
            }
            retval = new COSArrayList<>(fields, fieldArray);
        }
        return retval;
    }

    /**
     * This will set the list of fields. This should be a list of FDFField objects.
     *
     * @param fields The list of fields.
     */
    public final void setFields(final List<FDFField> fields)
    {
        fdf.setItem(COSName.FIELDS, new COSArray(fields));
    }

    /**
     * This will get the status string to be displayed as the result of an action.
     *
     * @return The status.
     */
    public String getStatus()
    {
        return fdf.getString(COSName.STATUS);
    }

    /**
     * This will set the status string.
     *
     * @param status The new status string.
     */
    public void setStatus(final String status)
    {
        fdf.setString(COSName.STATUS, status);
    }

    /**
     * This will get the list of FDF Pages. This will return a list of FDFPage objects.
     *
     * @return A list of FDF pages.
     */
    public List<FDFPage> getPages()
    {
        List<FDFPage> retval = null;
        final COSArray pageArray = (COSArray) fdf.getDictionaryObject(COSName.PAGES);
        if (pageArray != null)
        {
            final List<FDFPage> pages = new ArrayList<>();
            for (int i = 0; i < pageArray.size(); i++)
            {
                pages.add(new FDFPage((COSDictionary) pageArray.get(i)));
            }
            retval = new COSArrayList<>(pages, pageArray);
        }
        return retval;
    }

    /**
     * This will set the list of pages. This should be a list of FDFPage objects.
     *
     *
     * @param pages The list of pages.
     */
    public void setPages(final List<FDFPage> pages)
    {
        fdf.setItem(COSName.PAGES, new COSArray(pages));
    }

    /**
     * The encoding to be used for a FDF field. The default is PDFDocEncoding and this method will never return null.
     *
     * @return The encoding value.
     */
    public String getEncoding()
    {
        String encoding = fdf.getNameAsString(COSName.ENCODING);
        if (encoding == null)
        {
            encoding = "PDFDocEncoding";
        }
        return encoding;

    }

    /**
     * This will set the encoding.
     *
     * @param encoding The new encoding.
     */
    public void setEncoding(final String encoding)
    {
        fdf.setName(COSName.ENCODING, encoding);
    }

    /**
     * This will get the list of FDF Annotations. This will return a list of FDFAnnotation objects or null if the entry
     * is not set.
     *
     * @return A list of FDF annotations.
     *
     * @throws IOException If there is an error creating the annotation list.
     */
    public List<FDFAnnotation> getAnnotations() throws IOException
    {
        List<FDFAnnotation> retval = null;
        final COSArray annotArray = (COSArray) fdf.getDictionaryObject(COSName.ANNOTS);
        if (annotArray != null)
        {
            final List<FDFAnnotation> annots = new ArrayList<>();
            for (int i = 0; i < annotArray.size(); i++)
            {
                annots.add(FDFAnnotation.create((COSDictionary) annotArray.getObject(i)));
            }
            retval = new COSArrayList<>(annots, annotArray);
        }
        return retval;
    }

    /**
     * This will set the list of annotations. This should be a list of FDFAnnotation objects.
     *
     *
     * @param annots The list of annotations.
     */
    public final void setAnnotations(final List<FDFAnnotation> annots)
    {
        fdf.setItem(COSName.ANNOTS, new COSArray(annots));
    }

    /**
     * This will get the incremental updates since the PDF was last opened.
     *
     * @return The differences entry of the FDF dictionary.
     */
    public COSStream getDifferences()
    {
        return (COSStream) fdf.getDictionaryObject(COSName.DIFFERENCES);
    }

    /**
     * This will set the differences stream.
     *
     * @param diff The new differences stream.
     */
    public void setDifferences(final COSStream diff)
    {
        fdf.setItem(COSName.DIFFERENCES, diff);
    }

    /**
     * This will get the target frame in the browser to open this document.
     *
     * @return The target frame.
     */
    public String getTarget()
    {
        return fdf.getString(COSName.TARGET);
    }

    /**
     * This will set the target frame in the browser to open this document.
     *
     * @param target The new target frame.
     */
    public void setTarget(final String target)
    {
        fdf.setString(COSName.TARGET, target);
    }

    /**
     * This will get the list of embedded FDF entries, or null if the entry is null. This will return a list of
     * PDFileSpecification objects.
     *
     * @return A list of embedded FDF files.
     *
     * @throws IOException If there is an error creating the file spec.
     */
    public List<PDFileSpecification> getEmbeddedFDFs() throws IOException
    {
        List<PDFileSpecification> retval = null;
        final COSArray embeddedArray = (COSArray) fdf.getDictionaryObject(COSName.EMBEDDED_FDFS);
        if (embeddedArray != null)
        {
            final List<PDFileSpecification> embedded = new ArrayList<>();
            for (int i = 0; i < embeddedArray.size(); i++)
            {
                embedded.add(PDFileSpecification.createFS(embeddedArray.get(i)));
            }
            retval = new COSArrayList<>(embedded, embeddedArray);
        }
        return retval;
    }

    /**
     * This will set the list of embedded FDFs. This should be a list of PDFileSpecification objects.
     *
     *
     * @param embedded The list of embedded FDFs.
     */
    public void setEmbeddedFDFs(final List<PDFileSpecification> embedded)
    {
        fdf.setItem(COSName.EMBEDDED_FDFS, new COSArray(embedded));
    }

    /**
     * This will get the java script entry.
     *
     * @return The java script entry describing javascript commands.
     */
    public FDFJavaScript getJavaScript()
    {
        FDFJavaScript fs = null;
        final COSDictionary dic = (COSDictionary) fdf.getDictionaryObject(COSName.JAVA_SCRIPT);
        if (dic != null)
        {
            fs = new FDFJavaScript(dic);
        }
        return fs;
    }

    /**
     * This will set the JavaScript entry.
     *
     * @param js The javascript entries.
     */
    public void setJavaScript(final FDFJavaScript js)
    {
        fdf.setItem(COSName.JAVA_SCRIPT, js);
    }

}
