import processor.IQueryClusterer;
import processor.Preprocessor;
import processor.QChtc;
import processor.YagoProcessor;
import processor.yago.AYagoProcessor;
import reader.BigFileSampler;


public class Main {
	
	private static Preprocessor preprocessor;
	private static IQueryClusterer clusterer;
	private static BigFileSampler sampler;
	private static YagoProcessor yagoProcessor;
	
	/**
	 * Preprocessor - Take logs and output segmented JSON search session objects
	 * 
	 */
	private static void preprocessQueryLogs() {
		preprocessor = new Preprocessor();
		preprocessor.run();
	}
	
	/**
	 * Query clusterer
	 * 
	 */
	private static void runClusterer() {
		clusterer = new QChtc();
		// stub method
	}
	
	/**
	 * Sample - Takes large files and outputs the first n lines to another file
	 *  @param inputDir
	 */
	private static void sampleFiles(String inputDir) {
		sampler = new BigFileSampler(inputDir);
		sampler.run();
	}
	
	/**
	 * 
	 */
	private static void processYago() {
		yagoProcessor = new YagoProcessor(new AYagoProcessor[] {
				
		});
		yagoProcessor.run();
	}
	
	
	/** */
	public static void main(String[] args) {
		
		//preprocessQueryLogs();
		//sampleFiles("input/yago/tsv");
		processYago();
		
	}
	
}
