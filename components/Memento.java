package components;


public class Memento 
{
	   private MainOffice state;

	   public Memento(MainOffice state)
	   {
	      this.state = state;
	   }

	   public MainOffice getState()
	   {
	      return state;
	   }	
}
