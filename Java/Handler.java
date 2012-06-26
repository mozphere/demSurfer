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

/**
 * @author mozphere
 *
 */
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
	static DataOutputStream file;
	static final DataOutputStream out = new DataOutputStream(System.out);
	static final String nl = System.lineSeparator();
	static int n = 0;
	public static void main(String[] args) throws IOException{
		//		tstSnappy();
		//		printDemo();
		//		cis();

//		args = new String[1];
//		args[0] = "19472796.dem";

		String fileName = "ERROR";
		if(args.length==0){
			System.out.println("Usage: " + Handler.class.getSimpleName() + ".jar filename.dem");
			System.exit(0);
		}
		else if(args.length==1)
			fileName = args[0];
		else
			fileName = "results";

		file = new DataOutputStream(new FileOutputStream(fileName+".txt", true));
		for(String arg : args){
			Stopwatch s = new Stopwatch();
			demoDump = new DemoFileDump();

			s.start();
			if(demoDump.open(arg))
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
			file.write((nl+"Dump Time: "+dumpTime+nl).getBytes("UTF-8"));
			file.write(("Surf Time: "+surfTime+nl).getBytes("UTF-8"));
		}
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
		final ArrayList<String> combatLogNames = demoDump.getCombatLogNames();
		final HashMap<Short, Player> players = demoDump.getPlayers();
		final Player[] teams = demoDump.teams;
		int heroTargets = 0;
		int deaths = 0;
		int kills = 0;
		final int ASSIST_TLIMIT = 20;

		final short helmOfDominator;
		final short trollWarlord = (short) combatLogNames.indexOf("npc_dota_neutral_dark_troll_warlord");
		final short skeletonWarrior = (short) combatLogNames.indexOf("npc_dota_dark_troll_warlord_skeleton_warrior");
		final short enragedWildkin = (short) combatLogNames.indexOf("npc_dota_neutral_enraged_wildkin");
		final short tornado = (short) combatLogNames.indexOf("npc_dota_enraged_wildkin_tornado");
		final Player chen = players.get((short) combatLogNames.indexOf("npc_dota_hero_chen"));
		final short holyPersuasion = (short) combatLogNames.indexOf("modifier_chen_holy_persuasion");
		final Player enchantress = players.get((short)combatLogNames.indexOf("npc_dota_hero_enchantress"));
		final short enchant = (short) combatLogNames.indexOf("modifier_enchantress_enchant");
		final Player brewmaster = players.get((short)combatLogNames.indexOf("npc_dota_hero_brewmaster"));
		final Player warlock = players.get((short)combatLogNames.indexOf("npc_dota_hero_warlock"));
		final Player broodmother = players.get((short)combatLogNames.indexOf("npc_dota_hero_broodmother"));
		final Player venomancer = players.get((short)combatLogNames.indexOf("npc_dota_hero_venomancer"));
		final Player enigma = players.get((short)combatLogNames.indexOf("npc_dota_hero_enigma"));
		final Player beastmaster = players.get((short)combatLogNames.indexOf("npc_dota_hero_beastmaster"));
		final Player treantProtector;
		final Player Morphling;
		//druid?
		//weaver?
		//furion?
		//ivoker


		System.out.println("\nCombat Log Names: "+combatLogNames.size());
		System.out.println("Combat Events: "+demoDump.getCombatEvents().size());
		boolean denied = false;
		for(CombatEvent combatEvent : demoDump.getCombatEvents()){
			Player target = players.get(combatEvent.targetName);
			Player attacker = players.get(combatEvent.attackerSourceName);
			String attackerName = combatLogNames.get(combatEvent.attackerName);
			
			boolean isNeutral = attackerName.contains("creep_neutral");
			boolean isCreep = attackerName.contains("creep_g") || attackerName.contains("creep_b") || attackerName.contains("siege");
			boolean isTower = attackerName.contains("tower");
			boolean isGolem = attackerName.contains("warlock_golem");
			boolean isPanda = attackerName.contains("brewmaster");
			boolean isSpider = attackerName.contains("broodmother_spider");
			boolean isPlagueWard = attackerName.contains("plague_ward");
			boolean isEidolon = attackerName.contains("eidolon");
			boolean isBoar = attackerName.contains("boar");

			if(target!=null && attacker!=null) denied = attacker.team.equals(target.team);
			switch(types[combatEvent.type]){
			case DAMAGE: //if(attacker!=null && target!=null)heroTargets++; 
				if(target!=null && !combatEvent.targetIllusion){

					if(attacker!=null && !denied){								
						if(combatEvent.health!=0)
							target.damagedBy[attacker.slotID]=combatEvent;
					}
					else if(chen!=null && chen.minions.contains(combatEvent.attackerName)){
						if(combatEvent.health!=0)
							target.damagedBy[chen.slotID]=combatEvent;
						denied = chen.team.equals(target.team);
					}
					else if(enchantress!=null && enchantress.minions.contains(combatEvent.attackerName)){
						if(combatEvent.health!=0)
							target.damagedBy[enchantress.slotID]=combatEvent;
						denied = enchantress.team.equals(target.team);
					}
					else if(isGolem){
						if(combatEvent.health!=0)
							target.damagedBy[warlock.slotID]=combatEvent;
						denied = warlock.team.equals(target.team);
					}
					else if(isPanda){
						if(combatEvent.health!=0)
							target.damagedBy[brewmaster.slotID]=combatEvent;
						denied = brewmaster.team.equals(target.team);
					}
					else if(isSpider){
						if(combatEvent.health!=0)
							target.damagedBy[broodmother.slotID]=combatEvent;
						denied = broodmother.team.equals(target.team);
					}
					else if(isPlagueWard){
						if(combatEvent.health!=0)
							target.damagedBy[venomancer.slotID]=combatEvent;
						denied = venomancer.team.equals(target.team);
					}
					else if(isEidolon){
						if(combatEvent.health!=0)
							target.damagedBy[enigma.slotID]=combatEvent;
						denied = enigma.team.equals(target.team);
					}
					else if(isBoar){
						if(combatEvent.health!=0)
							target.damagedBy[beastmaster.slotID]=combatEvent;
						denied = beastmaster.team.equals(target.team);
					}
					else if(isTower || isCreep){
						denied = false;
					}
					else{
//						System.out.println(attackerName+" -> "+combatLogNames.get(combatEvent.targetName)+" at "+combatEvent.timeStamp/60);
//						System.out.println(combatEvent);
					}
				}

				break;
			case MODIFIER_GAIN: 
				if(combatEvent.inflictorName==holyPersuasion){
					chen.minions.add(combatEvent.targetName);
					if(combatEvent.targetName==trollWarlord)
						chen.minions.add(skeletonWarrior);
					if(combatEvent.targetName==enragedWildkin)
						chen.minions.add(tornado);

				}
				if(combatEvent.inflictorName==enchant){
					enchantress.minions.add(combatEvent.targetName);
					if(combatEvent.targetName==trollWarlord)
						enchantress.minions.add(skeletonWarrior);
					if(combatEvent.targetName==enragedWildkin)
						enchantress.minions.add(tornado);
				}
				break;
			case DEATH:	
				if(target!=null){ //target is a hero
					if(target.aegisTimeStamp==-1 || (combatEvent.timeStamp - target.aegisTimeStamp) > 600f){ //10mins *60
						deaths++; 
						target.deaths++;

						if(combatEvent.targetIllusion){
							System.out.println("illusions deaths shouldn't count towards a hero's kills or deaths");
							System.exit(0);
						}

						if(attacker!=null && !denied){//attacker is a hero
							kills++;
							attacker.kills++;
							target.damagedBy[attacker.slotID]=null;
						}
						else if(chen!=null && chen.minions.contains(combatEvent.attackerName)  && !denied){
							kills++;
							chen.kills++;
							target.damagedBy[chen.slotID]=null;
						}
						else if(enchantress!=null && enchantress.minions.contains(combatEvent.attackerName)  && !denied){
							kills++;
							enchantress.kills++;
							target.damagedBy[enchantress.slotID]=null;
						}
						else if(isGolem  && !denied){
							kills++;
							warlock.kills++;
							target.damagedBy[warlock.slotID]=null;
						}
						else if(isPanda  && !denied){
							kills++;
							brewmaster.kills++;
							target.damagedBy[brewmaster.slotID]=null;

						}
						else if(isSpider && !denied){
							kills++;
							broodmother.kills++;
							target.damagedBy[broodmother.slotID]=null;
						}
						else if(isPlagueWard && !denied){
							kills++;
							venomancer.kills++;
							target.damagedBy[venomancer.slotID]=null;
						}
						else if(isEidolon && !denied){
							kills++;
							enigma.kills++;
							target.damagedBy[enigma.slotID]=null;
						}
						else if(isBoar && !denied){
							kills++;
							beastmaster.kills++;
							target.damagedBy[beastmaster.slotID]=null;
						}
						else if(isTower || isCreep){
							int start, end, i = 0, assistsCount=0;
							if(target.slotID < 5){ start=5; end=10;} else {start=0; end=5;}
							for(int iAttacker=start; iAttacker<end; iAttacker++){
								CombatEvent[] assistEvent = target.damagedBy;
								if(assistEvent[iAttacker]!=null && combatEvent.timeStamp - assistEvent[iAttacker].timeStamp<=ASSIST_TLIMIT){
									assistsCount++;
									i = iAttacker;
								}
							}
							if(assistsCount==1){								
								kills++;
								teams[i].kills++;
								target.damagedBy[i]=null;
							}
						}							
						else if(denied){
							//grant deny achievement
						}
						else{// should be neutral --> hero
							System.out.println(attackerName+" -> "+combatLogNames.get(combatEvent.targetName)+" at "+combatEvent.timeStamp/60);
							System.out.println(combatEvent);
						}

						//if(!isCreep && !isTower)
						if(!denied){
							int start, end;
							if(target.slotID < 5){ start=5; end=10;} else {start=0; end=5;}
							for(int iAttacker=start; iAttacker<end; iAttacker++){
								if(target.damagedBy[iAttacker]!=null && combatEvent.timeStamp - target.damagedBy[iAttacker].timeStamp<=ASSIST_TLIMIT){
									teams[iAttacker].assists++;
									if(teams[iAttacker].hero.equals("Enigma")){
										//System.out.print(teams[iAttacker].hero+": ");
//System.out.println(attackerName+" -> "+combatLogNames.get(combatEvent.targetName)+" at "+combatEvent.timeStamp/60);
									}
								}
							}
						}
						
						boolean skRessurection = target.hero.equals("Skeleton King") && demoDump.resurrectionList.contains( (int)(combatEvent.timeStamp)+3 );

						if(skRessurection){
							deaths--; 
							target.deaths--;
							kills--;
							attacker.kills--;

							int begin, stop;
							if(target.slotID < 5){ begin=5; stop=10;} else {begin=0; stop=5;}
							for(int iAttacker=begin; iAttacker<stop; iAttacker++){
								if(target.damagedBy[iAttacker]!=null && combatEvent.timeStamp - target.damagedBy[iAttacker].timeStamp<=ASSIST_TLIMIT)
									teams[iAttacker].assists--;
							}
						}
					}
					else
						target.aegisTimeStamp = -1;
					
				} break;
			case AEGIS: teams[combatEvent.playerId].aegisTimeStamp = combatEvent.timeStamp; break;
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

		file.write((nl+"-------------------------"+(++n)+"-------------------------"+nl+nl+demoDump.getGameInfo()).getBytes("UTF-8"));

		out.write(demoDump.getGameInfo().toString().getBytes("UTF-8"));
		//		System.out.println("\n");
		//		out.close();
	}

	private static void addAssists(){

	}
}