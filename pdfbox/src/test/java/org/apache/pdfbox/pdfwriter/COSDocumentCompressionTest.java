package org.apache.pdfbox.pdfwriter;

import junit.framework.TestCase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdfwriter.compress.CompressParameters;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * This test attempts to save different documents compressed, without causing errors, it also checks, whether the
 * PDF is readable after compression and whether some central contents are still contained after compression.
 * Output files are created in "target/test-output/compression/" source files are placed in
 * "src/test/resources/input/compression/".
 *
 * @author Christian Appl
 */
public class COSDocumentCompressionTest extends TestCase {

	static File inDir = new File("src/test/resources/input/compression/");
	static File outDir = new File("target/test-output/compression/");

	public COSDocumentCompressionTest() {
		outDir.mkdirs();
	}

	/**
	 * Compress a document, that does contain images and touch the expected images.
	 *
	 * @throws Exception Shall be thrown, when compressing the document failed.
	 */
	public void testCompressImageDoc() throws Exception {
		File source = new File(inDir, "image.pdf");
		File target = new File(outDir, "image.pdf");

		PDDocument document = PDDocument.load(source);
		try {
			document.saveCompressed(target, new CompressParameters());
		} finally {
			document.close();
		}

		document = PDDocument.load(target);
		try {
			assertEquals("The number of pages should not have changed, during compression.",
					4, document.getNumberOfPages());
			for (PDPage page : document.getPages()) {
				assertNotNull("All pages should contain atleast one image named Im1",
						page.getResources().getXObject(COSName.getPDFName("Im1")));
			}
			PDPage page = document.getPage(0);
			assertEquals("The expected byte length of the image on page 1 differs.",
					515932, page.getResources().getXObject(COSName.getPDFName("Im1")).getCOSObject().getLength());
			page = document.getPage(1);
			assertEquals("The expected byte length of the image on page 2 differs.",
					530937, page.getResources().getXObject(COSName.getPDFName("Im1")).getCOSObject().getLength());
			page = document.getPage(2);
			assertEquals("The expected byte length of the image on page 3 differs.",
					533093, page.getResources().getXObject(COSName.getPDFName("Im1")).getCOSObject().getLength());
			page = document.getPage(3);
			assertEquals("The expected byte length of the image on page 4 differs.",
					530604, page.getResources().getXObject(COSName.getPDFName("Im1")).getCOSObject().getLength());
		} finally {
			document.close();
		}
	}

	/**
	 * Compress a document, that contains acroform fields and touch the expected fields.
	 *
	 * @throws Exception Shall be thrown, when compressing the document failed.
	 */
	public void testCompressAcroformDoc() throws Exception {
		File source = new File(inDir, "acroform.pdf");
		File target = new File(outDir, "acroform.pdf");

		PDDocument document = PDDocument.load(source);
		try {
			document.saveCompressed(target, new CompressParameters());
		} finally {
			document.close();
		}

		document = PDDocument.load(target);
		try {
			assertEquals("The number of pages should not have changed, during compression.",
					1, document.getNumberOfPages());
			PDPage page = document.getPage(0);
			List<PDAnnotation> annotations = page.getAnnotations();
			assertEquals("The number of annotations should not have changed", 13, annotations.size());
			assertEquals("The 1. annotation should have been a text field.",
					"TextField", annotations.get(0).getCOSObject().getNameAsString(COSName.T));
			assertEquals("The 2. annotation should have been a button.",
					"Button", annotations.get(1).getCOSObject().getNameAsString(COSName.T));
			assertEquals("The 3. annotation should have been a checkbox.",
					"CheckBox1", annotations.get(2).getCOSObject().getNameAsString(COSName.T));
			assertEquals("The 4. annotation should have been a checkbox.",
					"CheckBox2", annotations.get(3).getCOSObject().getNameAsString(COSName.T));
			assertEquals("The 5. annotation should have been a multiline textfield.",
					"TextFieldMultiLine", annotations.get(4).getCOSObject().getNameAsString(COSName.T));
			assertEquals("The 6. annotation should have been a multiline textfield.",
					"TextFieldMultiLineRT", annotations.get(5).getCOSObject().getNameAsString(COSName.T));
			assertNotNull("The 7. annotation should have had a parent entry.",
					annotations.get(6).getCOSObject().getItem(COSName.PARENT));
			assertEquals("The 7. annotation's parent should have been a GroupOption.",
					"GroupOption", annotations.get(6).getCOSObject().getCOSDictionary(COSName.PARENT).getNameAsString(COSName.T));
			assertNotNull("The 8. annotation should have had a parent entry.",
					annotations.get(7).getCOSObject().getItem(COSName.PARENT));
			assertEquals("The 8. annotation's parent should have been a GroupOption.",
					"GroupOption", annotations.get(7).getCOSObject().getCOSDictionary(COSName.PARENT).getNameAsString(COSName.T));
			assertEquals("The 9. annotation should have been a ListBox.",
					"ListBox", annotations.get(8).getCOSObject().getNameAsString(COSName.T));
			assertEquals("The 10. annotation should have been a ListBox Multiselect.",
					"ListBoxMultiSelect", annotations.get(9).getCOSObject().getNameAsString(COSName.T));
			assertEquals("The 11. annotation should have been a ComboBox.",
					"ComboBox", annotations.get(10).getCOSObject().getNameAsString(COSName.T));
			assertEquals("The 12. annotation should have been a EditableComboBox.",
					"ComboBoxEditable", annotations.get(11).getCOSObject().getNameAsString(COSName.T));
			assertEquals("The 13. annotation should have been a Signature.",
					"Signature", annotations.get(12).getCOSObject().getNameAsString(COSName.T));
		} finally {
			document.close();
		}
	}

	/**
	 * Compress a document, that contains an attachment and touch the expected attachment.
	 *
	 * @throws Exception Shall be thrown, when compressing the document failed.
	 */
	public void testCompressAttachmentsDoc() throws Exception {
		File source = new File(inDir, "attachment.pdf");
		File target = new File(outDir, "attachment.pdf");

		PDDocument document = PDDocument.load(source);
		try {
			document.saveCompressed(target, new CompressParameters());
		} finally {
			document.close();
		}

		document = PDDocument.load(target);
		try {
			assertEquals("The number of pages should not have changed, during compression.",
					2, document.getNumberOfPages());
			Map<String, PDComplexFileSpecification> embeddedFiles =
					document.getDocumentCatalog().getNames().getEmbeddedFiles().getNames();
			assertEquals("The document should have contained an attachment",
					1, embeddedFiles.size());
			PDComplexFileSpecification attachment;
			assertNotNull("The document should have contained 'A4Unicode.pdf'.",
					(attachment = embeddedFiles.get("A4Unicode.pdf")));
			assertEquals("The attachments length is not as expected.",
					14997, attachment.getEmbeddedFile().getLength());
		} finally {
			document.close();
		}
	}

	/**
	 * Compress and encrypt the given document, without causing an exception to be thrown.
	 *
	 * @throws Exception Shall be thrown, when compressing/encrypting the document failed.
	 */
	public void testCompressEncryptedDoc() throws Exception {
		File source = new File(inDir, "unencrypted.pdf");
		File target = new File(outDir, "encrypted.pdf");

		PDDocument document = PDDocument.load(source, "user");
		try {
			document.protect(new StandardProtectionPolicy(
					"owner", "user", new AccessPermission(0)
			));
			document.saveCompressed(target, new CompressParameters());
		} finally {
			document.close();
		}

		document = PDDocument.load(target, "user");
		// If this didn't fail, the encryption dictionary should be present and working.
		document.close();
	}

	/**
	 * Adds a page to an existing document, compresses it and touches the resulting page content stream.
	 *
	 * @throws Exception Shall be thrown, if compressing the document failed.
	 */
	public void testAlteredDoc() throws Exception {
		File source = new File(inDir, "unencrypted.pdf");
		File target = new File(outDir, "altered.pdf");

		PDDocument document = PDDocument.load(source);
		try {
			PDPage page = new PDPage(new PDRectangle(100, 100));
			document.addPage(page);
			PDPageContentStream contentStream = new PDPageContentStream(document, page);

			try {
				contentStream.beginText();
				contentStream.newLineAtOffset(20, 80);
				contentStream.setFont(PDType1Font.HELVETICA, 12);
				contentStream.showText("Test");
				contentStream.endText();
			} finally {
				contentStream.close();
			}

			document.saveCompressed(target, new CompressParameters());
		} finally {
			document.close();
		}

		document = PDDocument.load(target);
		try {
			assertEquals("The number of pages should not have changed, during compression.",
					3, document.getNumberOfPages());
			PDPage page = document.getPage(2);
			assertEquals("The stream length of the new page is not as expected.",
					43, page.getContentStreams().next().getLength());
		} finally {
			document.close();
		}
	}

	/**
	 * Compress the given source document, using the given parameters and write the results to the given target.
	 *
	 * @param source     The source file (PDF).
	 * @param target     The target file.
	 * @param parameters The compression parameters to be set.
	 * @throws IOException Shall be thrown, if compressing the document failed.
	 */
	private void compress(File source, File target, CompressParameters parameters) throws IOException {
		PDDocument document = PDDocument.load(source);
		try {
			document.saveCompressed(target, parameters);
		} finally {
			document.close();
		}
	}

}