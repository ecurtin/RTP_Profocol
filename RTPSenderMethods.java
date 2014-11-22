import java.io.File;
import java.io.IOException;


public interface RTPSenderMethods {
	
	public boolean requestingFileTransfer();
	
	public void sendFile(File file) throws IOException;
}
