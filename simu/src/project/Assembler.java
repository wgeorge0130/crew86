package project;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;


public class Assembler{
    public final static Set<String> mnemonics = Set.of("NOP","NOT","HALT","LOD","STO","ADD","SUB","MUL","DIV","AND","CMPL","CMPZ","JUMP","JMPZ");

    public final static Set<String> noArgMnemonics = Set.of("NOP","NOT","HALT");

    public final static Set<String> noImmedMnemonics = Set.of("STO", "CMPL", "CMPZ");

    public final static Set<String> jumpMnemonics = Set.of("JUMP", "JMPZ");

    public static int assemble(String inputFileName, String outputFileName, StringBuilder error) {
        String[] source = null;
        try (Stream<String> lines = Files.lines(Paths.get(inputFileName))) {
            source = lines.toArray(String[]::new);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (error == null)
            throw new IllegalArgumentException
                    ("Coding error: the error buffer is null");
        Set<String> errors = new TreeSet<>();
        Encoding.resetNumNoArg();
        List<Encoding> output = new ArrayList<>();
        
        
        for(int i =0; i < source.length; i++) {
            String[] parts = source[i].split("\\s+");
            String mode = "";
            int arg = 0;
            if(!blankLineCheck(source[i], errors, i+1)) continue;
            if(!noSpaceCheck(source[i], errors, i+1)) continue;
            if (!partsCheck(parts, errors, i+1)) continue;
            if (!mnconCheck(parts, errors, i+1)) continue;
            if (!mnCaseCheck(parts, errors, i+1)) continue;
            if(parts.length == 1 && noArgMnemonics.contains(parts[0])) {
            	output.add(new Encoding(true,(byte)Instruction.valueOf(parts[0]).getOpcode(), 0));
            	
            }
            	
            
            if(!mntwolenCheck(parts, errors, i+1)) continue;
            if(!argtwolenCheck(parts, errors, i+1)) continue;
            if(parts.length == 2) {
            	if(parts[1].startsWith("[[")) {
                    if(!parts[1].endsWith("]]")) {
                        errors.add("Error on line " + (i+1) + ": Illegal blank line in the source file");
                        continue;
                    }
                    parts[1] =  parts[1].substring(2, parts[1].length() - 2);
                    mode = "_IND";
                }
            	else if(parts[1].startsWith("[")) {
                    if(!parts[1].endsWith("]")) {
                        errors.add("Error on line " + (i+1) + ": Illegal blank line in the source file");
                        continue;
                    }
                    parts[1] =   parts[1] .substring(1, parts[1].length() - 1);
                    mode = "_DIR";
                }
            	
            	else if(parts[1].startsWith("{")) {
                    if(!parts[1].endsWith("}")) {
                        errors.add("Error on line " + (i+1) + ": Illegal blank line in the source file");
                        continue;
                    }
                    parts[1] =   parts[1] .substring(1, parts[1].length() - 1);
                    mode = "_ABS";
                } 
            	else {
            		mode = "_IMM";
            	}
            	try {
                    arg = Integer.parseInt(parts[1], 16);
                }
                catch(NumberFormatException e){
                    errors.add("Error on line " + (i+1) + ": Illegal blank line in the source file");
                    
                }
            	if(mode == "_IMM") {
            		if(noImmedMnemonics.contains(parts[0])) {
            			errors.add("Error on line " + (i+1) + ": Illegal blank line in the source file");
                        continue;
            		}
            	}
            	if(mode == "_ABS") {
            		if(!jumpMnemonics.contains(parts[0])) {
            			errors.add("Error on line " + (i+1) + ": Illegal blank line in the source file");
                        continue;
            		}
            	}
            	output.add(new Encoding(false,(byte)Instruction.valueOf(parts[0] + mode).getOpcode(), arg));
            }        
        }
            
        
        if(errors.size() == 0) {
        	int bytesNeeded = Encoding.getNumNoArg() +
        							5*(output.size()-Encoding.getNumNoArg());
        	ByteBuffer buff = ByteBuffer.allocate(bytesNeeded);
        	output.stream()
        	.forEach(instr -> {
        		  buff.put(instr.getOpcode());
        		  if(!instr.isNoArg()) {
        			  buff.putInt(instr.getArg());
        		  }
        	});
        	buff.rewind(); // go back to the beginning of the buffer before writing
        	boolean append = false;
        	try (FileChannel wChannel = new FileOutputStream(
        			new File(outputFileName), append).getChannel()){
        		wChannel.write(buff);
        		wChannel.close();
        	} catch (FileNotFoundException e) {
        		e.printStackTrace();
        	} catch (IOException e) {
        		e.printStackTrace();
        		}
        	} else {
        		Map<Integer, String> map = new TreeMap<>();
        		for(String str : errors) {
        		    Scanner scan = new Scanner(str.substring(14, str.indexOf(':')));
        		    map.put(scan.nextInt(), str); // this code finds the line number in the error message
        		}
        		for(int i : map.keySet()) {
        		    error.append(map.get(i) + "\n");
        		} 
        	}
        	return errors.size();
    }

    public static boolean blankLineCheck(String str, Set<String> errors, int lineNum){
        if(str.trim().length() == 0) {
            errors.add("Error on line " + lineNum
                    + ": Illegal blank line in the source file");
            return false;
        }
        return true;
    }

    public static boolean noSpaceCheck(String str, Set<String> errors, int lineNum) {
        if(str.trim().length() != str.length()) {
            errors.add("Error on line " + lineNum
                    + ": Illegal blank line in the source file");
            return false;
        }
        return true;
    }

    public static boolean partsCheck(String[] part, Set<String> errors, int lineNum) {
        if( part.length == 1 || part.length == 2) {
        	 return true;
        }else {
        	errors.add("Error on line " + lineNum
                    + ": Illegal blank line in the source file");
            return false;
        }
    }
    
    public static boolean mnconCheck(String[] part, Set<String> errors, int lineNum) {
    	if(!mnemonics.contains(part[0].toUpperCase())) {
    		errors.add("Error on line " + lineNum
                    + ": Illegal blank line in the source file");
            return false;
    	}
    	return true;
    }
    
    public static boolean mnCaseCheck(String[] part, Set<String> errors, int lineNum) {
    	if(mnemonics.contains(part[0].toUpperCase())) {
    		if(!mnemonics.contains(part[0])) {
    			errors.add("Error on line " + lineNum
                        + ": Illegal blank line in the source file");
                return false;
    		}
    	}
    	return true;
    }
    
    public static boolean partlenargCheck(String[] part, Set<String> errors, int lineNum) {
    	if(noArgMnemonics.contains(part[0])) {
    		if(part.length != 1) {
    			errors.add("Error on line " + lineNum
                        + ": Illegal blank line in the source file");
                return false;
    		}
    	}
    	return true;
    }
    
    public static boolean mntwolenCheck(String[] part, Set<String> errors, int lineNum) {
    	if (part.length == 2) {
    		if(!mnemonics.contains(part[0])){
    			errors.add("Error on line " + lineNum
                        + ": Illegal blank line in the source file");
                return false;
    		}
    	}
    	return true;
    }
    
    public static boolean argtwolenCheck(String[] part, Set<String> errors, int lineNum) {
    	if (part.length == 2) {
    		if(noArgMnemonics.contains(part[0])){
    			errors.add("Error on line " + lineNum
                        + ": Illegal blank line in the source file");
                return false;
    		}
    	}
    	return true;
    }
    

    public static void main(String[] args) {
    //  For running one test:
    //  StringBuilder error = new StringBuilder();
    //  System.out.println("Enter the name of the file in the \"pasm\" folder without extension: ");
    //  try (Scanner keyboard = new Scanner(System.in)) { 
    //   String filename = keyboard.nextLine();
    //   int i = Assembler.assemble("pasm/" +filename + ".pasm", 
//         "pexe/" + filename + ".pexe", error);
    //   System.out.println("result = " + i);
    //  }
      
    //   For running multiple error files:
      StringBuilder error = new StringBuilder();
      for(int i = 2; i < 16; ++i) {
       String filename = (i<10?"0":"") + i;
       Assembler.assemble("pasm/z0" + filename + "e.pasm", 
         "pexe/" + filename + ".pexe", error);
       error.append("=====================\n");
      }
      System.out.println(error);
     }

}

