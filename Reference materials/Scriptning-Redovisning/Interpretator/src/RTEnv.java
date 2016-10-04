
import file.*;
/* ***************************************************************************
 Run Time Environment
 Contains:
 The administration of block instances
 The memory used in a block instance
 The arithmetic
 The temporary storage used in expressions
 Library routines (built in procedures)
 *************************************************************************** */

class RTEnv {

    static final int C_Integer = 1, C_Real = 2, C_Boolean = 4, C_Text = 3, C_Pointer = 5;
    static final int M_Ar = 0, M_Arp = 1, M_Arr = 2, M_Tmp = 3, M_Lit = 4, M_Reg = 5;
    static final int A_Adel = 1000;
    private IStruct InPut;
    private OpPrec TheOpPrec;
    private Ipass2 TheInterpreter;
    private static final int SF = 0, DF = 1, PF = 2, PC = 3, From = 4, TP = 5, Size = 6;
    private AR[] Stack = new AR[100];
    private Template[] Templates = new Template[100];
    private int[] Temporary = new int[20];
    private String[] Literals = new String[20];
    private Array[] ArrayArea = new Array[20];
    private int Cur = 0, Par = 0, Stkp = 0, Lit = 0, CurArr = 0;
    private boolean Cond;
    private int Lw, Hg, Ind; // Used to remember lim and ind for array
    /* ***************************************************************************
     Inner class AR
     representing the activation record of a block (function, object or just a
     "normal" block). Each instance of a block has its own AR in a stack/heap of AR:s
     The AR holds an administration part and a data part.
     The variables in a block is allocated in the data area. The administration of
     the block, the head of the block contains the following information:
     SF	- the static father, the index of the AR representing the instance of the SF
     DF	- the dynamic father, the index of the AR from where this block is called
     PF	- the chain of AR:s used for parameter transfer
     PC	- the block number of the block. This AR is the instance of this block
     From-the actual adress of the coe in this block instance (index of token)
     TP	- adress of the template (may be used to represent the "type")
     The size of the head is Size (=6)
     After the head is the memory used to hold the values of the variables, including
     function value and parameters. The contents of a variable in memory is:
     Integers	-	the bitpattern for the integer (just an integer)
     Real		-	the bitpattern for a float (handled as an integer)
     Text		-	the text is stored as a string in literal table and the index in the
     variable in memory
     NOTICE! Each block has a block number representing the block, but a block may
     have several instances at the same time. An instance is placed in the stack
     (heap for objects) and is identified by the adress in the stack ("index").
     *************************************************************************** */

    class AR {

        int[] Mem;

        AR(int sz) {
            Mem = new int[Size + sz];
        }

        void SetFrom(int A) {
            Mem[From] = A;
        }

        int GetFrom() {
            return Mem[From];
        }

        void Init(int sf, int pc) {
            Mem[SF] = sf;
            Mem[DF] = Cur;
            Mem[PC] = pc;
        }

        int GetSF() {
            return Mem[SF];
        }

        int GetDF() {
            return Mem[DF];
        }

        int GetPF() {
            return Mem[PF];
        }

        int GetBlock() {
            return Mem[PC];
        }

        void SetPF(int r) {
            Mem[PF] = r;
        }

        int GetVal(int r) {
            return Mem[Size + r];
        }

        void SetVal(int r, int v) {
            Mem[Size + r] = v;
        }

        void Dump() {
            System.out.println("AR head");
            System.out.println("SF:" + Mem[SF]);
            System.out.println("DF:" + Mem[DF]);
            System.out.println("OldPar:" + Mem[PF]);
            System.out.println("Start:" + Mem[PC]);
            System.out.println("From:" + Mem[From]);
            System.out.println("TP:" + Mem[TP]);
            System.out.println("Variables");
            for (int i = 6; i < Mem.length; i = i + 1) {
                System.out.println("Mem " + (i - 6) + ":" + Mem[i]);
            }
        }
    }
    /* ***************************************************************************
     Inner class Template
     is used to represent the static data of a block. In this implementation it contains
     only two entries:
     BlkNr - The number identifying the block, and thus the code address.
     Size	- The the size requirements for variables in the block.
     *************************************************************************** */

    class Template {

        int BlkNr, Size;

        Template(int nr, int s) {
            BlkNr = nr;
            Size = s;
        }
    }
    /* ***************************************************************************
     Inner class Array

     *************************************************************************** */

    class Array {

        int Low, High;
        int[] Contents;

        Array(int L, int H) {
            Low = L;
            High = H;
            Contents = new int[H - L + 1];
        }
    }
    /* ***************************************************************************
     The constructor
     The input object is registered in the environment.
     A first AR is pushed on the stack top representing a fictive surrounding block.
     *************************************************************************** */

    RTEnv(IStruct IO) {
        Stack[0] = new AR(0);
        Stkp = 1;
        InPut = IO;
        StdInit();
    }
    /* ********************************************
     Setup environment.
     The Operator precedence object and the interpreter object is registered
     in the environment.
     ******************************************** */

    public void Init(OpPrec P, Ipass2 Q) {
        TheOpPrec = P;
        TheInterpreter = Q;
    }
    /* ********************************************
     Enter a block.
     If the template is already registerd - do nothing
     else create a template and register in the environment.
     ******************************************** */

    void Enter(int n, int sz) {
        Template Tm;
        Tm = Templates[n];
        if (Tm == null) {
            Templates[n] = new Template(n, sz);
        } else {
        }
    }
    /* ********************************************
     Return from a block.
     When a return is made from a block (function or other block)
     we will reset the input in order to continue interpretation
     in the correct place. The operator precedence must also know
     which is the current block for symbol search.
     ******************************************** */

    void Retur() {
        int RetBlock, RetAdr;
        if (Cur == 1) {
            RetAdr = 0;
            RetBlock = -1;
        } else {
            RetAdr = Stack[Cur].GetFrom();
            Cur = Stack[Cur].GetDF();
           // System.out.println("Retur - Dynamic Father: " + Cur);
            RetBlock = Stack[Cur].GetBlock();
            InPut.ContBlock(RetBlock, RetAdr);
            TheOpPrec.ResetBlock(RetBlock);
        }

    }
    /* ********************************************
     Allocate memory for a block (Activation Record)
     and put this in the correct structure.
     ******************************************** */

    void Allocate(int n, int lev) {
        System.out.println("Allocate Block(AR)\n\tBlockNr: " + n + "\n\tLevel: " + lev);
        int s, sf, l;
        AR P;
        s = Templates[n].Size;
        Stack[Stkp] = new AR(s);
        P = Stack[Stkp];
        P.SetPF(Par);
        Par = Stkp;
        Stkp = Stkp + 1;
        l = lev;
        sf = Cur;
        while (l > 0) {
            sf = Stack[sf].GetSF();
            l = l - 1;
        }
        P.Init(sf, n);
    }
    /* ********************************************
     Deallocate an AR.
     It is removed from the stack, meaning marked for reuse.
     ******************************************** */

    void DeAllocate() {
         Stkp = Par;
        Par = Stack[Par].GetPF();
    }
    /* ********************************************
     Execute a block.
     Set the allocated block as current, remember the
     return adress (index in token sequence) and start
     interpretation of the block.
     ******************************************** */

    void Execute(int retadr) {
        AR P;
        int BNr;
        P = Stack[Par];
        P.SetFrom(retadr);
        Cur = Par;
        BNr = P.GetBlock();
        TheInterpreter.InterBlock(BNr);//start execution of block
    }
    /* ********************************************
     Error!
     ******************************************** */

    void Error() {
    }
    /* ********************************************
     Create string literal
     ******************************************** */

    public int CreateLiteral(String S) {
        int L;
        L = S.length();
        Literals[Lit] = S.substring(1, L - 1);
        Lit = Lit + 1;
        return Lit - 1;
    }

    public int CreateText(String S) {
        Literals[Lit] = S;
        Lit = Lit + 1;
        return Lit - 1;
    }
    /* ********************************************
     Transfer value from address A2 to address A1
     It is the contents (bitpattern, index) that is moved.
     ******************************************** */

    void SetValue(Adr A1, Adr A2) {
        int Val;
        int ty1, ty2;
        float F;
        ty1 = A1.a4;
        ty2 = A2.a4;
        Val = GetVal(A2);
        if (ty1 == ty2) {
        } else if (ty2 == C_Integer) {
            F = (float) Val;
            Val = Float.floatToIntBits(F);
        } else if (ty2 == C_Real) {
            F = Float.intBitsToFloat(Val);
            Val = (int) F;
        } else {
        }
        SetVal(Val, A1);
    }
    /* ********************************************
     Calculate: A1 op A2 --> A3
     We are using the ordinary arithmetic of java.
     For integers there are no problems, integers are just 
     stored as integers (int) but float are stored as integers
     representing the bitpattern of a float. We must convert
     in a correct way!
     ******************************************** */

    void Calc(int op, Adr A1, Adr A2, Adr A3) {
        int V1, V2, V = 0;
        float Vr1, Vr2, Vr = 0;
        int ty1, ty2, ty3, ty;
        ty1 = A1.a4;
        ty2 = A2.a4;
        ty3 = A3.a4;
        V1 = GetVal(A1);
        V2 = GetVal(A2);
        if (ty1 == C_Integer && ty2 == C_Integer) {
            ty = C_Integer;
            switch (op) {
                case 4:
                    V = V1 + V2;
                    break;
                case 5:
                    V = V1 - V2;
                    break;
                case 6:
                    V = V1 * V2;
                    break;
                case 7:
                    Vr = (float) V1 / (float) V2;
                    ty = C_Real;
                    break;
                default:
                    V = 0;
                    break;
            }
        } else {
            if (ty1 == C_Integer) {
                Vr1 = (float) V1;
            } else {
                Vr1 = Float.intBitsToFloat(V1);
            }
            if (ty2 == C_Integer) {
                Vr2 = (float) V2;
            } else {
                Vr2 = Float.intBitsToFloat(V2);
            }
            ty = C_Real;
            switch (op) {
                case 4:
                    Vr = Vr1 + Vr2;
                    break;
                case 5:
                    Vr = Vr1 - Vr2;
                    break;
                case 6:
                    Vr = Vr1 * Vr2;
                    break;
                case 7:
                    Vr = Vr1 / Vr2;
                    break;
                default:
                    Vr = 0;
                    break;
            }
        }
        if (ty == C_Integer) {
            if (ty3 == C_Integer) {
            } else {
                Vr = (float) V;
                V = Float.floatToIntBits(Vr);
            }
        } else {
            if (ty3 == C_Integer) {
                V = (int) Vr;
            } else {
                V = Float.floatToIntBits(Vr);
            }
        }
        SetVal(V, A3);
    }
    /* ********************************************
     Compare: A1 op A2 --> Cond
     The same is true for comparsion as for arithmetic.
     ******************************************** */

    void cmp(int op, Adr A1, Adr A2) {
        int V1, V2;
        float Vr1, Vr2;
        int ty1, ty2;
        boolean V;
        ty1 = A1.a4;
        ty2 = A2.a4;
        V1 = GetVal(A1);
        V2 = GetVal(A2);
        if (ty1 == C_Integer && ty2 == C_Integer) {
            switch (op) {
                case 12:
                    V = V1 >= V2;
                    break;
                case 13:
                    V = V1 > V2;
                    break;
                case 14:
                    V = V1 <= V2;
                    break;
                case 15:
                    V = V1 < V2;
                    break;
                case 16:
                    V = V1 == V2;
                    break;
                case 17:
                    V = V1 != V2;
                    break;
                default:
                    V = false;
                    break;
            }
        } else {
            if (ty1 == C_Integer) {
                Vr1 = (float) V1;
            } else {
                Vr1 = Float.intBitsToFloat(V1);
            }
            if (ty2 == C_Integer) {
                Vr2 = (float) V2;
            } else {
                Vr2 = Float.intBitsToFloat(V2);
            }
            switch (op) {
                case 12:
                    V = Vr1 >= Vr2;
                    break;
                case 13:
                    V = Vr1 > Vr2;
                    break;
                case 14:
                    V = Vr1 <= Vr2;
                    break;
                case 15:
                    V = Vr1 < Vr2;
                    break;
                case 16:
                    V = Vr1 == Vr2;
                    break;
                case 17:
                    V = Vr1 != Vr2;
                    break;
                default:
                    V = false;
                    break;
            }
        }
        Cond = V;
    }

    boolean Condition() {
        return Cond;
    }
    /* ********************************************
     Array handling

     ******************************************** */

    public void ALim(int I, Adr A1, Adr A2) {
        Lw = GetVal(A1);
        Hg = GetVal(A2);
    }

    public void AAllocate(Adr A) {
        int V;
        V = CurArr;
        CurArr = CurArr + 1;
        ArrayArea[V] = new Array(Lw, Hg);
        SetVal(V, A);
    }

    public void AIndex(Adr A) {
        Ind = GetVal(A);
    }

    public void AAdr(Adr A1, Adr A2) {
        int I, L, H, Ri;
        Array A;
        I = GetVal(A1);
        A = ArrayArea[I];
        L = A.Low;
        H = A.High;
        if (Ind >= L && Ind <= H) {
            Ri = Ind - L;
        } else {
            Ri = 0;
        }
        Ri = A_Adel * I + Ri;
        SetVal(Ri, A2); // Value is a pointer (index)
    }

    /* ********************************************
     Local methods used to get and set values in 
     memory, temporay and "register"
     ******************************************** */
    int GetVal(Adr A) {
        int M, N, V = 0, ar, rel, typ;
        M = A.a1;
        rel = A.a3;
        typ = A.a4;
        Array TA;
        switch (M) {
            case 0: // in memory (Activation Record)
                N = A.a2;
                ar = Cur;
                if (N < 0) {
                    V = Stack[Par].GetVal(rel);
                } else {
                    while (N > 0) {
                        ar = Stack[ar].GetSF();
                        N = N - 1;
                    }
                    V = Stack[ar].GetVal(rel);
                }
                break;
            case 2:	// in array
                N = A.a2;
                V = Temporary[N];
                N = V / A_Adel;
                V = V % A_Adel;
                TA = ArrayArea[N];
                V = TA.Contents[V];
                break;
            case 3: // in temporary variable
                V = Temporary[rel];
                break;
            case 4: // literal
                if (typ == 1) {
                    V = rel;
                } else if (typ == 2) {
                    V = rel;
                } else if (typ == 3) {
                    V = rel;
                } else {
                    V = 0;
                }
                break;
            case 5: // "register"
                V = 0;
                break;
            default:
                V = 0;     // should not be used here
                break;
        }
        return V;
    }

    void SetVal(int V, Adr A) {
        int M, rel, N, ar, typ;
        Array TA;
        int I;
        M = A.a1;
        rel = A.a3;
        typ = A.a4;
        switch (M) {
            case 0: // in memory (Activation Record)
                N = A.a2;
                ar = Cur;
                if (N < 0) {
                    Stack[Par].SetVal(rel, V);
                } else {
                    while (N > 0) {
                        ar = Stack[ar].GetSF();
                        N = N - 1;
                    }
                    Stack[ar].SetVal(rel, V);
                }
                break;
            case 2:	// in array
                N = A.a2;
                I = Temporary[N];
                N = I / A_Adel;
                I = I % A_Adel;
                TA = ArrayArea[N];
                TA.Contents[I] = V;
                break;
            case 3: // in temporary variable
                Temporary[rel] = V;
                break;
            case 4: // literal, its not permitted to store into a literal!!!
                break;
            case 5: // "register"
                break;
            default:	 		// should not be used here
                break;
        }
    }

    void Finish() {
    }
    /* ********************************************
     Run time library procedures.
     The run time procedures are implemented in java.
     Instead of transmitting parameters in registers
     we are using java variables.
     We have to convert bitpatterns just as for arithmetic.
     ******************************************** */
    infile StdFile;
    int IV;
    float RV;
    String TV;
    int IP;
    float RP;
    String TeP;

    public void TransfStdPar(int par) {
        IP = par;
    }

    public void TransfStdPar(float par) {
        RP = par;
    }

    public void TransfStdPar(String par) {
        TeP = par;
    }

    public void TransfStdPar(Adr par) {
        IV = GetVal(par);
        if (par.a4 == C_Integer) {
        } else if (par.a4 == C_Real) {
            RV = Float.intBitsToFloat(IV);
        } else if (par.a4 == C_Text) {
            TV = Literals[IV];
        } else {
        }
    }

    public void SetIV(Adr A) {
        SetVal(IV, A);
    }

    public float GetRV() {
        return RV;
    }

    public String GetTV() {
        return TV;
    }

    public void ExecLib(int f) {
        switch (f) {
            case 1:
                ReadI();
                break;   //readint
            case 2:
                ReadR();
                break;   //readreal
            case 3:
                PrintI();
                break;   //writeint
            case 4:
                PrintR();
                break;   //writereal
            case 5:
                PrintT();
                break;   //writetext
            case 6:
                PrintL();
                break;   //writeline
            case 7:
                ReadT();
                break;   //readtext
            case 8:
                break;
            case 9:
                break;
            default:
                break;
        }
    }

    private void StdInit() {
        StdFile = new infile();
        StdFile.Open();
    }

    private void PrintI() {
        System.out.print(" " + IV);
    }

    private void PrintR() {
        System.out.print(" " + RV);
    }

    private void PrintT() {
        System.out.print(TV);
    }

    private void PrintL() {
        System.out.println(" ");
    }

    private void ReadI() {
        IV = StdFile.ReadInt();
    }

    private void ReadR() {
        RV = (float) StdFile.ReadReal();
        IV = Float.floatToIntBits(RV);
    }

    private void ReadT() {
        TV = StdFile.ReadText(80);
        IV = CreateText(TV);
    }

    private void Print(String S, int V) {
        System.out.println("Print " + S + " " + V);
    }

    public void DumpMem() {
        AR P;
        System.out.println("****** AR-stack dump ******");
        System.out.println("Cur:" + Cur + " Par:" + Par);
        for (int i = 0; i < 5; i = i + 1) {
            P = Stack[i];
            if (P != null) {
                System.out.println("Dump of AR:" + i);
                P.Dump();
            }
        }
        System.out.println("****** End of dump ******");
    }

    public int DebVal(Adr A) {
        return GetVal(A);
    }
}
