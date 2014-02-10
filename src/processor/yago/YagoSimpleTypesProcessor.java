package processor.yago;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import reader.YagoReader;
import writer.YagoWriter;

public class YagoSimpleTypesProcessor extends AYagoProcessor {
		
	public YagoSimpleTypesProcessor(String inputFilePath, String inputFileType, String outputFilePath, String outputFileType) {
		this.inputFileType = inputFileType;
		this.outputFileType = outputFileType;
		this.yagoReader = new YagoReader(inputFilePath);
		this.yagoWriter = new YagoWriter(outputFilePath, outputFileType);
	}
	
	@Override
	public void processTsv() throws UnsupportedOperationException {
		String line;
		
		line = this.yagoReader.readNextLine_Tsv();
		while(line != null) {
			Pattern p = Pattern.compile("[\\t]([<]([\\S]*)[>][\\t][\\S]*[\\t][<][\\S]*[>])[\\t]?");	// tab < nonwhitespace* > tab nonwhitespace* tab < nonwhitespace* > tab?
			Matcher m = p.matcher(line);
			if(m.find()) {
				String record = m.group(1);
				String fileName = m.group(2).replace('_', ' ');
				this.yagoWriter.write(record, fileName);
			}
			line = this.yagoReader.readNextLine_Tsv();
		}
	}
	
	// Ttl not supported
}
