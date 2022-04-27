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
	private static ArrayList<String> assemblyInstructions = new ArrayList<>();
	private static ArrayList<String> instructionsThatNeedLabels = new ArrayList<>();
	private static ArrayList<Integer> posOfLabels = new ArrayList<>();
	private static HashMap<String, String> listOfLabels = new HashMap<>();

	public static void main(String[] args) throws IOException {
		instructionsThatNeedLabels = new ArrayList<>();
		posOfLabels = new ArrayList<>();
		listOfLabels = new HashMap<>();
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

	public static int getTwosComplement(String binary) {
		String invert = invert(binary);
		int dec = Integer.parseInt(invert, 2);
		dec = (dec + 1) * -1;
		return dec;
	}

	public static String Itype(String instruction, String mnemonic) {
		String assembly = mnemonic;
		String imm = instruction.substring(10, 22);
		if(imm.charAt(0) == '1'){
			imm = String.valueOf(getTwosComplement(imm));
		}else{
			imm = String.valueOf(Integer.parseInt(imm, 2));
		}
		String rn = instruction.substring(22, 27);
		rn = register(rn);
		String rd = instruction.substring(27, 32);
		rd = register(rd);
		assembly = assembly + " " + rd + ", " + rn + ", #" + imm;
		return assembly;
	}

	public static String decode(String instruction) {
		String returnInstruction = "";

		//TODO: switch case for each type of instruction based on opcode

		String opcode = instruction.substring(0, 10);
		switch (opcode) {
		case "1001000100" ->  // addi
		returnInstruction = Itype(instruction, "ADDI");
		case "1001001000" ->  // andi
		returnInstruction = Itype(instruction, "ANDI");
		case "1101001000" ->  // eori
		returnInstruction = Itype(instruction, "EORI");
		case "1011001000" ->  // orri
		returnInstruction = Itype(instruction, "ORRI");
		case "1101000100" ->  // subi
		returnInstruction = Itype(instruction, "SUBI");
		case "1111000100" ->  // subis
		returnInstruction = Itype(instruction, "SUBIS");

		return returnInstruction;
		}
	}
	
	public static String register(String reg) {
        String number = String.valueOf(Integer.parseInt(reg, 2));
        return switch (number) {
            case "28" -> "SP";
            case "29" -> "FP";
            case "30" -> "LR";
            case "31" -> "XZR";
            default -> "X" + number;
        };
    }
	
	public static void printAssembly(ArrayList<String> instructions) {
		for (String instruction : instructions) {
			System.out.println(instruction);
		}
	}
	
	public static String invert(String binary) {
        String result = binary;
        result = result.replace("0", " ");
        result = result.replace("1", "0");
        result = result.replace(" ", "1");
        return result;
    }
	
	public static void putBranch(){
        for (String instructionsThatNeedLabel : instructionsThatNeedLabels) {
            int pos = assemblyInstructions.indexOf(instructionsThatNeedLabel);
            String instToAddLabel = assemblyInstructions.get(pos);
            char current;
            int stringPos = instToAddLabel.length() - 1;
            int lengthOfReplacement = 0;
            boolean replacementMade = false;
            while (!replacementMade) {
                current = instToAddLabel.charAt(stringPos);
                if (current == ' ') {
                    replacementMade = true;
                } else {
                    lengthOfReplacement++;
                    stringPos--;
                }
            }
            int index = Integer.parseInt(instToAddLabel.substring(instToAddLabel.length() - lengthOfReplacement));
            String replacement = listOfLabels.get(index + pos + "");
            instToAddLabel = instToAddLabel.substring(0, instToAddLabel.length() - lengthOfReplacement) + replacement;
            assemblyInstructions.set(pos, instToAddLabel);
        }
    }
	
	public static void addLabels() {
        Set<Integer> set = new HashSet<>(posOfLabels);
        posOfLabels.clear();
        posOfLabels.addAll(set);
        posOfLabels.sort(Collections.reverseOrder());
        addToSet();
        putBranch();
        putLabels();
    }
	
	public static void addToSet(){
        for (int i = 0; i < posOfLabels.size(); i++) {
            int currentIndex = posOfLabels.get(i) - 1;
            listOfLabels.put(currentIndex + "", "Label" + (posOfLabels.size() - i));
        }
    }
	
	public static void putLabels(){
        for (int i = 0; i < posOfLabels.size(); i++) {
            int currentIndex = posOfLabels.get(i) - 1;
            assemblyInstructions.add(currentIndex, "Label" + (posOfLabels.size() - i) + ":");
        }
    }
	
	public static void addIndex(int pos, String replacement) {
        posOfLabels.add(pos);
        instructionsThatNeedLabels.add(replacement);
    }
}

