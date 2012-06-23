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
		
		short playerId;
		CombatEvent(short id){
			type = 5; //AEGIS
			playerId = id;
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

		public String toString(){
			return String.format("Type: %d\nAttacker Source: %d\nTarget: %d\nAttacker: %d\nInflictor: %d\nAttacker Illusion: %s\nTarget Illusion: %s\nValue: %d\nHealth: %d\nTime Stamp: %f\nTarget Source: %d\n",
					type, attackerSourceName, targetName, attackerName, inflictorName, attackerIllusion, targetIllusion, value, health, timeStamp, targetSourceName);
		}
	}