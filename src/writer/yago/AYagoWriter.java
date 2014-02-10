package writer.yago;

import java.io.BufferedWriter;
import java.io.FileWriter;

import model.Triple;

/**
 * Abstract.
 * 
 * The class for writing extracted Yago relations to files.
 * 
 * NOTE: This class will create separate files within the given dir named with the
 * given entity's starting character or any other indexing feature. This is
 * specified as the indexChar argument in writeLine(). This is to try and make
 * file sizes more manageable for other processor classes downstream.
 * @author Li Quan Khoo
 * 
 */

//TODO implement
public abstract class AYagoWriter {
	
	protected String outputFilePath;
	protected FileWriter fileWriter;
	protected BufferedWriter bufferedWriter;
	
	public AYagoWriter(String outputFilePath) {
		this.outputFilePath = outputFilePath;
	}
	
	
	
	// Specify in subclass how to actually record the triple in the file
	protected abstract void writeLine(Triple triple, char indexChar);
	
}
