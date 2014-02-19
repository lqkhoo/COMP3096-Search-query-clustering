package processor.yago;

import model.YagoHierarchy;
import reader.YagoReader;
import writer.MongoWriter;

/**
 * Abstract class providing base functionality for Yago file processors
 * 
 * @author Li Quan Khoo
 */
public abstract class AYagoProcessor {
	
	public static final String TSV = "tsv";
	public static final String TTL = "ttl";
	
	protected String inputFileType;
	protected YagoReader yagoReader;
	protected MongoWriter mongoWriter;
	protected YagoHierarchy hierarchy;
		
	public AYagoProcessor(MongoWriter mongoWriter, String inputFilePath, String inputFileType) {
		this.mongoWriter = mongoWriter;
		this.inputFileType = inputFileType;
		this.yagoReader = new YagoReader(inputFilePath);
	}
	
	public AYagoProcessor(YagoHierarchy hierarchy, String inputFilePath, String inputFileType) {
		this.hierarchy = hierarchy;
		this.inputFileType = inputFileType;
		this.yagoReader = new YagoReader(inputFilePath);
	}
	
	public final void process() throws UnsupportedOperationException {
		if(this.inputFileType.equals(TSV)) {
			processTsv();
		} else if (this.inputFileType.equals(TTL)) {
			processTtl();
		} else {
			throw new UnsupportedOperationException("AYagoProcessor: File type " + this.inputFileType + " not supported.");
		}
	}
	
	public void processTsv() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	public void processTtl() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
}
