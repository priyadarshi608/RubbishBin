package sss.dpstate_1.state;

import sss.dpstate_1.context.Context;

public class StopState implements State {

	public void setInContext(Context context) {
		System.out.println("Player is in stop state");
		context.setState(this);	
	}

	public String toString(){
		return "Stop State";
	}
}
