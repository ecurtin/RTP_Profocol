package shared;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;


/**
 * A PacketCreator is responsible for creating all packets sent by a client or server.
 *
 */
public class PacketCreator {

	private int maxDataSizePerPacketInBytes = 832;
	
	protected PacketSender packetSender;
	
	protected String fileName;
	protected FileInputStream fileStream;
	protected byte[] fileData = new byte[maxDataSizePerPacketInBytes];
	protected final int INITIAL_ACK = -1;
	protected int currentSeqNumber = 0;
	protected final int TIMEOUT_SIZE = 250; // ms

	protected InetAddress destinationAddress;
	protected InetAddress sourceAddress;
	protected int windowSize;
	protected int destinationPort;
	protected int sourcePort;
	
	protected Timer timeout = new Timer();

	protected boolean isConnected = false;
	protected ConcurrentHashMap<Integer, DatagramPacket> sentPacketStore 
		= new ConcurrentHashMap<Integer, DatagramPacket>();
	
	protected ArrayList<Integer> timeoutPackets = new ArrayList<Integer>();
	
	public PacketCreator(DatagramSocket socket, int sourcePort, InetAddress sourceAddress) throws SocketException {
		this.packetSender = new PacketSender(socket);
		this.sourcePort = sourcePort;
		this.sourceAddress = sourceAddress;
	}
	
	/**
	 * Creates and sends connection ACK
	 * @throws IOException 
	 */
	public void sendConnectionPacket(int windowSize, String fileName) throws IOException {
		DatagramPacket sendingPacket = createConnectPacket(windowSize, fileName);
		sentPacketStore.put(INITIAL_ACK, sendingPacket);
		packetSender.sendPacket(sendingPacket);
		timeout.schedule(new ConnectTask(), TIMEOUT_SIZE);
	}
	
	// Creates connection packet
	private DatagramPacket createConnectPacket(int windowSize, String fileName) {
		Packet connectionPacket = new ConnectionPacket();
		connectionPacket.setSourceIPAddress(sourceAddress);
		connectionPacket.setSourcePort(sourcePort);
		connectionPacket.setDestinationIPAddress(destinationAddress);
		connectionPacket.setDestinationPort(destinationPort);
		connectionPacket.setSeqNumber(INITIAL_ACK);
		//connectionPacket.setChecksum(connectionPacket.computeChecksum());
		
		System.out.println("WINDOW SIZE: " + windowSize);
		if (windowSize > 0) {
			connectionPacket.setWindowSize(windowSize);
			connectionPacket.setFileName(fileName);
		}
		
		DatagramPacket sendingPacket = connectionPacket.packInUDP();
		
		storePacket(INITIAL_ACK, sendingPacket);
		timeoutPackets.add(INITIAL_ACK);
		return sendingPacket;
	}
	
	protected boolean storageContainsPacket(Integer key) {
		return sentPacketStore.containsKey(key);
	}
	
	public void removePacketFromStorage(Integer key) {
		sentPacketStore.remove(key);
	}
	
	protected DatagramPacket getPacketFromStorage(Integer key) {
		return sentPacketStore.get(key);
	}
	
	protected void storePacket(Integer seqNumber, DatagramPacket storedPacket) {
		sentPacketStore.put(seqNumber, storedPacket);
	}
	
	protected boolean isConnectionACK(int ackNumber) {
		return ackNumber == -1;
	}
	

	public void setDestinationAddress(InetAddress destinationAddress) {
		this.destinationAddress = destinationAddress;
		
	}

	public void setFileName(String fileName) throws FileNotFoundException {
		File file = new File(fileName.trim());
		System.out.println("Confirmed file: " + file.getAbsolutePath());
		fileStream = new FileInputStream(file);
	}
	
	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	public void setDestinationPort(int port) {
		this.destinationPort = port;
	}
	
	public boolean isConnected() {
		return isConnected;
	}
	
	/**
	 * Responsible for running the connect timeout task. It checks whether or 
	 * not the connection packet has been received. If it has not, we re-send.
	 *
	 */
	class ConnectTask extends TimerTask {

		@Override
		public void run() {
			try {
				while(!haveConnectionACK()) {
					Thread.sleep(TIMEOUT_SIZE);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			timeoutPackets.clear();
			System.out.println("TIMEOUT THREAD ENDED");
		}
		
		// Determines if we received the connection ACK
		private boolean haveConnectionACK() throws IOException {
			boolean haveConnection = true;
			DatagramPacket resentPacket = null;
			
			if (!timeoutPackets.isEmpty()) {
				int seqNumber = timeoutPackets.get(0);
				System.out.println("TimeoutPacket Seq Num: " + seqNumber);
				
				if (storageContainsPacket(seqNumber)) {
					haveConnection = false;
					resentPacket = getPacketFromStorage(seqNumber);
					packetSender.sendPacket(resentPacket);
				}
			}
			
			return haveConnection;
		}
	}

	public void clearTimeoutPacket() {
		timeoutPackets.clear();
	}
}
