package writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
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
	
	protected static final String[] HASH_LIST = new String[] {
		"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "_"
	};
	
	protected String outputFileExtension;
	protected String outputDirPath;
	protected boolean areBuffersOpened = false;
	protected HashMap<String, File> fileMap;
	protected HashMap<String, FileWriter> fileWriterMap;
	protected HashMap<String, BufferedWriter> bufferedWriterMap;
		
	public YagoWriter(String outputDirPath, String outputFileExtension) {
		this.outputDirPath = outputDirPath;
		this.outputFileExtension = outputFileExtension;
		
		this.fileMap = new HashMap<String, File>(27);
		this.fileWriterMap = new HashMap<String, FileWriter>(27);
		this.bufferedWriterMap = new HashMap<String, BufferedWriter>(27);
	}
	
	private void open() {
		for(int i = 0; i < HASH_LIST.length; i++) {
			try {
				File file = new File(this.outputDirPath + "/" + HASH_LIST[i] + "." + this.outputFileExtension);
				FileWriter fileWriter;
				fileWriter = new FileWriter(file, true);	// append, do not overwite
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
				this.fileMap.put(HASH_LIST[i], file);
				this.fileWriterMap.put(HASH_LIST[i], fileWriter);
				this.bufferedWriterMap.put(HASH_LIST[i], bufferedWriter);
			} catch (IOException e) {
				System.out.println("YagoWriter: Error opening file: " + HASH_LIST[i] + "." + this.outputFileExtension);
			}
		}
		System.out.println("YagoWriter: File buffers opened. Writing...");
		this.areBuffersOpened = true;
	}
	
	public void close() {
		for(int i = 0; i < HASH_LIST.length; i++) {
			try {
				this.bufferedWriterMap.get(HASH_LIST[i]).close();
			} catch (IOException e) {
				System.out.println("YagoWriter: Error closing file: " + HASH_LIST[i] + "." + this.outputFileExtension);
			}
		}
		System.out.println("YagoWriter: All files successfully closed.");
	}
	
	public void write(String[] stringArray, String indexChar) {
		if(! areBuffersOpened) {
			open();
		}
		for(String string : stringArray) {
			this._write(string, indexChar);
		}
	}
	
	public void write(String string, String indexChar) {
		if(! areBuffersOpened) {
			open();
		}
		_write(string, indexChar);
	}
	
	private void _write(String string, String indexChar) {
		try {
			this.bufferedWriterMap.get(indexChar).write(string + "\n");
		} catch (IOException e) {
			System.out.println("YagoWriter: Error writing to file.");
		}
	}
	
}
