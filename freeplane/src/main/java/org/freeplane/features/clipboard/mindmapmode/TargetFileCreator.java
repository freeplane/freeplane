package org.freeplane.features.clipboard.mindmapmode;

import java.io.File;
import java.io.IOException;

class TargetFileCreator {
	File createTargetFile(File mindmapFile, String prototypeName) throws IOException{
		final String mapFileNameWithExtension = mindmapFile.getName();
		final String mapFileName = removeExtension(mapFileNameWithExtension);
		final File mindMapDirectory = mindmapFile.getParentFile();
		final File mapFilesDirectory = new File(mindMapDirectory, mapFileName + "_files");
		if (! mapFilesDirectory.exists() || mapFilesDirectory.isDirectory()) {
			if(prototypeName.contains(".")){
				final File target = new File(mapFilesDirectory, prototypeName);
				if(! target.exists())
					return target;
			}
			String sourceFileName = removeExtension(prototypeName);
			String fileNameTemplate = sourceFileName + "_";
			while (fileNameTemplate.length() < 3)
				fileNameTemplate = fileNameTemplate + '_';
			File targetFile = File.createTempFile(fileNameTemplate, "."+ getExtension(prototypeName), mapFilesDirectory);
			return targetFile;
			
		} else {
			String sourceFileName = removeExtension(prototypeName);
			String fileNameTemplate = mapFileName + "_" + sourceFileName + "_";
			while (fileNameTemplate.length() < 3)
				fileNameTemplate = fileNameTemplate + '_';
			File targetFile = File.createTempFile(fileNameTemplate, "."+ getExtension(prototypeName), mindMapDirectory);
			return targetFile;
		}
	}

	private String removeExtension(final String fileNameWithExtension) {
		final int extensionIndex = fileNameWithExtension.lastIndexOf('.');
		if(extensionIndex >= 0)
			return fileNameWithExtension.substring(0,  extensionIndex);
		else
			return fileNameWithExtension;
	}
	
	private String getExtension(final String fileNameWithExtension) {
		return fileNameWithExtension.substring(fileNameWithExtension.lastIndexOf('.') + 1);
	}
}