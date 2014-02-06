package model;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Container class for properties within a single AOL query log line
 * @author Li Quan Khoo
 */
public class LogObject {
	
	private Integer anonId;
	private String query;
	private Date queryTime;
	private String itemRank; // not really intended to be used. Change to int otherwise.
	private String clickUrl;
	
	public LogObject(String logLine) throws NumberFormatException, ParseException {
		String[] tokens = logLine.split("\t", -1);
		this.anonId = Integer.parseInt(tokens[0]);
		this.query = tokens[1];
		this.queryTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(tokens[2]);
		this.itemRank = tokens[3];
		this.clickUrl = tokens[4];
	}
	
	public Integer getAnonId() { return this.anonId; }
	public String getQuery() { return this.query; }
	public void setQuery(String str) { this.query = str; }
	public Date getQueryTime() { return this.queryTime; }
	public String getItemRank() { return this.itemRank; }
	public String getClickUrl() { return this.clickUrl; }
	
	@Override
	public String toString() {
		return anonId.toString() + "\t" + query + "\t" + queryTime.toString() + "\t"
				+ itemRank + "\t" + clickUrl;
	}
	
}
