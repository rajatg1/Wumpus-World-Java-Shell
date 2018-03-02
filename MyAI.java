public class MyAI extends Agent
{
	public MyAI ( )
	{
		//direction = 1;
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
		if(steps == 0 && reverse){
			return Action.CLIMB;
		}
		
		if(glitter){
			if(!gold_found){
				gold_found = true;
				return Action.GRAB;
			}
		}
		if(gold_found || return_home){
			rCount++;
			if(rCount < 3){
				return Action.TURN_LEFT;
			}
			if(rCount>=3){
				reverse = true;
			}
			if(reverse){
				steps--;
				if(steps < 0){
					return Action.CLIMB;
				}
			}
			rCount = 0;
			gold_found = false;
			return_home = false;
			return Action.FORWARD;
		}
		else if(bump){
			// Rotate twice
			rCount++;
			return_home = true;
			return Action.TURN_LEFT;
		}
		else if(!breeze && !stench){
			if(reverse){
				steps--;
				if(steps < 0){
					return Action.CLIMB;
				}
			}
			else{
				steps++;
			}
			return Action.FORWARD;
		}
		else if(breeze || stench){
			//setDirection(Action.TURN_LEFT);
			if(steps == 0){
				return Action.CLIMB;
			}
			rCount++;
			if(rCount < 3){
				return Action.TURN_LEFT;
			}
			if(rCount>=3){
				reverse = true;
			}
			if(reverse){
				steps--;
				if(steps < 0){
					return Action.CLIMB;
				}
			}
			rCount = 0;
			return Action.FORWARD;
		}
		
		else{
			return Action.CLIMB;
		}
	}
	
	/*
	void setDirection(Action action){
		int adder = 1;
		if(action == Action.TURN_RIGHT){
			adder = -1;
		}
		direction += adder;
		switch(direction){
			case 0:{
				//east
				direction = 4;
			}
			case 5:{
				//South
				direction = 1;
			}
		}
	}
	*/
	//E-1 N-2 W-3 S-4
	//int direction;
	private int rCount;
	private int steps;
	private boolean gold_found;
	private boolean return_home;
	private boolean reverse;
}