package reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import exception.NotImplementedException;


/**
 * Facade.
 * Provides access to YAGO tsv/ttl files. Some entities are contained over multiple lines,
 * especially in tsv files. This class abstracts away from opening or closing the files - 
 * the class calling the concrete class implementation should only need to call readNext_X()
 * until null is returned - this class handles all file opening and closing.
 * @author Li Quan Khoo
 *
 */
public abstract class YagoReader {
	
	protected String inputFilePath;
	protected FileReader fileReader;
	protected BufferedReader bufferedReader;
	
	protected YagoReader(String inputFilePath) {
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
	
	protected String readLine() {
		
		if(this.fileReader == null) {
			this.open();
		}
		
		String line;
		try {
			line = this.bufferedReader.readLine();
			if(line != null) {
				return line;
			} else {
				this.close();
			}
		} catch (IOException e) {
			System.out.println("YagoReader: IOException while reading input file: " + inputFilePath);
		}
		return null;
	}
	
	// Override if subclass supports reading from a tsv file
	public String readNext_Tsv() throws NotImplementedException {
		throw new NotImplementedException();
	}
	
	// Override if subclass supports reading from a ttl file
	public String readNext_Ttl() throws NotImplementedException {
		throw new NotImplementedException();
	}
	
}
