import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.xerial.snappy.Snappy;

import com.generated_code.DotaUsermessages.CDOTAUserMsg_ChatEvent;
import com.google.protobuf.CodedInputStream;

enum Type {
	DAMAGE, HEALING, MODIFIER_GAIN, MODIFIER_LOSS, DEATH, AEGIS
}

public class Handler{

	static void tstSnappy() throws UnsupportedEncodingException, IOException{
		String input = "Hello snappy-java! Snappy-java is a JNI-based wrapper of Snappy, a fast compresser/decompresser.";

		byte[] compressed = Snappy.compress(input.getBytes("UTF-8"));
		String result = new String(compressed, "UTF-8");
		System.out.println(result);

		byte[] uncompressed = Snappy.uncompress(compressed);
		result = new String(uncompressed, "UTF-8");
		System.out.println(result);

	}

	static void printDemo() throws IOException{
		RandomAccessFile fp = new RandomAccessFile("spectre_tst.dem", "r");
		byte[] buf = new byte[7];
		System.out.println(fp.getFilePointer());
		fp.read(buf);

		String pb = new String(buf);
		System.out.println(pb);
		System.out.println(fp.getFilePointer());
		System.out.println(fp.readInt());
		System.out.println(fp.getFilePointer());
		System.out.println(pb.equals("PBUFDEM"));
	}

	static void cis() throws IOException{
		RandomAccessFile fp = new RandomAccessFile("spectre_tst.dem", "r");
		byte[] buf = new byte[(int) fp.length()];
		fp.read(buf);
		CodedInputStream cis = CodedInputStream.newInstance(buf);
		System.out.println(new String(cis.readRawBytes(7)));
		cis.skipRawBytes(4);
		System.out.println(cis.getTotalBytesRead());
	}

	//	public static Kernel32 INSTANCE = (Kernel32) Native.loadLibrary("Kernel32", Kernel32.class);

	static Type[] types = Type.values();
	static DemoFileDump demoDump;
	public static void main(String[] args) throws IOException{
		//		tstSnappy();
		//		printDemo();
		//		cis();

		Stopwatch s = new Stopwatch();
		demoDump = new DemoFileDump();

		args = new String[2];
		args[0] = "21907249.dem";

		if(args.length < 1){
			System.out.println("Usage: " + Handler.class.getSimpleName() + ".jar filename.dem");
			System.exit(0);
		}
		s.start();
		if(demoDump.open(args[0]))
			demoDump.doDump();
		s.stop();

		double dumpTime = s.time();

		s.start();
		combatLog();
		s.stop();
		//				kd();
		double surfTime = s.time();
		System.out.println("Dump Time: "+dumpTime);
		System.out.println("Surf Time: "+surfTime);
	}

	private static void kd(){
		Player[] teams = demoDump.teams;
		for(CDOTAUserMsg_ChatEvent heroKill : demoDump.kdHeroList){
			teams[heroKill.getPlayerid1()].deaths++;

			if(heroKill.getPlayerid3()==-1 && heroKill.getValue()!=0)
				teams[heroKill.getPlayerid2()].kills++;
			else{
				System.out.println(heroKill);
				if(heroKill.getPlayerid2()!=-1) teams[heroKill.getPlayerid2()].assists++;
				if(heroKill.getPlayerid3()!=-1) teams[heroKill.getPlayerid3()].assists++;
				if(heroKill.getPlayerid4()!=-1) teams[heroKill.getPlayerid4()].assists++;
				if(heroKill.getPlayerid5()!=-1) teams[heroKill.getPlayerid5()].assists++;
				if(heroKill.getPlayerid6()!=-1) teams[heroKill.getPlayerid6()].assists++;
			}
		}

		demoDump.getGameInfo().genScoreBoard(teams);
		System.out.println(demoDump.getGameInfo());

	}

	private static void combatLog() throws IOException{
		ArrayList<String> combatLogNames = demoDump.getCombatLogNames();
		HashMap<Short, Player> players = demoDump.getPlayers();
		Player[] teams = demoDump.teams;
		int heroTargets = 0;
		int deaths = 0;
		int kills = 0;
		final int ASSIST_TLIMIT = 20;
		final int holyPersuasion = combatLogNames.indexOf("modifier_chen_holy_persuasion");
		int trollWarlord = combatLogNames.indexOf("npc_dota_neutral_dark_troll_warlord");
		short skeletonWarrior = (short) combatLogNames.indexOf("npc_dota_dark_troll_warlord_skeleton_warrior");
		short cheni = (short) combatLogNames.indexOf("npc_dota_hero_chen");
		Player chen = players.get(cheni);
		Player warlock = players.get((short)combatLogNames.indexOf("npc_dota_hero_warlock"));
		Player enchantress = players.get((short)combatLogNames.indexOf("npc_dota_hero_enchantress"));
		final int enchant = combatLogNames.indexOf("modifier_enchantress_enchant");
		HashSet<Short> chensMinions = new HashSet<Short>();
		HashSet<Short> enchantsMinions = new HashSet<Short>();


		System.out.println("\nCombat Log Names: "+combatLogNames.size());
		System.out.println("Combat Events: "+demoDump.getCombatEvents().size());
		boolean denied = false;
		for(CombatEvent combatEvent : demoDump.getCombatEvents()){
			Player target = players.get(combatEvent.targetName);
			Player attacker = players.get(combatEvent.attackerSourceName);

			if(target!=null && attacker!=null) denied = attacker.team.equals(target.team);
			// && players.get(combatEvent.attackerName)!=null
			switch(types[combatEvent.type]){
			case DAMAGE: //if(attacker!=null && target!=null)heroTargets++; 
				if(target!=null && !combatEvent.targetIllusion){
					if(attacker!=null && !denied){								
						if(combatEvent.health!=0)
							target.deathAssist[attacker.slotID]=combatEvent;
					}
					else if(chen!=null && chensMinions.contains(combatEvent.attackerName)){
						if(combatEvent.health!=0)
							target.deathAssist[chen.slotID]=combatEvent;
						denied = chen.team.equals(target.team);
					}
					else if(warlock!=null && combatLogNames.get(combatEvent.attackerName).contains("warlock_golem")){
						if(combatEvent.health!=0)
							target.deathAssist[warlock.slotID]=combatEvent;
						denied = warlock.team.equals(target.team);
					}
					else if(enchantress!=null && enchantsMinions.contains(combatEvent.attackerName)){
						if(combatEvent.health!=0)
							target.deathAssist[enchantress.slotID]=combatEvent;
						denied = enchantress.team.equals(target.team);
					}
					else if(combatLogNames.get(combatEvent.attackerName).contains("tower")){
						denied = false;
					}
				}

				break;
			case MODIFIER_GAIN: 
				if(combatEvent.inflictorName==holyPersuasion){
					chensMinions.add(combatEvent.targetName);
					if(combatEvent.targetName==trollWarlord)
						chensMinions.add(skeletonWarrior);

				}
				if(combatEvent.inflictorName==enchant){
					enchantsMinions.add(combatEvent.targetName);
					if(combatEvent.targetName==trollWarlord)
						enchantsMinions.add(skeletonWarrior);

				}
				break;
			case DEATH:	
				if(target!=null){ //target is a hero
					if(target.aegis==false){
						deaths++; 
						target.deaths++;

						if(combatEvent.targetIllusion){
							System.out.println("illusions deaths shouldn't count towards a hero's kills or deaths");
							System.exit(0);
						}

						if(attacker!=null && !denied){//attacker is a hero
							kills++;
							attacker.kills++;
							target.deathAssist[attacker.slotID]=null;
						}
						else if(chen!=null && chensMinions.contains(combatEvent.attackerName)  && !denied){
							kills++;
							chen.kills++;
							target.deathAssist[chen.slotID]=null;
						}
						else if(enchantress!=null && enchantsMinions.contains(combatEvent.attackerName)  && !denied){
							kills++;
							enchantress.kills++;
							target.deathAssist[enchantress.slotID]=null;
						}
						else if(warlock!=null && combatLogNames.get(combatEvent.attackerName).contains("warlock_golem")  && !denied){
							kills++;
							warlock.kills++;
							target.deathAssist[warlock.slotID]=null;
						}	
						else if(combatLogNames.get(combatEvent.attackerName).contains("tower")){
							int start, end, i = 0, assistsCount=0;
							if(target.slotID < 5){ start=5; end=10;} else {start=0; end=5;}
							for(int iAttacker=start; iAttacker<end; iAttacker++){
								CombatEvent[] assistEvent = target.deathAssist;
								if(assistEvent[iAttacker]!=null && combatEvent.timeStamp - assistEvent[iAttacker].timeStamp<=ASSIST_TLIMIT){
									assistsCount++;
									i = iAttacker;
								}
							}
							if(assistsCount==1){
								kills++;
								teams[i].kills++;
								target.deathAssist[i]=null;
							}
						}							
						else{
							System.out.println(combatLogNames.get(combatEvent.attackerName)+" -> "+combatLogNames.get(combatEvent.targetName)+" at "+combatEvent.timeStamp/60);
							System.out.println(combatEvent);
						}

						boolean skRessurection = target.hero.equals("Skeleton King") && demoDump.resurrectionList.contains( (int)(combatEvent.timeStamp)+3 );
						boolean isNeutral = combatLogNames.get(combatEvent.attackerName).contains("neutral");
						boolean isCreep = combatLogNames.get(combatEvent.attackerName).contains("creep"); //!"npc_dota_creep_neutral"?
						boolean isGolem = combatLogNames.get(combatEvent.attackerName).contains("warlock_golem");
						boolean isTower = combatLogNames.get(combatEvent.attackerName).contains("tower");

						//if(!isCreep && !isTower)
						if(!denied){
							int start, end;
							if(target.slotID < 5){ start=5; end=10;} else {start=0; end=5;}
							for(int iAttacker=start; iAttacker<end; iAttacker++){
								if(target.deathAssist[iAttacker]!=null && combatEvent.timeStamp - target.deathAssist[iAttacker].timeStamp<=ASSIST_TLIMIT){
									teams[iAttacker].assists++;
									if(teams[iAttacker].hero.equals("Vengefulspirit")){
										//System.out.print(teams[iAttacker].hero+": ");
										System.out.println(combatLogNames.get(combatEvent.attackerName)+" -> "+combatLogNames.get(combatEvent.targetName)+" at "+combatEvent.timeStamp/60);
									}
								}
							}
						}
						else
							System.out.println(combatLogNames.get(combatEvent.attackerName)+" -> "+combatLogNames.get(combatEvent.targetName)+" at "+combatEvent.timeStamp/60);

						if(skRessurection){
							deaths--; 
							target.deaths--;
							kills--;
							attacker.kills--;

							int begin, stop;
							if(target.slotID < 5){ begin=5; stop=10;} else {begin=0; stop=5;}
							for(int iAttacker=begin; iAttacker<stop; iAttacker++){
								if(target.deathAssist[iAttacker]!=null && combatEvent.timeStamp - target.deathAssist[iAttacker].timeStamp<=ASSIST_TLIMIT)
									teams[iAttacker].assists--;
							}
						}
					}
					else
						target.aegis = false;
				} break;
			case AEGIS: teams[combatEvent.playerId].aegis = true; break;
			}
		}

		System.out.println("Damaged Hero Targets: " + heroTargets );
		System.out.println("Total Kills: "+ kills);
		System.out.println("Total Deaths: "+ deaths);
		System.out.println("\nCombatLogNames HeroIDs: " + players.keySet());
		System.out.println();

		demoDump.getGameInfo().genScoreBoard(teams);

		//		RandomAccessFile file = new RandomAccessFile("results.txt", "rws");
		//		file.writeUTF(demoDump.getGameInfo().toString());
		DataOutputStream file = new DataOutputStream(new FileOutputStream("results.txt"));
		file.write(demoDump.getGameInfo().toString().getBytes("UTF-8"));

		DataOutputStream out = new DataOutputStream(System.out);
		out.write(demoDump.getGameInfo().toString().getBytes("UTF-8"));
		System.out.println();
		//		out.close();
	}

	private static void addAssists(){

	}
}