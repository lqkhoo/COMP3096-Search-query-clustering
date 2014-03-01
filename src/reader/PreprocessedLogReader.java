package reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.google.gson.Gson;

import model.SearchSessionSerial;

/**
 * Batch file reader for preprocessed query logs
 * @author Li Quan Khoo
 *
 */
public class PreprocessedLogReader {
	
	public static final String DEFAULT_INPUT_DIR_PATH = "output/preprocessor-out/";
	private String inputDirPath;
	
	private FileReader fileReader;
	private BufferedReader bufferedReader;
	
	private int currentFileIndex;
	private File[] logFiles;
	
	public PreprocessedLogReader() {
		this(DEFAULT_INPUT_DIR_PATH);
	}
	
	public PreprocessedLogReader(String inputDirPath) {
		this.inputDirPath = inputDirPath;
		this.currentFileIndex = 0;
		this.logFiles = new File(this.inputDirPath).listFiles();
	}
	
	public SearchSessionSerial[] getLogs() {
		
		StringBuilder stringBuilder;
		File logFile;
		
		if(this.logFiles.length == 0 || this.currentFileIndex == this.logFiles.length) {
			return null;
		}
		
		stringBuilder = new StringBuilder();
		logFile = this.logFiles[this.currentFileIndex];
		try {
			String line;
			
			this.fileReader = new FileReader(logFile);
			this.bufferedReader = new BufferedReader(this.fileReader);
			
			line = this.bufferedReader.readLine();
			while(line != null) {
				stringBuilder.append(line);
				line = this.bufferedReader.readLine();
			}
			
			System.out.println("ProcessedLogReader: Finished reading in file " + logFile.getName());
			this.currentFileIndex++;
			
		} catch(FileNotFoundException e) {
			System.out.println("PreprocessedLogReader: FileNotFoundException");
		} catch (IOException e) {
			System.out.println("PreprocessedLogReader: IOException");
		} finally {
			if(this.bufferedReader != null) {
				try {
					this.bufferedReader.close();
				} catch(IOException e) {}
			}
		}
		
		return new Gson().fromJson(stringBuilder.toString(), new SearchSessionSerial[]{}.getClass());
		
	}
	
}
