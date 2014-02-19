package processor.yago;

import writer.MongoWriter;

public class YagoTransitiveTypesProcessor extends YagoSimpleTypesProcessor {

	public YagoTransitiveTypesProcessor(MongoWriter mongoWriter, String inputFilePath, String inputFileType) {
		super(mongoWriter, inputFilePath, inputFileType);
	}

}
