package writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 * Facade.
 * Handles multiple file writes to a directory
 * @author Li Quan Khoo
 *
 */
public class BatchFileWriter {
	
	public static final String DEFAULT_ENCODING = "UTF-8";
	private String fileExtension;
	private File dir;
	private HashMap<String, Integer> numberingMap;
	
	public BatchFileWriter(String dirPath) {
		this(dirPath, null);
	}
	
	public BatchFileWriter(String dirPath, String fileExtension) {
		this.dir = new File(dirPath);
		this.numberingMap = new HashMap<String, Integer>();
		this.fileExtension = fileExtension;
	}
	
	public void writeToFile(String string, String fileName) {
		writeToFile(string, fileName, DEFAULT_ENCODING);
	}
	
	/*
	 * Writes a string to a file. If the file already exists 
	 *   then the file is appended with "-0", "-1", and so on
	 */
	
	private String makeFileName(String fileName, String numbering) {
		String str = fileName;
		if(! numbering.equals("")) {
			str += "-" + numbering;
		}
		if( this.fileExtension != null) {
			str += "." + this.fileExtension;
		}
		return str;
	}
	
	public void writeToFile(String string, String fileName, String encoding) {
		
		if(! this.numberingMap.containsKey(fileName)) {
			this.numberingMap.put(fileName, 0);
		}
		Integer numbering = this.numberingMap.get(fileName);
		String modifiedFileName = makeFileName(fileName, numbering.toString());
		File file = new File(dir, modifiedFileName);
		// Loop only runs if directory not fully cleared beforehand, and files are left over
		// If there are under 500 files, loop runs writes new files using gaps between the numbering
		// If there are over 500 files, files over 500 would be overwritten.
		while(numbering < 500) {
			if(! file.exists()) {
				break;
			} else {
				numbering++;
				modifiedFileName = makeFileName(fileName, numbering.toString());
				file = new File(dir, modifiedFileName);
			}
		}
		this.numberingMap.put(fileName, numbering);
		try {
	        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
	        writer.write(string);
	        writer.close();
		} catch (FileNotFoundException e) {
			System.out.println("ERROR: Cannot write to file " + modifiedFileName + ".");
		} catch (IOException e) {
			System.out.println("ERROR: IO Exception writing file " + modifiedFileName + ".");
		}
		
	}
	
	/*
	 * This deletes all files in a given directory. Will not touch
	 *   subdirectories or any files in subdirectories.
	 */
	public void deleteFilesInDir(String dirPath) {
		File dir = new File(dirPath);
		for(File file : dir.listFiles()) {
			if (! file.isDirectory()) {
				file.delete();
			}
		}
	}
	
}
