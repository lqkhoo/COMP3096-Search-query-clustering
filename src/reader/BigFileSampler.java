package reader;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Reads in the first n lines of large files and copies them to another file.
 * Convenience / utility classes for checking out files which can't be conveniently inspected otherwise.
 * 
 * If given a directory, it will process all files within that directory (non-recursive).
 * 
 * @author Li Quan Khoo
 *
 */
public class BigFileSampler {
	
	public static final int DEFAULT_NUM_OF_LINES_TO_READ = 100;
	public static final String DEFAULT_OUTPUT_DIR = "output/scout-out/";
	
	private String inputPath;
	private String outputDir;
	private int numOfLinesToRead;
	
	public BigFileSampler(String inputFilePath) {
		this(inputFilePath, DEFAULT_NUM_OF_LINES_TO_READ);
	}
	
	public BigFileSampler(String inputPath, int numOfLinesToRead) {
		this.inputPath = inputPath;
		this.outputDir = DEFAULT_OUTPUT_DIR;
		this.numOfLinesToRead = numOfLinesToRead;
	}
	
	private void processFile(File inputFile) {
		
		int linesRead = 0;
		try {
			
			// Readers
			FileReader fr = new FileReader(inputFile);
			BufferedReader br = new BufferedReader(fr);
			
			//Writers
			File outputFile = new File(this.outputDir, inputFile.getName());
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
			
			String line = br.readLine();
			while(line != null) {
				writer.write(line + "\n");
				linesRead++;
				if(linesRead >= this.numOfLinesToRead) {
					break;
				}
				line = br.readLine();
			}
			br.close();
			writer.close();
			
		} catch (FileNotFoundException e) {
			System.out.println("BigFileScout: Input file not found");
		} catch (IOException e) {
			System.out.println("BigFileScout: IO exception reading input file");
		}
	}
	
	public void run() {
		
		File input = new File(this.inputPath);
		if(! input.isDirectory()) {
			processFile(input);
		} else {
			for(File file : input.listFiles()) {
				if (! file.isDirectory()) {
					processFile(file);
				}
			}
		}


	}
	
}
