import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Class which reads the query log files.
 * Designed to abstract away from the fact that it is reading multiple files.
 * Automatically opens next file in list and returns results until no more files are configured to be read.
 * @author Li Quan Khoo
 *
 */
public class LogReader {
	
	// Path to query logs
	public static String DEFAULT_LOG_DIR_PATH = "../querylogs";
	
	// File containing names of all query files to process
	public static String DEFAULT_CONFIG_FILE_PATH = "src/config/logfiles.ini";
	
	private int currentFileIndex;
	private String nextLine; // field to hold readLine()'s output to avoid repeated initialization
	
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
				word = line.replace("\n", "").replace("\r", "");
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
	public String readNextLine() {
		
		while(true) {
			
			if(this.bufferedReader != null) {
				
				// Read in line in current file
				try {
					nextLine = this.bufferedReader.readLine();
				} catch (IOException e) {
					System.out.println("Error reading log file " + queryLogFileNames.get(currentFileIndex) + ".");
				}
				
				// Return the line if it's valid
				if(nextLine != null) {
					return nextLine;
				} else {
					// Otherwise EOF in current file reached. Close the file and increment the file index
					try {
						this.bufferedReader.close();
						System.out.println("Finished reading file" + queryLogFileNames.get(currentFileIndex));
					} catch (IOException e) {
						System.out.println("Error closing query log file " + queryLogFileNames.get(currentFileIndex));
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
					this.fileReader = new FileReader(logDirPath + queryLogFileNames.get(currentFileIndex));
					this.bufferedReader = new BufferedReader(this.fileReader);
				} catch (FileNotFoundException e) {
					System.out.println("ERROR: Query log file " + queryLogFileNames.get(currentFileIndex) + " not found.");
					this.currentFileIndex++;
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
