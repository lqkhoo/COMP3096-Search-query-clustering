package processor.yago;

public abstract class AYagoProcessor {
	
	public static final String TSV = "tsv";
	public static final String TTL = "ttl";
	
	protected String inputFileType;
	protected String outputFileType;
	
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
