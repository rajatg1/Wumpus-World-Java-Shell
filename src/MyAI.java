import java.util.Stack;

import Agent.Action;

enum Directions{
	EAST, NORTH, WEST, SOUTH
}

public class MyAI extends Agent
{
	int Xmax;
	int Ymax;
	boolean directionFlag;
	boolean bumpFlag;
	boolean goldFlag;
	
	private State myAgentWorld[][];
	
	private MyAgent myAgent;
	
	
	Stack<State> path = new Stack<State>();
	
	private class State{
		int X;
		int Y;
		boolean isVisited;
		boolean isSafe;
		boolean isWumpusThere;
		boolean isPitThere;
		
		State(int x, int y){
			this.X = x;
			this.Y = y;
			isVisited = false;
			isSafe = false;
			isWumpusThere = false;
			isPitThere = false;
		}
		
		@Override
		public String toString(){
			return String.format("X: "+this.X+"\tY: "+this.Y+"\tVisited:"+this.isVisited+"\tSafe:"+this.isSafe);
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
		directionFlag = false;
		bumpFlag = false;
		goldFlag = false;
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
		if((stench || breeze) && (myAgent.X == 0 && myAgent.Y == 0)){			
			//printStatus();
			return Action.CLIMB;
		}
		
		if(glitter){
			goldFlag = true;
			return Action.GRAB;
		}
		
		if(goldFlag){
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
		}
		
		if(bump){
			bumpFlag = true;
			State state = path.pop();
			
			myAgent.X = state.X;
			myAgent.Y = state.Y;
			
			if(myAgent.currentDirection == Directions.EAST){
				Xmax = myAgent.X;
			}
			if(myAgent.currentDirection == Directions.NORTH){
				Ymax = myAgent.Y;
			}			
		}

		boolean c1 = (!stench && !breeze);
		boolean c2 = (stench || breeze);
		if(c1 || c2){			
			if(c1){
				markNeighborsSafe(myAgent.X, myAgent.Y);
			}
			
			//mark itself safe and visited
			markCellSafeAndVisited(myAgent.X, myAgent.Y);
			
			//find the next adjacent unvisited safe cell
			State nextUnvisitedSafeCell = getNextUnvisitedSafeCell(myAgent.X, myAgent.Y);
			
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
	
	private void printStatus() {
		// TODO Auto-generated method stub
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


	private State getNextUnvisitedSafeCell(int x, int y) {
		// TODO Auto-generated method stub
		//east
		if(x+1 < Xmax && x+1 >= 0){
			if((myAgentWorld[x+1][y].isSafe == true) && (myAgentWorld[x+1][y].isVisited == false)){
				return myAgentWorld[x+1][y];
			}
		}

		//north
		if(y+1 < Ymax && y+1 >= 0){
			if((myAgentWorld[x][y+1].isSafe == true) && (myAgentWorld[x][y+1].isVisited == false)){
				return myAgentWorld[x][y+1];
			}
		}
		
		//west
		if(x-1 < Xmax && x-1 >= 0){
			if((myAgentWorld[x-1][y].isSafe == true) && (myAgentWorld[x-1][y].isVisited == false)){
				return myAgentWorld[x-1][y];
			}
		}
		
		//south
		if(y-1 < Ymax && y-1 >= 0){
			if((myAgentWorld[x][y-1].isSafe == true) && (myAgentWorld[x][y-1].isVisited == false)){
				return myAgentWorld[x][y-1];
			}
		}
		
		return null;
	}


	private void markCellSafeAndVisited(int x, int y) {
		// TODO Auto-generated method stub
		State currentCell = myAgentWorld[x][y];
		currentCell.isSafe = true;
		currentCell.isVisited = true;
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
		
		if(desiredDirection == myAgent.currentDirection){
			return Action.FORWARD;
		}
		
		directionFlag = true;
		return Action.TURN_LEFT;
	}


	private void markNeighborsSafe(int x, int y) {
		// TODO Auto-generated method stub
		//System.out.println("inside neigh:"+x+" "+Xmax+" "+y+" "+Ymax);
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