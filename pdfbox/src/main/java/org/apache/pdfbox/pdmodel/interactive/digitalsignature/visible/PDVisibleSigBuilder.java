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
import java.io.IOException;
import java.io.InputStream;
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
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectForm;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;

/**
 * That's implementation of <b>PDFTemplateBuilder </b>
 * @see org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDFTemplateBuilder
 * @author <a href="mailto:vakhtang.koroghlishvili@gmail.com"> vakhtang koroghlishvili (gogebashvili) </a>
 * 
 */
public class PDVisibleSigBuilder implements PDFTemplateBuilder
{

    private PDFTemplateStructure pdfStructure;
    private static final Log logger = LogFactory.getLog(PDVisibleSigBuilder.class);

    @Override
    public void createPage(PDVisibleSignDesigner properties)
    {
        PDPage page = new PDPage();
        page.setMediaBox(new PDRectangle(properties.getPageWidth(), properties.getPageHeight()));
        pdfStructure.setPage(page);
        logger.info("PDF page has been created");
    }

    @Override
    public void createTemplate(PDPage page) throws IOException
    {
        PDDocument template = new PDDocument();
        template.addPage(page);
        pdfStructure.setTemplate(template);
    }

    public PDVisibleSigBuilder()
    {
        pdfStructure = new PDFTemplateStructure();
        logger.info("PDF Strucure has been Created");

    }

    @Override
    public void createAcroForm(PDDocument template)
    {
        PDAcroForm theAcroForm = new PDAcroForm(template);
        template.getDocumentCatalog().setAcroForm(theAcroForm);
        pdfStructure.setAcroForm(theAcroForm);
        logger.info("Acro form page has been created");

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
        logger.info("Signature field has been created");
    }

    @Override
    public void createSignature(PDSignatureField pdSignatureField, PDPage page, String signatureName)
            throws IOException
    {
        PDSignature pdSignature = new PDSignature();
        pdSignatureField.setSignature(pdSignature);
        pdSignatureField.getWidget().setPage(page);
        page.getAnnotations().add(pdSignatureField.getWidget());
        pdSignature.setName(signatureName);
        pdSignature.setByteRange(new int[] { 0, 0, 0, 0 });
        pdSignature.setContents(new byte[4096]);
        pdfStructure.setPdSignature(pdSignature);
        logger.info("PDSignatur has been created");
    }

    @Override
    public void createAcroFormDictionary(PDAcroForm acroForm, PDSignatureField signatureField) throws IOException
    {
        @SuppressWarnings("unchecked")
        List<PDField> acroFormFields = acroForm.getFields();
        COSDictionary acroFormDict = acroForm.getDictionary();
        acroFormDict.setDirect(true);
        acroFormDict.setInt(COSName.SIG_FLAGS, 3);
        acroFormFields.add(signatureField);
        acroFormDict.setString(COSName.DA, "/sylfaen 0 Tf 0 g");
        pdfStructure.setAcroFormFields(acroFormFields);
        pdfStructure.setAcroFormDictionary(acroFormDict);
        logger.info("AcroForm dictionary has been created");
    }

    @Override
    public void createSignatureRectangle(PDSignatureField signatureField, PDVisibleSignDesigner properties)
            throws IOException
    {

        PDRectangle rect = new PDRectangle();
        rect.setUpperRightX(properties.getxAxis() + properties.getWidth());
        rect.setUpperRightY(properties.getTemplateHeight() - properties.getyAxis());
        rect.setLowerLeftY(properties.getTemplateHeight() - properties.getyAxis() - properties.getHeight());
        rect.setLowerLeftX(properties.getxAxis());
        signatureField.getWidget().setRectangle(rect);
        pdfStructure.setSignatureRectangle(rect);
        logger.info("rectangle of signature has been created");
    }

    @Override
    public void createAffineTransform(byte[] params)
    {
        AffineTransform transform = new AffineTransform(params[0], params[1], params[2], params[3], params[4],
                params[5]);
        pdfStructure.setAffineTransform(transform);
        logger.info("Matrix has been added");
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
        logger.info("ProcSet array has been created");
    }

    @Override
    public void createSignatureImage(PDDocument template, InputStream inputStream) throws IOException
    {
        PDJpeg img = new PDJpeg(template, inputStream);
        pdfStructure.setJpedImage(img);
        logger.info("Visible Signature Image has been created");
        // pdfStructure.setTemplate(template);
        inputStream.close();

    }

    @Override
    public void createFormaterRectangle(byte[] params)
    {

        PDRectangle formrect = new PDRectangle();
        formrect.setUpperRightX(params[0]);
        formrect.setUpperRightY(params[1]);
        formrect.setLowerLeftX(params[2]);
        formrect.setLowerLeftY(params[3]);

        pdfStructure.setFormaterRectangle(formrect);
        logger.info("Formater rectangle has been created");

    }

    @Override
    public void createHolderFormStream(PDDocument template)
    {
        PDStream holderForm = new PDStream(template);
        pdfStructure.setHolderFormStream(holderForm);
        logger.info("Holder form Stream has been created");
    }

    @Override
    public void createHolderFormResources()
    {
        PDResources holderFormResources = new PDResources();
        pdfStructure.setHolderFormResources(holderFormResources);
        logger.info("Holder form resources have been created");

    }

    @Override
    public void createHolderForm(PDResources holderFormResources, PDStream holderFormStream, PDRectangle formrect)
    {

        PDXObjectForm holderForm = new PDXObjectForm(holderFormStream);
        holderForm.setResources(holderFormResources);
        holderForm.setBBox(formrect);
        holderForm.setFormType(1);
        pdfStructure.setHolderForm(holderForm);
        logger.info("Holder form has been created");

    }

    @Override
    public void createAppearanceDictionary(PDXObjectForm holderForml, PDSignatureField signatureField)
            throws IOException
    {

        PDAppearanceDictionary appearance = new PDAppearanceDictionary();
        appearance.getCOSObject().setDirect(true);

        PDAppearanceStream appearanceStream = new PDAppearanceStream(holderForml.getCOSStream());

        appearance.setNormalAppearance(appearanceStream);
        signatureField.getWidget().setAppearance(appearance);

        pdfStructure.setAppearanceDictionary(appearance);
        logger.info("PDF appereance Dictionary has been created");

    }

    @Override
    public void createInnerFormStream(PDDocument template)
    {
        PDStream innterFormStream = new PDStream(template);
        pdfStructure.setInnterFormStream(innterFormStream);
        logger.info("Strean of another form (inner form - it would be inside holder form) has been created");
    }

    @Override
    public void createInnerFormResource()
    {
        PDResources innerFormResources = new PDResources();
        pdfStructure.setInnerFormResources(innerFormResources);
        logger.info("Resources of another form (inner form - it would be inside holder form) have been created");
    }

    @Override
    public void createInnerForm(PDResources innerFormResources, PDStream innerFormStream, PDRectangle formrect)
    {
        PDXObjectForm innerForm = new PDXObjectForm(innerFormStream);
        innerForm.setResources(innerFormResources);
        innerForm.setBBox(formrect);
        innerForm.setFormType(1);
        pdfStructure.setInnerForm(innerForm);
        logger.info("Another form (inner form - it would be inside holder form) have been created");

    }

    @Override
    public void insertInnerFormToHolerResources(PDXObjectForm innerForm, PDResources holderFormResources)
    {
        String name = holderFormResources.addXObject(innerForm, "FRM");
        pdfStructure.setInnerFormName(name);
        logger.info("Alerady inserted inner form  inside holder form");
    }

    @Override
    public void createImageFormStream(PDDocument template)
    {
        PDStream imageFormStream = new PDStream(template);
        pdfStructure.setImageFormStream(imageFormStream);
        logger.info("Created image form Stream");

    }

    @Override
    public void createImageFormResources()
    {
        PDResources imageFormResources = new PDResources();
        pdfStructure.setImageFormResources(imageFormResources);
        logger.info("Created image form Resources");
    }

    @Override
    public void createImageForm(PDResources imageFormResources, PDResources innerFormResource,
            PDStream imageFormStream, PDRectangle formrect, AffineTransform affineTransform, PDJpeg img)
            throws IOException
    {

        /*
         * if you need text on the visible signature 
         * 
         * PDFont font = PDTrueTypeFont.loadTTF(this.pdfStructure.getTemplate(), new File("D:\\arial.ttf")); 
         * font.setFontEncoding(new WinAnsiEncoding());
         * 
         * Map<String, PDFont> fonts = new HashMap<String, PDFont>(); fonts.put("arial", font);
         */
        PDXObjectForm imageForm = new PDXObjectForm(imageFormStream);
        imageForm.setBBox(formrect);
        imageForm.setMatrix(affineTransform);
        imageForm.setResources(imageFormResources);
        imageForm.setFormType(1);
        /*
         * imageForm.getResources().addFont(font); 
         * imageForm.getResources().setFonts(fonts);
         */

        imageFormResources.getCOSObject().setDirect(true);
        String imageFormName = innerFormResource.addXObject(imageForm, "n");
        String imageName = imageFormResources.addXObject(img, "img");
        this.pdfStructure.setImageForm(imageForm);
        this.pdfStructure.setImageFormName(imageFormName);
        this.pdfStructure.setImageName(imageName);
        logger.info("Created image form");
    }

    @Override
    public void injectProcSetArray(PDXObjectForm innerForm, PDPage page, PDResources innerFormResources,
            PDResources imageFormResources, PDResources holderFormResources, COSArray procSet)
    {

        innerForm.getResources().getCOSDictionary().setItem(COSName.PROC_SET, procSet); //
        page.getCOSDictionary().setItem(COSName.PROC_SET, procSet);
        innerFormResources.getCOSDictionary().setItem(COSName.PROC_SET, procSet);
        imageFormResources.getCOSDictionary().setItem(COSName.PROC_SET, procSet);
        holderFormResources.getCOSDictionary().setItem(COSName.PROC_SET, procSet);
        logger.info("inserted ProcSet to PDF");
    }

    @Override
    public void injectAppearanceStreams(PDStream holderFormStream, PDStream innterFormStream, PDStream imageFormStream,
            String imageObjectName, String imageName, String innerFormName, PDVisibleSignDesigner properties)
            throws IOException
    {

        // 100 means that document width is 100% via the rectangle. if rectangle
        // is 500px, images 100% is 500px.
        // String imgFormComment = "q "+imageWidthSize+ " 0 0 50 0 0 cm /" +
        // imageName + " Do Q\n" + builder.toString();
        String imgFormComment = "q " + 100 + " 0 0 50 0 0 cm /" + imageName + " Do Q\n";
        String holderFormComment = "q 1 0 0 1 0 0 cm /" + innerFormName + " Do Q \n";
        String innerFormComment = "q 1 0 0 1 0 0 cm /" + imageObjectName + " Do Q\n";

        appendRawCommands(pdfStructure.getHolderFormStream().createOutputStream(), holderFormComment);
        appendRawCommands(pdfStructure.getInnterFormStream().createOutputStream(), innerFormComment);
        appendRawCommands(pdfStructure.getImageFormStream().createOutputStream(), imgFormComment);
        logger.info("Injected apereance stream to pdf");

    }

    public void appendRawCommands(OutputStream os, String commands) throws IOException
    {
        os.write(commands.getBytes("UTF-8"));
        os.close();
    }

    @Override
    public void createVisualSignature(PDDocument template)
    {
        this.pdfStructure.setVisualSignature(template.getDocument());
        logger.info("Visible signature has been created");

    }

    @Override
    public void createWidgetDictionary(PDSignatureField signatureField, PDResources holderFormResources)
            throws IOException
    {

        COSDictionary widgetDict = signatureField.getWidget().getDictionary();
        widgetDict.setNeedToBeUpdate(true);
        widgetDict.setItem(COSName.DR, holderFormResources.getCOSObject());

        pdfStructure.setWidgetDictionary(widgetDict);
        logger.info("WidgetDictionary has been crated");
    }

    @Override
    public void closeTemplate(PDDocument template) throws IOException
    {
        template.close();
        this.pdfStructure.getTemplate().close();

    }
}
