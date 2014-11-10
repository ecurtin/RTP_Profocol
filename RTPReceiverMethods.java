import java.io.File;


public interface RTPReceiverMethods {
	
	public boolean establishConnection(String IPAddress, int windowSize);
	
	public File getFile(String filename);	
}
