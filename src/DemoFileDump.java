
import com.generated_code.Demo.CDemoFileInfo;
import com.generated_code.Demo.CDemoFullPacket;
import com.generated_code.Demo.CDemoPacket;
import com.generated_code.Demo.CDemoStringTables;
import com.generated_code.Demo.CDemoStringTables.items_t;
import com.generated_code.Demo.CDemoStringTables.table_t;
import com.generated_code.Demo.CGameInfo.CDotaGameInfo;
import com.generated_code.Demo.CGameInfo.CDotaGameInfo.CPlayerInfo;
import com.generated_code.DotaModifiers.CDOTAModifierBuffTableEntry;
import com.generated_code.Netmessages.CSVCMsg_CreateStringTable;
import com.generated_code.Netmessages.CSVCMsg_GameEventList;
import com.generated_code.DotaUsermessages.*;
import com.generated_code.Usermessages.*;
import com.generated_code.Netmessages.*;
import com.generated_code.Demo.EDemoCommands;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

public class DemoFileDump{

	private DemoFile m_demoFile;
	private CSVCMsg_GameEventList m_GameEventList;
	private ArrayList<String> combatLogNames;
	private ArrayList<String> modifierNames;
	private ArrayList<CombatEvent> combatEvents;
	public HashSet<Integer> resurrectionList;
	private HashMap<String, PlayerInfo> userInfo;
	private HashMap<Short, Player> players;
	public Player[] teams;
	private int m_nFrameNumber;
	public ArrayList<CDOTAUserMsg_ChatEvent> kdHeroList;
	int reincarnation;
	private GameInfo gameInfo;
	private String combatSummary;
	private String tmp;
	private int prevID;
	private float[] heroGold;
	private float lastGameTime;

	DemoFileDump(){
		m_demoFile = new DemoFile();
		combatLogNames = new ArrayList<String>();
		modifierNames = new ArrayList<String>();
		combatEvents = new ArrayList<CombatEvent>();
		resurrectionList = new HashSet<Integer>();
		userInfo = new HashMap<String, PlayerInfo>();
		players = new HashMap<Short, Player>();
		teams = new Player[10];
		kdHeroList =  new ArrayList<CDOTAUserMsg_ChatEvent>();
		gameInfo = new GameInfo();
		combatSummary = "\nCOMBAT SUMMARY\n";
		tmp = "";
		prevID = 0;
		heroGold = new float[64];
	}

	ArrayList<String> getCombatLogNames(){
		return combatLogNames;
	}

	ArrayList<CombatEvent> getCombatEvents(){
		return combatEvents;
	}

	HashMap<Short, Player> getPlayers(){
		return players;
	}

	GameInfo getGameInfo(){
		return gameInfo;
	}

	boolean open(final String filename) throws IOException{
		if(!m_demoFile.Open(filename)){
			System.out.println("Couldn't open " + filename);
			return false;
		}
		return true;
	}

	void doDump() throws IOException{
		boolean bStopReading = false;
		DemoMessage message = new DemoMessage();
		CDemoFullPacket fullPacket = null;

		for(m_nFrameNumber = 0; !bStopReading && !m_demoFile.isDone(); m_nFrameNumber++){
			//						if(m_nFrameNumber==8)
			//							bStopReading = true;

			EDemoCommands cmd = m_demoFile.readMessageType(message);
			m_demoFile.readMessage(message, cmd);

			switch(cmd){			
			case DEM_FileHeader: break;
			case DEM_FileInfo: handleGameInfo((CDemoFileInfo) message.msg); break;
			case DEM_Stop: break;
			case DEM_SyncTick: break;
			case DEM_ConsoleCmd: break;
			case DEM_SendTables: break;
			case DEM_ClassInfo: break;
			case DEM_StringTables: spewStringTables((CDemoStringTables) message.msg); break;
			case DEM_UserCmd: break;
			case DEM_CustomDataCallbacks: break;
			case DEM_CustomData:break;

			case DEM_FullPacket: {
				//					printDemoHeader(cmd, message.tick, message.size, message.uncompressed_size);
				fullPacket = (CDemoFullPacket) message.msg;	
				dumpDemoStringTable(fullPacket.getStringTable()); // Spew the stringtable
				dumpDemoPacket(fullPacket.getPacket().getData()); // Ok, now the packet.
			}break;

			case DEM_Packet:
			case DEM_SignonPacket: {
				//					printDemoHeader(cmd, message.tick, message.size, message.uncompressed_size);
				CDemoPacket packet = (CDemoPacket) message.msg;
				dumpDemoPacket(packet.getData());
			}break;

			default:
			case DEM_Error:
				bStopReading = true;
				//				fatal_errorf( "Shouldn't ever get this demo command?!? %d\n", demoCommand );
				break;		  			
			}
			//			addEntry(message.msg);
		}
	}

	void dumpDemoPacket(ByteString buf) throws IOException{
		CodedInputStream dataBuffer = buf.newCodedInput();

		while(!dataBuffer.isAtEnd()){
			int cmd = dataBuffer.readRawVarint32();
			SVC_Messages SVC_cmd = SVC_Messages.valueOf(cmd);
			NET_Messages net_cmd = NET_Messages.valueOf(cmd);
			int size = dataBuffer.readRawVarint32();
			byte[] parseBuffer = dataBuffer.readRawBytes(size);

			if(net_cmd!=null)
				switch(net_cmd){
				case net_NOP: printNetMessage(CNETMsg_NOP.parseFrom(parseBuffer)); break;
				case net_Disconnect: printNetMessage(CNETMsg_Disconnect.parseFrom(parseBuffer)); break;
				case net_File: printNetMessage(CNETMsg_File.parseFrom(parseBuffer)); break;
				case net_SplitScreenUser: printNetMessage(CNETMsg_SplitScreenUser.parseFrom(parseBuffer)); break;
				case net_Tick: printNetMessage(CNETMsg_Tick.parseFrom(parseBuffer)); break;
				case net_StringCmd: printNetMessage(CNETMsg_StringCmd.parseFrom(parseBuffer)); break;
				case net_SetConVar: printNetMessage(CNETMsg_SetConVar.parseFrom(parseBuffer)); break;
				case net_SignonState: printNetMessage(CNETMsg_SignonState.parseFrom(parseBuffer)); break;					
				default:
					System.out.println("WARNING. dumpDemoPacket(): Unknown netmessage " + net_cmd); break;
				}
			else
				switch(SVC_cmd){
				case svc_ServerInfo: printNetMessage(CSVCMsg_ServerInfo.parseFrom(parseBuffer)); break;
				case svc_SendTable: printNetMessage(CSVCMsg_SendTable.parseFrom(parseBuffer)); break;
				case svc_ClassInfo: printNetMessage(CSVCMsg_ClassInfo.parseFrom(parseBuffer)); break;
				case svc_SetPause: printNetMessage(CSVCMsg_SetPause.parseFrom(parseBuffer)); break;
				case svc_CreateStringTable: printNetMessage(CSVCMsg_CreateStringTable.parseFrom(parseBuffer)); break;
				case svc_UpdateStringTable: printNetMessage(CSVCMsg_UpdateStringTable.parseFrom(parseBuffer)); break;
				case svc_VoiceInit: printNetMessage(CSVCMsg_VoiceInit.parseFrom(parseBuffer)); break;
				case svc_VoiceData: printNetMessage(CSVCMsg_VoiceData.parseFrom(parseBuffer)); break;
				case svc_Print: printNetMessage(CSVCMsg_Print.parseFrom(parseBuffer)); break;
				case svc_Sounds: printNetMessage(CSVCMsg_Sounds.parseFrom(parseBuffer)); break;
				case svc_SetView: printNetMessage(CSVCMsg_SetView.parseFrom(parseBuffer)); break;
				case svc_FixAngle: printNetMessage(CSVCMsg_FixAngle.parseFrom(parseBuffer)); break;
				case svc_CrosshairAngle: printNetMessage(CSVCMsg_CrosshairAngle.parseFrom(parseBuffer)); break;
				case svc_BSPDecal: printNetMessage(CSVCMsg_BSPDecal.parseFrom(parseBuffer)); break;
				case svc_SplitScreen: printNetMessage(CSVCMsg_SplitScreen.parseFrom(parseBuffer)); break;
				case svc_UserMessage: dumpUserMessage(CSVCMsg_UserMessage.parseFrom(parseBuffer)); break;
				//misssing		case svc_EntityMessage: printNetMessage(.parseFrom(parseBuffer)); break;
				case svc_GameEvent: printGameEvent(CSVCMsg_GameEvent.parseFrom(parseBuffer)); break;
				case svc_PacketEntities: printNetMessage(CSVCMsg_PacketEntities.parseFrom(parseBuffer)); break;
				case svc_TempEntities: printNetMessage(CSVCMsg_TempEntities.parseFrom(parseBuffer)); break;
				case svc_Prefetch: printNetMessage(CSVCMsg_Prefetch.parseFrom(parseBuffer)); break;
				case svc_Menu: printNetMessage(CSVCMsg_Menu.parseFrom(parseBuffer)); break;
				case svc_GameEventList: printNetMessage(CSVCMsg_GameEventList.parseFrom(parseBuffer)); break;
				case svc_GetCvarValue: printNetMessage(CSVCMsg_GetCvarValue.parseFrom(parseBuffer)); break;
				default: System.out.println("WARNING. dumpDemoPacket(): Unknown netmessage " + SVC_cmd); break;	
				}
		}
	}

	void printNetMessage(Message msg) throws IOException{
		String msgType = msg.getClass().getSimpleName();

		if(msgType.equals("CSVCMsg_GameEventList"))
			m_GameEventList = (CSVCMsg_GameEventList) msg;		

		//		msgPrintf(msgType, msg.getSerializedSize(), msg);
	}

	void dumpUserMessage(CSVCMsg_UserMessage msg) {
		int cmd = msg.getMsgType();
		EBaseUserMessages baseUsr_cmd = EBaseUserMessages.valueOf(cmd);
		EDotaUserMessages dotaUsr_cmd = EDotaUserMessages.valueOf(cmd);
		ByteString parseBuffer = msg.getMsgData();
		Message userMsg = null;
		try{
			if(baseUsr_cmd!=null)
				switch(baseUsr_cmd){
				case UM_AchievementEvent: userMsg = CUserMsg_AchievementEvent.parseFrom(parseBuffer); break;
				case UM_CloseCaption: userMsg = CUserMsg_CloseCaption.parseFrom(parseBuffer); break;
				//missing		case UM_CloseCaptionDirect: userMsg = CUserMsg_CloseCaptionDirect.parseFrom(parseBuffer); break;
				case UM_CurrentTimescale: userMsg = CUserMsg_CurrentTimescale.parseFrom(parseBuffer); break;
				case UM_DesiredTimescale: userMsg = CUserMsg_DesiredTimescale.parseFrom(parseBuffer); break;
				case UM_Fade: userMsg = CUserMsg_Fade.parseFrom(parseBuffer); break;
				case UM_GameTitle: userMsg = CUserMsg_GameTitle.parseFrom(parseBuffer); break;
				case UM_Geiger: userMsg = CUserMsg_Geiger.parseFrom(parseBuffer); break;
				case UM_HintText: userMsg = CUserMsg_HintText.parseFrom(parseBuffer); break;
				case UM_HudMsg: userMsg = CUserMsg_HudMsg.parseFrom(parseBuffer); break;
				case UM_HudText: userMsg = CUserMsg_HudText.parseFrom(parseBuffer); break;
				case UM_KeyHintText: userMsg = CUserMsg_KeyHintText.parseFrom(parseBuffer); break;
				case UM_MessageText: userMsg = CUserMsg_MessageText.parseFrom(parseBuffer); break;
				case UM_RequestState: userMsg = CUserMsg_RequestState.parseFrom(parseBuffer); break;
				case UM_ResetHUD: userMsg = CUserMsg_ResetHUD.parseFrom(parseBuffer); break;
				case UM_Rumble: userMsg = CUserMsg_Rumble.parseFrom(parseBuffer); break;
				case UM_SayText: userMsg = CUserMsg_SayText.parseFrom(parseBuffer); break;
				case UM_SayText2: userMsg = CUserMsg_SayText2.parseFrom(parseBuffer); break;
				case UM_SayTextChannel: userMsg = CUserMsg_SayTextChannel.parseFrom(parseBuffer); break;
				case UM_Shake: userMsg = CUserMsg_Shake.parseFrom(parseBuffer); break;
				case UM_ShakeDir: userMsg = CUserMsg_ShakeDir.parseFrom(parseBuffer); break;
				case UM_StatsCrawlMsg: userMsg = CUserMsg_StatsCrawlMsg.parseFrom(parseBuffer); break;
				case UM_StatsSkipState: userMsg = CUserMsg_StatsSkipState.parseFrom(parseBuffer); break;
				case UM_TextMsg: userMsg = handleTextMsg(CUserMsg_TextMsg.parseFrom(parseBuffer)); break;
				case UM_Tilt: userMsg = CUserMsg_Tilt.parseFrom(parseBuffer); break;
				case UM_Train: userMsg = CUserMsg_Train.parseFrom(parseBuffer); break;
				case UM_VGUIMenu: userMsg = CUserMsg_VGUIMenu.parseFrom(parseBuffer); break;
				case UM_VoiceMask: userMsg = CUserMsg_VoiceMask.parseFrom(parseBuffer); break;
				case UM_VoiceSubtitle: userMsg = CUserMsg_VoiceSubtitle.parseFrom(parseBuffer); break;
				case UM_SendAudio: userMsg = CUserMsg_SendAudio.parseFrom(parseBuffer); break;
				default: System.out.println("WARNING. dumpUserMessage(): Unknown netmessage " + cmd); break;	
				}
			else
				switch(dotaUsr_cmd){
				//missing			case DOTA_UM_AddUnitToSelection: userMsg = CDOTAUserMsg_AddUnitToSelection.parseFrom(parseBuffer); break;
				case DOTA_UM_AIDebugLine: userMsg = CDOTAUserMsg_AIDebugLine.parseFrom(parseBuffer); break;
				case DOTA_UM_ChatEvent: userMsg = handleChatMsg(CDOTAUserMsg_ChatEvent.parseFrom(parseBuffer)); break; //if(type.equals("CHAT_MESSAGE_AEGIS")
				case DOTA_UM_CombatHeroPositions: userMsg = CDOTAUserMsg_CombatHeroPositions.parseFrom(parseBuffer); break;
				//				case DOTA_UM_CombatLogData: userMsg = CDOTAUserMsg_CombatLogData.parseFrom(parseBuffer); break;
				//missing			case DOTA_UM_CombatLogName: userMsg = CDOTAUserMsg_CombatLogName.parseFrom(parseBuffer); break;
				case DOTA_UM_CombatLogShowDeath: userMsg = CDOTAUserMsg_CombatLogShowDeath.parseFrom(parseBuffer); break;
				case DOTA_UM_CreateLinearProjectile: userMsg = CDOTAUserMsg_CreateLinearProjectile.parseFrom(parseBuffer); break;
				case DOTA_UM_DestroyLinearProjectile: userMsg = CDOTAUserMsg_DestroyLinearProjectile.parseFrom(parseBuffer); break;
				case DOTA_UM_DodgeTrackingProjectiles: userMsg = CDOTAUserMsg_DodgeTrackingProjectiles.parseFrom(parseBuffer); break;
				case DOTA_UM_GlobalLightColor: userMsg = CDOTAUserMsg_GlobalLightColor.parseFrom(parseBuffer); break;
				case DOTA_UM_GlobalLightDirection: userMsg = CDOTAUserMsg_GlobalLightDirection.parseFrom(parseBuffer); break;
				case DOTA_UM_InvalidCommand: userMsg = CDOTAUserMsg_InvalidCommand.parseFrom(parseBuffer); break;
				case DOTA_UM_LocationPing: userMsg = CDOTAUserMsg_LocationPing.parseFrom(parseBuffer); break;
				case DOTA_UM_MapLine: userMsg = CDOTAUserMsg_MapLine.parseFrom(parseBuffer); break;
				case DOTA_UM_MiniKillCamInfo: userMsg = CDOTAUserMsg_MiniKillCamInfo.parseFrom(parseBuffer); break;
				case DOTA_UM_MinimapDebugPoint: userMsg = CDOTAUserMsg_MinimapDebugPoint.parseFrom(parseBuffer); break;
				case DOTA_UM_MinimapEvent: userMsg = CDOTAUserMsg_MinimapEvent.parseFrom(parseBuffer); break;
				case DOTA_UM_NevermoreRequiem: userMsg = CDOTAUserMsg_NevermoreRequiem.parseFrom(parseBuffer); break;
				case DOTA_UM_OverheadEvent: userMsg = handleOverheadEvent(CDOTAUserMsg_OverheadEvent.parseFrom(parseBuffer)); break;
				case DOTA_UM_SetNextAutobuyItem: userMsg = CDOTAUserMsg_SetNextAutobuyItem.parseFrom(parseBuffer); break;
				case DOTA_UM_SharedCooldown: userMsg = CDOTAUserMsg_SharedCooldown.parseFrom(parseBuffer); break;
				case DOTA_UM_SpectatorPlayerClick: userMsg = CDOTAUserMsg_SpectatorPlayerClick.parseFrom(parseBuffer); break;
				case DOTA_UM_TutorialTipInfo: userMsg = CDOTAUserMsg_TutorialTipInfo.parseFrom(parseBuffer); break;
				case DOTA_UM_UnitEvent: userMsg = handleUnitEvent(CDOTAUserMsg_UnitEvent.parseFrom(parseBuffer)); break;
				case DOTA_UM_ParticleManager: userMsg = CDOTAUserMsg_ParticleManager.parseFrom(parseBuffer); break;
				case DOTA_UM_BotChat: userMsg = CDOTAUserMsg_BotChat.parseFrom(parseBuffer); break;
				case DOTA_UM_HudError: userMsg = CDOTAUserMsg_HudError.parseFrom(parseBuffer); break;
				case DOTA_UM_ItemPurchased: userMsg = CDOTAUserMsg_ItemPurchased.parseFrom(parseBuffer); break;
				case DOTA_UM_Ping: userMsg = CDOTAUserMsg_Ping.parseFrom(parseBuffer); break;
				case DOTA_UM_ItemFound: userMsg = CDOTAUserMsg_ItemFound.parseFrom(parseBuffer); break;	
				default: System.out.println("WARNING. dumpUserMessage(): Unknown netmessage " + cmd); System.exit(0);break;	
				}
		}catch (InvalidProtocolBufferException e){
			System.err.printf("Could not parse message %s because enum constant type %d does not exist.\n", dotaUsr_cmd, parseBuffer.byteAt(1));
			//			e.printStackTrace();
			System.exit(0);
		}
		//		msgPrintf(userMsg.getClass().getSimpleName(), userMsg.getSerializedSize(), userMsg);
	}

	Message handleChatMsg(CDOTAUserMsg_ChatEvent msg){
		switch(msg.getType()){
		case CHAT_MESSAGE_AEGIS: combatEvents.add(new CombatEvent((short) msg.getPlayerid1())); break;
		case CHAT_MESSAGE_HERO_KILL: kdHeroList.add(msg); break;
//		case CHAT_MESSAGE_REPORT_REMINDER:gameInfo.setStartTime(lastGameTime); break;
		}
		return msg;
	}

	Message handleUnitEvent(CDOTAUserMsg_UnitEvent msg){
		switch(msg.getMsgType()){
		case DOTA_UNIT_SPEECH:
			if(msg.getSpeech().getResponse().contains("battle_begin")) gameInfo.setStartTime(lastGameTime);
			if(msg.getSpeech().getResponse().contains("victory")) gameInfo.setEndTime(combatEvents.get(combatEvents.size()-1).timeStamp);
			break;
		}
		return msg;
	}

	Message handleTextMsg(CUserMsg_TextMsg msg){

		if(combatSummary.contains("COMBAT SUMMARY"))
			combatSummary += tmp;

		if(!msg.getParam(0).equals("\nCOMBAT SUMMARY\n"))
			tmp = msg.getParam(0);

		return msg;
	}
	
	Message handleOverheadEvent(CDOTAUserMsg_OverheadEvent msg){
		switch(msg.getMessageType()){
		case OVERHEAD_ALERT_GOLD: heroGold[msg.getTargetPlayerEntindex()]+=msg.getValue(); break;
		case OVERHEAD_ALERT_XP: break;
		}
		return msg;
	}

	void printGameEvent(CSVCMsg_GameEvent msg) throws InvalidProtocolBufferException{
		Object[] field = null;
		int iDescriptor;
		
		for(iDescriptor = 0; iDescriptor < m_GameEventList.getDescriptorsCount(); iDescriptor++ ){
			CSVCMsg_GameEventList.descriptor_t descriptor = m_GameEventList.getDescriptors(iDescriptor);

			if(descriptor.getEventid() == msg.getEventid())
				break;
		}

		if(iDescriptor == m_GameEventList.getDescriptorsCount()){ // ?
			System.out.print(msg.toString());
		}
		else
		{			
			int numKeys = msg.getKeysCount();
			CSVCMsg_GameEventList.descriptor_t descriptor = m_GameEventList.getDescriptors(iDescriptor);
			boolean isChaseHero = descriptor.getName().equals("dota_chase_hero");
			boolean isCombatLog = descriptor.getName().equals("dota_combatlog");
			if(isCombatLog) field =  new Object[11];
			Object v = null;

			//			System.out.printf( "%s eventid:%d %s\n", descriptor.getName(), msg.getEventid(),
			//					msg.hasEventid() ? msg.getEventName() : "" );

			for( int i = 0; i < numKeys; i++ )
			{
				CSVCMsg_GameEventList.key_t key = descriptor.getKeys(i);
				CSVCMsg_GameEvent.key_t keyValue = msg.getKeys(i);
				if(isChaseHero) lastGameTime = keyValue.getValFloat();
				//				System.out.printf(" %s: ", key.getName());

				if( keyValue.hasValString() )		v = keyValue.getValString();
				else if( keyValue.hasValFloat() )	v = keyValue.getValFloat();
				else if( keyValue.hasValLong() )	v = keyValue.getValLong();
				else if( keyValue.hasValShort() )	v = keyValue.getValShort();
				else if( keyValue.hasValByte() )	v = keyValue.getValByte();
				else if( keyValue.hasValBool())		v = keyValue.getValBool();
				else if( keyValue.hasValUint64() )	v = keyValue.getValUint64();

				//				System.out.println(v);

				if(isCombatLog) field[i] = v;
			}
			if(isCombatLog) combatEvents.add(new CombatEvent(field));
		}
	}

	void dumpDemoStringTable(CDemoStringTables stringTables) throws IOException{
		int itemID = 0;
		for(table_t table : stringTables.getTablesList())
			for(items_t item : table.getItemsList())
				switch(table.getTableName()){
				case "ActiveModifiers":
					CDOTAModifierBuffTableEntry entry =  CDOTAModifierBuffTableEntry.parseFrom(item.getData());
					if(entry.getName()==reincarnation)	resurrectionList.add((int) entry.getCreationTime());
					break;
				case "CombatLogNames":
					itemID++;
					if(itemID>prevID){
						combatLogNames.add(item.getStr());
						prevID = itemID;
					}
					break;
				}
	}

	void spewStringTables(CDemoStringTables stringTables) throws IOException{
		int index = -1;
		for(table_t table : stringTables.getTablesList()){
			for(items_t item : table.getItemsList())
				switch(table.getTableName()){
				case "ModifierNames": modifierNames.add(item.getStr()); break;
				case "userinfo": 
					if(item.getData().size() == PlayerInfo.SIZE){
						PlayerInfo playerInfo = new PlayerInfo(item.getData());
						index += playerInfo.name.equals("SourceTV") ? 2 : 1;
						playerInfo.index = index;
						userInfo.put(playerInfo.name, playerInfo);
					}
					break;
				}
		}
		reincarnation = modifierNames.indexOf("modifier_skeleton_king_reincarnation");
	}

	void handleGameInfo(CDemoFileInfo msg) throws UnsupportedEncodingException{
		short i = 0;
		float timedGold = gameInfo.gameLength/0.8f;
		byte hWidth = 0;
		byte pWidth = 0;
		byte tmpWidth;
		short heroIndx;

		CDotaGameInfo demoInfo = msg.getGameInfo().getDota();
		
		for( CPlayerInfo pi : demoInfo.getPlayerInfoList() ){
			String heroName = pi.getHeroName();
			heroIndx = (short) combatLogNames.indexOf(heroName);
			
			String[] words = heroName.replaceFirst("npc_dota_hero_", "").split("_");
			heroName = "";
			for(String w : words) heroName += w.replaceFirst(""+w.charAt(0), ""+(char)(w.charAt(0)-32))+" "; 
			heroName = heroName.trim();
			
			tmpWidth = (byte) heroName.length();
			if(tmpWidth>hWidth) hWidth = tmpWidth;
			
			String playerName = pi.getPlayerName();
			tmpWidth = (byte) playerName.length();
			if(tmpWidth>pWidth) pWidth = tmpWidth;
			
//			System.out.println(i+"=>"+user.index);
			PlayerInfo user = userInfo.get(playerName);
			Player p = new Player();
			p.gUID = user!=null ? user.gUID : "";
			p.name = playerName;
			p.gold = user!=null ? heroGold[user.index]+timedGold : -1;
			p.hero = heroName;
			p.slotID = i;
			p.team = i<5 ? "Radiant" : "Dire";
//System.out.println(p.gold/(gameInfo.gameLength/60f));

			players.put(heroIndx, p);			
			teams[i++] = p;
		}
		for(Player p: teams) p.setStrFmt(pWidth, hWidth);
		gameInfo.setGameInfo(msg.getPlaybackTime(), demoInfo.getMatchId(), demoInfo.getGameMode(), demoInfo.getGameWinner());
		gameInfo.fmtHeader(pWidth, hWidth);
	}

	void printDemoHeader(EDemoCommands demoCommand, int tick, int size, int uncompressed_size){
		System.out.printf( "==== #%d: Tick:%d '%s' Size:%d UncompressedSize:%d ====\n", m_nFrameNumber, tick, demoCommand, size, uncompressed_size);

	}

	void msgPrintf(String typeName, int size, Message msg){
		System.out.printf( "---- %s (%d bytes) -----------------\n", typeName, size );
		System.out.print(msg);
	}

	void addEntry(Message message){
		System.out.println(message.getDescriptorForType().getName());
		for(Entry<FieldDescriptor, Object> e : message.getAllFields().entrySet()){
			System.out.println(e.getKey().getName());
			System.out.println(e.getValue());
		}
	}
}