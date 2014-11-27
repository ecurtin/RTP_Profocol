package client;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;


public class fta_client {
	private static PacketReceiverForClient receiver;
	
	public static void main(String[] args) throws IOException {
		// Must be even number port
		if (args.length < 3) {
			printUsageAndExit();
		}
		
		int localPortNumber = 0;
		InetAddress ipAddressOfNetEmu = null;
		int udpPortNumberOfNetEmu = 0;
		
		try {
			localPortNumber = Integer.parseInt(args[0]);
			ipAddressOfNetEmu = InetAddress.getByName(args[1]);
			udpPortNumberOfNetEmu = Integer.parseInt(args[2]);
		}
		catch (Exception e) {
			printUsageAndExit();
		}
		
		int windowSize = 0;
		
		while (true) {
			BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Enter command: ");
			
			String command = buffer.readLine();
			
			if (command.substring(0, 6).equals("window")) {
				windowSize = Integer.parseInt(command.substring(7));
				
			} else if (command.substring(0, 11).equals("connect-get")) {
				String fileName = command.substring(12);
				if (windowSize < 1) {
					System.out.println("Please enter a valid window size.");
				} else {
					receiver = new PacketReceiverForClient(localPortNumber, ipAddressOfNetEmu, 
							udpPortNumberOfNetEmu, fileName, windowSize);
				}
			}
		}
	}
	
	public static void printUsageAndExit() {
		System.out.println("Usage: fta_client [LocalPort] [NetEmuAddress] [NetEmuPort]");
		System.exit(0);
	}

}
