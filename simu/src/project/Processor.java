package project;

public class Processor {
	private int accumulator;
	private int instructionPointer;
	
	
	public int getAcc() {
		return accumulator;
	}
	public void setAcc(int accumulator) {
		this.accumulator = accumulator;
	}
	public int getIP() {
		return instructionPointer;
	}
	public void setIP(int instructionPointer) {
		this.instructionPointer = instructionPointer;
	}
	
	public void incIP() {
		instructionPointer++;
	}
	
	public void applyNot() {
		if(accumulator == 0) accumulator = 1;
		else accumulator = 0;
	}
	
}
