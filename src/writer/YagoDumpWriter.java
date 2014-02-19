package writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Class which handles writing of any inconsistent data that throws exceptions etc.
 * Writes everything to one file for manual inspection so filters can be refined.
 * @author Li Quan Khoo
 *
 */
@Deprecated
public class YagoDumpWriter {
	
	private boolean isBufferOpen = false;
	
	private String outputDirPath;
	private String outputFileName;
	
	private FileWriter fileWriter;
	private BufferedWriter bufferedWriter;
	
	public YagoDumpWriter(String outputDirPath, String outputFileName) {
		this.outputDirPath = outputDirPath;
		this.outputFileName = outputFileName;
	}
	
	private void open() {
		try {
			File file = new File(this.outputDirPath + "/" + outputFileName);
			this.fileWriter = new FileWriter(file);
			this.bufferedWriter = new BufferedWriter(this.fileWriter);
			System.out.println("YagoDumpWriter: File buffers opened. Dumping...");
			isBufferOpen = true;
		} catch (IOException e) {
			System.out.println("AYagoWriter: Error opening file " + this.outputFileName);
		}
	}
	
	public void writeLine(String str) {
		if(! isBufferOpen) {
			open();
		}
		try {
			this.bufferedWriter.write(str);
		} catch (IOException e) {
			System.out.println("YagoDumpWriter: Error writing to file " + this.outputFileName);
		}
	}
	
	public void safeClose() {
		if(isBufferOpen) {
			try {
				this.bufferedWriter.close();
			} catch (IOException e) {
				System.out.println("YagoDumpWriter: Error closing file " + this.outputFileName);
			}
			System.out.println("YagoDumpWriter: File successfully closed.");
		}

	}
	
	
}
