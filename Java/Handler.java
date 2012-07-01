import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.generated_code.DotaUsermessages.CDOTAUserMsg_ChatEvent;
import com.generated_code.DotaUsermessages.CDOTAUserMsg_OverheadEvent;

enum eventType {
	DAMAGE, HEALING, MODIFIER_GAIN, MODIFIER_LOSS, DEATH, AEGIS
}

/**
 * @author mozphere
 *
 */
public class Handler{
	static final eventType[] types = eventType.values();
	static final DataOutputStream out = new DataOutputStream(System.out);
	static final String nl = System.lineSeparator();
	static FileOutputStream file;

	public static void main(String[] args) throws IOException{
		args = new String[1];
		args[0] = "16168247.dem";

		String fileName = "ERROR";
		DemoFileDump demoDump;
		Stopwatch s = new Stopwatch();

		if(args.length==0){
			System.out.println("Usage: " + Handler.class.getSimpleName() + ".jar filename.dem");
			System.exit(0);
		}

		fileName = args.length==1 ? args[0] : "results";

		file = new FileOutputStream(fileName+".txt", true);

		for(int i=0; i<args.length; i++){
			demoDump = new DemoFileDump();

			s.start();
			if(demoDump.open(args[i]))
				demoDump.doDump();
			s.stop();

			double dumpTime = s.time();

			s.start();
			getKDs(demoDump.getTeams(), demoDump.getHeroKillMsgs(), demoDump.getUserInfo());
//			combatLog(demoDump);
			s.stop();

			double surfTime = s.time();		

			printGameInfo(demoDump.getGameInfo(), i, dumpTime);
			System.out.println("Dump Time: "+dumpTime);
			System.out.println("Surf Time: "+surfTime);
		}
	}

	private static void printGameInfo(String matchInfo, int n, double dumpTime){
		try {			
			byte[] byteString =  (nl+"-------------------------"+(++n)+"-------------------------"+nl+nl+matchInfo).getBytes("UTF-8") ;

			file.write(byteString);
			out.write(byteString);	

			file.write((nl+"Dump Time: "+dumpTime+nl).getBytes("UTF-8"));

		} catch (IOException e) {
			e.printStackTrace();
		}

		//		System.out.println("\n");
		//		out.close(); file.close();
	}

	private static void getKDs(Player[] teams, ArrayList<HeroKill> kdHeroList, HashMap<String, PlayerInfo> userInfo){
		int lastTarget = -1;
		String type = "";
		short heroIndex;

		for(HeroKill heroKill : kdHeroList){
			CDOTAUserMsg_ChatEvent chatMsg = heroKill.chatMsg;
			type = chatMsg.getType().toString();
			//			if(teams[heroKill.getPlayerid2()].hero.equals("Sven"))
			//				System.out.println(teams[heroKill.getPlayerid2()].hero+" -> "+teams[heroKill.getPlayerid1()].hero);

			if(type.equals("CHAT_MESSAGE_HERO_KILL")){
				if(chatMsg.getPlayerid3()==-1){
					if(chatMsg.getValue()!=0){
						teams[chatMsg.getPlayerid2()].kills++;
						teams[chatMsg.getPlayerid1()].deaths++;
						lastTarget = chatMsg.getPlayerid1();
						
						for(String assist : heroKill.assists.keySet()){
							if(teams[userInfo.get(assist).slotId].hero.equals("Antimage")){
								System.out.println(teams[chatMsg.getPlayerid2()].hero+" -> "+teams[chatMsg.getPlayerid1()].hero+" for "+heroKill.assists.get(assist).getValue());
								System.out.println(teams[userInfo.get(assist).slotId].hero);
							}
							teams[userInfo.get(assist).slotId].assists++;
						}
					}
					else{//neutral kills hero
						teams[chatMsg.getPlayerid1()].deaths++;
					}
				}
				else{//should be creep/tower kill and gold split among assists
					//System.out.println(heroKill);
					teams[chatMsg.getPlayerid1()].deaths++;
					if(chatMsg.getPlayerid2()!=-1) teams[chatMsg.getPlayerid2()].assists++;
					if(chatMsg.getPlayerid3()!=-1) teams[chatMsg.getPlayerid3()].assists++;
					if(chatMsg.getPlayerid4()!=-1) teams[chatMsg.getPlayerid4()].assists++;
					if(chatMsg.getPlayerid5()!=-1) teams[chatMsg.getPlayerid5()].assists++;
					if(chatMsg.getPlayerid6()!=-1) teams[chatMsg.getPlayerid6()].assists++;
				}
			}
			else if(type.equals("CHAT_MESSAGE_STREAK_KILL") && chatMsg.getPlayerid4()!=lastTarget){
				teams[chatMsg.getPlayerid1()].kills++;
				teams[chatMsg.getPlayerid4()].deaths++;
				for(String assist : heroKill.assists.keySet()){
					teams[userInfo.get(assist).slotId].assists++;
				}
			}
			else if(type.equals("CHAT_MESSAGE_HERO_DENY"))
				teams[chatMsg.getPlayerid1()].deaths++;
			//teams[heroKill.getPlayerid2()] deny achievement
		}
	}

	private static HashMap<String, Player> getSpecialCaseHeroes(final HashMap<Short, Player> players, final HashMap<String, Short> heroList){
		HashMap<String, Player> specialCaseHeroes =  new HashMap<String, Player>();
		Short heroIndex;
		String heroName;

		heroName = "Enchantress"; 	heroIndex = heroList.get(heroName); if(heroIndex!=null) specialCaseHeroes.put(heroName, players.get(heroIndex));
		heroName = "Chen"; 			heroIndex = heroList.get(heroName); if(heroIndex!=null) specialCaseHeroes.put(heroName, players.get(heroIndex));
		heroName = "Skeleton King"; heroIndex = heroList.get(heroName); if(heroIndex!=null) specialCaseHeroes.put(heroName, players.get(heroIndex));
		heroName = "Brewmaster";	heroIndex = heroList.get(heroName); if(heroIndex!=null) specialCaseHeroes.put(heroName, players.get(heroIndex));
		heroName = "Warlock"; 		heroIndex = heroList.get(heroName); if(heroIndex!=null) specialCaseHeroes.put(heroName, players.get(heroIndex));
		heroName = "Broodmother"; 	heroIndex = heroList.get(heroName); if(heroIndex!=null) specialCaseHeroes.put(heroName, players.get(heroIndex));
		heroName = "Venomancer"; 	heroIndex = heroList.get(heroName); if(heroIndex!=null) specialCaseHeroes.put(heroName, players.get(heroIndex));
		heroName = "Enigma"; 		heroIndex = heroList.get(heroName); if(heroIndex!=null) specialCaseHeroes.put(heroName, players.get(heroIndex));
		heroName = "Beastmaster"; 	heroIndex = heroList.get(heroName); if(heroIndex!=null) specialCaseHeroes.put(heroName, players.get(heroIndex));
		heroName = "Furion"; 		heroIndex = heroList.get(heroName); if(heroIndex!=null) specialCaseHeroes.put(heroName, players.get(heroIndex));
		heroName = "Lycan"; 		heroIndex = heroList.get(heroName); if(heroIndex!=null) specialCaseHeroes.put(heroName, players.get(heroIndex));
		heroName = "Morphling"; 	heroIndex = heroList.get(heroName); if(heroIndex!=null) specialCaseHeroes.put(heroName, players.get(heroIndex));

		/*		
		for(Map.Entry<String, Short> hero: heroList.entrySet())	
			if(heroList.get(hero.getKey())!=null){
				p = players.get(hero.getValue());
				specialCases.put(p.hero, p);
			}
		 */

		return specialCaseHeroes;
	}

	private static Player findOwner(final String attackerName, final short attackerIndex, final HashMap<String, Player> specialCaseHeroes){
		Player attacker;

		attacker = specialCaseHeroes.get("Chen");
		if(attacker==null) attacker = specialCaseHeroes.get("Enchantress");
		if(attacker==null) attacker = specialCaseHeroes.get("Morphling");

		if(attacker!=null && attacker.minions.contains(attackerIndex)){
			if(attacker.hero.equals("Morphling"))
				attacker.minions.clear();

			return attacker;
		}

		if(attackerName.contains("warlock_golem")) 		return specialCaseHeroes.get("Warlock");
		if(attackerName.contains("brewmaster")) 		return specialCaseHeroes.get("Brewmaster");
		if(attackerName.contains("broodmother_spider")) return specialCaseHeroes.get("Broodmother");
		if(attackerName.contains("plague_ward")) 		return specialCaseHeroes.get("Venomancer");
		if(attackerName.contains("eidolon")) 			return specialCaseHeroes.get("Enigma");
		if(attackerName.contains("boar")) 				return specialCaseHeroes.get("BeastMaster");
		if(attackerName.contains("furion_treant")) 		return specialCaseHeroes.get("Furion");
		if(attackerName.contains("lycan_wolf")) 		return specialCaseHeroes.get("Lycan");



		return null;
	}

	private static void combatLog(DemoFileDump demoDump) throws IOException{

		final HashMap<String, Short> heroList = demoDump.getHeroList();
		final ArrayList<String> combatLogNames = demoDump.getCombatLogNames();
		ArrayList<CombatEvent> combatEvents = demoDump.getCombatEvents();
		final HashMap<Short, Player> players = demoDump.getPlayers();
		final Player[] teams = demoDump.getTeams();
		final HashMap<Short, String> modifierList = demoDump.getModifierList();
		//System.out.println(combatLogNames);
		int heroTargets = 0;
		int deaths = 0;
		int kills = 0;
		final int ASSIST_TLIMIT = 20;
		final HashMap<String, Player> specialCaseHeroes = getSpecialCaseHeroes(players, heroList);

		//		final short helmOfDominator = (short) combatLogNames.indexOf("");
		final short trollWarlord = (short) combatLogNames.indexOf("Neutral Dark Troll Warlord");
		final short skeletonWarrior = (short) combatLogNames.indexOf("Dark Troll Warlord Skeleton Warrior");
		final short enragedWildkin = (short) combatLogNames.indexOf("Neutral Enraged Wildkin");
		final short tornado = (short) combatLogNames.indexOf("Enraged Wildkin Tornado");

		CombatEvent skLastDeath = null;
		CombatEvent[] lastDamagedBy = null;
		HashSet<Float> reincarnateList = new HashSet<Float>();
		CombatEvent lastIllusion = null;

		//		final short disruption = (short) combatLogNames.indexOf("modifier_shadow_demon_disruption");
		//		final Player shadowDemon = players.get((short)combatLogNames.indexOf("npc_dota_hero_shadow_demon"));
		//invoker

		System.out.println("\nCombat Log Names: "+combatLogNames.size());
		System.out.println("Combat Events: "+combatEvents.size());
		boolean friendlyFire = false;
		for(CombatEvent combatEvent : combatEvents){
			Player target = players.get(combatEvent.targetName);
			Player attackerSource = players.get(combatEvent.attackerSourceName);
			String attackerName = combatLogNames.get(combatEvent.attackerName);

			boolean isNeutral = attackerName.contains("Neutral");
			boolean isCreep = attackerName.contains("Creep") || attackerName.contains("Siege");
			boolean isTower = attackerName.contains("Tower");

			if(target!=null && attackerSource!=null) friendlyFire = attackerSource.team.equals(target.team);

			switch(types[combatEvent.type]){
			case DAMAGE: //if(attacker!=null && target!=null)heroTargets++; 
				if(target!=null && !combatEvent.targetIllusion){

					if( attackerSource==null || (combatEvent.attackerIllusion && friendlyFire) ) //Morphling's illu's don't have him as source, unlike other heroes.
						attackerSource =  findOwner(attackerName, combatEvent.attackerName, specialCaseHeroes);

					if(attackerSource!=null){
						friendlyFire = attackerSource.team.equals(target.team);
						if(combatEvent.health!=0 && !friendlyFire)
							target.damagedBy[attackerSource.slotID] = combatEvent;

					}
					else if(isTower || isCreep){
						friendlyFire = false;
					}
					else if(isNeutral){

					}
					else{//Some unit not yet accounted for
						//	System.out.println(attackerName+" -> "+combatLogNames.get(combatEvent.targetName)+" at "+combatEvent.timeStamp/60);
					}

					if(combatEvent.health==0){

					}
				}

				break;
			case MODIFIER_GAIN: 
				attackerSource = players.get(combatEvent.attackerName);
				String inflictor = modifierList.get(combatEvent.inflictorName);
				//				System.out.println(attackerName+" -> "+combatLogNames.get(combatEvent.targetName)+" at "+combatEvent.timeStamp/60+" with "+combatLogNames.get(combatEvent.inflictorName));

				if(inflictor.matches("Chen Holy Persuasion|Enchantress Enchant")){
					attackerSource.minions.add(combatEvent.targetName);
					if(combatEvent.targetName==trollWarlord)
						attackerSource.minions.add(skeletonWarrior);
					if(combatEvent.targetName==enragedWildkin)
						attackerSource.minions.add(tornado);
				}
				else if(inflictor.contains("Illusion")){
					lastIllusion = combatEvent;
				}
				else if(inflictor.contains("Morphling Replicate Timer") && combatEvent.timeStamp==lastIllusion.timeStamp){
					attackerSource.minions.add(lastIllusion.targetName);
				}
				else if(inflictor.contains("Skeleton King Reincarnate Slow") && !reincarnateList.contains(combatEvent.timeStamp)){ //failsafe
					int begin, stop;
					if(attackerSource.slotID < 5){ begin=5; stop=10;} else {begin=0; stop=5;}
					for(int iAttacker=begin; iAttacker<stop; iAttacker++){
						if(attackerSource.damagedBy[iAttacker]!=null && skLastDeath.timeStamp - attackerSource.damagedBy[iAttacker].timeStamp<= ASSIST_TLIMIT){
//							teams[iAttacker].assists--;
						}
					}
					reincarnateList.add(combatEvent.timeStamp);
					attackerSource.damagedBy = lastDamagedBy;
				}
				break;
			case DEATH:	
				if(target!=null){ //target is a hero
					if(target.aegisTimeStamp==-1 || (combatEvent.timeStamp - target.aegisTimeStamp) > 600f){ //10mins *60
						deaths++; 
//						target.deaths++;

						if(combatEvent.targetIllusion){
							System.out.println("illusions deaths shouldn't count towards a hero's kills or deaths");
							System.exit(0);
						}

						boolean skRessurection = target.hero.equals("Skeleton King") && demoDump.resurrectionList.contains( (int)(combatEvent.timeStamp)+3 );

						if(attackerSource==null ||(combatEvent.attackerIllusion && friendlyFire))
							attackerSource =  findOwner(attackerName, combatEvent.attackerName, specialCaseHeroes);

						if(attackerSource!=null){
							if(!friendlyFire){
								//attacker.kills++;
								if(target.damagedBy[attackerSource.slotID]==null){//KS Achievement
									for(CombatEvent ce: target.damagedBy) if(ce!=null)System.out.println(combatLogNames.get(ce.attackerName)+" at "+ce.timeStamp/60);
									System.out.println(attackerName+" KSED "+target.hero+" at "+combatEvent.timeStamp/60);									
								}
							}
							else {//grant deny achievement or suicide
								
//								System.out.println(attackerName+" -> "+combatLogNames.get(combatEvent.targetName)+" at "+combatEvent.timeStamp/60);
							}


							target.damagedBy[attackerSource.slotID]=null;
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
								attackerSource = teams[i];
							}
						}
						else if(isNeutral){//should be kill by a neutral achievement
							target.damagedBy = new CombatEvent[10];
							//System.out.println(attackerName+" -> "+combatLogNames.get(combatEvent.targetName)+" at "+combatEvent.timeStamp/60);
						}

						else//Some unit not yet accounted for
							System.out.println(attackerName+" -> "+combatLogNames.get(combatEvent.targetName)+" at "+combatEvent.timeStamp/60);


						if(!friendlyFire && !isCreep && !isTower){
							int start, end;
							if(target.slotID < 5){ start=5; end=10;} else {start=0; end=5;}
							for(int iAttacker=start; iAttacker<end; iAttacker++){

								if(target.damagedBy[iAttacker]!=null && combatEvent.timeStamp - target.damagedBy[iAttacker].timeStamp<=ASSIST_TLIMIT){
//									teams[iAttacker].assists++;
									//									if(teams[iAttacker].hero.equals("Shadow Demon"))
									//										System.out.println(attackerName+" -> "+combatLogNames.get(combatEvent.targetName)+" at "+combatEvent.timeStamp/60);
								}
							}
						}

						if(target.hero.equals("Skeleton King")){
							lastDamagedBy = target.damagedBy.clone();
							lastDamagedBy[attackerSource.slotID] = combatEvent;
							skLastDeath = combatEvent;
						}
						if(skRessurection && !friendlyFire && !isCreep && !isTower){
							deaths--; 
							//							target.deaths--;
							kills--;
							//							attacker.kills--;

							int begin, stop;
							if(target.slotID < 5){ begin=5; stop=10;} else {begin=0; stop=5;}
							for(int iAttacker=begin; iAttacker<stop; iAttacker++){
								if(target.damagedBy[iAttacker]!=null && combatEvent.timeStamp - target.damagedBy[iAttacker].timeStamp<=ASSIST_TLIMIT){
//									teams[iAttacker].assists--;
									target.damagedBy[iAttacker] = null;
								}
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


	}

	private static void addAssists(){

	}
}