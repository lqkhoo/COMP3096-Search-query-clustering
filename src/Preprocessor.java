import lib.Stemmer;

/**
 * This is the preprocessor class handling all the data cleanup
 * @author Li Quan Khoo
 *
 */
public class Preprocessor {
	
	private LogReader logReader;
	private StopwordsFilter stopwordsFilter;
	private Stemmer stemmer;
	
	public Preprocessor() {
		this.logReader = new LogReader();
		this.stopwordsFilter = new StopwordsFilter();
		this.stemmer = new Stemmer();
	}
	
	
}
