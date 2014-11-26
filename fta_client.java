import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;


public class fta_client {
	private static PacketReceiverForClient receiver;
	
	public static void main(String[] args) throws IOException {
		// Must be even number port
		int localPortNumber = Integer.parseInt(args[0]);
		InetAddress ipAddressOfNetEmu = InetAddress.getByName(args[1]);
		int udpPortNumberOfNetEmu = Integer.parseInt(args[2]);
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

}
