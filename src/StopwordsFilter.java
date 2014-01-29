import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Class implementing stopwords removal.
 * Calling filter(String) will return a string with punctuation marks except ' and 
 *   stopwords initialized to the class removed.
 * @author Li Quan Khoo
 *
 */
public class StopwordsFilter {
	
	public static final String DEFAULT_INPUT_FILE_PATH = "src/config/stopwords.ini";
	private static final String nonsenseRegex = "\\p{Punct}[ ]";
	private static final String punctuationRegex = "\\p{Punct}";
	
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
				word = line.replaceAll("[\n\r]", "");
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
		if (! Pattern.matches(nonsenseRegex, string)) {
			string = string.replaceAll(punctuationRegex, " ");
			String[] tokens = string.split(" ");
			for(int i = 0; i < tokens.length; i++) {
				if(! this.stopwords.contains(tokens[i])) {
					if(output.equals("")) {
						output += tokens[i];
					} else {
						output += " " + tokens[i];
					}
				}
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
