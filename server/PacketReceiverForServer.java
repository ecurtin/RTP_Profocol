package server;
//import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
//import java.net.SocketException;
//import java.util.Queue;

import shared.Packet;
import shared.PacketReceiver;



/**
 * This class is responsible for interfacing between an application and our 
 * SENDER implementation of a reliable transport protocol.
 *
 */
public class PacketReceiverForServer extends PacketReceiver {
	private String requestedFile = null;
	private boolean isTerminated = false;
	
	public PacketReceiverForServer(int sourcePort, InetAddress destinationAddress, 
			int destinationPort) throws IOException {
		// SETUP SERVER
		DatagramSocket socket = new DatagramSocket(sourcePort);
		InetAddress sourceAddress = InetAddress.getLocalHost();
		
		this.packetCreator = new PacketCreatorForServer(socket, sourcePort, sourceAddress);
		packetCreator.setDestinationAddress(destinationAddress);
		packetCreator.setDestinationPort(destinationPort);
		
		// Size of allowed packet data
		byte[] receiveData = new byte[PACKET_SIZE];
		
		while(!isTerminated) {
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			socket.receive(receivePacket);
			
			// Translate datagram packet into something RTP understands
			Packet packet = new Packet(receivePacket);
			//packet.makeRTPPacket();
			System.out.println("");
			System.out.println("-------------------------------------");
			System.out.println("received packet");
			System.out.println("Is valid: "+packet.validateChecksum());
			System.out.println("Is ack: " + packet.isACK());
			System.out.println("Is connection: " + packet.isConnection());

			// Can either be a data ACK or finalizing Connection
			if (packet.isACK()) {
				int ACKNumber = packet.getACK();
				((PacketCreatorForServer) packetCreator).receiveACK(ACKNumber);
				
			// Connection Packet (receive file name to transfer)
			} else if (packet.isConnection()) {
				String fileName = packet.getFileName();
				System.out.println("Requested File: " + fileName);
				setFileTransferRequest(fileName);
				setWindowSize(packet.getWindowSize());
				System.out.println("Calling for a connection packet to be sent");
				packetCreator.sendConnectionPacket(-1, "");
				
			}
			if (((PacketCreatorForServer) packetCreator).doneSending()) {
				isTerminated = true;
				((PacketCreatorForServer) packetCreator).sendDisconnectPackets();
				
			}
		}
		System.exit(0);
	}
	
	public void terminate() {
		isTerminated = true;
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
