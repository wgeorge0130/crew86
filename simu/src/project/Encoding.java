package project;

public class Encoding{
    private static int numNoArg;
    private boolean noArg;
    private byte opcode;
    private int arg;

    public Encoding(boolean nArg, byte opcd, int rg){
        noArg = nArg;
        opcode = opcd;
        arg = rg;
        if(noArg == true) numNoArg++;
    }

    public static int getNumNoArg() {
        return numNoArg;
    }

    public boolean isNoArg() {
        return noArg;
    }

    public byte getOpcode() {
        return opcode;
    }

    public int getArg() {
        return arg;
    }

    public static void resetNumNoArg(){
        numNoArg = 0;
    }

    public String toString() {
        if(noArg) return "[" + opcode + "]";
        return "[" + opcode + ", " + arg + "]";
    }
    
    public long asLong() {
    	long longOp = opcode;
    	// treat the byte as a long
    	long longArg = arg;
    	// shift left 32 times, i.e. multiply by 2 to-the-power 32
    	longOp = longOp << 32;
    	// treat arg as a long but if negative, remove the top
    	// 32 bits of the 2-s complement form
    	longArg = longArg & 0x00000000FFFFFFFFL;
    	//join the upper 32 bits and the lower 32 bits
    	return longOp | longArg;
    }
    	public static byte opFromLong(long lng) {
    		return (byte)(lng >> 32);
    	}
    	public static int argFromLong(long lng) {
    		return (int)(lng & 0x00000000FFFFFFFFL);
    	}

    public static void main(String[] args) {
    	 Encoding test = new Encoding(false, (byte)25, -15);
    	 long val = test.asLong();
    	 System.out.println(val);
    	 System.out.println(Long.toBinaryString(val));
    	 System.out.println(opFromLong(val));
    	 System.out.println(argFromLong(val));
    	 Encoding test1 = new Encoding(false, (byte)17, 15);
    	 val = test1.asLong();
    	 System.out.println(val);
    	 System.out.println(Long.toBinaryString(val));
    	 System.out.println(opFromLong(val));
    	 System.out.println(argFromLong(val));
    	}
}