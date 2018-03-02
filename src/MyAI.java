public class MyAI extends Agent
{
	
	private class State{
		int X;
		int Y;
		boolean isVisited;
		boolean isSafe;
		boolean isWumpusThere;
		boolean isPitThere;
		
		State(){
			isVisited = false;
			isSafe = false;
			isWumpusThere = false;
			isPitThere = false;
		}
	}
	
	private class MyAgent{
		int X;
		int Y;
				
	}
	
	private State agentWorld[][];
	
	enum Directions{
		EAST, NORTH, WEST, SOUTH
	}
	
	
	
	public MyAI ( )
	{
		agentWorld = new State[10][10];
	}
	
	public Action getAction
	(
		boolean stench,
		boolean breeze,
		boolean glitter,
		boolean bump,
		boolean scream
	)
	{
		
		
		return Action.CLIMB;
	}
}