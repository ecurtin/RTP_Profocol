//import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
//import java.net.SocketException;
//import java.util.Queue;


/**
 * This class is responsible for interfacing between an application and our 
 * SENDER implementation of a reliable transport protocol.
 *
 */
public class PacketReceiverForServer extends PacketReceiver {
	private String requestedFile = null;
	
	public PacketReceiverForServer(int sourcePort) throws IOException {
		// SETUP SERVER
		DatagramSocket socket = new DatagramSocket(sourcePort);
		InetAddress sourceAddress = InetAddress.getLocalHost();
		this.packetCreator = new PacketCreatorForServer(socket, sourcePort, sourceAddress);
		
		// Size of allowed packet data
		byte[] receiveData = new byte[PACKET_SIZE];
		
		while(true) {
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			socket.receive(receivePacket);
			
			// Get and store client IP address and port number
			InetAddress IPAddress = receivePacket.getAddress();
			int port = receivePacket.getPort();
			packetCreator.setDestinationAddress(IPAddress);
			packetCreator.setDestinationPort(port);
			
			// Translate datagram packet into something RTP understands
			Packet packet = new Packet(receivePacket);
			
			// Can either be a data ACK or finalizing Connection
			if (packet.isACK()) {
				int ACKNumber = packet.getACK();
				((PacketCreatorForServer) packetCreator).receiveACK(ACKNumber);
				
			// Connection Packet (receive file name to transfer)
			} else if (packet.isConnection()) {
				String fileName = packet.getFileName();
				setFileTransferRequest(fileName);
				packetCreator.sendConnectionPacket(-1, "");
			}
		}
	}

	/**
	 * Tells an application whether or not a file transfer has been requested.
	 */
	public String requestingFileTransfer() {
		return requestedFile;
	}
	
	/**
	 * Packet Receiver sets this based on connection packets from receiver
	 * @param sizeOfWindow
	 */
	public void setWindowSize(int sizeOfWindow) {
		packetCreator.setWindowSize(sizeOfWindow);
	}
	
	/**
	 * Called when a file transfer request comes in.
	 * @throws FileNotFoundException 
	 */
	public void setFileTransferRequest(String fileName) throws FileNotFoundException {
		requestedFile = fileName;
		packetCreator.setFileName(fileName);
	}

}
