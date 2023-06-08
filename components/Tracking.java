package components;


public class Tracking 
{
	public final int time;
	public final Node node; // place
	public final Status status;
	public final int packageID;
	
	
	public Tracking(int time, Node node, Status status, int packageID) {
		super();
		this.time = time;
		this.node = node;
		this.status = status;
		this.packageID = packageID;
	}

	
	@Override
	public String toString() {
		String name = (node==null)? "Customer" : node.getName();
		return "package ID: " + packageID + ", " + time + ": " + name + ", status = " + status;
	}	
}
