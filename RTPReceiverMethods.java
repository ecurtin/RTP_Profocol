import java.io.File;
import java.io.IOException;


public interface RTPReceiverMethods {
	
	public boolean establishConnection(String IPAddress, int windowSize);
	
	public File sendFileRequest(String filename) throws IOException;	
}
