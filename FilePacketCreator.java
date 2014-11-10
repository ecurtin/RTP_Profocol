import java.io.File;


/**
 * PacketCreator is responsible for taking a file, serializing it, then 
 * breaking the serialized object into packets, placing them in a "sending 
 * packet buffer".
 *
 */
public class FilePacketCreator {
	private File file;
	
	public FilePacketCreator(File file) {
		this.file = file;
	}
}
