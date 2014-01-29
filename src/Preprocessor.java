
/**
 * This is the preprocessor class handling all the data cleanup
 * @author Li Quan Khoo
 *
 */
public class Preprocessor {
	
	private LogReader logReader;
	private Cleaner cleaner;
	
	public Preprocessor() {
		this.logReader = new LogReader();
		this.cleaner = new Cleaner();
	}
	
	public void test() {
		LogObject obj = this.logReader.readNextLine();
		while(obj != null) {
			obj.setQuery(cleaner.filter(obj.getQuery()));
			if(! obj.getQuery().equals("")) {
				System.out.println(obj.toString());
			}
			obj = this.logReader.readNextLine();
		}
	}
}
