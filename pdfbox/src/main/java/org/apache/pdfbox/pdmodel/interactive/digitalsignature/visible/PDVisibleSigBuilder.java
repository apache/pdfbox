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
    public void createPage(PDVisibleSignDesigner properties)
    {
        PDPage page = new PDPage(new PDRectangle(properties.getPageWidth(),
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
    public void createTemplate(PDPage page) throws IOException
    {
        PDDocument template = new PDDocument();
        template.addPage(page);
        pdfStructure.setTemplate(template);
    }

    @Override
    public void createAcroForm(PDDocument template)
    {
        PDAcroForm theAcroForm = new PDAcroForm(template);
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
    public void createSignatureField(PDAcroForm acroForm) throws IOException
    {
        PDSignatureField sf = new PDSignatureField(acroForm);
        pdfStructure.setSignatureField(sf);
        LOG.info("Signature field has been created");
    }

    @Override
    public void createSignature(PDSignatureField pdSignatureField, PDPage page, String signerName)
            throws IOException
    {
        PDSignature pdSignature = new PDSignature();
        PDAnnotationWidget widget = pdSignatureField.getWidgets().get(0);
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
    public void createAcroFormDictionary(PDAcroForm acroForm, PDSignatureField signatureField)
            throws IOException
    {
        @SuppressWarnings("unchecked")
        List<PDField> acroFormFields = acroForm.getFields();
        COSDictionary acroFormDict = acroForm.getCOSObject();
        acroForm.setSignaturesExist(true);
        acroForm.setAppendOnly(true);
        acroFormDict.setDirect(true);
        acroFormFields.add(signatureField);
        acroForm.setDefaultAppearance("/sylfaen 0 Tf 0 g");
        pdfStructure.setAcroFormFields(acroFormFields);
        pdfStructure.setAcroFormDictionary(acroFormDict);
        LOG.info("AcroForm dictionary has been created");
    }

    @Override
    public void createSignatureRectangle(PDSignatureField signatureField,
                                         PDVisibleSignDesigner properties) throws IOException
    {

        PDRectangle rect = new PDRectangle();
        rect.setUpperRightX(properties.getxAxis() + properties.getWidth());
        rect.setUpperRightY(properties.getTemplateHeight() - properties.getyAxis());
        rect.setLowerLeftY(properties.getTemplateHeight() - properties.getyAxis() -
                           properties.getHeight());
        rect.setLowerLeftX(properties.getxAxis());
        signatureField.getWidgets().get(0).setRectangle(rect);
        pdfStructure.setSignatureRectangle(rect);
        LOG.info("Signature rectangle has been created");
    }

    /**
     * {@inheritDoc }
     *
     * @deprecated use {@link #createAffineTransform(java.awt.geom.AffineTransform) }
     */
    @Override
    @Deprecated
    public void createAffineTransform(byte[] params)
    {
        AffineTransform transform = new AffineTransform(params[0], params[1], params[2],
                                                        params[3], params[4],  params[5]);
        pdfStructure.setAffineTransform(transform);
        LOG.info("Matrix has been added");
    }

    @Override
    public void createAffineTransform(AffineTransform affineTransform)
    {
        pdfStructure.setAffineTransform(affineTransform);
        LOG.info("Matrix has been added");
    }

    @Override
    public void createProcSetArray()
    {
        COSArray procSetArr = new COSArray();
        procSetArr.add(COSName.getPDFName("PDF"));
        procSetArr.add(COSName.getPDFName("Text"));
        procSetArr.add(COSName.getPDFName("ImageB"));
        procSetArr.add(COSName.getPDFName("ImageC"));
        procSetArr.add(COSName.getPDFName("ImageI"));
        pdfStructure.setProcSet(procSetArr);
        LOG.info("ProcSet array has been created");
    }

    @Override
    public void createSignatureImage(PDDocument template, BufferedImage image) throws IOException
    {
        pdfStructure.setImage(LosslessFactory.createFromImage(template, image));
        LOG.info("Visible Signature Image has been created");
    }

    @Override
    public void createFormatterRectangle(byte[] params)
    {
        PDRectangle formatterRectangle = new PDRectangle();
        formatterRectangle.setUpperRightX(params[0]);
        formatterRectangle.setUpperRightY(params[1]);
        formatterRectangle.setLowerLeftX(params[2]);
        formatterRectangle.setLowerLeftY(params[3]);

        pdfStructure.setFormatterRectangle(formatterRectangle);
        LOG.info("Formatter rectangle has been created");
    }

    @Override
    public void createHolderFormStream(PDDocument template)
    {
        PDStream holderForm = new PDStream(template);
        pdfStructure.setHolderFormStream(holderForm);
        LOG.info("Holder form stream has been created");
    }

    @Override
    public void createHolderFormResources()
    {
        PDResources holderFormResources = new PDResources();
        pdfStructure.setHolderFormResources(holderFormResources);
        LOG.info("Holder form resources have been created");

    }

    @Override
    public void createHolderForm(PDResources holderFormResources, PDStream holderFormStream,
                                 PDRectangle formrect)
    {
        PDFormXObject holderForm = new PDFormXObject(holderFormStream);
        holderForm.setResources(holderFormResources);
        holderForm.setBBox(formrect);
        holderForm.setFormType(1);
        pdfStructure.setHolderForm(holderForm);
        LOG.info("Holder form has been created");

    }

    @Override
    public void createAppearanceDictionary(PDFormXObject holderForml,
                                           PDSignatureField signatureField) throws IOException
    {
        PDAppearanceDictionary appearance = new PDAppearanceDictionary();
        appearance.getCOSObject().setDirect(true);

        PDAppearanceStream appearanceStream = new PDAppearanceStream(holderForml.getCOSObject());

        appearance.setNormalAppearance(appearanceStream);
        signatureField.getWidgets().get(0).setAppearance(appearance);

        pdfStructure.setAppearanceDictionary(appearance);
        LOG.info("PDF appearance dictionary has been created");
    }

    @Override
    public void createInnerFormStream(PDDocument template)
    {
        PDStream innerFormStream = new PDStream(template);
        pdfStructure.setInnterFormStream(innerFormStream);
        LOG.info("Stream of another form (inner form - it will be inside holder form) " +
                 "has been created");
    }

    @Override
    public void createInnerFormResource()
    {
        PDResources innerFormResources = new PDResources();
        pdfStructure.setInnerFormResources(innerFormResources);
        LOG.info("Resources of another form (inner form - it will be inside holder form)" +
                 "have been created");
    }

    @Override
    public void createInnerForm(PDResources innerFormResources, PDStream innerFormStream,
                                PDRectangle formrect)
    {
        PDFormXObject innerForm = new PDFormXObject(innerFormStream);
        innerForm.setResources(innerFormResources);
        innerForm.setBBox(formrect);
        innerForm.setFormType(1);
        pdfStructure.setInnerForm(innerForm);
        LOG.info("Another form (inner form - it will be inside holder form) has been created");
    }

    @Override
    public void insertInnerFormToHolderResources(PDFormXObject innerForm,
                                                PDResources holderFormResources)
    {
        holderFormResources.put(COSName.FRM, innerForm);
        pdfStructure.setInnerFormName(COSName.FRM);
        LOG.info("Now inserted inner form inside holder form");
    }

    @Override
    public void createImageFormStream(PDDocument template)
    {
        PDStream imageFormStream = new PDStream(template);
        pdfStructure.setImageFormStream(imageFormStream);
        LOG.info("Created image form stream");
    }

    @Override
    public void createImageFormResources()
    {
        PDResources imageFormResources = new PDResources();
        pdfStructure.setImageFormResources(imageFormResources);
        LOG.info("Created image form resources");
    }

    @Override
    public void createImageForm(PDResources imageFormResources, PDResources innerFormResource,
                                PDStream imageFormStream, PDRectangle formrect, AffineTransform at,
                                PDImageXObject img) throws IOException
    {
        PDFormXObject imageForm = new PDFormXObject(imageFormStream);
        imageForm.setBBox(formrect);
        imageForm.setMatrix(at);
        imageForm.setResources(imageFormResources);
        imageForm.setFormType(1);

        imageFormResources.getCOSObject().setDirect(true);

        COSName imageFormName = COSName.getPDFName("n2");
        innerFormResource.put(imageFormName, imageForm);
        COSName imageName = imageFormResources.add(img, "img");
        pdfStructure.setImageForm(imageForm);
        pdfStructure.setImageFormName(imageFormName);
        pdfStructure.setImageName(imageName);
        LOG.info("Created image form");
    }

    @Override
    public void createBackgroundLayerForm(PDResources innerFormResource, PDRectangle formatter)
             throws IOException
    {
        // create blank n0 background layer form
        PDFormXObject n0Form = new PDFormXObject(pdfStructure.getTemplate().getDocument().createCOSStream());
        n0Form.setBBox(formatter);
        n0Form.setResources(new PDResources());
        n0Form.setFormType(1);
        innerFormResource.put(COSName.getPDFName("n0"), n0Form);
        LOG.info("Created background layer form");
    }

    @Override
    public void injectProcSetArray(PDFormXObject innerForm, PDPage page,
                                   PDResources innerFormResources,  PDResources imageFormResources,
                                   PDResources holderFormResources, COSArray procSet)
    {
        innerForm.getResources().getCOSObject().setItem(COSName.PROC_SET, procSet);
        page.getCOSObject().setItem(COSName.PROC_SET, procSet);
        innerFormResources.getCOSObject().setItem(COSName.PROC_SET, procSet);
        imageFormResources.getCOSObject().setItem(COSName.PROC_SET, procSet);
        holderFormResources.getCOSObject().setItem(COSName.PROC_SET, procSet);
        LOG.info("Inserted ProcSet to PDF");
    }

    @Override
    public void injectAppearanceStreams(PDStream holderFormStream, PDStream innerFormStream,
                                        PDStream imageFormStream, COSName imageFormName,
                                        COSName imageName, COSName innerFormName,
                                        PDVisibleSignDesigner properties) throws IOException
    {
        // 100 means that document width is 100% via the rectangle. if rectangle
        // is 500px, images 100% is 500px.
        // String imgFormContent = "q "+imageWidthSize+ " 0 0 50 0 0 cm /" +
        // imageName + " Do Q\n" + builder.toString();
        String imgFormContent    = "q " + 100 + " 0 0 50 0 0 cm /" + imageName.getName() + " Do Q\n";
        String holderFormContent = "q 1 0 0 1 0 0 cm /" + innerFormName.getName() + " Do Q\n";
        String innerFormContent  = "q 1 0 0 1 0 0 cm /n0 Do Q q 1 0 0 1 0 0 cm /" + imageFormName.getName() + " Do Q\n";

        appendRawCommands(pdfStructure.getHolderFormStream().createOutputStream(), holderFormContent);
        appendRawCommands(pdfStructure.getInnerFormStream().createOutputStream(), innerFormContent);
        appendRawCommands(pdfStructure.getImageFormStream().createOutputStream(), imgFormContent);
        LOG.info("Injected appearance stream to pdf");
    }

    public void appendRawCommands(OutputStream os, String commands) throws IOException
    {
        os.write(commands.getBytes("UTF-8"));
        os.close();
    }

    @Override
    public void createVisualSignature(PDDocument template)
    {
        pdfStructure.setVisualSignature(template.getDocument());
        LOG.info("Visible signature has been created");
    }

    @Override
    public void createWidgetDictionary(PDSignatureField signatureField,
                                       PDResources holderFormResources) throws IOException
    {
        COSDictionary widgetDict = signatureField.getWidgets().get(0).getCOSObject();
        widgetDict.setNeedToBeUpdated(true);
        widgetDict.setItem(COSName.DR, holderFormResources.getCOSObject());

        pdfStructure.setWidgetDictionary(widgetDict);
        LOG.info("WidgetDictionary has been created");
    }

    @Override
    public void closeTemplate(PDDocument template) throws IOException
    {
        template.close();
        pdfStructure.getTemplate().close();
    }
}
