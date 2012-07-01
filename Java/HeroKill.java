import java.util.HashMap;
import com.generated_code.DotaUsermessages.CDOTAUserMsg_ChatEvent;
import com.generated_code.DotaUsermessages.CDOTAUserMsg_OverheadEvent;

	public class HeroKill{
		CDOTAUserMsg_ChatEvent chatMsg;
		HashMap<String, CDOTAUserMsg_OverheadEvent> assists;
		
		HeroKill(CDOTAUserMsg_ChatEvent msg){
			chatMsg = msg;
			assists = new HashMap<String, CDOTAUserMsg_OverheadEvent>();
		}
	}