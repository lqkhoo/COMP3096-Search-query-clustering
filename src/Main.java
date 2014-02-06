import util.BigFileScout;

public class Main {
	
	private static Preprocessor preprocessor;
	private static IQueryClusterer clusterer;
	private static BigFileScout scout;
	
	/**
	 * Preprocessor - Take logs and output segmented JSON search session objects
	 */
	private void runPreprocessor() {
		preprocessor = new Preprocessor();
		preprocessor.run();
	}
	
	/**
	 * Query clusterer
	 */
	private void runClusterer() {
		clusterer = new QChtc();
		// stub method
	}
	
	/**
	 * Scout - Takes large files and outputs the first n lines to another file
	 * @param inputDir
	 */
	private static void scoutFiles(String inputDir) {
		scout = new BigFileScout(inputDir);
		scout.run();
	}
	
	public static void main(String[] args) {
		
		//runPreprocessor();
		scoutFiles("input/yago/ttl");
		
	}
	
}
