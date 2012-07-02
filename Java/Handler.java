import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.generated_code.DotaUsermessages.CDOTAUserMsg_ChatEvent;

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
//		args = new String[1];
//		args[0] = "19910918.dem";

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
			getKDs(demoDump.getTeams(), demoDump.getHeroKillMsgs());
			ArrayList<String> combatLog = combatLog(demoDump);
			s.stop();

			double surfTime = s.time();		

			printGameInfo(demoDump.getGameInfo(), i, dumpTime);
			System.out.println("Dump Time: "+dumpTime);
			System.out.println("Surf Time: "+surfTime);
			
//			file = new FileOutputStream("Combat Log.txt", false);
//			for(String logEntry : combatLog)
//				file.write((logEntry+nl).getBytes("UTF-8"));
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

	private static void getKDs(Player[] teams, ArrayList<CDOTAUserMsg_ChatEvent> kdHeroList){
		int lastTarget = -1;
		String type = "";

		for(CDOTAUserMsg_ChatEvent heroKill : kdHeroList){
			type = heroKill.getType().toString();
			//			if(teams[heroKill.getPlayerid2()].hero.equals("Sven"))
			//				System.out.println(teams[heroKill.getPlayerid2()].hero+" -> "+teams[heroKill.getPlayerid1()].hero);

			if(type.equals("CHAT_MESSAGE_HERO_KILL")){
				if(heroKill.getPlayerid3()==-1){
					if(heroKill.getValue()!=0){
						teams[heroKill.getPlayerid2()].kills++;
						teams[heroKill.getPlayerid1()].deaths++;
						lastTarget = heroKill.getPlayerid1();	
					}
					else{//neutral kills hero
						teams[heroKill.getPlayerid1()].deaths++;
					}
				}
				else{//kill by creep, tower, or fountain and gold split among assists
					teams[heroKill.getPlayerid1()].deaths++;
					if(heroKill.getPlayerid2()!=-1) teams[heroKill.getPlayerid2()].assists++;
					if(heroKill.getPlayerid3()!=-1) teams[heroKill.getPlayerid3()].assists++;
					if(heroKill.getPlayerid4()!=-1) teams[heroKill.getPlayerid4()].assists++;
					if(heroKill.getPlayerid5()!=-1) teams[heroKill.getPlayerid5()].assists++;
					if(heroKill.getPlayerid6()!=-1) teams[heroKill.getPlayerid6()].assists++;
				}
			}
			else if(type.equals("CHAT_MESSAGE_STREAK_KILL") && heroKill.getPlayerid4()!=lastTarget){
				teams[heroKill.getPlayerid1()].kills++;
				teams[heroKill.getPlayerid4()].deaths++;				
			}
			else if(type.equals("CHAT_MESSAGE_HERO_DENY"))
				teams[heroKill.getPlayerid1()].deaths++;
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
		heroName = "Invoker"; 		heroIndex = heroList.get(heroName); if(heroIndex!=null) specialCaseHeroes.put(heroName, players.get(heroIndex));
		heroName = "Shadow Shaman"; heroIndex = heroList.get(heroName); if(heroIndex!=null) specialCaseHeroes.put(heroName, players.get(heroIndex));
		heroName = "Lone Druid"; 	heroIndex = heroList.get(heroName); if(heroIndex!=null) specialCaseHeroes.put(heroName, players.get(heroIndex));
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

		if(attackerName.contains("Warlock Golem")) 		return specialCaseHeroes.get("Warlock");
		if(attackerName.contains("Brewmaster")) 		return specialCaseHeroes.get("Brewmaster");
		if(attackerName.contains("Broodmother Spider")) return specialCaseHeroes.get("Broodmother");
		if(attackerName.contains("Plague Ward")) 		return specialCaseHeroes.get("Venomancer");
		if(attackerName.contains("Eidolon")) 			return specialCaseHeroes.get("Enigma");
		if(attackerName.contains("Boar")) 				return specialCaseHeroes.get("Beastmaster");
		if(attackerName.contains("Furion Treant")) 		return specialCaseHeroes.get("Furion");
		if(attackerName.contains("Lycan Wolf")) 		return specialCaseHeroes.get("Lycan");
		if(attackerName.contains("Forged Spirit")) 		return specialCaseHeroes.get("Invoker");
		if(attackerName.contains("Shaman Ward")) 		return specialCaseHeroes.get("Shadow Shaman");
		if(attackerName.contains("Druid Bear")) 		return specialCaseHeroes.get("Lone Druid");

		return null;
	}

	private static ArrayList<String> combatLog(DemoFileDump demoDump) throws IOException{
		ArrayList<String> combatLog = new ArrayList<String>();
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
			String targetName =  combatLogNames.get(combatEvent.targetName);
			String inflictor = combatLogNames.get(combatEvent.inflictorName);
			combatEvent.fmtLogStr(attackerName, targetName, inflictor);

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
//				System.out.println(attackerName+" -> "+combatLogNames.get(combatEvent.targetName)+" at "+combatEvent.timeStamp/60+" with "+combatLogNames.get(combatEvent.inflictorName));
				
				if(inflictor.matches("Chen Holy Persuasion|Enchantress Enchant")){// |Helmf Of Dominator |Necronomicon Archer Aura
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
							teams[iAttacker].assists--;
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
							//if(target.damagedBy[attacker.slotID]==null)
							//KS Achievement
							if(friendlyFire){
								//grant deny achievement or suicide
								//System.out.println(attackerName+" -> "+combatLogNames.get(combatEvent.targetName)+" at "+combatEvent.timeStamp/60);
							}

							//attacker.kills++;
							target.damagedBy[attackerSource.slotID]=null;
						}
						else if(isTower || isCreep){ // || isFountain
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
							System.out.println(attackerName+" killed "+combatLogNames.get(combatEvent.targetName)+" at "+combatEvent.timeStamp/60);


						if(!friendlyFire && !isCreep && !isTower){
							int start, end;
							if(target.slotID < 5){ start=5; end=10;} else {start=0; end=5;}
							for(int iAttacker=start; iAttacker<end; iAttacker++){

								if(target.damagedBy[iAttacker]!=null && combatEvent.timeStamp - target.damagedBy[iAttacker].timeStamp<=ASSIST_TLIMIT){
									teams[iAttacker].assists++;
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
									teams[iAttacker].assists--;
									target.damagedBy[iAttacker] = null;
								}
							}
						}
					}
					else{
						target.aegisTimeStamp = -1;
						combatEvent.fmtLogStr(attackerName, targetName, "Aegis");
					}

				} break;
			case AEGIS: teams[combatEvent.playerId].aegisTimeStamp = combatEvent.timeStamp; break;
			}
			combatLog.add(combatEvent.toString());
		}

		System.out.println("Damaged Hero Targets: " + heroTargets );
		System.out.println("Total Kills: "+ kills);
		System.out.println("Total Deaths: "+ deaths);
		System.out.println("\nCombatLogNames HeroIDs: " + players.keySet());
		System.out.println();

		return combatLog;
	}

	private static void addAssists(){

	}
}