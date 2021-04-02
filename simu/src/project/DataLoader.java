package project;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class DataLoader {
	public static String load(Memory memory, File programData) {
		// if programData is null return null
		// create an int lineNum set to 0
		if (programData == null) return null;
		int lineNum = 0;
 		try (Scanner input = new Scanner(programData)) {
		// in a while loop, while input hasNextLine
		// add 1 to lineNum
		// read the next line using input.nextLine().trim() into the String data
		// make a Scanner for this line: Scanner parts = new Scanner(data);
		// write the values into memory:
		// memory.setData(parts.nextInt(16), parts.nextInt(16));
		// call parts.close()
		// after the while loop return "read " + lineNum + " data lines"
 			while(input.hasNextLine()) {
 				lineNum++;
 				String data = input.nextLine().trim();
				Scanner parts = new Scanner(data);
 				memory.setData(parts.nextInt(16), parts.nextInt(16));
 				parts.close();
 			}
 			return "read" + lineNum + "data lines";
 			
		} catch (FileNotFoundException e) {
		return("File " + programData.getName() + " Not Found");
		} catch (Exception e) {
		return("Unexpected exception in loading line " + lineNum
		+ " in " + programData.getName() + " " + e.getMessage());
		}
	}
}
