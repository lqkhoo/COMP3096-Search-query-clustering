import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

/**
 * Class which reads the query log files.
 * Designed to abstract away from the fact that it is reading multiple files.
 * Automatically opens next file in list and returns results until no more files are configured to be read.
 * It ignores the first line in every file as AOL logs contain column information in the first line.
 * @author Li Quan Khoo
 *
 */
public class LogReader {
	
	// Path to query logs
	public static final String DEFAULT_LOG_DIR_PATH = "testquerylogs/";
	
	// File containing names of all query files to process
	public static final String DEFAULT_CONFIG_FILE_PATH = "src/config/logfiles.ini";
	
	private int currentFileIndex;
	private String nextLine; // field to hold readLine()'s output to avoid repeated initialization
	private LogObject nextLogObject;
	
	private ArrayList<String> queryLogFileNames;
	private String logDirPath;
	private FileReader fileReader;
	private BufferedReader bufferedReader;
	
	public LogReader() {
		this(DEFAULT_CONFIG_FILE_PATH, DEFAULT_LOG_DIR_PATH);
	}
	
	public LogReader(String configFilePath, String logDirPath) {
		this.queryLogFileNames = new ArrayList<String>();
		this.currentFileIndex = 0;
		this.logDirPath = logDirPath;
		this.fileReader = null;
		this.bufferedReader = null;
		readInputFile(configFilePath);
	}
	
	private void readInputFile(String configFilePath) {
		
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
					} catch (ParseException e) { // thrown by SimleDateFormat.parse()
						return null;
					}
					
				} else {
					// Otherwise EOF in current file reached. Close the file and increment the file index
					try {
						this.bufferedReader.close();
						System.out.println("Finished reading file" + logDirPath + queryLogFileNames.get(currentFileIndex));
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
					System.out.println("Opening query log file " + logDirPath + queryLogFileNames.get(currentFileIndex));
					this.fileReader = new FileReader(logDirPath + queryLogFileNames.get(currentFileIndex));
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
