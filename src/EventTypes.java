//Enum outlining the event types.
public class EventTypes {
   
	enum Events {
      ENGINEARR, TRUCKARR, RESCUEARR, DISPATCH, TANKOUT, FIREOUT, EOUT,TOUT,ROUT;
	}
	
}
/*

ENGINEARR: Engine arrival
TRUCKARR: Truck arrival
RESCUEARR: Rescue arrival
DISPATCH: Dispatch is the alarm scheduling and sending
TANKOUT: Air tank is out, when 2 tanks run out, the FF is done.
FIREOUT: Fire out, used to create the STAT collection and terminate simulation
EOUT: Engine has run out of units to work, its dead
TOUT: same for trucks
ROUT: same for rescue

*/