import processor.Preprocessor;

public class Main {
	
	private static Preprocessor preprocessor;

	
	/** Preprocessor - Take logs and output segmented JSON search session objects */
	/*
	private static void preprocessQueryLogs() {
		preprocessor = new Preprocessor();
		preprocessor.run();
	}
	*/
	

	
	/** */
	public static void main(String[] args) {
		
		//preprocessQueryLogs();
		//sampleFiles("input/yago/tsv");
		//preprocessor = new Preprocessor();
		//preprocessor.run();
		
		//getFreeBaseEntities();
		processor.FreebaseProcessor();
		//getYagoHierarchy();
		//mongoDBQueryPerformanceTest();
		
		// REMEMBER TO DELETE PREVIOUS OUTPUT FILES before running anything below this line!!

		
	}
	
}
