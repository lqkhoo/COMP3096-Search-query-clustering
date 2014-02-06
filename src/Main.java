import processor.IQueryClusterer;
import processor.Preprocessor;
import processor.QChtc;
import reader.BigFileSampler;


public class Main {
	
	private static Preprocessor preprocessor;
	private static IQueryClusterer clusterer;
	private static BigFileSampler sampler;
	
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
	 * Sample - Takes large files and outputs the first n lines to another file
	 * @param inputDir
	 */
	private static void sampleFiles(String inputDir) {
		sampler = new BigFileSampler(inputDir);
		sampler.run();
	}
	
	public static void main(String[] args) {
		
		//runPreprocessor();
		sampleFiles("input/yago/tsv");
		
	}
	
}
