package org.docear.plugin.pdfutilities.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.docear.plugin.pdfutilities.pdf.PdfAnnotation;
import org.docear.plugin.pdfutilities.pdf.PdfAnnotationImporter;
import org.docear.plugin.pdfutilities.pdf.PdfFileFilter;
import org.junit.Assert;
import org.junit.Test;

import de.intarsys.pdf.cos.COSRuntimeException;
import de.intarsys.pdf.parser.COSLoadException;

public class AnnotationDestinationUnitTest {

	@Test
	public void testFindPageDestination() {
		File pdfDir = new File("C:\\_Dissertation\\Literature");
		
		List<File> pdfFiles = new ArrayList<File>();
		pdfFiles.addAll(Arrays.asList(pdfDir.listFiles(new PdfFileFilter())));
		int failCounter = 0;
		int ioExceptionCounter = 0;
		int cosLoadExceptionCounter = 0;
		int classCastExceptionCounter = 0;
		int cosRuntimeExceptionCounter = 0;
		int pageNotFoundCounter = 0;
		int totalFiles = pdfFiles.size();
		
		File classCastExceptionFiles = new File("");
		File cosLoadExceptionFiles = new File("");
		File cosRuntimeExceptionFiles = new File("");
		File ioExceptionFiles = new File("");
		File pageNotFoundFiles = new File("");
		
		for(File pdfFile : pdfFiles){
			try {
				//System.out.println("Testing file " + (pdfFiles.indexOf(pdfFile) + 1) + " of " + totalFiles);
				
				classCastExceptionFiles = new File(this.getClass().getClassLoader().getResource("resources/ClassCastExceptionFiles").toURI());
				cosLoadExceptionFiles = new File(this.getClass().getClassLoader().getResource("resources/CosLoadExceptionFiles").toURI());
				cosRuntimeExceptionFiles = new File(this.getClass().getClassLoader().getResource("resources/CosRuntimeExceptionFiles").toURI());
				ioExceptionFiles = new File(this.getClass().getClassLoader().getResource("resources/IoExceptionFiles").toURI());
				pageNotFoundFiles = new File(this.getClass().getClassLoader().getResource("resources/PageNotFoundFiles").toURI());
				
				
				List<PdfAnnotation> annotations = new PdfAnnotationImporter().importAnnotations(pdfFile);
				if(checkPages(annotations) == false){
					failCounter++;
					pageNotFoundCounter++;
					copy(pdfFile, pageNotFoundFiles);
				}
			} catch (IOException e) {
				System.out.println("IOException on file: " + pdfFile.getAbsolutePath());
				failCounter++;
				ioExceptionCounter++;
				copy(pdfFile, ioExceptionFiles);
			} catch (COSLoadException e) {
				System.out.println("COSLoadException on file: " + pdfFile.getAbsolutePath());
				failCounter++;
				cosLoadExceptionCounter++;
				copy(pdfFile, cosLoadExceptionFiles);
			} catch(COSRuntimeException e) {
				System.out.println("COSRuntimeException on file: " + pdfFile.getAbsolutePath());
				failCounter++;
				cosRuntimeExceptionCounter++;
				copy(pdfFile, cosRuntimeExceptionFiles);
			} catch(ClassCastException e){
				System.out.println("ClassCastException on file: " + pdfFile.getAbsolutePath());
				failCounter++;
				classCastExceptionCounter++;
				copy(pdfFile, classCastExceptionFiles);
			} catch (URISyntaxException e) {
				// This Exception can only be thrown by this test itself
				e.printStackTrace();
			}
		}
		System.out.println("============================================");
		System.out.println("IOException total: " + ioExceptionCounter);
		System.out.println("COSLoadException total: " + cosLoadExceptionCounter);
		System.out.println("COSRuntimeException total: " + cosRuntimeExceptionCounter);
		System.out.println("ClassCastException total: " + classCastExceptionCounter);
		System.out.println("Page not found total: " + pageNotFoundCounter);
		System.out.println("Tested files total :" + totalFiles);
		System.out.println("Failed files count total :" + failCounter);
		Assert.assertEquals(0, failCounter);
	}
	
	@Test
	public void testFindPageDestinationWithOneFile() {
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
			
			List<PdfAnnotation> annotations = new PdfAnnotationImporter().importAnnotations(pdfFile);
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
	

	private boolean checkPages(List<PdfAnnotation> annotations) {
		boolean result = true;
		for(PdfAnnotation annotation : annotations){
			/*if(annotation.getPage() == null && annotation.getAnnotationType() != PdfAnnotation.BOOKMARK_WITH_URI && annotation.getAnnotationType() != PdfAnnotation.BOOKMARK_WITHOUT_DESTINATION){
				System.out.println("Could not get page from annotation: " + annotation.getTitle());
				System.out.println("Annotation file: " + annotation.getFile().getAbsolutePath());
				System.out.println("Annotation type: " + annotation.getAnnotationType());
				result =  false;
			}*/
			if(checkPages(annotation.getChildren()) == false){
				result = false;
			}
		}
		return result;
	}
	
	private void copy(File file, File destination){
		FileInputStream from = null;
	    FileOutputStream to = null;
	    try {
	      from = new FileInputStream(file);
	      to = new FileOutputStream(destination);
	      byte[] buffer = new byte[4096];
	      int bytesRead;

	      while ((bytesRead = from.read(buffer)) != -1)
	        to.write(buffer, 0, bytesRead); // write
	    } catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
	      if (from != null)
	        try {
	          from.close();
	        } catch (IOException e) {
	          ;
	        }
	      if (to != null)
	        try {
	          to.close();
	        } catch (IOException e) {
	          ;
	        }
	    }
	}

}
