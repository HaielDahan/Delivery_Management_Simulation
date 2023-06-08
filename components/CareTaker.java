package components;



import java.util.ArrayList;
import java.util.List;

public class CareTaker
{
	   private List<Memento> mementoList = new ArrayList<Memento>();

	   /**
	    * A function that adds a state
	    * @param state
	    */
	   public void add(Memento state)
	   {
	      mementoList.add(state);
	   }

	   /**
	    * A function that returns a state
	    * @param index
	    * @return
	    */
	   public Memento get(int index)
	   {
	      return mementoList.get(index);
	   }
}
