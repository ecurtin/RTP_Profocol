import java.io.File;


/**
 * This class is responsible for interfacing between an application and our 
 * SENDER implementation of a reliable transport protocol.
 *
 */
public class RTPSender implements RTPSenderMethods {
	private boolean requestFile = false;
	
	public RTPSender() {
		
	}

	/**
	 * Tells an application whether or not a file transfer has been requested.
	 */
	@Override
	public boolean requestingFileTransfer() {
		return requestFile;
	}

	@Override
	public boolean sendFile(File file) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean disconnect() {
		// TODO Auto-generated method stub
		return false;
	}

}
