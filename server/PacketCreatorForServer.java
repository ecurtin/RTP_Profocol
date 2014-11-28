package server;
//import java.io.File;
import java.io.FileInputStream;
//import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
//import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
//import java.util.Timer;
import java.util.TimerTask;
//import java.util.concurrent.ConcurrentHashMap;


import shared.DataPacket;
import shared.DisconnectionPacket;
import shared.Packet;
import shared.PacketCreator;


/**
 * PacketCreator is responsible for creating all packets sent.
 *
 */
public class PacketCreatorForServer extends PacketCreator {
	private boolean moreDataPackets = true;
	private boolean disconnected = false;
	
	
	public PacketCreatorForServer(DatagramSocket socket, int sourcePort, InetAddress sourceAddress) throws SocketException {
		super(socket, sourcePort, sourceAddress);
	}
	
	/**
	 * Deals with all incoming ACKs, removing the associated buffered packages 
	 * from the store.
	 * @param ackNumber
	 * @throws IOException 
	 */
	public void receiveACK(int ackNumber) throws IOException {
		// Check for packet that we received ACK for
		if (storageContainsPacket(ackNumber)) {
			removePacketFromStorage(ackNumber);
			System.out.println("RECEIVED ACK for:" + ackNumber);
			System.out.println("Removing packet " + ackNumber + " from storage.");
			// Begin sending file if ACK was for connection
			if (!isConnected) {
				timeoutPackets.clear();
				System.out.println("~~~~~~~~~~~~~~~~~~~~~");
				System.out.println("We are now connected.");
				System.out.println("~~~~~~~~~~~~~~~~~~~~~");
				isConnected = true;
				sendWindowOfPackets();
			}
		}
	}
	
	// Create all file packets and begin to send them a window size at a time to the receiver.
	/**
	 * @throws IOException 
	 */
	private void sendWindowOfPackets() throws IOException {
		Queue<DatagramPacket> packetsQueue = (Queue<DatagramPacket>) createFilePackets();
		int sizeOfQueue = packetsQueue.size();
		DatagramPacket[] packetsToBeSent = new DatagramPacket[sizeOfQueue];
		
		System.out.println("Created some packets...");
		
		for(int i = 0; i < sizeOfQueue; i++) {
			packetsToBeSent[i] = packetsQueue.remove();
		};
		
		packetSender.sendPackets(packetsToBeSent);
		System.out.println("Sent those packets.");
		timeout.schedule(new TimeoutTask(), TIMEOUT_SIZE);
	}
	
	// Creates a window size of file packets and places them in the returning queue
	private Queue<DatagramPacket> createFilePackets() throws IOException {
		Queue<DatagramPacket> packetsQueue = new LinkedList<DatagramPacket>(); //changed this from null
		int numberOfPacketsCreated = 0;
		System.out.println("Window size:" + windowSize);
		System.out.println("More data: " + moreFileDataToPacketize(fileStream));
		while (numberOfPacketsCreated != windowSize && moreFileDataToPacketize(fileStream)) {
			// Store as much data from the file as you can into fileData
			DataPacket dataPacket;
			
			if (fileStream.available() < fileData.length) {
				byte[] lastPartOfFile = new byte[fileStream.available()];
				fileStream.read(lastPartOfFile, 0, lastPartOfFile.length);
				dataPacket = new DataPacket(lastPartOfFile);
			} else {
				fileStream.read(fileData, 0, fileData.length);
				dataPacket = new DataPacket(fileData);
			}
			
			System.out.println("Sending packet with data size of: " + dataPacket.getData().length);
			
			// Set all of the header values
			dataPacket.setSeqNumber(currentSeqNumber);
			dataPacket.setSourceIPAddress(sourceAddress);
			dataPacket.setSourcePort(sourcePort);
			dataPacket.setDestinationIPAddress(destinationAddress);
			dataPacket.setDestinationPort(destinationPort);
			DatagramPacket sendPacket = dataPacket.packInUDP();
			
			// Add data packet to list of sent packets and storage
			timeoutPackets.add(currentSeqNumber);
			sentPacketStore.put(currentSeqNumber, sendPacket);
			System.out.println();
			System.out.println("TimeoutPackets:");
			System.out.println(timeoutPackets.toString());
			numberOfPacketsCreated++;
			currentSeqNumber++;
			packetsQueue.add(sendPacket);
		}
		if (!moreFileDataToPacketize(fileStream)) {
			moreDataPackets = false;
			fileStream.close();
		}
		return packetsQueue;
	}
	
	private boolean moreFileDataToPacketize(FileInputStream fileStream) throws IOException {
		return fileStream.available() != 0;
	}
	
	
	public void sendDisconnectPackets() throws IOException, InterruptedException {
		Packet disconnectPacket = new DisconnectionPacket();
		
		// Add appropriate values to disconnect packet header
		disconnectPacket.setSourceIPAddress(sourceAddress);
		disconnectPacket.setSourcePort(sourcePort);
		disconnectPacket.setDestinationIPAddress(destinationAddress);
		disconnectPacket.setDestinationPort(destinationPort);
		disconnectPacket.setSeqNumber(currentSeqNumber);
		DatagramPacket udpDisconnectPacket = disconnectPacket.packInUDP();
		
		timeoutPackets.add(currentSeqNumber);
		sentPacketStore.put(currentSeqNumber, udpDisconnectPacket);
		currentSeqNumber++;
		try{
			packetSender.sendPacket(udpDisconnectPacket);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		timeout.schedule(new DisconnectTask(), TIMEOUT_SIZE*2);
	}
	
	/**
	 * Responsible for performing all tasks after timeout is complete.
	 *
	 */
	class TimeoutTask extends TimerTask {
		@Override
		public void run() {
			try {
				while(!allACK()) {
					Thread.sleep(TIMEOUT_SIZE);
				}
				// If all are ACK'd send next window size of data packets
				if (moreDataPackets) {
					sendWindowOfPackets();
				} else {
					System.out.println("SENDING DISCONNECT FROM TIMEOUT");
					sendDisconnectPackets();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// Determines which packets have not been ACK'd
		private boolean allACK() throws IOException {
			boolean isDone = true;
			int numberOfTimeoutPackets = timeoutPackets.size();
			Queue<DatagramPacket> resentPacketsQueue = new LinkedList<DatagramPacket>();

			System.out.println();
			System.out.println("Checking acks in timeout...");
			System.out.println("Number of Timeout packets: " + numberOfTimeoutPackets);
			
			for (int i = 0; i < numberOfTimeoutPackets; i++) {
				// Get the packet sequence numbers sent
				int seqNumber = timeoutPackets.get(i);
				
				System.out.println("Checking seq number: " + seqNumber);
				
				// Packet has not been ack'd
				if (storageContainsPacket(seqNumber)) {
					resentPacketsQueue.add(getPacketFromStorage(seqNumber));
					System.out.println("Resending Packet: " + seqNumber);
					isDone = false;
				}
			}
			// Re-send all non-ack'd packets
			if (!isDone) {
				int numberOfPacketsToBeResent = resentPacketsQueue.size();
				DatagramPacket[] resentPackets = new DatagramPacket[numberOfPacketsToBeResent];
				for (int i = 0; i < numberOfPacketsToBeResent; i++) {
					resentPackets[i] = resentPacketsQueue.remove();
				}
				packetSender.sendPackets(resentPackets);
			} else {
				timeoutPackets.clear();
			}
			return isDone;
		}
	}
	
	public boolean receivedDisconnectACK() {
		return disconnected;
	}
	
	/**
	 * Responsible for running the disconnect timeout task. It checks whether or 
	 * not the disconnect ACK has been received. If it has not, we resend
	 *
	 */
	class DisconnectTask extends TimerTask {

		@Override
		public void run() {
			try {
				while(!disconnectACK()) {
					Thread.sleep(TIMEOUT_SIZE);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			timeoutPackets.clear();
			disconnected = true;			
		}
		
		// Determines if we received the disconnect ACK
		private boolean disconnectACK() throws IOException {
			boolean ackReceived = false;
			DatagramPacket resentPacket = null;
			int seqNumber = timeoutPackets.get(0);
			
			if (storageContainsPacket(seqNumber)) {
				ackReceived = true;
				resentPacket = getPacketFromStorage(seqNumber);
				packetSender.sendPacket(resentPacket);
			}
			
			return ackReceived;
		}
	}
	
}
