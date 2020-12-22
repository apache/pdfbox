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
package org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;

/**
 * Implementation of {@link PDFTemplateBuilder}. This builds the signature PDF but doesn't keep the
 * elements, these are kept in its PDF template structure.
 *
 * @author Vakhtang Koroghlishvili
 */
public class PDVisibleSigBuilder implements PDFTemplateBuilder
{
    private final PDFTemplateStructure pdfStructure;
    private static final Log LOG = LogFactory.getLog(PDVisibleSigBuilder.class);

    /**
     * Constructor, creates PDF template structure.
     */
    public PDVisibleSigBuilder()
    {
        pdfStructure = new PDFTemplateStructure();
        LOG.info("PDF Structure has been created");
    }

    @Override
    public void createPage(final PDVisibleSignDesigner properties)
    {
        final PDPage page = new PDPage(new PDRectangle(properties.getPageWidth(),
                                                 properties.getPageHeight()));
        pdfStructure.setPage(page);
        LOG.info("PDF page has been created");
    }

    /**
     * Creates a PDDocument and adds the page parameter to it and keeps this as a template in the
     * PDF template Structure.
     *
     * @param page
     * @throws IOException
     */
    @Override
    public void createTemplate(final PDPage page) throws IOException
    {
        final PDDocument template = new PDDocument();
        template.addPage(page);
        pdfStructure.setTemplate(template);
    }

    @Override
    public void createAcroForm(final PDDocument template)
    {
        final PDAcroForm theAcroForm = new PDAcroForm(template);
        template.getDocumentCatalog().setAcroForm(theAcroForm);
        pdfStructure.setAcroForm(theAcroForm);
        LOG.info("AcroForm has been created");
    }

    @Override
    public PDFTemplateStructure getStructure()
    {
        return pdfStructure;
    }

    @Override
    public void createSignatureField(final PDAcroForm acroForm) throws IOException
    {
        final PDSignatureField sf = new PDSignatureField(acroForm);
        pdfStructure.setSignatureField(sf);
        LOG.info("Signature field has been created");
    }

    @Override
    public void createSignature(final PDSignatureField pdSignatureField, final PDPage page, final String signerName)
            throws IOException
    {
        final PDSignature pdSignature = new PDSignature();
        final PDAnnotationWidget widget = pdSignatureField.getWidgets().get(0);
        pdSignatureField.setValue(pdSignature);
        widget.setPage(page);
        page.getAnnotations().add(widget);
        if (!signerName.isEmpty())
        {
            pdSignature.setName(signerName);
        }
        pdfStructure.setPdSignature(pdSignature);
        LOG.info("PDSignature has been created");
    }

    @Override
    public void createAcroFormDictionary(final PDAcroForm acroForm, final PDSignatureField signatureField)
            throws IOException
    {
        @SuppressWarnings("unchecked") final List<PDField> acroFormFields = acroForm.getFields();
        final COSDictionary acroFormDict = acroForm.getCOSObject();
        acroForm.setSignaturesExist(true);
        acroForm.setAppendOnly(true);
        acroFormDict.setDirect(true);
        acroFormFields.add(signatureField);
        // WTF sylfaen? 
        acroForm.setDefaultAppearance("/sylfaen 0 Tf 0 g");
        pdfStructure.setAcroFormFields(acroFormFields);
        pdfStructure.setAcroFormDictionary(acroFormDict);
        LOG.info("AcroForm dictionary has been created");
    }

    @Override
    public void createSignatureRectangle(final PDSignatureField signatureField,
                                         final PDVisibleSignDesigner properties) throws IOException
    {

        final PDRectangle rect = new PDRectangle();
        rect.setUpperRightX(properties.getxAxis() + properties.getWidth());
        rect.setUpperRightY(properties.getTemplateHeight() - properties.getyAxis());
        rect.setLowerLeftY(properties.getTemplateHeight() - properties.getyAxis() -
                           properties.getHeight());
        rect.setLowerLeftX(properties.getxAxis());
        signatureField.getWidgets().get(0).setRectangle(rect);
        pdfStructure.setSignatureRectangle(rect);
        LOG.info("Signature rectangle has been created");
    }

    @Override
    public void createAffineTransform(final AffineTransform affineTransform)
    {
        pdfStructure.setAffineTransform(affineTransform);
        LOG.info("Matrix has been added");
    }

    @Override
    public void createProcSetArray()
    {
        final COSArray procSetArr = new COSArray();
        procSetArr.add(COSName.getPDFName("PDF"));
        procSetArr.add(COSName.getPDFName("Text"));
        procSetArr.add(COSName.getPDFName("ImageB"));
        procSetArr.add(COSName.getPDFName("ImageC"));
        procSetArr.add(COSName.getPDFName("ImageI"));
        pdfStructure.setProcSet(procSetArr);
        LOG.info("ProcSet array has been created");
    }

    @Override
    public void createSignatureImage(final PDDocument template, final BufferedImage image) throws IOException
    {
        pdfStructure.setImage(LosslessFactory.createFromImage(template, image));
        LOG.info("Visible Signature Image has been created");
    }

    @Override
    public void createFormatterRectangle(final int[] params)
    {
        final PDRectangle formatterRectangle = new PDRectangle();
        formatterRectangle.setLowerLeftX(Math.min(params[0],params[2]));
        formatterRectangle.setLowerLeftY(Math.min(params[1],params[3]));
        formatterRectangle.setUpperRightX(Math.max(params[0],params[2]));
        formatterRectangle.setUpperRightY(Math.max(params[1],params[3]));

        pdfStructure.setFormatterRectangle(formatterRectangle);
        LOG.info("Formatter rectangle has been created");
    }

    @Override
    public void createHolderFormStream(final PDDocument template)
    {
        final PDStream holderForm = new PDStream(template);
        pdfStructure.setHolderFormStream(holderForm);
        LOG.info("Holder form stream has been created");
    }

    @Override
    public void createHolderFormResources()
    {
        final PDResources holderFormResources = new PDResources();
        pdfStructure.setHolderFormResources(holderFormResources);
        LOG.info("Holder form resources have been created");

    }

    @Override
    public void createHolderForm(final PDResources holderFormResources, final PDStream holderFormStream,
                                 final PDRectangle bbox)
    {
        final PDFormXObject holderForm = new PDFormXObject(holderFormStream);
        holderForm.setResources(holderFormResources);
        holderForm.setBBox(bbox);
        holderForm.setFormType(1);
        pdfStructure.setHolderForm(holderForm);
        LOG.info("Holder form has been created");

    }

    @Override
    public void createAppearanceDictionary(final PDFormXObject holderForml,
                                           final PDSignatureField signatureField) throws IOException
    {
        final PDAppearanceDictionary appearance = new PDAppearanceDictionary();
        appearance.getCOSObject().setDirect(true);

        final PDAppearanceStream appearanceStream = new PDAppearanceStream(holderForml.getCOSObject());

        appearance.setNormalAppearance(appearanceStream);
        signatureField.getWidgets().get(0).setAppearance(appearance);

        pdfStructure.setAppearanceDictionary(appearance);
        LOG.info("PDF appearance dictionary has been created");
    }

    @Override
    public void createInnerFormStream(final PDDocument template)
    {
        final PDStream innerFormStream = new PDStream(template);
        pdfStructure.setInnterFormStream(innerFormStream);
        LOG.info("Stream of another form (inner form - it will be inside holder form) " +
                 "has been created");
    }

    @Override
    public void createInnerFormResource()
    {
        final PDResources innerFormResources = new PDResources();
        pdfStructure.setInnerFormResources(innerFormResources);
        LOG.info("Resources of another form (inner form - it will be inside holder form)" +
                 "have been created");
    }

    @Override
    public void createInnerForm(final PDResources innerFormResources, final PDStream innerFormStream,
                                final PDRectangle bbox)
    {
        final PDFormXObject innerForm = new PDFormXObject(innerFormStream);
        innerForm.setResources(innerFormResources);
        innerForm.setBBox(bbox);
        innerForm.setFormType(1);
        pdfStructure.setInnerForm(innerForm);
        LOG.info("Another form (inner form - it will be inside holder form) has been created");
    }

    @Override
    public void insertInnerFormToHolderResources(final PDFormXObject innerForm,
                                                 final PDResources holderFormResources)
    {
        holderFormResources.put(COSName.FRM, innerForm);
        pdfStructure.setInnerFormName(COSName.FRM);
        LOG.info("Now inserted inner form inside holder form");
    }

    @Override
    public void createImageFormStream(final PDDocument template)
    {
        final PDStream imageFormStream = new PDStream(template);
        pdfStructure.setImageFormStream(imageFormStream);
        LOG.info("Created image form stream");
    }

    @Override
    public void createImageFormResources()
    {
        final PDResources imageFormResources = new PDResources();
        pdfStructure.setImageFormResources(imageFormResources);
        LOG.info("Created image form resources");
    }

    @Override
    public void createImageForm(final PDResources imageFormResources, final PDResources innerFormResource,
                                final PDStream imageFormStream, final PDRectangle bbox, final AffineTransform at,
                                final PDImageXObject img) throws IOException
    {
        final PDFormXObject imageForm = new PDFormXObject(imageFormStream);
        imageForm.setBBox(bbox);
        imageForm.setMatrix(at);
        imageForm.setResources(imageFormResources);
        imageForm.setFormType(1);

        imageFormResources.getCOSObject().setDirect(true);

        final COSName imageFormName = COSName.getPDFName("n2");
        innerFormResource.put(imageFormName, imageForm);
        final COSName imageName = imageFormResources.add(img, "img");
        pdfStructure.setImageForm(imageForm);
        pdfStructure.setImageFormName(imageFormName);
        pdfStructure.setImageName(imageName);
        LOG.info("Created image form");
    }

    @Override
    public void createBackgroundLayerForm(final PDResources innerFormResource, final PDRectangle bbox)
             throws IOException
    {
        // create blank n0 background layer form
        final PDFormXObject n0Form = new PDFormXObject(pdfStructure.getTemplate().getDocument().createCOSStream());
        n0Form.setBBox(bbox);
        n0Form.setResources(new PDResources());
        n0Form.setFormType(1);
        innerFormResource.put(COSName.getPDFName("n0"), n0Form);
        LOG.info("Created background layer form");
    }

    @Override
    public void injectProcSetArray(final PDFormXObject innerForm, final PDPage page,
                                   final PDResources innerFormResources, final PDResources imageFormResources,
                                   final PDResources holderFormResources, final COSArray procSet)
    {
        innerForm.getResources().getCOSObject().setItem(COSName.PROC_SET, procSet);
        page.getCOSObject().setItem(COSName.PROC_SET, procSet);
        innerFormResources.getCOSObject().setItem(COSName.PROC_SET, procSet);
        imageFormResources.getCOSObject().setItem(COSName.PROC_SET, procSet);
        holderFormResources.getCOSObject().setItem(COSName.PROC_SET, procSet);
        LOG.info("Inserted ProcSet to PDF");
    }

    @Override
    public void injectAppearanceStreams(final PDStream holderFormStream, final PDStream innerFormStream,
                                        final PDStream imageFormStream, final COSName imageFormName,
                                        final COSName imageName, final COSName innerFormName,
                                        final PDVisibleSignDesigner properties) throws IOException
    {
        // Use width and height of BBox as values for transformation matrix.
        final int width = (int) this.getStructure().getFormatterRectangle().getWidth();
        final int height = (int) this.getStructure().getFormatterRectangle().getHeight();

        final String imgFormContent    = "q " + width + " 0 0 " + height + " 0 0 cm /" + imageName.getName() + " Do Q\n";
        final String holderFormContent = "q 1 0 0 1 0 0 cm /" + innerFormName.getName() + " Do Q\n";
        final String innerFormContent  = "q 1 0 0 1 0 0 cm /n0 Do Q q 1 0 0 1 0 0 cm /" + imageFormName.getName() + " Do Q\n";

        appendRawCommands(pdfStructure.getHolderFormStream().createOutputStream(), holderFormContent);
        appendRawCommands(pdfStructure.getInnerFormStream().createOutputStream(), innerFormContent);
        appendRawCommands(pdfStructure.getImageFormStream().createOutputStream(), imgFormContent);
        LOG.info("Injected appearance stream to pdf");
    }

    public void appendRawCommands(final OutputStream os, final String commands) throws IOException
    {
        os.write(commands.getBytes(StandardCharsets.UTF_8));
        os.close();
    }

    @Override
    public void createVisualSignature(final PDDocument template)
    {
        pdfStructure.setVisualSignature(template.getDocument());
        LOG.info("Visible signature has been created");
    }

    @Override
    public void createWidgetDictionary(final PDSignatureField signatureField,
                                       final PDResources holderFormResources) throws IOException
    {
        final COSDictionary widgetDict = signatureField.getWidgets().get(0).getCOSObject();
        widgetDict.setNeedToBeUpdated(true);
        widgetDict.setItem(COSName.DR, holderFormResources.getCOSObject());

        pdfStructure.setWidgetDictionary(widgetDict);
        LOG.info("WidgetDictionary has been created");
    }

    @Override
    public void closeTemplate(final PDDocument template) throws IOException
    {
        template.close();
        pdfStructure.getTemplate().close();
    }
}
