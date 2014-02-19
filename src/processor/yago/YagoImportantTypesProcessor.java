package processor.yago;

import writer.MongoWriter;

public class YagoImportantTypesProcessor extends YagoSimpleTypesProcessor {

	public YagoImportantTypesProcessor(MongoWriter mongoWriter, String inputFilePath, String inputFileType) {
		super(mongoWriter, inputFilePath, inputFileType);
	}

}
