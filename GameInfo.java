public class GameInfo {
	int matchID;
	int mins;
	float secs;
	String mode;
	String winner;
	String boardHeader;
	String scoreBoard;
	float startTime;
	float gameLength;
	String duration;
	String demoLength;
	static final String nl = System.lineSeparator();
	static final String scoreBoardFmt = "MatchID: %sDemo Length: %sGame Length: %sMode: %sWinner: %s%s%s";
	
	void setGameInfo(float t, int id, int m, int w){
		matchID = id;
		secs = t%60;
		mins = (int) (t-secs)/60;
		demoLength = String.format("%dm %02.0fs", mins, secs);
		switch(m){
		case 1: mode = "All Pick"; break;
		case 2: mode = "Single Draft"; break;
		case 3: mode = "Random Draft"; break;
		case 4: mode = "Captains Mode"; break;
		}
		if(w==2) winner = "The Radiant";
		else winner = "The Dire";
		scoreBoard = "There was no data given to generate score board.";
	}
	
	void setStartTime(float gameTime){
		startTime = gameTime;
	}
	
	void setEndTime(float endTime){
		gameLength = endTime-startTime;
		float secs = gameLength%60;
		int tmp = ((int)((gameLength-secs))/60);
		int mins = tmp%60;
		int hours = (tmp-mins)/60;
//		duration = String.format("%02d:%02d:%02.0f", hours, mins, secs);
		duration = String.format("%02d mins", tmp);
	}
	
	void fmtHeader(byte pWidth, byte hWidth){
		final String PLAYER = "Player", HERO = "Hero";
		byte hOffset = pWidth;
		byte kdaOffset;
		pWidth = (byte)((pWidth/2)+PLAYER.length()/2);
		hOffset -= pWidth;
		
		kdaOffset = hWidth;
		hWidth = (byte)((hWidth/2)+HERO.length()/2);
		kdaOffset -= hWidth;
		final String SPACE = "%"+4+"s";
		
//		boardHeader  = nl+"\tPlayer \t\t Hero \t\t  K  D  A"+nl;
		boardHeader  = String.format(nl+"%"+pWidth+"s%"+hOffset+"s"+SPACE+"%"+hWidth+"s%"+kdaOffset+"s"+SPACE+"  K  D  A"+nl, PLAYER, "", "", HERO, "", "");
	}
	
	void genScoreBoard(Player[] players){
		scoreBoard = "";
		for(Player p : players)
			scoreBoard+=p+nl;
	}
	
	public String toString(){
		return String.format(scoreBoardFmt, matchID+nl, demoLength+nl, duration+nl, mode+nl, winner+nl, boardHeader, scoreBoard);
	}
}
