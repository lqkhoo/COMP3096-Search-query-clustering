package processor.yago;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import reader.YagoReader;
import writer.YagoDumpWriter;
import writer.YagoWriter;

public class YagoSimpleTypesProcessor extends AYagoProcessor {
	
	private YagoReader yagoReader;
	private YagoWriter yagoWriter;
	private YagoDumpWriter yagoDumpWriter;
	
	public YagoSimpleTypesProcessor(String inputFilePath, String inputFileType, String outputFilePath, String outputFileType) {
		this.inputFileType = inputFileType;
		this.outputFileType = outputFileType;
		this.yagoReader = new YagoReader(inputFilePath);
		this.yagoWriter = new YagoWriter(outputFilePath, outputFileType);
		this.yagoDumpWriter = new YagoDumpWriter(outputFilePath, "yagoSimpleTypes-dump." + outputFileType);
	}
	
	@Override
	public void processTsv() throws UnsupportedOperationException {
		String line;
		
		line = this.yagoReader.readNextLine_Tsv();
		while(line != null) {
			Pattern p = Pattern.compile("([\\t][<]([\\S]*)[>][\\t][\\S]*[\\t][<][\\S]*[>][\\t])?");	// tab < nonwhitespace* > tab nonwhitespace* tab < nonwhitespace* > tab?
			Matcher m = p.matcher(line);
			if(m.find()) {
				String record = m.group(1);
				try {
					String indexChar = m.group(2).replace('_', ' ').substring(0, 1);
					if(! Pattern.matches("\\p{Alnum}", indexChar)) {
						indexChar = "_";
					}
					this.yagoWriter.write(record, indexChar);
				} catch (NullPointerException e ) {
					// Degenerate values
					this.yagoDumpWriter.writeLine(line + "\n");
				}
			}
			line = this.yagoReader.readNextLine_Tsv();
		}
		this.yagoWriter.close();
		this.yagoDumpWriter.safeClose();
	}
	
	// Ttl not supported
}
