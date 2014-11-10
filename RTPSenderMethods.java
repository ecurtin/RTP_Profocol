import java.io.File;


public interface RTPSenderMethods {
	
	public boolean requestingFileTransfer();
	
	public boolean sendFile(File file);
	
	public boolean disconnect();
}
