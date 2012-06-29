import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
				args = new String[1];
				args[0] = "16164678.dem";

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
			combatLog(demoDump);
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
				else{//should be creep/tower kill and gold split among assists
					//					System.out.println(heroKill);
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
		Player attacker = specialCaseHeroes.get("Chen");
		if(attacker==null) attacker = specialCaseHeroes.get("Enchantress");
		if(attacker==null) attacker = specialCaseHeroes.get("Morphling");

		if(attacker!=null){
			if( attacker.minions.contains(attackerIndex) ) return attacker;
		}
		else{
			if(attackerName.contains("warlock_golem")) 		return specialCaseHeroes.get("Warlock");
			if(attackerName.contains("brewmaster")) 		return specialCaseHeroes.get("Brewmaster");
			if(attackerName.contains("broodmother_spider")) return specialCaseHeroes.get("Broodmother");
			if(attackerName.contains("plague_ward")) 		return specialCaseHeroes.get("Venomancer");
			if(attackerName.contains("eidolon")) 			return specialCaseHeroes.get("Enigma");
			if(attackerName.contains("boar")) 				return specialCaseHeroes.get("BeastMaster");
			if(attackerName.contains("furion_treant")) 		return specialCaseHeroes.get("Furion");
			if(attackerName.contains("lycan_wolf")) 		return specialCaseHeroes.get("Lycan");
		}


		return null;
	}

	private static void combatLog(DemoFileDump demoDump) throws IOException{
		
		final HashMap<String, Short> heroList = demoDump.getHeroList();
		final ArrayList<String> combatLogNames = demoDump.getCombatLogNames();
		ArrayList<CombatEvent> combatEvents = demoDump.getCombatEvents();
		final HashMap<Short, Player> players = demoDump.getPlayers();
		final Player[] teams = demoDump.getTeams();
		
		int heroTargets = 0;
		int deaths = 0;
		int kills = 0;
		final int ASSIST_TLIMIT = 20;
//		final HashMap<String, Player> specialCaseHeroes = getSpecialCaseHeroes(players, heroList);

		final short helmOfDominator = (short) combatLogNames.indexOf("");
		final short trollWarlord = (short) combatLogNames.indexOf("npc_dota_neutral_dark_troll_warlord");
		final short skeletonWarrior = (short) combatLogNames.indexOf("npc_dota_dark_troll_warlord_skeleton_warrior");
		final short enragedWildkin = (short) combatLogNames.indexOf("npc_dota_neutral_enraged_wildkin");
		final short tornado = (short) combatLogNames.indexOf("npc_dota_enraged_wildkin_tornado");
		final Player chen = players.get((short) combatLogNames.indexOf("npc_dota_hero_chen"));
		final short holyPersuasion = (short) combatLogNames.indexOf("modifier_chen_holy_persuasion");
		final Player enchantress = players.get((short)combatLogNames.indexOf("npc_dota_hero_enchantress"));
		final short enchant = (short) combatLogNames.indexOf("modifier_enchantress_enchant");
		final Player skeletonKing = players.get((short)combatLogNames.indexOf("npc_dota_hero_skeleton_king"));
		final short reincarnate = (short) combatLogNames.indexOf("modifier_skeleton_king_reincarnate_slow");
		CombatEvent skLastDeath = null;
		CombatEvent[] lastDamagedBy = null;
		HashSet<Float> reincarnateList = new HashSet<Float>();
		final Player brewmaster = players.get((short)combatLogNames.indexOf("npc_dota_hero_brewmaster"));
		final Player warlock = players.get((short)combatLogNames.indexOf("npc_dota_hero_warlock"));
		final Player broodmother = players.get((short)combatLogNames.indexOf("npc_dota_hero_broodmother"));
		final Player venomancer = players.get((short)combatLogNames.indexOf("npc_dota_hero_venomancer"));
		final Player enigma = players.get((short)combatLogNames.indexOf("npc_dota_hero_enigma"));
		final Player beastmaster = players.get((short)combatLogNames.indexOf("npc_dota_hero_beastmaster"));
		final Player furion = players.get((short)combatLogNames.indexOf("npc_dota_hero_furion"));
		final Player lycan = players.get((short)combatLogNames.indexOf("npc_dota_hero_lycan"));
		final Player morphling = players.get((short)combatLogNames.indexOf("npc_dota_hero_morphling"));
		final short replicate = (short) combatLogNames.indexOf("modifier_morphling_replicate_timer");
		final short illusion = (short) combatLogNames.indexOf("modifier_illusion");
		CombatEvent lastIllusion = null;
		//		final short disruption = (short) combatLogNames.indexOf("modifier_shadow_demon_disruption");
		//		final Player shadowDemon = players.get((short)combatLogNames.indexOf("npc_dota_hero_shadow_demon"));
		//invoker



		System.out.println("\nCombat Log Names: "+combatLogNames.size());
		System.out.println("Combat Events: "+combatEvents.size());
		boolean denied = false;
		for(CombatEvent combatEvent : combatEvents){
			Player target = players.get(combatEvent.targetName);
			Player attackerSource = players.get(combatEvent.attackerSourceName);
			String attackerName = combatLogNames.get(combatEvent.attackerName);

			boolean isNeutral = attackerName.contains("neutral");
			boolean isCreep = attackerName.contains("creep_g") || attackerName.contains("creep_b") || attackerName.contains("siege");
			boolean isTower = attackerName.contains("tower");
			boolean isGolem = attackerName.contains("warlock_golem");
			boolean isPanda = attackerName.contains("brewmaster");
			boolean isSpider = attackerName.contains("broodmother_spider");
			boolean isPlagueWard = attackerName.contains("plague_ward");
			boolean isEidolon = attackerName.contains("eidolon");
			boolean isBoar = attackerName.contains("boar");
			boolean isTreant =  attackerName.contains("furion_treant");
			boolean isWolf = attackerName.contains("lycan_wolf");
			boolean isChen = chen!=null && chen.minions.contains(combatEvent.attackerName);
			boolean isEnchantress = enchantress!=null && enchantress.minions.contains(combatEvent.attackerName);
			boolean isMorphling = morphling!=null && combatEvent.attackerIllusion && morphling.minions.contains(combatEvent.attackerName);


			if(target!=null && attackerSource!=null) denied = attackerSource.team.equals(target.team);
			switch(types[combatEvent.type]){
			case DAMAGE: //if(attacker!=null && target!=null)heroTargets++; 
				if(target!=null && !combatEvent.targetIllusion){
					if(isChen){
						attackerSource =  chen;
					}
					else if(isEnchantress){
						attackerSource =  enchantress;
					}
					else if(isMorphling){
						attackerSource =  morphling;
						morphling.minions.clear();
					}
					else if(isGolem){
						attackerSource =  warlock;
					}
					else if(isPanda){
						attackerSource =  brewmaster;
					}
					else if(isSpider){
						attackerSource =  broodmother;
					}
					else if(isPlagueWard){
						attackerSource =  venomancer;
					}
					else if(isEidolon){
						attackerSource =  enigma;
					}
					else if(isBoar){
						attackerSource =  beastmaster;
					}
					else if(isTreant){
						attackerSource =  furion;
					}
					else if(isWolf){
						attackerSource =  lycan;
					}


					if(attackerSource!=null){
						denied = attackerSource.team.equals(target.team);
						if(combatEvent.health!=0 && !denied)
							target.damagedBy[attackerSource.slotID] = combatEvent;

					}
					else if(isTower || isCreep){
						denied = false;
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
				if(combatEvent.inflictorName==illusion){
					lastIllusion = combatEvent;
				}
				if(combatEvent.inflictorName==replicate && combatEvent.timeStamp==lastIllusion.timeStamp){
					morphling.minions.add(lastIllusion.targetName);
				}
				if(combatEvent.inflictorName==reincarnate && !reincarnateList.contains(combatEvent.timeStamp)){ //failsafe
					int begin, stop;
					if(skeletonKing.slotID < 5){ begin=5; stop=10;} else {begin=0; stop=5;}
					for(int iAttacker=begin; iAttacker<stop; iAttacker++){
						if(skeletonKing.damagedBy[iAttacker]!=null && skLastDeath.timeStamp - skeletonKing.damagedBy[iAttacker].timeStamp<= ASSIST_TLIMIT){
							teams[iAttacker].assists--;
						}
					}
					reincarnateList.add(combatEvent.timeStamp);
					skeletonKing.damagedBy = lastDamagedBy;
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

						if(isChen){		
							attackerSource = chen;
						}
						else if(isEnchantress){
							attackerSource = enchantress;
						}
						else if(isMorphling){
							attackerSource = morphling;
							morphling.minions.clear();
						}
						else if(isGolem){
							attackerSource = warlock;
						}
						else if(isPanda){
							attackerSource = brewmaster;
						}
						else if(isSpider){
							attackerSource = broodmother;
						}
						else if(isPlagueWard){
							attackerSource = venomancer;
						}
						else if(isEidolon){
							attackerSource = enigma;
						}
						else if(isBoar){
							attackerSource = beastmaster;
						}
						else if(isTreant){
							attackerSource = furion;
						}
						else if(isWolf){
							attackerSource = lycan;
						}
						else if(denied){
							//grant deny achievement or suicide
							//System.out.println(attackerName+" -> "+combatLogNames.get(combatEvent.targetName)+" at "+combatEvent.timeStamp/60);
						}

						if(attackerSource!=null){
							//if(target.damagedBy[attacker.slotID]==null)
							//KS Achievement

							//attacker.kills++;
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


						if(!denied && !isCreep && !isTower){
							int start, end;
							if(target.slotID < 5){ start=5; end=10;} else {start=0; end=5;}
							for(int iAttacker=start; iAttacker<end; iAttacker++){

								if(target.damagedBy[iAttacker]!=null && combatEvent.timeStamp - target.damagedBy[iAttacker].timeStamp<=ASSIST_TLIMIT){
									teams[iAttacker].assists++;
									if(teams[iAttacker].hero.equals("Morphling"))
										System.out.println(attackerName+" -> "+combatLogNames.get(combatEvent.targetName)+" at "+combatEvent.timeStamp/60);
								}
							}
						}

						if(target.hero.equals("Skeleton King")){
							lastDamagedBy = target.damagedBy.clone();
							lastDamagedBy[attackerSource.slotID] = combatEvent;
							skLastDeath = combatEvent;
						}
						if(skRessurection && !denied && !isCreep && !isTower){
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