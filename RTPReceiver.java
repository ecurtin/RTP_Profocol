import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;

/**
 * This class is responsible for interfacing between an application and our 
 * RECEIVER implementation of a reliable transport protocol.
 *
 */
public class RTPReceiver implements RTPReceiverMethods {
	private PacketCreator packetCreator;
	private PacketSender packetSender;
	
	public RTPReceiver() {
		packetCreator = new PacketCreator();
	}

	/**
	 * Sets up connection with SENDER (server), providing a window size.
	 */
	@Override
	public boolean establishConnection(String IPAddress, int windowSize) {
		DatagramPacket[] connectionPackets = packetCreator.createConnectPackets(IPAddress, windowSize);
		return false;
	}

	/**
	 * Creates file request packets then sends them.
	 * @throws IOException 
	 */
	@Override
	public File sendFileRequest(String filename) throws IOException {
		DatagramPacket[] requestPackets = packetCreator.createRequestFilePackets(filename);
		packetSender.sendPackets(requestPackets);
		return null;
	}

}
