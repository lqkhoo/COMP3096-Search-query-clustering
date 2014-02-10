package writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;

import model.Triple;

/**
 * Abstract.
 * 
 * The class for writing extracted Yago relations to files.
 * 
 * This class will create 27 directories (1 for each letter, 1 for special symbols)
 * to classify entities. Each entity gets one file, segregated into the directory
 * named with their starting letter.
 * 
 * NOTE: This class will create separate files within the given dir named with the
 * given entity's starting character or any other indexing feature. This is
 * specified as the indexChar argument in writeLine(). This is to try and make
 * file sizes more manageable for other processor classes downstream.
 * 
 * The class expects calls to write() to be writing to different files each time.
 * If there are multiple lines to write, use the write(String[], fileName) method,
 * otherwise the class will open / close the same file repeatedly.
 * 
 * @author Li Quan Khoo
 * 
 */

public class YagoWriter {
	
	protected String outputFileExtension;
	protected String outputDirPath;
	protected String subDirPath;
	protected String filePath;
	protected File file;
	protected FileWriter fileWriter;
	protected BufferedWriter bufferedWriter;
		
	public YagoWriter(String outputDirPath, String outputFileExtension) {
		this.outputDirPath = outputDirPath;
		this.outputFileExtension = outputFileExtension;
	}
	
	private void open(String fileName) {
		
		String startingChar = fileName.substring(0, 1);
		this.subDirPath = this.outputDirPath + "/" + startingChar;
		this.filePath = this.subDirPath + "/" + fileName + "." + this.outputFileExtension;
		this.file = new File(this.filePath);
		
		try {
			new File(this.subDirPath).mkdirs();
			this.fileWriter = new FileWriter(this.file);
			this.bufferedWriter = new BufferedWriter(this.fileWriter);
		} catch (IOException e) {
			System.out.println("AYagoWriter: Error creating file: " + filePath);
		}
		
		
	}
	
	private void close() {
		try {
			this.bufferedWriter.close();
		} catch (IOException e) {
			System.out.println("AYagoWriter: Error closing file.");
		}
	}
	
	public void write(String[] stringArray, String fileName) {
		open(fileName);
		for(String string : stringArray) {
			this._write(string, fileName);
		}
	}
	
	public void write(String string, String fileName) {
		open(fileName);
		_write(string, fileName);
		close();
	}
	
	private void _write(String string, String fileName) {
		
		try {
			this.bufferedWriter.write(string + "\n");
		} catch (IOException e) {
			System.out.println("AYagoWriter: Error writing to file: " + fileName);
		}
		
	}
	
}
