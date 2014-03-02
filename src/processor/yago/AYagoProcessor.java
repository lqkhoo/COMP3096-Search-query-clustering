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
			processEntitiesTsv();
		} else if (this.inputFileType.equals(TTL)) {
			processEntitiesTtl();
		} else {
			throw new UnsupportedOperationException("AYagoProcessor: File type " + this.inputFileType + " not supported.");
		}
	}
	
	public void processEntitiesTsv() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	public void processEntitiesTtl() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	public void processClassesTsv() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	public void processClassesTtl() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
}
