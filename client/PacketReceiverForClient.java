package client;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

import shared.Packet;
import shared.PacketReceiver;

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
		String fileLocation = fileName.trim();
		String localLocationSetup = provideLocalPath(fileLocation);
		File transferredFile = new File(localLocationSetup);
		fileStream = new FileOutputStream(transferredFile);
		
		DatagramSocket socket = new DatagramSocket(sourcePort);
		InetAddress sourceAddress = InetAddress.getLocalHost();
		this.packetCreator = new PacketCreatorForClient(socket, sourcePort, sourceAddress);
		this.packetCreator.setDestinationAddress(destinationAddress);
		this.packetCreator.setDestinationPort(destinationPort);
		
		System.out.println("sending packet");
		packetCreator.sendConnectionPacket(windowSize, fileLocation);
		
		while(!isDisconnected) {
			// Size of allowed packet data
			byte[] receiveData = new byte[PACKET_SIZE];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			
			socket.receive(receivePacket);
			
			// Translate datagram packet into something RTP understands
			Packet packet = new Packet(receivePacket);
			//packet.makeRTPPacket();
			System.out.println();
			System.out.println("---------------------------------");
			// Error Check
			System.out.println("Received Valid packet: "+ packet.validateChecksum());
			System.out.println("Received Connection packet: " + packet.isConnection());
			System.out.println("Received Data packet: " + packet.isData());
			if (packet.validateChecksum()) {
				
				// Send acknowledgment of packets retrieval
				int seqNumber = packet.getSeqNumber();
				System.out.println("Sending ACK..." + seqNumber);
				((PacketCreatorForClient) packetCreator).sendACK(seqNumber);
				
				// Can either be a data ACK or finalizing Connection
				 if (packet.isConnection()) {
						System.out.println("Removing Packet from storage: " + INITIAL_ACK);
						packetCreator.removePacketFromStorage(INITIAL_ACK);
						packetCreator.clearTimeoutPacket();
						
				 }else if (packet.isData()) {
					// Store file data to be parsed after receiving entire file
					System.out.println("Packet data size: " + packet.getData().length);
					dataStore.put(seqNumber, packet.getData());
					System.out.println("RECEIVING DATA - SEQ NUMBER: " + seqNumber);
					
				} else if (packet.isDisconnection()) {
					isDisconnected = true;
					System.out.println("DISCONNECTING");
				}
			}
		}
		// Put all the data pieces into a file
		int lengthOfDataStore = dataStore.size();
		for (int i = 0; i < lengthOfDataStore; i++) {
			System.out.println("size of written packet data: " + dataStore.get(i).length);
			fileStream.write(dataStore.get(i));
		}
		fileStream.close();
	}

	private String provideLocalPath(String fileLocation) {
		String localPath = null;
		
		if (fileLocation.contains("/")) {
			int locationOfDirectoryMark = fileLocation.lastIndexOf("/");
			localPath = fileLocation.substring(locationOfDirectoryMark + 1);
		}
		
		return localPath;
	}
}
