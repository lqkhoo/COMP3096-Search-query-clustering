package processor.yago;

import writer.MongoWriter;
/**
 * Specialized processor for the Yago file "yagoTransitiveTypes"
 * The file happens to have the same syntax as "yagoSimpleTypes"
 * 
 * @author Li Quan Khoo
 */
public class YagoTransitiveTypesProcessor extends YagoSimpleTypesProcessor {

	public YagoTransitiveTypesProcessor(MongoWriter mongoWriter, String inputFilePath, String inputFileType) {
		super(mongoWriter, inputFilePath, inputFileType);
	}

}
