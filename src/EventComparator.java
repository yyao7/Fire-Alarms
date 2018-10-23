//This gives the protocol for comparing events.
import java.util.Comparator;

public class EventComparator implements Comparator<Event> {
   @Override
   public int compare(Event e1, Event e2) {
      return e1.getTime()-e2.getTime();
   }
}