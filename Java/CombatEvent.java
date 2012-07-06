class CombatEvent{
	byte type;
	short attackerSourceName;
	short targetName;
	short attackerName;
	short inflictorName;
	boolean attackerIllusion;
	boolean targetIllusion;
	short value;
	short health; 
	float timeStamp; 
	short targetSourceName; 

	String logStr;
	String time;
	String attacker;
	String target;
	String inflictor;

	short playerId = -1;
	CombatEvent(short id, float time){
		type = 5; //AEGIS
		playerId = id;
		timeStamp = time;
	}

	CombatEvent(Object[] field){
		type = ((Integer) field[0]).byteValue();
		attackerSourceName = ((Integer) field[1]).shortValue();
		targetName = ((Integer) field[2]).shortValue();
		attackerName = ((Integer) field[3]).shortValue();
		inflictorName = ((Integer) field[4]).shortValue();
		attackerIllusion = (boolean) field[5];
		targetIllusion = (boolean) field[6];
		value = ((Integer) field[7]).shortValue();
		health = ((Integer) field[8]).shortValue();
		timeStamp = (float) field[9];
		targetSourceName = ((Integer) field[10]).shortValue();
	}

	public void fmtLogStr(String attacker, String target, String inflictor){
		this.attacker = attacker;
		this.target = target;
		this.inflictor = inflictor;
		
		float secs = timeStamp%60;
		int mins = (int) (timeStamp-secs)/60;
		time = String.format("[%02d:%05.2f]", mins, secs);;
		
		if(attackerIllusion)
			attacker = attacker+"'s Illusion";
		if(targetIllusion)
			target = target+"'s Illusion";

		switch(type){
		case 0: 
			if(inflictorName==0){
				if(attackerName!=0)
					logStr = String.format("%s %s hits %s for %d damage (%d->%d).", time, attacker, target, value, health+value, health);
				else
					logStr = String.format("%s %s is hit for %d damage (%d->%d).", time, target, value, health+value, health);
			}
			else if(attackerName==targetName)
				logStr = String.format("%s %s hits %s for %d damage (%d->%d).", time, inflictor, target, value, health+value, health);
			else
				logStr = String.format("%s %s hits %s with %s for %d damage (%d->%d).", time, attacker, target, inflictor.replaceFirst(attacker+" ", ""), value, health+value, health);
				
			break;
		case 1:
			if(attackerName!=0)
				logStr = String.format("%s %s's %s heals %s for %d health (%d->%d).", time, attacker, inflictor, attacker, value, health-value, health);
			else
				logStr = String.format("%s %s heals %s for %d health (%d->%d).", time, target, target, value, health-value, health);
			break;
		case 2: logStr = String.format("%s %s receives %s from %s.", time, target, inflictor.replaceFirst(attacker+" ", ""), attacker); break;
		case 3: logStr = String.format("%s %s loses %s.", time, target, inflictor); break;
		case 4:
			if(inflictor.equals("Aegis"))
				logStr = String.format("%s %s will ressurect %s.", time, inflictor, target);
			else if(inflictorName==0)
				logStr = String.format("%s %s is killed by %s.", time, target, attacker);
			else if(attackerName==targetName)
				logStr = String.format("%s %s is killed by %s.", time, target, inflictor);
			else
				logStr = String.format("%s %s is killed by %s's %s.", time, target, attacker, inflictor.replaceFirst(attacker+" ", "") );
			break;
		case 5: logStr = String.format("%s %s picks up %s.", time, target, inflictor); break;
		}
	}

	public String toString(){
		if(logStr!=null)
			return logStr;
		
		return String.format("Type: %d\nAttacker Source: %d\nTarget: %d\nAttacker: %d\nInflictor: %d\nAttacker Illusion: %s\nTarget Illusion: %s\nValue: %d\nHealth: %d\nTime Stamp: %f\nTarget Source: %d\n",
				type, attackerSourceName, targetName, attackerName, inflictorName, attackerIllusion, targetIllusion, value, health, timeStamp, targetSourceName);
	}
}