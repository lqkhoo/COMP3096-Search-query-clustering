package processor.yago;

import reader.yago.YagoReader;
import writer.yago.YagoSimpleTypesWriter;

public class YagoSimpleTypesProcessor extends AYagoProcessor {
	
	public YagoSimpleTypesProcessor(String inputFilePath, String outputFilePath) {
		this.yagoReader = new YagoReader(inputFilePath);
		this.yagoWriter = new YagoSimpleTypesWriter(outputFilePath);
	}
	
	@Override
	public void process() {
		//TODO
		
	}
	
}
