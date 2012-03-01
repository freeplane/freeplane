package org.docear.plugin.pdfutilities.test;

import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.docear.plugin.core.features.AnnotationID;
import org.docear.plugin.core.features.AnnotationModel;
import org.docear.plugin.core.features.IAnnotation.AnnotationType;
import org.docear.plugin.pdfutilities.pdf.PdfAnnotationImporter;
import org.docear.plugin.pdfutilities.pdf.PdfFileFilter;
import org.junit.Assert;
import org.junit.Test;

import de.intarsys.pdf.content.CSDeviceBasedInterpreter;
import de.intarsys.pdf.content.text.CSTextExtractor;
import de.intarsys.pdf.cos.COSRuntimeException;
import de.intarsys.pdf.parser.COSLoadException;
import de.intarsys.pdf.pd.PDAnnotation;
import de.intarsys.pdf.pd.PDDocument;
import de.intarsys.pdf.pd.PDPage;
import de.intarsys.pdf.tools.kernel.PDFGeometryTools;

public class AnnotationImportUnitTest {

	@Test
	public void testAnnotationImport() {
		File pdfDir = new File("C:\\_Dissertation\\Literature");
		
		List<File> pdfFiles = new ArrayList<File>();
		pdfFiles.addAll(Arrays.asList(pdfDir.listFiles(new PdfFileFilter())));
		int failCounter = 0;
		int ioExceptionCounter = 0;
		int cosLoadExceptionCounter = 0;
		int classCastExceptionCounter = 0;
		int cosRuntimeExceptionCounter = 0;
		int pageNotFoundCounter = 0;
		int objectNumberNotFoundCounter = 0;
		int objectNumberNotUniqueCounter = 0;
		int generationNumberNotFoundCounter = 0;
		int totalFiles = pdfFiles.size();		
		
		
		for(File pdfFile : pdfFiles){
			try {
				System.out.println("Testing file " + (pdfFiles.indexOf(pdfFile) + 1) + " of " + totalFiles);			
				PdfAnnotationImporter importer = new PdfAnnotationImporter();
				importer.setImportAll(true);
				List<AnnotationModel> annotations = importer.importAnnotations(pdfFile.toURI());
				importer.setImportAll(false);
				boolean fileFailed = false;
				if(checkPages(annotations) == false){
					fileFailed = true;
					pageNotFoundCounter++;
					//copy(pdfFile, pageNotFoundFiles);
				}
				if(checkObjectNumber(annotations) == false){
					fileFailed = true;
					objectNumberNotFoundCounter++;
				}
				if(checkObjectNumberUnique(annotations, new ArrayList<Integer>()) == false){
					fileFailed = true;
					objectNumberNotUniqueCounter++;
				}
				if(checkGenerationNumber(annotations) == false){
					fileFailed = true;
					generationNumberNotFoundCounter++;
				}
				if(fileFailed){
					failCounter++;
				}
			} catch (IOException e) {
				System.out.println("IOException on file: " + pdfFile.getAbsolutePath());
				failCounter++;
				ioExceptionCounter++;
				//copy(pdfFile, ioExceptionFiles);
			} catch (COSLoadException e) {
				System.out.println("COSLoadException on file: " + pdfFile.getAbsolutePath());
				failCounter++;
				cosLoadExceptionCounter++;
				//copy(pdfFile, cosLoadExceptionFiles);
			} catch(COSRuntimeException e) {
				System.out.println("COSRuntimeException on file: " + pdfFile.getAbsolutePath());
				failCounter++;
				cosRuntimeExceptionCounter++;
				//copy(pdfFile, cosRuntimeExceptionFiles);
			} catch(ClassCastException e){
				System.out.println("ClassCastException on file: " + pdfFile.getAbsolutePath());
				failCounter++;
				classCastExceptionCounter++;
				//copy(pdfFile, classCastExceptionFiles);
			}
		}
		System.out.println("============================================");
		System.out.println("IOException total: " + ioExceptionCounter);
		System.out.println("COSLoadException total: " + cosLoadExceptionCounter);
		System.out.println("COSRuntimeException total: " + cosRuntimeExceptionCounter);
		System.out.println("ClassCastException total: " + classCastExceptionCounter);
		System.out.println("Page not found total: " + pageNotFoundCounter);
		System.out.println("Object number not found total: " + objectNumberNotFoundCounter);
		System.out.println("Object number not unique total: " + objectNumberNotUniqueCounter);
		System.out.println("Generation number not found total: " + generationNumberNotFoundCounter);
		System.out.println("Tested files total :" + totalFiles);
		System.out.println("Failed files count total :" + failCounter);
		Assert.assertEquals(0, failCounter);
	}

	@Test
	public void testAnnotationImportWithOneFile() {
		File pdfFile = new File("C:\\_Dissertation\\Literature\\Document expansion for speech retrieval.PDF");
		//Working file for reference
		//File pdfFile = new File("C:\\_Dissertation\\Literature\\07_Chim_A New Suffix Tree Similarity Measure for Document Clustering.PDF");
		int failCounter = 0;
		int ioExceptionCounter = 0;
		int cosLoadExceptionCounter = 0;
		int classCastExceptionCounter = 0;
		int cosRuntimeExceptionCounter = 0;
		int pageNotFoundCounter = 0;
		
		try {
			System.out.println("Testing file " + pdfFile.getAbsolutePath());			
			
			List<AnnotationModel> annotations = new PdfAnnotationImporter().importAnnotations(pdfFile.toURI());
			if(checkPages(annotations) == false){
				failCounter++;
				pageNotFoundCounter++;				
			}
		} catch (IOException e) {
			System.out.println("IOException on file: " + pdfFile.getAbsolutePath());
			failCounter++;
			ioExceptionCounter++;			
		} catch (COSLoadException e) {
			System.out.println("COSLoadException on file: " + pdfFile.getAbsolutePath());
			failCounter++;
			cosLoadExceptionCounter++;			
		} catch(COSRuntimeException e) {
			System.out.println("COSRuntimeException on file: " + pdfFile.getAbsolutePath());
			failCounter++;
			cosRuntimeExceptionCounter++;			
		} catch(ClassCastException e){
			System.out.println("ClassCastException on file: " + pdfFile.getAbsolutePath());
			failCounter++;
			classCastExceptionCounter++;			
		}
		
		System.out.println("============================================");
		System.out.println("IOException total: " + ioExceptionCounter);
		System.out.println("COSLoadException total: " + cosLoadExceptionCounter);
		System.out.println("COSRuntimeException total: " + cosRuntimeExceptionCounter);
		System.out.println("ClassCastException total: " + classCastExceptionCounter);
		System.out.println("Page not found total: " + pageNotFoundCounter);
		System.out.println("Tested files total :" + 1);
		System.out.println("Failed files count total :" + failCounter);
		Assert.assertEquals(0, failCounter);		
	}
	
	
	private boolean checkGenerationNumber(List<AnnotationModel> annotations) {
		boolean result = true;
		for(AnnotationModel annotation : annotations){
			if(annotation.getGenerationNumber() == null || annotation.getGenerationNumber() < 0){
				System.out.println("Could not get Generation Number from annotation: " + annotation.getTitle());
				System.out.println("Annotation file: " + annotation.getUri());
				System.out.println("Annotation type: " + annotation.getAnnotationType());
				System.out.println("Annotation Objectnumber: " + annotation.getObjectNumber());
				System.out.println("Annotation Generationnumber: " + annotation.getGenerationNumber());
				result =  false;
			}
			if(checkGenerationNumber(annotation.getChildren()) == false){
				result = false;
			}
		}
		return result;
	}

	private boolean checkObjectNumberUnique(List<AnnotationModel> annotations, List<Integer> objectNumbers) {
		boolean result = true;
		for(AnnotationModel annotation : annotations){
			if(annotation.getObjectNumber() != null && annotation.getObjectNumber() > 0){
				if(objectNumbers.contains(annotation.getObjectNumber())){
					System.out.println("Could not get Generation Number from annotation: " + annotation.getTitle());
					System.out.println("Annotation file: " + annotation.getUri());
					System.out.println("Annotation type: " + annotation.getAnnotationType());
					System.out.println("Annotation Objectnumber: " + annotation.getObjectNumber());
					System.out.println("Annotation Generationnumber: " + annotation.getGenerationNumber());
					result =  false;
				}
				else{
					objectNumbers.add(annotation.getObjectNumber());
				}
			}
			
			if(checkObjectNumberUnique(annotation.getChildren(), objectNumbers) == false){
				result = false;
			}
		}
		return result;
	}

	private boolean checkObjectNumber(List<AnnotationModel> annotations) {
		boolean result = true;
		for(AnnotationModel annotation : annotations){
			if(annotation.getObjectNumber() == null || annotation.getObjectNumber() <= 0){
				System.out.println("Could not get Object Number from annotation: " + annotation.getTitle());
				System.out.println("Annotation file: " + annotation.getUri());
				System.out.println("Annotation type: " + annotation.getAnnotationType());
				System.out.println("Annotation Objectnumber: " + annotation.getObjectNumber());
				System.out.println("Annotation Generationnumber: " + annotation.getGenerationNumber());
				result =  false;
			}
			if(checkObjectNumber(annotation.getChildren()) == false){
				result = false;
			}
		}
		return result;
	}

	private boolean checkPages(List<AnnotationModel> annotations) {
		boolean result = true;
		for(AnnotationModel annotation : annotations){
			if(annotation.getPage() == null && annotation.getAnnotationType() != AnnotationType.BOOKMARK_WITH_URI && annotation.getAnnotationType() != AnnotationType.BOOKMARK_WITHOUT_DESTINATION){
				System.out.println("Could not get page from annotation: " + annotation.getTitle());
				System.out.println("Annotation file: " + annotation.getUri());
				System.out.println("Annotation type: " + annotation.getAnnotationType());
				result =  false;
			}
			if(checkPages(annotation.getChildren()) == false){
				result = false;
			}
		}
		return result;
	}
	
	@Test
	public void testAnnotationIDComparability() throws IllegalArgumentException, URISyntaxException {
		AnnotationID id1 = new AnnotationID(new URI("file://C:/test.mm"), 359);
		AnnotationID id2 = new AnnotationID(new URI("file://C:/test.mm").normalize(), 359);
		Map<AnnotationID, String> map = new HashMap<AnnotationID, String>();
		System.out.println("id1 hash :" + id1.hashCode());
		System.out.println("id1 hash :" + id2.hashCode());
		map.put(id1, "id1");
		Assert.assertTrue("Objects are not equal", id1.equals(id2));
		Assert.assertTrue(map.containsKey(id2));
		
	}
	
	@Test
	public void testHighlightedTextImport(){
		File pdfFile = new File("C:\\_Dissertation\\test\\00_Campbell_Copy Detection Systems for Digital Documents_NORMAN.PDF");
		System.out.println("Testing file " + pdfFile.getAbsolutePath());			
		
		try {
			this.extractText(pdfFile);
			PdfAnnotationImporter importer = new PdfAnnotationImporter();
			importer.setImportAll(true);
			List<AnnotationModel> annotations = importer.importAnnotations(pdfFile.toURI());
			importer.setImportAll(false);
			System.out.println(annotations.size());
		} catch (COSRuntimeException e) {			
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (COSLoadException e) {
			e.printStackTrace();
		}
	}
	
	protected void extractText(File file) {
		PdfAnnotationImporter importer = new PdfAnnotationImporter();
		PDDocument doc = null;
		try {
			doc = importer.getPDDocument(file.toURI());
			@SuppressWarnings("unchecked")
			List<PDAnnotation> pdAnnotations = doc.getAnnotations();
			for(PDAnnotation annotation : pdAnnotations){
				if(annotation.isMarkupAnnotation()){
					PDPage page = annotation.getPage();
					CSTextExtractor extractor = new CSTextExtractor();
					AffineTransform pageTx = new AffineTransform();
					PDFGeometryTools.adjustTransform(pageTx, page);
					extractor.setDeviceTransform(pageTx);
					CSDeviceBasedInterpreter interpreter = new CSDeviceBasedInterpreter(
							null, extractor);
					interpreter.process(page.getContentStream(), page
							.getResources());
					String test = extractor.getContent();
					System.out.println(test);
				}
			}
		} catch (COSRuntimeException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (COSLoadException e) {
			e.printStackTrace();
		} finally{
			try {				
				doc.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
			
			
			
			
			
			
		
	}

}
