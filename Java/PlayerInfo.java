import java.io.IOException;
import java.math.BigInteger;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;

	class PlayerInfo
	{
		static final int
		MAX_PLAYER_NAME_LENGTH = 32,
		MAX_CUSTOM_FILES = 4, //each is a C++ long(4 bytes)
		SIGNED_GUID_LEN = 32+1, // Hashed CD Key (32 hex alphabetic chars + "0 terminator" )
		SIZE = 140;
		static final BigInteger UINT64MAX = BigInteger.valueOf(2).pow(64).subtract(BigInteger.valueOf(1)); //(2^64)-1
		int index;
		
		BigInteger xUID; // network xuid - uint64 		
		String name; // scoreboard information
		int	userID; // local server user ID, unique while server is running		
		String gUID; // global unique player identifer	
		long	friendsID; // friends identification number - uint32
		String friendsName; // friends name		
		boolean fakePlayer; // true, if player is a bot controlled by game.dll		
		boolean isHLTV; // true if player is the HLTV proxy
		//		boolean	isReplay; // true if player is the Replay proxy	
		long[] customFiles = new long[MAX_CUSTOM_FILES]; // custom files CRC for this player - CRC32_t(uint32)		
		byte filesDownloaded; // this counter increases each time the server downloaded a new file - unsigned char
		
		short slotId;

		PlayerInfo(ByteString itemData) throws IOException{
			CodedInputStream cis = itemData.newCodedInput();

			xUID = BigInteger.valueOf(cis.readFixed64()).and(UINT64MAX);
			name = ( new String (cis.readRawBytes(MAX_PLAYER_NAME_LENGTH), "UTF-8") ).trim();
			userID = cis.readFixed32();
			gUID = (new String (cis.readRawBytes(SIGNED_GUID_LEN), "UTF-8") ).trim();
			cis.skipRawBytes(3);
			friendsID = cis.readFixed32() & 0xffffffffL;
			friendsName = (new String (cis.readRawBytes(MAX_PLAYER_NAME_LENGTH), "UTF-8") ).trim();
			fakePlayer = cis.readBool();
			isHLTV = cis.readBool(); //115 bytes read up to this point
			cis.skipRawBytes(2);
			cis.skipRawBytes(4*MAX_CUSTOM_FILES); //customFiles
			filesDownloaded = cis.readRawByte(); //last byte
			cis.skipRawBytes(3);
		}

		public String toString(){
			return String.format("    xuid:%-20d name:%-32s userID:%-10d guid:%-20s friendsID:%-10d friendsName:%s fakeplayer:%-5s ishltv:%-5s filesDownloaded:%d",
					xUID, name, userID, gUID, friendsID, friendsName, fakePlayer, isHLTV, filesDownloaded);
		}
	}