import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

	public static void main(String[] args){
		//				args = new String[1];
		//				args[0] = "14612633.dem";

		String fileName = "ERROR";
		boolean append;
		DemoFileDump demoDump = null;
		String matchInfo = "No Info";
		Stopwatch s = new Stopwatch();
		double dumpTime = 0;
		double surfTime = 0;

		if(args.length==0){
			System.out.println("Usage: " + Handler.class.getSimpleName() + ".jar filename.dem");
			System.exit(0);
		}

		if(args.length==1){
			fileName = args[0].replace(".dem", "");
			append = false;
		}
		else{
			fileName = "results";
			append = true;
		}

		try {
			file = new FileOutputStream(fileName+".txt", append);

			for(int i=0; i<args.length; i++){
				demoDump = new DemoFileDump();
				demoDump.open(args[i]);
				s.start();
				demoDump.doDump();
				getKDs(demoDump.getTeams(), demoDump.getHeroKills());
				combatLog(demoDump);
				s.stop();
				dumpTime = s.time();

				matchInfo = demoDump.getGameInfo();
				printGameInfo(matchInfo, i, dumpTime);
				System.out.printf("%n%s Surfed in %.2fs", args[i], dumpTime);
			}
			file.close();

			if(args.length==1){
				System.out.printf("%n%n");
				out.write(matchInfo.getBytes("UTF-8"));	
				System.out.printf("%n*Match info above also written to %s.txt%n", fileName );
				showMainMenu(demoDump.getCombatEvents(), demoDump.getTeams(), fileName, demoDump.getUserList());
			}
			else
				System.out.printf("%n%n*Matches info written to %s.txt%n", fileName );

		} catch (IOException e) {
			System.err.println("Could not write game info to file."+fileName);
		}
		//		file = new FileOutputStream("Combat Log.txt", false);
		//		for(String logEntry : combatLog)
		//			file.write((logEntry+nl).getBytes("UTF-8"));
	}

	private static void printGameInfo(String matchInfo, int n, double dumpTime){
		try {			
			file.write( (nl+"-------------------------"+(++n)+"-------------------------"+nl+nl+matchInfo).getBytes("UTF-8") );
			file.write((nl+"Dump Time: "+dumpTime+nl).getBytes("UTF-8"));
		} catch (IOException e) {
			System.err.println("Could not write game info to file.");
		}
	}

	private static void showMainMenu(ArrayList<CombatEvent> combatEvents, Player[] player, String fileName, PlayerInfo[] userList) throws UnsupportedEncodingException, IOException{
		int option = -1;
		do{
			System.out.printf("%n%40s%n", "--------Main Menu--------");
			System.out.printf("0 - Exit.%n");
			System.out.printf("1 - Dump entire Combat Log (%,d entries).%n", combatEvents.size());
			System.out.printf("2 - Detailed hero K/D/A and Achievements.%n");
			System.out.printf("3 - All achievements and fails in this match.%n");
			System.out.printf("4 - Players info.%n");
			option = getOption(4);
			if(option==1){			
				dumpCombatLog(combatEvents, fileName);
				System.out.printf("Finished dumping to %s_combat_log.txt%n%n", fileName );
			}
			else if(option==2){
				showHeroMainMenu(player, fileName);
			}
			else if(option==3){
				for(Player p : player){
					if(!p.achievements.isEmpty()) out.write(String.format("%n%s%n", p.name, p.hero).getBytes("UTF-8"));
					for(String a : p.achievements)
						System.out.println(a);
				}
			}
			else if(option==4){
				for(PlayerInfo user : userList)
					if(user!=null) out.write(String.format("%-20s %s%n", user.gUID, user.name).getBytes("UTF-8"));
			}
		}while(option!=0);
	}
	
	private static void getAllAchievements(){

	}

	private static void dumpCombatLog(ArrayList<CombatEvent> combatEvents, String fileName){
		try {
			file = new FileOutputStream(fileName+"_combat_log.txt", false);			
			for(CombatEvent logEntry : combatEvents)
				file.write((logEntry+nl).getBytes("UTF-8"));
			file.close();

		} catch (IOException e) {
			System.err.println("Could not write combat log to file "+fileName);
		}
	}

	private static void showPlayerMenu(Player[] player){

	}

	private static void showHeroMainMenu(Player[] player, String fileName){
		int option = -1;
		do{
			System.out.printf("%n%40s%n", "--------Hero Main Menu--------");
			System.out.printf("0 - Go back to Main Menu.%n");
			for(int i=1; i<=player.length; i++){
				System.out.printf("%2d - %s%n", i, player[i-1].hero);
				//				if(i==player.length/2 || i==player.length)
				//					System.out.printf("%n");
			}

			option = getOption(player.length);
			if(option!=0)
				showHeroMenu(player[option-1]);

		}while(option!=0);
	}

	private static void showHeroMenu(Player hero){
		int option = -1;
		do{
			System.out.printf("%n%40s%n", "--------Hero Menu--------");
			System.out.printf("%s%n%n", hero);
			System.out.printf("0 - Go back to Hero Main Menu.%n");
			System.out.printf("%2d - Kills%n", 1);
			System.out.printf("%2d - Deaths%n", 2);
			System.out.printf("%2d - Assists%n", 3);
			System.out.printf("%2d - Achievements%n", 4);

			option = getOption(4);
			if(option==1)
				for(CombatEvent kill : hero.killEvents)
					System.out.println(kill);
			if(option==2)
				for(CombatEvent death : hero.deathEvents)
					System.out.println(death);
			if(option==3)
				for(CombatEvent assist : hero.assistEvents)
					System.out.println(assist);
			if(option==4)
				for(String achievement : hero.achievements)
					System.out.println(achievement);

		}while(option!=0);
	}

	private static int getOption(int limit){
		System.out.printf("%nOption #");
		int option = -1;
		try{
			option = Integer.parseInt(System.console().readLine().trim());
			if(option<0 || option>limit){
				System.err.printf("Input must be a number between 0 and %d%n", limit);
				option = getOption(limit);
			}
		}
		catch(NumberFormatException e){
			System.err.printf("Input must be a number between 0 and %d%n", limit);
			option = getOption(limit);
		}

		return option;
	}

	private static void getKDs(Player[] teams, ArrayList<HeroKill> heroKills){
		int lastTarget = -1;
		String type = "";

		for(HeroKill kd : heroKills){
			CDOTAUserMsg_ChatEvent msg = kd.chatMsg;
			type = msg.getType().toString();
			//			if(teams[heroKill.getPlayerid2()].hero.equals("Sven"))
			//				System.out.println(teams[heroKill.getPlayerid2()].hero+" -> "+teams[heroKill.getPlayerid1()].hero);

			if(type.equals("CHAT_MESSAGE_HERO_KILL")){
				if(msg.getPlayerid3()==-1){
					if(msg.getValue()!=0 && msg.getPlayerid2()!=-1){
						kd.combatEvent.playerId = (short) msg.getPlayerid2();
						teams[msg.getPlayerid2()].killEvents.add(kd.combatEvent);
						teams[msg.getPlayerid1()].deathEvents.add(kd.combatEvent);
						lastTarget = msg.getPlayerid1();	
					}
					else{//neutral, creep, tower, or fountain kills hero. No assists.
						teams[msg.getPlayerid1()].deathEvents.add(kd.combatEvent);
					}
				}
				else{//kill by creep, tower, or fountain and gold split among assists
					teams[msg.getPlayerid1()].deathEvents.add(kd.combatEvent);
					if(msg.getPlayerid2()!=-1) teams[msg.getPlayerid2()].addAssist(kd.combatEvent);
					if(msg.getPlayerid3()!=-1) teams[msg.getPlayerid3()].addAssist(kd.combatEvent);
					if(msg.getPlayerid4()!=-1) teams[msg.getPlayerid4()].addAssist(kd.combatEvent);
					if(msg.getPlayerid5()!=-1) teams[msg.getPlayerid5()].addAssist(kd.combatEvent);
					if(msg.getPlayerid6()!=-1) teams[msg.getPlayerid6()].addAssist(kd.combatEvent);
				}
			}
			else if(type.equals("CHAT_MESSAGE_STREAK_KILL") && msg.getPlayerid4()!=lastTarget){
				kd.combatEvent.playerId = (short) msg.getPlayerid1();
				teams[msg.getPlayerid1()].killEvents.add(kd.combatEvent);
				teams[msg.getPlayerid4()].deathEvents.add(kd.combatEvent);			
			}
			else if(type.equals("CHAT_MESSAGE_HERO_DENY")){
				kd.combatEvent.playerId = (short) msg.getPlayerid2();
				teams[msg.getPlayerid1()].deathEvents.add(kd.combatEvent);
			}
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

	private static void combatLog(DemoFileDump demoDump){
		final HashMap<String, Short> heroList = demoDump.getHeroList();
		final ArrayList<String> combatLogNames = demoDump.getCombatLogNames();
		ArrayList<CombatEvent> combatEvents = demoDump.getCombatEvents();
		final HashMap<Short, Player> players = demoDump.getPlayers();
		final Player[] teams = demoDump.getTeams();
		final HashMap<Short, String> modifierList = demoDump.getModifierList();
		//System.out.println(combatLogNames);

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
							teams[iAttacker].assistEvents.remove(teams[iAttacker].assistEvents.size()-1);
						}
					}
					reincarnateList.add(combatEvent.timeStamp);
					attackerSource.damagedBy = lastDamagedBy;
				}

				break;
			case DEATH:	
				if(target!=null){ //target is a hero
					if(target.aegisTimeStamp==-1 || (combatEvent.timeStamp - target.aegisTimeStamp) > 600f){ //10mins *60

						//target.deaths++;
						//						target.deathEvents.add(combatEvent);

						if(combatEvent.targetIllusion){
							System.out.println("illusions deaths shouldn't count towards a hero's kills or deaths");
							System.exit(0);
						}

						boolean skRessurection = target.hero.equals("Skeleton King") && demoDump.resurrectionList.contains( (int)(combatEvent.timeStamp)+3 );

						if(attackerSource==null ||(combatEvent.attackerIllusion && friendlyFire))
							attackerSource =  findOwner(attackerName, combatEvent.attackerName, specialCaseHeroes);
						
						if(attackerSource==null && combatEvent.playerId!=-1)
							attackerSource =  teams[combatEvent.playerId];

						if(attackerSource!=null){
							friendlyFire = attackerSource.team.equals(target.team);
							
							if(target.damagedBy[attackerSource.slotID]==null && !friendlyFire){
								boolean ks = false;
								for(CombatEvent assistEvent : target.damagedBy){			
									if(assistEvent!=null && combatEvent.timeStamp - assistEvent.timeStamp<=ASSIST_TLIMIT){
										ks = true;
										break;
									}								
								}
								if(ks) attackerSource.addAchievement(combatEvent, A.KS);
							}
														
							if(friendlyFire){
								//grant deny achievement or suicide
								if(attackerSource.hero.equals(targetName))
									attackerSource.addAchievement(combatEvent, A.SUICIDE);
								else
									attackerSource.addAchievement(combatEvent, A.HERO_DENY);
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
						else if(isNeutral){
							target.damagedBy = new CombatEvent[10]; //No assists
							target.addAchievement(combatEvent, A.NEUTRAL_KILL);
						}
						else if(attackerName.equals("Roshan"))
							target.addAchievement(combatEvent, A.ROSHAN_KILL);
						else if(attackerName.contains("Fountain"))
							target.addAchievement(combatEvent, A.FOUNTAIN_KILL);
						else//Some unit not yet accounted for
							System.out.println(attackerName+" killed "+targetName+" at "+combatEvent.timeStamp/60);


						if(!friendlyFire && !isCreep && !isTower){
							int start, end;
							if(target.slotID < 5){ start=5; end=10;} else {start=0; end=5;}
							for(int iAttacker=start; iAttacker<end; iAttacker++){
								if(target.damagedBy[iAttacker]!=null){
									CombatEvent assitEvent = target.damagedBy[iAttacker];
									if(combatEvent.timeStamp - assitEvent.timeStamp <= ASSIST_TLIMIT){
										teams[iAttacker].addAssist(assitEvent);
										//if(teams[iAttacker].hero.equals("Shadow Demon"))
										//		System.out.println(attackerName+" -> "+combatLogNames.get(combatEvent.targetName)+" at "+combatEvent.timeStamp/60);
									}
								}
							}
						}

						if(target.hero.equals("Skeleton King")){
							lastDamagedBy = target.damagedBy.clone();
							lastDamagedBy[attackerSource.slotID] = combatEvent;
							skLastDeath = combatEvent;
						}
						if(skRessurection && !friendlyFire && !isCreep && !isTower){
							//							target.deaths--;
							//							attacker.kills--;

							int begin, stop;
							if(target.slotID < 5){ begin=5; stop=10;} else {begin=0; stop=5;}
							for(int iAttacker=begin; iAttacker<stop; iAttacker++){
								if(target.damagedBy[iAttacker]!=null && combatEvent.timeStamp - target.damagedBy[iAttacker].timeStamp<=ASSIST_TLIMIT){
									teams[iAttacker].assistEvents.remove(teams[iAttacker].assistEvents.size()-1);
									target.damagedBy[iAttacker] = null;
								}
							}
						}
					}
					else{
						target.aegisTimeStamp = -1;
						combatEvent.fmtLogStr(attackerName, targetName, "Aegis");
					}

				}
				else if(targetName.contains("Fort") && attackerSource!=null)
					attackerSource.addAchievement(combatEvent, A.THRONE_LH);
				else if(targetName.equals("Roshan") && attackerSource!=null)
					attackerSource.addAchievement(combatEvent, A.ROSHAN_LH);
				break;
			case AEGIS:
				combatEvent.fmtLogStr("", teams[combatEvent.playerId].hero, "Aegis");
				teams[combatEvent.playerId].aegisTimeStamp = combatEvent.timeStamp;
				break;
			}
		}
	}
}