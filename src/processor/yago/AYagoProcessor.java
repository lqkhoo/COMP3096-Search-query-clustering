package processor.yago;

import reader.yago.YagoReader;
import writer.yago.AYagoWriter;

public abstract class AYagoProcessor {
	
	protected YagoReader yagoReader;
	protected AYagoWriter yagoWriter;
	
	
	
	public abstract void process();
	
}
