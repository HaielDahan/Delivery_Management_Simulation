package components;


public class Originator 
{
	   private MainOffice state;

	   public void setState(MainOffice state)
	   {
	      this.state = state;
	   }

	   public MainOffice getState()
	   {
	      return state;
	   }

	   public Memento saveStateToMemento()
	   {
	      return new Memento(state);
	   }

	   public void getStateFromMemento(Memento memento)
	   {
	      state = memento.getState();
	   }
}
