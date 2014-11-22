import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Queue;


/**
 * This class is responsible for interfacing between an application and our 
 * SENDER implementation of a reliable transport protocol.
 *
 */
public class PacketReceiverForSender implements RTPSenderMethods {
	private boolean requestFile = false;
	private PacketCreator packetCreator;
	private int windowSize;
	private DatagramSocket socket;
	
	public PacketReceiverForSender(int localPort) throws IOException {
		// SETUP SERVER
		DatagramSocket socket = new DatagramSocket(localPort);
		this.packetCreator = new PacketCreator(socket, windowSize);
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];
		
		while(true) {
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			socket.receive(receivePacket);
			Packet packet = new Packet(receivePacket);
			
			if (packet.isACKPacket()) {
				
			} else if (packet.isConnectionPacket()) {
				
			}
			
		}
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
	 * Called when a file transfer request comes in.
	 */
	public void setFileTransferRequest() {
		requestFile = true;
	}

}
