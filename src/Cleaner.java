import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import lib.Stemmer;

/**
 * Class implementing stopwords removal.
 * Calling filter(String) will return a string with punctuation marks except ' and 
 *   stopwords initialized to the class removed.
 * @author Li Quan Khoo
 *
 */
public class Cleaner {
	
	public static final String DEFAULT_INPUT_FILE_PATH = "src/config/stopwords.ini";
	private static final String nonsenseRegex = "[\\p{Punct} ]*";
	private static final String preStemmingFilterRegex = "[[\\p{Punct}]&&[^. ']]"; // All punctuation marks except '.', ' ', '\''
	
	private char[] stemmerCharArray;
	private Stemmer stemmer;
	private ArrayList<String> stopwords;
	
	public Cleaner() {
		this(DEFAULT_INPUT_FILE_PATH);
	}
	
	public Cleaner(String inputFilePath) {
		
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
		
		// If string is nonsense, ignore
		if (! Pattern.matches(nonsenseRegex, string)) {
			
			// Replace most symbols with spaces
			string = string.replaceAll(preStemmingFilterRegex, " ");
			
			String[] tokens = string.split(" ");
			for(int i = 0; i < tokens.length; i++) {
				if(! this.stopwords.contains(tokens[i])) {
					
					// Porter-stemming
					this.stemmer = new Stemmer();
					this.stemmerCharArray = tokens[i].toCharArray();
					this.stemmer.add(stemmerCharArray, stemmerCharArray.length);
					this.stemmer.stem();
					if(output.equals("")) {
						output += this.stemmer.toString();
					} else {
						output += " " + this.stemmer.toString();
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
