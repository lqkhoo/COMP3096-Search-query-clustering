package reader.yago;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Facade.
 * Provides access to YAGO tsv/ttl files. Some entities are contained over multiple lines,
 * especially in tsv files. This class abstracts away from opening or closing the files - 
 * the class calling the concrete class implementation should only need to call readNext_X()
 * until null is returned - this class handles all file opening and closing.
 * 
 * This is the class for reading in general files. Subclass this under the package
 * reader.specialized if the file requires special processing.
 * 
 * @author Li Quan Khoo
 *
 */
public class YagoReader {
	
	protected String inputFilePath;
	protected FileReader fileReader;
	protected BufferedReader bufferedReader;
	
	public YagoReader(String inputFilePath) {
		this.inputFilePath = inputFilePath;
	}
		
	private boolean open() {
		try {
			this.fileReader = new FileReader(new File(this.inputFilePath));
			this.bufferedReader = new BufferedReader(this.fileReader);
		} catch (FileNotFoundException e) {
			System.out.println("YagoReader: Input file not found.");
			return false;
		}
		return true;
	}
	
	private boolean close() {
		try {
			this.fileReader.close();
			this.bufferedReader.close();
		} catch(IOException e) {
			System.out.println("YagoReader: Unable to close file properly: " + inputFilePath);
			return false;
		}
		return true;
	}
	
	/*
	 * This is only callable from specialized readers. The API methods to call are 
	 * readNextLine_Tsv and readNextLine_Ttl, which should use this method to read.
	 * 
	 * YAGO asides are denoted by lines starting with characters:
	 * '@'		rdf description
	 * '#'		comment
	 * '@#'		??
	 */
	protected final String readLine() {
		
		if(this.fileReader == null) {
			this.open();
		}
		
		String line;
		try {
			line = this.bufferedReader.readLine();
			while(true) {
				if(line != null) {
					// short circuit for performance reasons - the vast majority of lines start with "<".
					// Tsv files commonly start with "\t"
					if(! line.startsWith("<") || ! line.startsWith("\t")) {
						// if required, then set specialized filters to filter out particular asides here
						if(line.equals("") ||
								line.startsWith("@") ||
								line.startsWith("#")) {
							continue;
						}
						return line;
					}
					continue;
				} else {
					this.close();
				}
			}

		} catch (IOException e) {
			System.out.println("YagoReader: IOException while reading input file: " + inputFilePath);
		}
		return null;
	}
	
	// Should usually be return this.readLine(), unless the file requires specialized reading behavior, like
	// reading in multiple lines at once
	
	// Override if subclass supports reading from a tsv file
	public String readNextLine_Tsv() {
		return this.readLine();
	}
	
	// Override if subclass supports reading from a ttl file
	public String readNextLine_Ttl() {
		return this.readLine();
	}
	
}
