package disassembler;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

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
		//File file = new File(args[0]);
		FileInputStream sc = new FileInputStream("input.legv8asm.machine");
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

	public static String Btype(String instruction, String mnemonic) {
		int posOfCurrentInst = assemblyInstructions.size() + 1;
		int labelIndex;
		String assembly = mnemonic;
		String addr = instruction.substring(6,32);
		if(addr.charAt(0) == '0'){
			addr = String.valueOf(Integer.parseInt(addr, 2));
		}
		else{
			addr = String.valueOf(getTwosComplement(addr));
		}
		assembly = assembly + " " + addr;
		labelIndex = Integer.parseInt(addr) + posOfCurrentInst;
		addIndex(labelIndex, assembly);
		return assembly;		
	}

	public static String CBtype(String instruction, String mnemonic) {
		int posOfCurrentInst = assemblyInstructions.size() + 1;
		int labelIndex;
		String assembly = mnemonic;
		String addr = instruction.substring(8, 27);
		String rt = instruction.substring(27, 32);
		if(addr.charAt(0) == '0'){
			addr = String.valueOf(Integer.parseInt(addr, 2));
		}
		else{
			addr = String.valueOf(getTwosComplement(addr));
		}if (mnemonic.equals("B.")) {
			rt = String.valueOf(Integer.parseInt(rt, 2));
			switch (rt) {
			case "0" -> assembly += "EQ" + " " + addr;
			case "1" -> assembly += "NE" + " " + addr;
			case "2" -> assembly += "HS" + " " + addr;
			case "3" -> assembly += "LO" + " " + addr;
			case "4" -> assembly += "MI" + " " + addr;
			case "5" -> assembly += "PL" + " " + addr;
			case "6" -> assembly += "VS" + " " + addr;
			case "7" -> assembly += "VC" + " " + addr;
			case "8" -> assembly += "HI" + " " + addr;
			case "9" -> assembly += "LS" + " " + addr;
			case "10" -> assembly += "GE" + " " + addr;
			case "11" -> assembly += "LT" + " " + addr;
			case "12" -> assembly += "GT" + " " + addr;
			case "13" -> assembly += "LE" + " " + addr;
			}
		} else {
			rt = register(rt);
			assembly = assembly + " " + rt + ", " + addr;
		}
		labelIndex = Integer.parseInt(addr) + posOfCurrentInst;
		addIndex(labelIndex, assembly);
		return assembly;
	}

	public static String Rtype(String instruction, String mnemonic) {
		String assembly = mnemonic;
		String rm = instruction.substring(11, 16);
		rm = register(rm);
		String shamt = instruction.substring(16, 22);
		shamt = String.valueOf(Integer.parseInt(shamt, 2));
		String rn = instruction.substring(22, 27);
		rn = register(rn);
		String rd = instruction.substring(27, 32);
		rd = register(rd);
		assembly = switch (mnemonic) {
		case "PRNT" -> assembly + " " + rd;
		case "LSL", "LSR" -> assembly + " " + rd + ", " + rn + " #" + shamt;
		case "PRNL", "DUMP", "HALT" -> assembly;
		case "BR" -> assembly + " " + rn;
		default -> assembly + " " + rd + ", " + rn + ", " + rm;
		};
		return assembly;
	}

	public static String Dtype(String instruction, String mnemonic) {
		String assembly = mnemonic;
		String addr = instruction.substring(11, 20);
		addr = String.valueOf(Integer.parseInt(addr, 2));
		String rn = instruction.substring(22, 27);
		rn = register(rn);
		String rt = instruction.substring(27, 32);
		rt = register(rt);
		assembly = assembly + " " + rt + ", [" + rn + ", #" + addr + "]";
		return assembly;
	}

	public static String decode(String instruction) {
		String returnInstruction = "";
		String opcode = instruction.substring(0, 6);
		switch (opcode) {
		case "000101" ->  // b
		returnInstruction = Btype(instruction, "B");
		case "100101" ->  // bl
		returnInstruction = Btype(instruction, "BL");
		default -> {
			opcode = instruction.substring(0, 8);
			switch (opcode) {
			case "10110101" ->  // cbnz
			returnInstruction = CBtype(instruction, "CBNZ");
			case "10110100" ->  // cbz
			returnInstruction = CBtype(instruction, "CBZ");
			case "01010100" ->  // b.cond
			returnInstruction = CBtype(instruction, "B."); 
			default -> {
				opcode = instruction.substring(0, 10);
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
				//things that I have added
				default -> {
					opcode = instruction.substring(0, 11);
					switch (opcode) {
					case "10001011000" ->  // add
					returnInstruction = Rtype(instruction, "ADD");
					case "10001010000" ->  // and
					returnInstruction = Rtype(instruction, "AND");
					case "11010110000" -> //BR
					returnInstruction = Rtype (instruction, "BR");
					case "11001010000" -> //EOR
					returnInstruction = Rtype (instruction, "EOR");
					case "11111000010" -> //LDUR 
					returnInstruction = Dtype (instruction, "LDUR");
					case "11010011011" -> //LSL
					returnInstruction = Rtype (instruction, "LSL");
					case "11010011010" -> //LSR
					returnInstruction = Rtype (instruction, "LSR");
					case "10101010000" -> //ORR
					returnInstruction = Rtype (instruction, "ORR");
					case "11111000000" -> //STUR
					returnInstruction = Dtype (instruction, "STUR");
					case "11001011000" ->  // sub
					returnInstruction = Rtype(instruction, "SUB");
					case "11101011000" ->  // subs
					returnInstruction = Rtype(instruction, "SUBS");
					case "10011011000" -> //MUL
					returnInstruction = Rtype (instruction, "MUL");
					case "11111111101" ->  // prnt
					returnInstruction = Rtype(instruction, "PRNT");
					case "11111111100" ->  // prnl
					returnInstruction = Rtype(instruction, "PRNL");
					case "11111111110" ->  // dump
					returnInstruction = Rtype(instruction, "DUMP");
					case "11111111111" ->  // halt
					returnInstruction = Rtype(instruction, "HALT");
					}
				}
				}
			}
			}
		}
		}
		return returnInstruction;		
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