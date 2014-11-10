import java.io.File;

/**
 * This class is responsible for interfacing between an application and our 
 * RECEIVER implementation of a reliable transport protocol.
 *
 */
public class RTPReceiver implements RTPReceiverMethods {

	public RTPReceiver() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean establishConnection(String IPAddress, int windowSize) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public File getFile(String filename) {
		// TODO Auto-generated method stub
		return null;
	}

}
