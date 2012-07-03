import java.util.ArrayList;
import java.util.HashSet;

	class Player{
		String gUID;
		String name;
		String hero;
		short kills;
		short deaths;
		short assists;
		ArrayList<CombatEvent> killEvents = new ArrayList<CombatEvent>();
		ArrayList<CombatEvent> deathEvents = new ArrayList<CombatEvent>();
		float gold;
		float aegisTimeStamp = -1;
		short slotID;
		String team;
		String strFormat;
		CombatEvent[] damagedBy = new CombatEvent[10];
		HashSet<Short> minions = new HashSet<Short>();
		
		void setStrFmt(byte pWidth, byte hWidth){
			final String SPACE = "%"+2+"s|"+"%"+2+"s";
			strFormat = "%-"+pWidth+"s"+SPACE+"%-"+hWidth+"s"+SPACE+"%02d %02d %02d";
		}

		public String toString(){
			return String.format(strFormat, name, "", "", hero, "", "", kills, deaths, assists);
		}
	}