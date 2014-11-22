import java.io.File;
import java.io.IOException;


public interface RTPSenderMethods {
	
	public String requestingFileTransfer();
	
	public void sendFile(File file) throws IOException;
}
