import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;


public class fta_server {
	private static PacketReceiverForServer receiver;
	private static boolean isTerminated = false;
	public static void main(String[] args) throws IOException {
		// Has to be odd
		int localPortNumber = Integer.parseInt(args[0]);
		InetAddress ipAddressOfNetEmu = InetAddress.getByName(args[1]);
		int udpPortNumberOfNetEmu = Integer.parseInt(args[2]);
		
		receiver = new PacketReceiverForServer(localPortNumber, ipAddressOfNetEmu, udpPortNumberOfNetEmu);
		
		while (!isTerminated) {
			BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Enter command: ");
			
			String command = buffer.readLine();
			
			if (command.equals("terminate")) {
				receiver.terminate();
				isTerminated = true;
			}
		}
		System.exit(0);
	}

}
