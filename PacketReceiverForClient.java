//import java.io.BufferedReader;
//import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
//import java.net.SocketException;
//import java.net.UnknownHostException;
import java.util.HashMap;

/**
 * This class is responsible for interfacing between an application and our 
 * RECEIVER implementation of a reliable transport protocol.
 *
 */
public class PacketReceiverForClient extends PacketReceiver {
	private boolean isDisconnected = false;
	private final int INITIAL_ACK = -1;
	private FileOutputStream fileStream = null;
	private HashMap<Integer, byte[]> dataStore = new HashMap<Integer, byte[]>();
	
	public PacketReceiverForClient(int sourcePort, InetAddress destinationAddress, 
			int destinationPort, String fileName, int windowSize) throws IOException {
		// SETUP CLIENT
		fileStream = new FileOutputStream(fileName, true);
		DatagramSocket socket = new DatagramSocket(sourcePort);
		InetAddress sourceAddress = InetAddress.getLocalHost();
		this.packetCreator = new PacketCreatorForClient(socket, sourcePort, sourceAddress);

		packetCreator.sendConnectionPacket(windowSize, fileName);
		
		while(!isDisconnected) {
			// Size of allowed packet data
			byte[] receiveData = new byte[PACKET_SIZE];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			
			// Translate datagram packet into something RTP understands
			Packet packet = new Packet(receivePacket);
			
			// Error Check
			if (packet.validateChecksum()) {
				
				// Send acknowledgment of packets retrieval
				int ackNumber = packet.getSeqNumber();
				((PacketCreatorForClient) packetCreator).sendACK(ackNumber);
				
				// Can either be a data ACK or finalizing Connection
				if (packet.isData()) {
					// Store file data to be parsed after receiving entire file
					dataStore.put(ackNumber, packet.getData());

				} else if (packet.isConnection()) {
					packetCreator.removePacketFromStorage(INITIAL_ACK);
					
				} else if (packet.isDisconnection()) {
					isDisconnected = true;
				}
			}
		}
		// Put all the data pieces into a file
		int lengthOfData = dataStore.size();
		for (int i = 0; i < lengthOfData; i++) {
			fileStream.write(dataStore.get(i));
		}
		fileStream.close();
	}
}
