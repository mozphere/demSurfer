	class Player{
		String gUID;
		String name;
		String hero;
		short kills;
		short deaths;
		short assists;
		float gold;
		boolean aegis;
		short slotID;
		String team;
		String strFormat;
		CombatEvent[] deathAssist = new CombatEvent[10];
		
		void setStrFmt(byte pWidth, byte hWidth){
			final String SPACE = "%"+2+"s|"+"%"+2+"s";
			strFormat = "%-"+pWidth+"s"+SPACE+"%-"+hWidth+"s"+SPACE+"%02d %02d %02d";
		}

		public String toString(){
			return String.format(strFormat, name, "", "", hero, "", "", kills, deaths, assists);
		}
	}