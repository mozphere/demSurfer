import com.generated_code.DotaUsermessages.CDOTAUserMsg_ChatEvent;

	public class HeroKill{
		CDOTAUserMsg_ChatEvent chatMsg;
		CombatEvent combatEvent;
		
		HeroKill(CDOTAUserMsg_ChatEvent msg, CombatEvent lastDeath){
			chatMsg = msg;
			combatEvent = lastDeath;
		}
	}