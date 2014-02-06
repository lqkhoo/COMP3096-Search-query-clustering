package reader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import model.LogObject;

/**
 * Facade.
 * Class which reads the query log files. Abstracts away from the fact that it is reading multiple files.
 * Automatically opens next file in list and returns results until no more files are configured to be read.
 * It ignores the first line in every file as AOL logs contain column information in the first line.
 * @author Li Quan Khoo
 *
 */
public class AolLogReader {
	
	// Path to query logs
	public static final String DEFAULT_LOG_DIR_PATH = "input/querylogs/";
	
	// File containing names of all query files to process
	public static final String DEFAULT_CONFIG_FILE_PATH = "src/config/logfiles.ini";
	
	private int currentFileIndex;
	private String nextLine; // field to hold readLine()'s output to avoid repeated initialization
	private LogObject nextLogObject;
	private long fileSize;
	private int reportPerPercent = 10; // How much of the current file is read before generating a report in the console
	private int lastReportPercent;
	private long bytesRead;
	
	private ArrayList<String> queryLogFileNames;
	private String logDirPath;
	private FileReader fileReader;
	private BufferedReader bufferedReader;
	
	public AolLogReader() {
		this(DEFAULT_CONFIG_FILE_PATH, DEFAULT_LOG_DIR_PATH);
	}
	
	public AolLogReader(String configFilePath, String logDirPath) {
		this.queryLogFileNames = new ArrayList<String>();
		this.currentFileIndex = 0;
		this.logDirPath = logDirPath;
		this.fileReader = null;
		this.bufferedReader = null;
		this.fileSize = 0;
		this.lastReportPercent = 0;
		this.bytesRead = 0;
		getTargetFiles(configFilePath);
	}
	
	private void getTargetFiles(String configFilePath) {
		
		File configFile = new File(configFilePath);
		try {
			FileReader fr = new FileReader(configFile);
			BufferedReader br = new BufferedReader(fr);
			
			String line = br.readLine();
			String word = null;
			while(line != null) {
				word = line.replaceAll("[\n\r]", "");
				this.queryLogFileNames.add(word);
				line = br.readLine();
			}
			br.close();
			
		} catch (FileNotFoundException e) {
			System.out.println("Query log configuration file (" + configFilePath + ") not found");
		} catch (IOException e) {
			System.out.println("IO exception reading query log configuration file");
		}
	}
	
	/**
	 * Gives next query in line until EOF of last file is reached, then it returns null.
	 */
	public LogObject readNextLine() {
		
		while(true) {
			
			if(this.bufferedReader != null) {
				// Read in line in current file
				try {
					nextLine = this.bufferedReader.readLine();
					if(nextLine != null) {
						bytesRead += nextLine.getBytes().length;
						if(bytesRead > fileSize / 100 * lastReportPercent) {
							System.out.println("    " + lastReportPercent + "%");
							lastReportPercent += reportPerPercent;
						}
					}
					
				} catch (IOException e) {
					System.out.println("Error reading log file " + logDirPath + queryLogFileNames.get(currentFileIndex) + ".");
				}
				
				// Return the line if it's valid
				if(nextLine != null) {
					try {
						this.nextLogObject = new LogObject(nextLine);
						return this.nextLogObject;
					} catch (NumberFormatException e) { // thrown by Integer.parseInt()
						System.out.println(e);
						System.out.println("foo");
						return null;
					} catch (ParseException e) { // thrown by SimpleDateFormat.parse()
						return null;
					}
					
				} else {
					// Otherwise EOF in current file reached. Close the file and increment the file index
					try {
						this.bufferedReader.close();
						bytesRead = 0;
						System.out.println("Finished reading file " + logDirPath + queryLogFileNames.get(currentFileIndex));
					} catch (IOException e) {
						System.out.println("Error closing query log file " + logDirPath + queryLogFileNames.get(currentFileIndex));
					}
					
					this.fileReader = null;
					this.bufferedReader = null;
					this.currentFileIndex++;
				}
			}
			
			// Open next valid file, try until max number of files is reached.
			while((this.fileReader == null
					|| this.bufferedReader == null
					) && currentFileIndex < queryLogFileNames.size()) {
				try {
					System.out.println("Processing query log file " + logDirPath + queryLogFileNames.get(currentFileIndex));
					File file = new File(new File(logDirPath), queryLogFileNames.get(currentFileIndex));
					this.fileSize = file.length();
					this.fileReader = new FileReader(file);
					this.bufferedReader = new BufferedReader(this.fileReader);
					this.bufferedReader.readLine(); // This skips the first line of every log file
				} catch (FileNotFoundException e) {
					System.out.println("ERROR: Query log file " + logDirPath + queryLogFileNames.get(currentFileIndex) + " not found.");
					this.currentFileIndex++;
				} catch (IOException e) {
					System.out.println("Error reading log file " + logDirPath + queryLogFileNames.get(currentFileIndex) + ".");
				}
			}
			
			// If still cannot be initialized, we have reached the end of the list of files. Return null.
			if(this.fileReader == null || this.bufferedReader == null) {
				return null;
			}
			
			// otherwise we loop to the top again, now with a valid file initialized
		}
	}
	
}
