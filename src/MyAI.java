import java.util.Stack;

enum Directions{
	EAST, NORTH, WEST, SOUTH
}

public class MyAI extends Agent
{
	int Xmax;
	int Ymax;
	
	private State myAgentWorld[][];
	
	private MyAgent myAgent;
	
	boolean directionFlag = false;
	Stack<State> path = new Stack<State>();
	Stack<State> unExplored = new Stack<State>();
	
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
		Directions currentDirection;
		
		MyAgent(){
			this.X = 0;
			this.Y = 0;
			this.currentDirection = Directions.EAST;
		}
		
	}
	
	
	public MyAI ( )
	{
		myAgentWorld = new State[10][10];
		
		for(int i=0; i<10; i++){
			for(int j=0; j<10; j++){
				myAgentWorld[i][j] = new State();
			}
		}
		
		myAgent = new MyAgent(); 
		Xmax = 9;
		Ymax = 9;
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
		// Edge Case, if danger at the starting block
		if((stench || breeze) && (myAgent.X == 0 && myAgent.Y == 0))
			return Action.CLIMB;
		
		// Safe Cell, push path and unexplored cells in stacks and Mark Neighbouring cells safe
		if(!stench && !breeze){
			markNeighboursSafe(myAgent.X, myAgent.Y);
			path.push(myAgentWorld[myAgent.X][myAgent.Y]);
			unExplored.push(myAgentWorld[myAgent.X][myAgent.Y+1]);
			myAgent.X += 1;
			return Action.FORWARD;
		}
		
		// Danger Sensed, Backtrace using path stack
		if(stench || breeze){
			State state = path.peek();
			Action currAction = getNextAction(state, myAgent);
			
			// Turn Action, Update Agent Direction
			if(currAction != Action.FORWARD){
				editAgentDirection(myAgent, currAction);
			}
			// Forward Action, Update Agent Coordinates as state Coordinates
			else{
				myAgent.X = state.X;
				myAgent.Y = state.Y;
			}
			return currAction;
		}
		return Action.CLIMB;
	}	
	
	private void editAgentDirection(MyAgent myAgent, Action currAction){
		int val = myAgent.currentDirection.ordinal();
		if(currAction == Action.TURN_LEFT){
			// Add one to myagent direction and handle case when it is > 3
			// if > 3 : Make it east
			val++;
			if(val > 3){
				myAgent.currentDirection = Directions.EAST;
			}
			else{
				myAgent.currentDirection = getDirections(val);
			}
		}
		else{
			// Subtract one to myagent direction and handle case when it is < 0
			// if < 0 : Make it South
			val--;
			if(val < 0){
				myAgent.currentDirection = Directions.SOUTH;
			}
			else{
				myAgent.currentDirection = getDirections(val);
			}
		}
		
		
	}
	
	private Directions getDirections(int val) {
		// TODO Auto-generated method stub
		switch(val){
			case 0: {
				return Directions.EAST;
			}
			case 1: {
				return Directions.NORTH;
			}
			case 2: {
				return Directions.WEST;
			}
			case 3: {
				return Directions.SOUTH;
			}
		}
		return null;

	}


	private Action getNextAction(State state, MyAgent myAgent) {
		// TODO Auto-generated method stub
		
		Directions desiredDirection = null;
		
		if((state.X == myAgent.X && state.Y == myAgent.Y) || (state.X != myAgent.X && state.Y != myAgent.Y)){
			return null;
		}
		
		if(state.X != myAgent.X){
			if(state.X > myAgent.X){
				desiredDirection = Directions.EAST;
			}
			else{
				desiredDirection = Directions.WEST;
			}
		}
		
		if(state.Y != myAgent.Y){
			if(state.Y > myAgent.Y){
				desiredDirection = Directions.NORTH;
			}
			else{
				desiredDirection = Directions.SOUTH;
			}
		}
		
		if(desiredDirection.ordinal() == myAgent.currentDirection.ordinal()){
			return Action.FORWARD;
		}
		if((desiredDirection.ordinal() - myAgent.currentDirection.ordinal() == 1)  && !directionFlag){
			return Action.TURN_RIGHT;
		}
		directionFlag = true;
		return Action.TURN_LEFT;
	}


	private void markNeighboursSafe(int x, int y) {
		// TODO Auto-generated method stub
		State currentCell = myAgentWorld[x][y];
		currentCell.isSafe = true;
		currentCell.isVisited = true;
		
		if(x+1 < Xmax && x+1 >= 0){
			myAgentWorld[x+1][y].isSafe = true;
		}
		if(x-1 < Xmax && x-1 >= 0){
			myAgentWorld[x-1][y].isSafe = true;
		}
		if(y+1 < Ymax && y+1 >= 0){
			myAgentWorld[x][y+1].isSafe = true;
		}
		if(y-1 < Ymax && y-1 >= 0){
			myAgentWorld[x][y-1].isSafe = true;
		}
	}
}