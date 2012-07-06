import java.util.ArrayList;
import java.util.HashSet;


class Player{
	String gUID;
	String name;
	String hero;
	ArrayList<CombatEvent> killEvents = new ArrayList<CombatEvent>();
	ArrayList<CombatEvent> deathEvents = new ArrayList<CombatEvent>();
	ArrayList<CombatEvent> assistEvents = new ArrayList<CombatEvent>();
	ArrayList<String> achievements = new ArrayList<String>();
	ArrayList<String> fails = new ArrayList<String>();
	float gold;
	float aegisTimeStamp = -1;
	short slotID;
	String team;
	String strFormat;
	CombatEvent[] damagedBy = new CombatEvent[10];
	HashSet<Short> minions = new HashSet<Short>();

	public void setStrFmt(byte pWidth, byte hWidth){
		final String SPACE = "%"+2+"s|"+"%"+2+"s";
		strFormat = "%-"+pWidth+"s"+SPACE+"%-"+hWidth+"s"+SPACE+"%02d %02d %02d";
	}

	public void addAssist(CombatEvent assist){
		int i = 0;
		for(i=0; i<assistEvents.size() && assist.timeStamp > assistEvents.get(i).timeStamp; i++);
		assistEvents.add(i, assist);				
	}

	public void addAchievement(CombatEvent achievement, A type){
		String fmt = "No Achievements";
		
		switch(type){
		case HERO_DENY: fmt = String.format("Deny allied hero - %s", achievement.toString() ); break;
		case ROSHAN_LH: fmt = String.format("Last hit Roshan - %s", achievement.toString() ); break;
		case THRONE_LH: fmt = String.format("Last hit the Ancient - %s", achievement.toString() ); break;
		case ROSHAN_KILL: fmt = String.format("Killed by Roshan - %s", achievement.toString() ); break;
		case FOUNTAIN_KILL: fmt = String.format("Killed by Fountain - %s", achievement.toString() ); break;
		case NEUTRAL_KILL: fmt = String.format("Killed by Neutral - %s", achievement.toString() ); break;
		case SUICIDE: fmt = String.format("Commit suicide - %s", achievement.toString() ); break;
		case KS: fmt = String.format("Kill Steal - %s", achievement.toString() ); break;
		}
		achievements.add(fmt);
	}

	public String toString(){
		return String.format(strFormat, name, "", "", hero, "", "", killEvents.size(), deathEvents.size(), assistEvents.size());
	}


}