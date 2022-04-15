package disassembler;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/*
 * Given in the piazza post of programming assignment 2
 * 
 * //This is in main():
    if (binary) {
      fd = open(argv[1], O_RDONLY);
      fstat(fd, &buf);
      program = mmap(NULL, buf.st_size, PROT_READ | PROT_WRITE,
                     MAP_PRIVATE, fd, 0);
      bprogram = calloc(buf.st_size / 4, sizeof (*bprogram));
      for (i = 0; i < (buf.st_size / 4); i++) {
        program[i] = be32toh(program[i]);
        decode(program[i], bprogram + i);
      }
      emulate(bprogram, buf.st_size / 4, &m);

      return 0;
    }
 */

public class disassembler {
	private static ArrayList<String> assemblyInstructions;
    private static ArrayList<String> instructionsThatNeedLabels = new ArrayList<>();
    private static ArrayList<Integer> posOfLabels = new ArrayList<>();
    //private static HashMap<String, String> listOfLabels = new HashMap<>();
    
    public static void main(String[] args) throws IOException {
        instructionsThatNeedLabels = new ArrayList<>();
        posOfLabels = new ArrayList<>();
        //listOfLabels = new HashMap<>();
        String instruction = "";
        ArrayList<String> binary = new ArrayList<>();
        File file = new File(args[0]);
        FileInputStream sc = new FileInputStream(file);
        byte[] inst = new byte[4];
        while (sc.read(inst) != -1) {
            for (int i = 0; i < 4; i++) {
                // convert byte to binary string representation
                instruction += String.format("%8s", Integer.toBinaryString(inst[i] & 0xFF)).replace(' ', '0');
            }
            binary.add(instruction);
            instruction = "";
        }
        convertBinaryToAssembly(binary);
        addLabels();
        printAssembly(assemblyInstructions);
    }
    
    public static void convertBinaryToAssembly(ArrayList<String> instructions) {
    	assemblyInstructions = new ArrayList<>(instructions.size());
        for (String instruction : instructions) {
            String inst = decode(instruction);
            assemblyInstructions.add(inst);
        }
    }
    
    public static void addLabels() {
    	
    }
    
    public static String decode(String instruction) {
        String returnInstruction = "";
        String opcode = instruction.substring(0, 6);
        
        //TODO: switch case for each type of instruction based on opcode
        
        return returnInstruction;
    }
    
    public static void printAssembly(ArrayList<String> instructions) {
        for (String instruction : instructions) {
            System.out.println(instruction);
        }
    }
    
    public static int getTwosComplement(String binary) {
        String invert = invert(binary);
        int dec = Integer.parseInt(invert, 2);
        dec = (dec + 1) * -1;
        return dec;
    }
}
