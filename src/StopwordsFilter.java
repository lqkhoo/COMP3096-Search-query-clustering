import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;


/**
 * Class implementing stopwords removal.
 * Calling filter(String) will return a string with all stopwords initialized to the class removed.
 * @author Li Quan Khoo
 *
 */
public class StopwordsFilter {
	
	public static String DEFAULT_INPUT_FILE_PATH = "src/config/stopwords.txt"; 
	
	private ArrayList<String> stopwords;
	
	public StopwordsFilter() {
		this(DEFAULT_INPUT_FILE_PATH);
	}
	
	public StopwordsFilter(String inputFilePath) {
		
		this.stopwords = new ArrayList<String>();
		
		File inputFile = new File(inputFilePath);
		try {
			FileReader fr = new FileReader(inputFile);
			BufferedReader br = new BufferedReader(fr);
			
			String line = br.readLine();
			String word = null;
			while(line != null) {
				word = line.replace("\n", "").replace("\r", "");
				stopwords.add(word);
				line = br.readLine();
			}
			br.close();
			
		} catch (FileNotFoundException e) {
			System.out.println("Stopwords file (" + inputFilePath + ") not found");
		} catch (IOException e) {
			System.out.println("IO exception reading stopwords file");
		}
	}
	
	public String filter(String string) {
		
		String output = "";
		String[] tokens = string.split(" ");
		for(int i = 0; i < tokens.length; i++) {
			if(! this.stopwords.contains(string)) {
				output += " " + tokens[i];
			}
		}
		return output;
	}
	
	public void printStopwords() {
		for(int i = 0; i < stopwords.size(); i++) {
			System.out.println(stopwords.get(i));
		}
	}
	
}
