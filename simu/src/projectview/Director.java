package projectview;

import javax.swing.Timer;
import static projectview.States.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import project.CodeAccessException;
import project.DataAccessException;
import project.DivideByZeroException;
import project.Encoding;
import project.Instruction;
import project.Memory;
import project.Processor;


public class Director {
	private Memory memory = new Memory();
	private Processor processor = new Processor();
	private CodeViewPanel codeViewPanel;
	private MemoryViewPanel memoryViewPanel1;
	private MemoryViewPanel memoryViewPanel2;
	private MemoryViewPanel memoryViewPanel3;
	private ControlPanel controlPanel;
	private ProcessorViewPanel processorPanel;
	private JFrame frame;
	private MenuBarBuilder menuBuilder;
	private FilesMgr filesMgr;
	private States currentState = NOTHING_LOADED;
	private static final int TICK = 500;
	private boolean autoStepOn = false;
	private Timer timer = new Timer(TICK, e -> {if(autoStepOn) step();});
	public void step() {
		if (currentState != States.PROGRAM_HALTED &&
				currentState != States.NOTHING_LOADED) {
			try {
				int ip = processor.getIP();
			if (ip < 0 || ip >= memory.getProgramSize()) {
				throw new CodeAccessException(
						"access to illegal address in the code");
				}
				long code = memory.getCode(ip);
				Instruction instr = 
				Instruction.getInstruction(Encoding.opFromLong(code));
				int arg = Encoding.argFromLong(code);
				instr.execute(arg);
			} catch (CodeAccessException e) {
				exceptionHalt();
				JOptionPane.showMessageDialog(frame,
						"Illegal access to code from line " + processor.getIP() + "\n"
								+ "Exception message: " + e.getMessage(),
								"Run time error",
								JOptionPane.OK_OPTION);
				System.out.println("Illegal access to code from line " +
						processor.getIP()); // just for debugging
				System.out.println("Exception message: " + e.getMessage());
			} catch(DataAccessException e) {
				// exceptionHalt();
				// then similar JOPtionPane
				// YOU HAVE TO FILL OUT ALL THESE CATCH BLOCKS
				// —they all have different JOptionPane
				// code with different messages appropriate to the exception
				exceptionHalt();
				JOptionPane.showMessageDialog(frame,
						"Illegal access to code from line " + processor.getIP() + "\n"
								+ "Exception message: " + e.getMessage(),
								"Run time error",
								JOptionPane.OK_OPTION);
				
			} catch(DivideByZeroException e) {
				// similar to above JOPtionPane
				JOptionPane.showMessageDialog(frame,
						"Illegal syntax to code from line " + processor.getIP() + "\n"
								+ "Exception message: " + e.getMessage(),
								"DivideByZeroException",
								JOptionPane.OK_OPTION);
			} catch(Exception e) {
				e.printStackTrace(); // if this happens we have to add a
				// specific handler with the appropriate message
				JOptionPane.showMessageDialog(frame,
						"Illegal access to code from line " + processor.getIP() + "\n"
								+ "Exception message: " + e.getMessage(),
								"Exception error",
								JOptionPane.OK_OPTION);
			}
			notify("");
		}
	};
	public void clear() {
		memory.clearCode();
		memory.clearData();
		processor.setAcc(0);
		processor.setIP(0);
		autoStepOn = false;
		currentState = States.NOTHING_LOADED;
		currentState.enter();
		notify("Clear");
		}
	public void toggleAutoStep() {
		autoStepOn = !autoStepOn;
		if(autoStepOn) currentState = States.AUTO_STEPPING;
		else currentState =  States.PROGRAM_LOADED_NOT_AUTOSTEPPING;
	}
	
	public void reload() {
		 autoStepOn = false;
		 clear();
		 filesMgr.finalCodeLoad_Reload();
		 filesMgr.finalDataLoad_Reload();
	};
	public void setPeriod(int period) {
		timer.setDelay(period);
	}
	public States getCurrentState() {
		return currentState;
	}
	
	
	
	public void createAndShowGUI() {
		filesMgr = new FilesMgr(this, memory, frame);
		codeViewPanel = new CodeViewPanel(memory, processor);
		memoryViewPanel1 = new MemoryViewPanel(memory, 0, 160);
		memoryViewPanel2 = new MemoryViewPanel(memory, 160, Memory.DATA_SIZE/2);
		memoryViewPanel3 = new MemoryViewPanel(memory, Memory.DATA_SIZE/2, Memory.DATA_SIZE);
		controlPanel = new ControlPanel(this);
		processorPanel = new ProcessorViewPanel(processor);
		menuBuilder = new MenuBarBuilder(this, filesMgr); 
		frame = new JFrame("Simulator");
		JMenuBar bar = new JMenuBar();
		frame.setJMenuBar(bar);
		bar.add(menuBuilder.createFileMenu());
		bar.add(menuBuilder.createExecuteMenu());
		Container content = frame.getContentPane();
		content.setLayout(new BorderLayout(1,1));
		content.setBackground(Color.BLACK);
		frame.setSize(1200,600);
		frame.add(codeViewPanel.createCodeDisplay(), BorderLayout.LINE_START);
		frame.add(processorPanel.createProcessorDisplay(),BorderLayout.PAGE_START);
		JPanel center = new JPanel();
		center.setLayout(new GridLayout(1,3));
		center.add(memoryViewPanel1.createMemoryDisplay());
		center.add(memoryViewPanel2.createMemoryDisplay());
		center.add(memoryViewPanel3.createMemoryDisplay());
		frame.add(center, BorderLayout.CENTER);
		frame.add(controlPanel.createControlDisplay(), BorderLayout.PAGE_END);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(WindowListenerFactory.windowClosingFactory(e -> exit()));
		frame.setLocationRelativeTo(null);//centers the frame on the screen
		timer.start();
		currentState.enter();
		notify("");
		frame.setVisible(true);

	}
	
	private void notify(String str) {
		codeViewPanel.update(str);
		memoryViewPanel1.update(str);
		memoryViewPanel2.update(str);
		memoryViewPanel3.update(str);
		controlPanel.update();
		processorPanel.update();
		}
	
	
	public void makeReady(String s) {
		autoStepOn = false;
		currentState = States.PROGRAM_LOADED_NOT_AUTOSTEPPING;
		currentState.enter();
		notify(s);
		}

	public void execute() {
		while (currentState != States.PROGRAM_HALTED &&
				currentState != States.NOTHING_LOADED) {
			try {
				int ip = processor.getIP();
			if (ip < 0 || ip >= memory.getProgramSize()) {
				throw new CodeAccessException(
						"access to illegal address in the code");
				}
				long code = memory.getCode(ip);
				Instruction instr = 
				Instruction.getInstruction(Encoding.opFromLong(code));
				int arg = Encoding.argFromLong(code);
				instr.execute(arg);
			} catch (CodeAccessException e) {
				exceptionHalt();
				JOptionPane.showMessageDialog(frame,
						"Illegal access to code from line " + processor.getIP() + "\n"
								+ "Exception message: " + e.getMessage(),
								"Run time error",
								JOptionPane.OK_OPTION);
				System.out.println("Illegal access to code from line " +
						processor.getIP()); // just for debugging
				System.out.println("Exception message: " + e.getMessage());
			} catch(DataAccessException e) {
				// exceptionHalt();
				// then similar JOPtionPane
				// YOU HAVE TO FILL OUT ALL THESE CATCH BLOCKS
				// —they all have different JOptionPane
				// code with different messages appropriate to the exception
				exceptionHalt();
				JOptionPane.showMessageDialog(frame,
						"Illegal access to code from line " + processor.getIP() + "\n"
								+ "Exception message: " + e.getMessage(),
								"Run time error",
								JOptionPane.OK_OPTION);
				
			} catch(DivideByZeroException e) {
				// similar to above JOPtionPane
				JOptionPane.showMessageDialog(frame,
						"Illegal syntax to code from line " + processor.getIP() + "\n"
								+ "Exception message: " + e.getMessage(),
								"DivideByZeroException",
								JOptionPane.OK_OPTION);
			} catch(Exception e) {
				e.printStackTrace(); // if this happens we have to add a
				// specific handler with the appropriate message
				JOptionPane.showMessageDialog(frame,
						"Illegal access to code from line " + processor.getIP() + "\n"
								+ "Exception message: " + e.getMessage(),
								"Exception error",
								JOptionPane.OK_OPTION);
			}
		}
		notify("");
	};
	
	
	
	public void exit() { // method executed when user exits the program
		int decision = JOptionPane.showConfirmDialog(
		frame, "Do you really wish to exit?",
		"Confirmation", JOptionPane.YES_NO_OPTION);
		if (decision == JOptionPane.YES_OPTION) {
		System.exit(0);
		}
		}
	public void exceptionHalt() {
		Instruction instr = Instruction.HALT;
		instr.execute(0);
		currentState = States.PROGRAM_HALTED;
		currentState.enter();
		notify("");
		}


	
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Director director = new Director();
				Instruction.setHalt(() -> {
					director.autoStepOn = false;
					director.currentState = States.PROGRAM_HALTED;
					director.currentState.enter();
					director.notify("");
				});
				Instruction.setMemory(director.memory);
				Instruction.setProcessor(director.processor);
				director.createAndShowGUI();

			}
		});
	}
}
