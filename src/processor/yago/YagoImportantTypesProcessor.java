package processor.yago;

import writer.MongoWriter;
/**
 * Specialized processor for the Yago file "yagoImportantTypes"
 * The file happens to have the same syntax as "yagoSimpleTypes"
 * 
 * @author Li Quan Khoo
 */
public class YagoImportantTypesProcessor extends YagoSimpleTypesProcessor {

	public YagoImportantTypesProcessor(MongoWriter mongoWriter, String inputFilePath, String inputFileType) {
		super(mongoWriter, inputFilePath, inputFileType);
	}

}
