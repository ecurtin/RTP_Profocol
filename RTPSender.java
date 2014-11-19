import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.Queue;


/**
 * This class is responsible for interfacing between an application and our 
 * SENDER implementation of a reliable transport protocol.
 *
 */
public class RTPSender implements RTPSenderMethods {
	private boolean requestFile = false;
	private PacketCreator packetCreator;
	private PacketSender packetSender;
	private int windowSize;
	private boolean sendMorePackets = true;
	
	public RTPSender(int localPort) throws SocketException {
		// Need to setup server
		this.packetSender = new PacketSender(localPort);
		this.packetCreator = new PacketCreator();
	}

	/**
	 * Tells an application whether or not a file transfer has been requested.
	 */
	@Override
	public boolean requestingFileTransfer() {
		return requestFile;
	}

	@Override
	public void sendFile(File file) throws IOException {
		// Create packets and put them in a queue to be sent
		Queue<DatagramPacket> packetsQueue = (Queue<DatagramPacket>) packetCreator.createFilePackets(file);
		
		while (!packetsQueue.isEmpty()) {
			// While we still have packets to send...
			while(sendMorePackets) {
				DatagramPacket[] packets = null;
				int numberOfPacketsToSend;
				int queueSize = packetsQueue.size();
				
				if (queueSize > windowSize) {
					numberOfPacketsToSend = windowSize;
				} else {
					numberOfPacketsToSend = queueSize;
				}
				
				for(int i = 0; i < numberOfPacketsToSend; i++) {
					packets[i] = packetsQueue.remove();
				};
				
				packetSender.sendPackets(packets);
			};
		};
	}

	@Override
	public void disconnect() throws IOException {
		DatagramPacket[] packets = packetCreator.createDisconnectPackets();
		packetSender.sendPackets(packets);
	}
	
	/**
	 * Packet Receiver sets this based on connection packets from receiver
	 * @param sizeOfWindow
	 */
	public void setWindowSize(int sizeOfWindow) {
		windowSize = sizeOfWindow;
	}
	
	/**
	 * Called by PacketSender? to let RTPSender that it is ok to provide more packets
	 */
	public void readyToSendMorePackets() {
		sendMorePackets = true;
	}
	
	/**
	 * Called by PackageReceiver? when a file transfer request comes in.
	 */
	public void setFileTransferRequest() {
		requestFile = true;
	}

}
