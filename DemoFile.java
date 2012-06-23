import org.xerial.snappy.Snappy;

import com.generated_code.Demo.*;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

import java.io.IOException;
import java.io.RandomAccessFile;

public class DemoFile{	

//	private static final int DEMOFILE_FULLPACKETS_VERSION = 2;
	private CodedInputStream m_fileBuffer;

	boolean Open(final String name) throws IOException{
		final String PROTODEMO_HEADER_ID = "PBUFDEM";
		final int
		DEMO_FILE_STAMP = PROTODEMO_HEADER_ID.length(),
		FILE_INFO_OFFSET = 5, //'\0' terminator + int
		SIZE_OF_DOTA_DEMO_HEADER = DEMO_FILE_STAMP+FILE_INFO_OFFSET;

		RandomAccessFile fp = new RandomAccessFile(name, "r");
		int length = (int) fp.length();

		if(length < SIZE_OF_DOTA_DEMO_HEADER){
			System.out.println("CDemoFile::Open: file too small." + name);
			return false;
		}

		byte[] fileStamp = new byte[DEMO_FILE_STAMP];
		fp.read(fileStamp);

		if(! new String(fileStamp).equals(PROTODEMO_HEADER_ID)){
			System.out.println("CDemoFile::Open: demofilestamp doesn't match " + name);
			return false;
		}

		fp.skipBytes(FILE_INFO_OFFSET);
		length -= SIZE_OF_DOTA_DEMO_HEADER;

		byte[] buf = new byte[length];
		fp.read(buf);

		m_fileBuffer = CodedInputStream.newInstance(buf);

		if(length==0){
			System.out.println("CDemoFile::Open: couldn't open file " + name);
			return false;
		}

		return true;
	}

	EDemoCommands readMessageType(DemoMessage metaData) throws IOException{
		int cmd = m_fileBuffer.readRawVarint32();

		metaData.compressed = (cmd & EDemoCommands.DEM_IsCompressed_VALUE) != 0;
		cmd = (cmd & ~EDemoCommands.DEM_IsCompressed_VALUE);
		metaData.tick = m_fileBuffer.readRawVarint32(); 

		if(isDone())
			return EDemoCommands.DEM_Error;

		return EDemoCommands.valueOf(cmd);
	}

	boolean readMessage(DemoMessage message, EDemoCommands cmd) throws IOException{
		int size = m_fileBuffer.readRawVarint32();
		byte[] buffer;

		message.size = size;

		if(isDone())
			return false;

		buffer = m_fileBuffer.readRawBytes(size);

		if(message.compressed && Snappy.isValidCompressedBuffer(buffer)){
			buffer = Snappy.uncompress(buffer);
			message.uncompressed_size = buffer.length;
		}
		else
			message.uncompressed_size = 0;

		message.msg = getMessage(cmd, buffer);
		if(message.msg==null){
			System.err.println("CDemoFile::ReadMessage() failed");
			return false;
		}

		return true;
	}

	private Message getMessage(EDemoCommands demoCommand, byte[] buffer) throws InvalidProtocolBufferException, IOException{
		Message msg;

		switch(demoCommand){
		case DEM_FileHeader: msg = CDemoFileHeader.parseFrom(buffer); break;
		case DEM_FileInfo: msg = CDemoFileInfo.parseFrom(buffer); break;
		case DEM_Stop: msg = CDemoStop.parseFrom(buffer); break;
		case DEM_SyncTick: msg = CDemoSyncTick.parseFrom(buffer); break;
		case DEM_ConsoleCmd: msg = CDemoConsoleCmd.parseFrom(buffer); break;
		case DEM_SendTables: msg = CDemoSendTables.parseFrom(buffer); break;
		case DEM_ClassInfo: msg = CDemoClassInfo.parseFrom(buffer); break;
		case DEM_StringTables: msg = CDemoStringTables.parseFrom(buffer); break;
		case DEM_UserCmd: msg = CDemoUserCmd.parseFrom(buffer); break;
		case DEM_CustomDataCallbacks: msg = CDemoCustomDataCallbacks.parseFrom(buffer); break;
		case DEM_CustomData: msg = CDemoCustomData.parseFrom(buffer); break;
		case DEM_FullPacket: msg = CDemoFullPacket.parseFrom(buffer); break;
		case DEM_Packet:
		case DEM_SignonPacket: msg = CDemoPacket.parseFrom(buffer); break;
		default: msg = null;
		}

		return msg;
	}

	boolean isDone() throws IOException{
		return m_fileBuffer.isAtEnd();
	}
}