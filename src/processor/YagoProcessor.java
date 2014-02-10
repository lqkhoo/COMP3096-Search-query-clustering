package processor;

import java.util.ArrayList;

import processor.yago.AYagoProcessor;

/**
 * 
 * The class controlling all processing involving YAGO input files.
 * 
 * Note: This class calls the specialized YAGO processor classes -
 * they do not subclass this class.
 * 
 * @author Li Quan Khoo
 *
 */
public class YagoProcessor {
	
	private ArrayList<AYagoProcessor> yagoProcessors;
	
	public YagoProcessor(AYagoProcessor[] yagoProcessors) {
		this.yagoProcessors = new ArrayList<AYagoProcessor>();
		for(AYagoProcessor processor : yagoProcessors) {
			this.yagoProcessors.add(processor);
		}
	};
	
	public void addProcessor(AYagoProcessor yagoProcessor) {
		this.yagoProcessors.add(yagoProcessor);
	}
	
	public void run() {
		for(AYagoProcessor processor : yagoProcessors) {
			processor.process();
		}
	}
	
}
