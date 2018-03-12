import java.util.*;

enum Directions{
	EAST, NORTH, WEST, SOUTH
}

public class MyAI extends Agent
{
	//caveBoundary_X
	int Xmax;
	//caveBoundary_Y
	int Ymax;
	boolean bumpFlag;
	boolean goldFlag;
	boolean screamFlag;
	int arrow;
	
	private State myAgentWorld[][];
	private MyAgent myAgent;
	
	
	Stack<State> path = new Stack<State>();
	// Returning Shortest Path DFS stack (after grabbing gold)
	Stack<State> goldStack = new Stack<State>();
	
	private class State{
		int X;
		int Y;
		int cost;
		boolean isVisited;
		boolean isVisitedReturn;		// isVistedReturnFlag for returning shortest path DFS
		boolean isSafe;
		boolean isWumpusThere;
		
		State(int x, int y){
			this.X = x;
			this.Y = y;
			isVisited = false;
			isVisitedReturn = false;
			isSafe = false;
			isWumpusThere = false;
			this.cost = Integer.MAX_VALUE;
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
		
		@Override
		public String toString(){
			return String.format("X:"+this.X+" Y:"+this.Y+" Dir:"+this.currentDirection);
		}
	}
	
	
	public MyAI ( )
	{
		myAgentWorld = new State[10][10];
		
		for(int y=0; y<10; y++){
			for(int x=0; x<10; x++){
				myAgentWorld[y][x] = new State(y,x);
			}
		}
		
		myAgent = new MyAgent(); 
		Xmax = 10;
		Ymax = 10;
		bumpFlag = false;
		goldFlag = false;
		screamFlag = false;
		arrow = 1;
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
		// Logic for scream
		if(scream){
			screamFlag = true;
			arrow = 0;
		}

		// Logic for glitter
		if(glitter){
			goldFlag = true;
			return Action.GRAB;
		}
		
		// Logic for shortest path returning DFS after grabbing gold
		if(goldFlag){
			if(myAgent.X == 0 && myAgent.Y == 0)
				return Action.CLIMB;
			
			// Mark Own Cell Safe
			markCellSafeAndVisitedReturn(myAgent.X, myAgent.Y);
			
			// Mark Neighbours safe, it will help in shortest path.
			if((!breeze && !stench) || (screamFlag && stench && !breeze)){
				markNeighborsSafe(myAgent.X, myAgent.Y);
			}
			
			// Start Shortest DFS from here
			State nextShortestCell = getShortestPathNeighbour(myAgent.X, myAgent.Y, myAgent.currentDirection);
			
			// If no more neighbours to explore, backtrace to previous node 
			if(nextShortestCell == null) {
				State destState = goldStack.peek();
				Action currAction = getNextAction(destState, myAgent);
				
				if(currAction != Action.FORWARD){
					editAgentDirection(myAgent, currAction);
				}
				// Forward Action, Update Agent Coordinates as state Coordinates
				else{
					myAgent.X = destState.X;
					myAgent.Y = destState.Y;
					goldStack.pop();
				}
				//printStatus();
				return currAction;
			}
			else {
				
				Action currAction = getNextAction(nextShortestCell, myAgent);
				
				if(currAction != Action.FORWARD){
					editAgentDirection(myAgent, currAction);
				}
				// Forward Action, Update Agent Coordinates as state Coordinates
				else{
					// Push in stack and move forward into the depth
					goldStack.push(myAgentWorld[myAgent.X][myAgent.Y]);
					myAgent.X = nextShortestCell.X;
					myAgent.Y = nextShortestCell.Y;
				}
				
				return currAction;
			}
			
			// old code below
			/*
			if(path.isEmpty()){
				return Action.CLIMB;
			}
			
			State state = path.peek();
			//System.out.println(state.toString());
			//System.out.println(myAgent.toString());
			Action action = getNextAction(state, myAgent);
			
			if(action == Action.FORWARD){
				myAgent.X = state.X;
				myAgent.Y = state.Y;
				path.pop();
			}
			else{
				editAgentDirection(myAgent, action);
			}
			
			return action;
			*/
			
			/*
			 * 
			 *  possible extension
			//Dijstra's
			PriorityQueue<State> pq = new PriorityQueue<State>(new Comparator<State>(){
				@Override
				public int compare(State s1, State s2){
					if(s1.cost <= s2.cost){
						return -1;
					}
					return 1;
				}
			});
			*/
		}
		
		// Edge Case, if breeze at the starting position, climb out directly		
		if((breeze) && (myAgent.X == 0 && myAgent.Y == 0)){			
			//printStatus();
			return Action.CLIMB;
		}

		// Logic for only stench and arrow shoot
		if((stench) && (arrow != 0) && !screamFlag){
			arrow--;
			if(!breeze){
				markNextCellSafe(myAgent);
			}
			return Action.SHOOT;
		}

		// Logic for bump
		if(bump){
			//bumpFlag = true;
			State state = path.pop();
			
			if(myAgent.currentDirection == Directions.EAST){
				Xmax = myAgent.X;
			}
			if(myAgent.currentDirection == Directions.NORTH){
				Ymax = myAgent.Y;
			}	

			myAgent.X = state.X;
			myAgent.Y = state.Y;	
		}

		// Logic for stench and breeze
		boolean c1 = (!stench && !breeze);
		boolean c2 = (stench || breeze);
		if(c1 || c2){			
			if(c1){
				markNeighborsSafe(myAgent.X, myAgent.Y);
			}
			
			if(c2){
				if(screamFlag && stench && !breeze){
					markNeighborsSafe(myAgent.X, myAgent.Y);
				}
			}

			//mark itself safe and visited
			markCellSafeAndVisited(myAgent.X, myAgent.Y);
			
			//find the next adjacent unvisited safe cell
			State nextUnvisitedSafeCell = getNextUnvisitedSafeCell(myAgent.X, myAgent.Y, myAgent.currentDirection);
			
			if(path.isEmpty() && (nextUnvisitedSafeCell == null)){
				return Action.CLIMB;
			}
			
			if(nextUnvisitedSafeCell == null){
				State destState = path.peek();
				Action currAction = getNextAction(destState, myAgent);
				
				if(currAction != Action.FORWARD){
					editAgentDirection(myAgent, currAction);
				}
				// Forward Action, Update Agent Coordinates as state Coordinates
				else{
					myAgent.X = destState.X;
					myAgent.Y = destState.Y;
					path.pop();
				}
				//printStatus();
				return currAction;
			}
			else{
				Action currAction = getNextAction(nextUnvisitedSafeCell, myAgent);
				
				if(currAction != Action.FORWARD){
					editAgentDirection(myAgent, currAction);
				}
				// Forward Action, Update Agent Coordinates as state Coordinates
				else{
					path.push(myAgentWorld[myAgent.X][myAgent.Y]);
					myAgent.X = nextUnvisitedSafeCell.X;
					myAgent.Y = nextUnvisitedSafeCell.Y;
				}
				//printStatus();
				return currAction;
			}
		}
		
		
		if(path.isEmpty()){
			//printStatus();
			return Action.CLIMB;
		}
		//printStatus();
		return Action.CLIMB;
	}
	
	// mark adjacent next (based on the agent direction) cell safe 
	private void markNextCellSafe(MyAgent agent){
		
		int x = agent.X;
		int y = agent.Y;
		
		switch(agent.currentDirection){
		
			case EAST:{
				if(x+1 < Xmax && x+1 >= 0){
					myAgentWorld[x+1][y].isSafe = true;
					myAgentWorld[x+1][y].cost = x+1+y;
				}
				break;
			}
			case WEST:{
				if(x-1 < Xmax && x-1 >= 0){
					myAgentWorld[x-1][y].isSafe = true;
					myAgentWorld[x-1][y].cost = x-1+y;
				}
				break;
			}
			case NORTH:{
				if(y+1 < Ymax && y+1 >= 0){
					myAgentWorld[x][y+1].isSafe = true;
					myAgentWorld[x][y+1].cost = x+y+1;
				}
				break;
			}
			case SOUTH:{
				if(y-1 < Ymax && y-1 >= 0){
					myAgentWorld[x][y-1].isSafe = true;
					myAgentWorld[x][y-1].cost = x+y-1;
				}
				break;
			}
		}
	}
	
	// print function for debugging
	private void printStatus() {
		//agent
		System.out.println("Agent:");
		System.out.println(myAgent.toString());
		
		System.out.println();
		System.out.println("World:");
		
		for(int y=0; y<10; y++){
			for(int x=0; x<10; x++){
				System.out.println(myAgentWorld[y][x].toString());
			}
		}
	}
	
	// get Next Unvisited Shorted Path Neighbour for returning DFS after grabbing gold
	private State getShortestPathNeighbour(int x, int y, Directions agentsCurrentDirection) {
		
		// if facing west, give preference to west first.
		if(agentsCurrentDirection == Directions.WEST) {
			// West
			if(x-1 < Xmax && x-1 >= 0){
				if((myAgentWorld[x-1][y].isSafe == true) && (myAgentWorld[x-1][y].isVisitedReturn == false)){
					return myAgentWorld[x-1][y];
				}
			}
			// South 
			if(y-1 < Ymax && y-1 >= 0){
				if((myAgentWorld[x][y-1].isSafe == true) && (myAgentWorld[x][y-1].isVisitedReturn == false)){
					return myAgentWorld[x][y-1];
				}
			}
		}
		else{	// if facing south, give preference to south first.
			// South 
			if(y-1 < Ymax && y-1 >= 0){
				if((myAgentWorld[x][y-1].isSafe == true) && (myAgentWorld[x][y-1].isVisitedReturn == false)){
					return myAgentWorld[x][y-1];
				}
			}
			// West
			if(x-1 < Xmax && x-1 >= 0){
				if((myAgentWorld[x-1][y].isSafe == true) && (myAgentWorld[x-1][y].isVisitedReturn == false)){
					return myAgentWorld[x-1][y];
				}
			}
		}
		
		//East
		if(x+1 < Xmax && x+1 >= 0){
			if((myAgentWorld[x+1][y].isSafe == true) && (myAgentWorld[x+1][y].isVisitedReturn == false)){
				return myAgentWorld[x+1][y];
			}
		}
		
		// North
		if(y+1 < Ymax && y+1 >= 0){
			if((myAgentWorld[x][y+1].isSafe == true) && (myAgentWorld[x][y+1].isVisitedReturn == false)){
				return myAgentWorld[x][y+1];
			}
		}
		
		return null;
	}

	// Helper Functions
	private State goEast(int x, int y){
		//east
		if(x+1 < Xmax && x+1 >= 0){
			if((myAgentWorld[x+1][y].isSafe == true) && (myAgentWorld[x+1][y].isVisited == false)){
				return myAgentWorld[x+1][y];
			}
		}
		return null;
	}

	// Helper Functions
	private State goNorth(int x, int y){
		//north
		if(y+1 < Ymax && y+1 >= 0){
			if((myAgentWorld[x][y+1].isSafe == true) && (myAgentWorld[x][y+1].isVisited == false)){
				return myAgentWorld[x][y+1];
			}
		}
		return null;
	}

	// Helper Functions
	private State goWest(int x, int y){
		//west
		if(x-1 < Xmax && x-1 >= 0){
			if((myAgentWorld[x-1][y].isSafe == true) && (myAgentWorld[x-1][y].isVisited == false)){
				return myAgentWorld[x-1][y];
			}
		}
		return null;
	}

	// Helper Functions
	private State goSouth(int x, int y){
		//south
		if(y-1 < Ymax && y-1 >= 0){
			if((myAgentWorld[x][y-1].isSafe == true) && (myAgentWorld[x][y-1].isVisited == false)){
				return myAgentWorld[x][y-1];
			}
		}
		return null;
	}

	// get Next Unvisited Safe Cell for DFS
	private State getNextUnvisitedSafeCell(int x, int y, Directions agentsCurrentDirection) {
		if(agentsCurrentDirection == Directions.EAST){
			State east = goEast(x, y);
			if(east != null){
				return east;
			}
			State north = goNorth(x, y);
			if(north != null){
				return north;
			}
			State south = goSouth(x, y);
			if(south != null){
				return south;
			}
			State west = goWest(x, y);
			if(west != null){
				return west;
			}
		}
		else if(agentsCurrentDirection == Directions.WEST){
			State west = goWest(x, y);
			if(west != null){
				return west;
			}
			State north = goNorth(x, y);
			if(north != null){
				return north;
			}
			State south = goSouth(x, y);
			if(south != null){
				return south;
			}
			State east = goEast(x, y);
			if(east != null){
				return east;
			}
		}
		else if(agentsCurrentDirection == Directions.NORTH){
			State north = goNorth(x, y);
			if(north != null){
				return north;
			}
			State west = goWest(x, y);
			if(west != null){
				return west;
			}
			State east = goEast(x, y);
			if(east != null){
				return east;
			}
			State south = goSouth(x, y);
			if(south != null){
				return south;
			}
		}
		else{
			State south = goSouth(x, y);
			if(south != null){
				return south;
			}
			State west = goWest(x, y);
			if(west != null){
				return west;
			}
			State east = goEast(x, y);
			if(east != null){
				return east;
			}
			State north = goNorth(x, y);
			if(north != null){
				return north;
			}
		}
		return null;
	}

	// Mark isVisited and isSafe flags as true
	private void markCellSafeAndVisited(int x, int y) {
		State currentCell = myAgentWorld[x][y];
		currentCell.isSafe = true;
		currentCell.isVisited = true;
		myAgentWorld[x][y].cost = x+y;
	}
	
	// Mark isVisitedReturn and isSafe flags as true
	private void markCellSafeAndVisitedReturn(int x, int y) {
		State currentCell = myAgentWorld[x][y];
		currentCell.isSafe = true;
		currentCell.isVisitedReturn = true;
		myAgentWorld[x][y].cost = x+y;
	}

	// editAgentDirection on the basis of rotation action
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

	// getNextaction of agent on the basis of next adjacent state
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
		
		if(desiredDirection == myAgent.currentDirection){
			return Action.FORWARD;
		}

		if(myAgent.currentDirection == Directions.EAST){
			if(desiredDirection == Directions.NORTH){
				return Action.TURN_LEFT;
			}
		}

		if(myAgent.currentDirection == Directions.NORTH){
			if(desiredDirection == Directions.WEST){
				return Action.TURN_LEFT;
			}
		}

		if(myAgent.currentDirection == Directions.WEST){
			if(desiredDirection == Directions.SOUTH){
				return Action.TURN_LEFT;
			}
		}

		if(myAgent.currentDirection == Directions.SOUTH){
			if(desiredDirection == Directions.EAST){
				return Action.TURN_LEFT;
			}
		}
		
		return Action.TURN_RIGHT;		
	}

	// Mark all neighbours safe
	private void markNeighborsSafe(int x, int y) {
		if(x+1 < Xmax && x+1 >= 0){
			myAgentWorld[x+1][y].isSafe = true;
			myAgentWorld[x+1][y].cost = x+1+y;
		}
		if(x-1 < Xmax && x-1 >= 0){
			myAgentWorld[x-1][y].isSafe = true;
			myAgentWorld[x-1][y].cost = x-1+y;
		}
		if(y+1 < Ymax && y+1 >= 0){
			myAgentWorld[x][y+1].isSafe = true;
			myAgentWorld[x][y+1].cost = x+y+1;
		}
		if(y-1 < Ymax && y-1 >= 0){
			myAgentWorld[x][y-1].isSafe = true;
			myAgentWorld[x][y-1].cost = x+y-1;
		}
	}
}
