package br.com.vigilante;

import br.com.arduino.Arduino;

public enum AnalysisSituation {

	IDLE(1), LEARNING(2), NORMAL(3), CRITIC(4);
	
	private final int option;
	AnalysisSituation(int option){
		this.option = option;
	}
	
	public int getOption(){
		return option;
	}
	
	public String getOptionName(){
		switch(this){
			case IDLE:
				return "Idle";
			case LEARNING:
				return "Identifing pattern";
			case NORMAL:
				return "Normal movement";
			case CRITIC:
				return "Critical movement";
			default:
				return "";
		}

	}
}
