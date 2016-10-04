
import java.util.ArrayList;
import komp.*;
/* **************************************************************************
 This class is the real expert on Operator Precedence and is a subclass
 of the abstract class OperatorPrecedence in the package komp.
 This superclass is implementing the common features of Op.prec.
 and here we have to add the language specific parts.
 ************************************************************************** */

class OpPrec extends OperatorPrecedence {

    static final int AM_Become = 1, AM_New = 2, AM_Add = 3, AM_Mul = 4, AM_Cmp = 5, AM_Func = 6;
    static final int AM_Array = 7, AM_Class = 8, AM_Comma = 9, AM_Lpar = 10, AM_Rpar = 11;
    static final int AM_Fpar = 12, AM_Dot = 13, AM_Empty = 14;
    static final int K_Simple = 0, K_Array = 1, K_Func = 2, K_Funcval = 3, K_Class = 4, K_Ref = 5;
    static final int TC_Plus = 13, TC_Minus = 14, TC_Mul = 15, TC_Div = 16;
    static final int C_Asis = 0, C_Any = 0, C_Void = 0;
    static final int C_Integer = 1, C_Real = 2, C_Boolean = 4, C_Text = 3, C_Pointer = 5;
    static final int C_G0 = 0, C_G5 = 5, C_O0 = 8, C_O1 = 9, C_O2 = 10, C_O3 = 11, C_Sp = 24;
    /* ************************************************
     *		Intermediate Code
     *
     ************************************************ */
    static final int M_Ar = 0, M_Arp = 1, M_Arr = 2, M_Tmp = 3, M_Lit = 4, M_Reg = 5;
    static final int M_Unspec = 0, M_Readint = 1, M_Readreal = 2, M_Readtext = 7;
    static final int M_Writeint = 3, M_Writereal = 4, M_Writetext = 5, M_Writeln = 6;
    static final int M_Local = 0, M_Block = 1, M_Template = 2, M_Sys = 3, M_Litadr = 4;

    static final int MAX_TEMPS = 20;
    Boolean[] allocatedTemporaries = new Boolean[MAX_TEMPS];
    
    
    private int LitId = -1, TempNr = -1;
    IStruct IO;
    Op LastInputOp;
    SymSearch SM;
    Ipass2 Rec;
    RTEnv Env;
    /* *******************************************************************
     *		Constructor
     ******************************************************************* */

    OpPrec(Interface I, Control C, RTEnv V, Ipass2 RD) {
        super(I, C);					// use the constructor in super class
        Env = V;						// remember environment
        Rec = RD;						// remember the recursive descent object
        IO = (IStruct) I;				// remember input IStruct subclass of Interface
        SM = new SymSearch(IO);	// create an object for symbol search
        
        for(int i = 0; i < MAX_TEMPS; i++)
            allocatedTemporaries[i] = false;
    }
    
    int AllocateTemporary()
    {
        for(int i = 0; i < MAX_TEMPS; i++)
        {
            if(allocatedTemporaries[i] == false)
            {
                allocatedTemporaries[i] = true;
                return i;
            }
        }
        
        //Shouldn't get here
        return -1;
    }
    
    void UnallocateTemporary(int rel)
    {
        allocatedTemporaries[rel] = false;
    }
    
    /* *******************************************************************
     *		Recursive descent interface to translating routines.
     ******************************************************************* */

    public void InitBlock(int N) {
        SM.SetBlock(N);
    }

    public int GetBlockNr()
    {
        return SM.GetBlockNr();
    }
    public void ResetBlock(int N) {
        Token T;
        Op P;
        SM.ResetBlock(N);
        T = (Token) IO.GetToken();
        P = MakeOp(T);
        SetCurrentOp(P);
        SetCurrentToken(T);
    }
    /* *******************************************************************
     Interpreting an expression, Translate in super class is used
     ******************************************************************* */

    public Token IntExpr(Token T) {
        Token TT;
        LastInputOp = null;
        TT = (Token) Translate(T);
        return TT;
    }
    /* *******************************************************************
     Interpreting a condition. This will be used for a jump if false.
     Translate in super class is used
     ******************************************************************* */

    public Token IntJumpFalse(int L) {
        Op P;
        Token T;
        LastInputOp = null;
        P = null;//P=new Lab(null,M_Local,L); use then Lab constructor is implemented.
        PutOperand((Operand) P);
        return (Token) Translate(IO.GetToken());
    }

    /* *******************************************************************
     *		MakeOp
     *	This routine is used to translate a token into an Op 
     *	(Operand or Operator)
     ******************************************************************* */
    public Op MakeOp(Lexeme L) {
        Token T = (Token) L;
        Op P = null;

        //Operand
         
        if (T.Id()) {
            System.out.println("MakeOp->T.Id");
            System.out.println("\tText: " + T.Text);
            
            if(T.Else() || T.Then())
                return new Stop();
            
            P = DSearch(T);
        } 
        else if (T.Literal()) {
            System.out.println("MakeOp->T.Literal");
            System.out.println("\tText: " + T.Text);

            P = new Literal(T.Text, 0, C_Integer);
        } 
        else if(T.Else() || T.Then())
            return new Stop();
        else if(T.Comma())
            return new Comma();
        else if (T.Op()) {
            System.out.println("MakeOp->T.Op");
            System.out.println("\tText: " + T.Text);
            
            if(T.Become())
                P = new Become();
            else if(T.Semi())
                P = new Stop();
            else if(T.Plus())
                P = new ArithmOp(TC_Plus);
            else if(T.Minus())
                P = new ArithmOp(TC_Minus);
            else if(T.Mul())
                P = new ArithmOp(TC_Mul);
            else if(T.Div())
                P = new ArithmOp(TC_Div);
            else if(T.Gteq())
                P = new Compare(12);
            else if(T.Gt())
                P = new Compare(13);
            else if(T.Lesseq())
                P = new Compare(14);
            else if(T.Less())
                P = new Compare(15);
            else if(T.Eq())
                P = new Compare(16);
            else if(T.Neq())
                P = new Compare(17);
            else if(T.Lpar())
                P = new Lpar();
            else if(T.Rpar())
                P = new Rpar();
        }
        else
        {
            System.out.println("Something else");
        }
        
        LastInputOp = P; 
        return P;
    }

    /* **********************************************************
     This method is used to get the result operand, with mode and type
     according to rules given in the matrices below.
     Type matrix used to determine the type of the result
     The possible types are:
     input		  result
     A - any type	/ any
     I - integer	/ integer or if operation is div then real
     R - real		/ real
     T - text		/ any (text not used as result)
     B - boolean	/ any (boolean not used as result)
     P - an address	/ any (address not used as result)
     E - (not possible)	/ any
     opnd2
                  A   I   R   T   B   P
     opnd1  A	{'A','A','A','A','A','A'}
            I	{'A','I','R','E','E','E'}
            R	{'A','R','R','E','E','E'}
            T	{'A','E','E','T','E','E'}
            B	{'A','E','E','E','B','E'}
            P	{'A','E','E','E','E','P'}
     Mode matrix used to determine the addressing mode of the result
     The possible modes are:
     AR  -  Memory, AR on stack
     ARP -  Memory, AR pointed at by TMP		*not used here
     ARR -  Memory, array pointed at by TMP             *not used here
     TMP -  Temporary variable in memory
     LIT -  Literal
     REG -  Special system dependent("Register") *not used here
     The possible result actions are:
     N   -  A new temporary (on stack!)
     F   -  First operand temporary is used
     S   -  Second operand temporary is used
     R   -  First operand temporary is used, and second is released (from stack!)
     A   -  First operand temporary is used, see below!
     B   -  Second operand temporary is used, see below!
     D   -  First operand temporary is used, second is released (stack!), see below
     L   -  A new literal computed at compile time(N may be used!)
     ?   -  System dependent action (not used here!)
     The actions A,B and D are almost the same as F,S and R. The difference is that the
     operands are "indirect operands", meaning the address of the operand in temp and the
     result operand shall be a "direct operand", meaning the result in temporary.
     opnd 2
                AR  ARP ARR TMP LIT REG
     opnd1  AR {'N','N','B','S','N','?'},
            ARP{'N','N','B','S','N','?'},
            ARR{'A','A','D','D','A','?'},
            TMP{'F','F','R','R','F','?'},
            LIT{'N','N','B','S','L','?'},
            REG{'?','?','?','?','?','?'}
     ********************************************************** */
    private MyOperand MakeResOp(MyOperand P1, MyOperand P2, int C) {
        MyOperand outOp = null;
        /*
        AR  -  Memory, AR on stack
        TMP -  Temporary variable in memory
        LIT -  Literal
        REG -  Special system dependent("Register") *not used here
        The possible result actions are:
        N   -  A new temporary (on stack!)
        F   -  First operand temporary is used
        S   -  Second operand temporary is used
        R   -  First operand temporary is used, and second is released (from stack!)
        L   -  A new literal computed at compile time(N may be used!)
        ?   -  System dependent action (not used here!)
        The actions A,B and D are almost the same as F,S and R. The difference is that the
        operands are "indirect operands", meaning the address of the operand in temp and the
        result operand shall be a "direct operand", meaning the result in temporary.
        opnd 2
                   AR  TMP LIT
        opnd1  AR {'N','S','N'},
               TMP{'F','R','F'},
               LIT{'N','S','L'},
        */
        char resType = GetResMode(P1.Mode, P2.Mode);
        
        switch(resType)
        {
            case 'N':
                outOp = new Temporary(AllocateTemporary());
                break;
            case 'S':
                outOp = P2;
                break;
            case 'F':
                outOp = P1;
                break;
            case 'R':
                UnallocateTemporary(((Temporary)P2).Rel);
                outOp = P1;
                break;
            case 'L':
                outOp = new Temporary(AllocateTemporary());
                break;
            default:
                outOp = new Temporary(AllocateTemporary());
                break;
        }
        
        /*
        A - any type        / any
        I - integer         / integer or if operation is div then real
        R - real            / real
        T - text            / any (text not used as result)
        B - boolean         / any (boolean not used as result)
        P - an address      / any (address not used as result)
        E - (not possible)  / any
        opnd2
                   A   I   R   T   B   P
        opnd1  A {'A','A','A','A','A','A'}
               I {'A','I','R','E','E','E'}
               R {'A','R','R','E','E','E'}
               T {'A','E','E','T','E','E'}
               B {'A','E','E','E','B','E'}
               P {'A','E','E','E','E','P'}
        */
        char resMode = ResType(P1.Type, P2.Type);

        switch(resMode)
        {
            case 'A':
                outOp.Type = C_Any;
                break;
            case 'I':
                outOp.Type = C_Integer;
                break;
            case 'R':
                outOp.Type = C_Real;
                break;
            case 'T':
                outOp.Type = C_Text;
                break;
            case 'B':
                outOp.Type = C_Boolean;
                break;
            case 'P':
                outOp.Type = C_Pointer;
                break;
            case 'E':
                outOp.Type = C_Void;
                break;
        }
        
        return outOp;
    }
    /* **********************************************************
     Make literal calculations.
     In a compiler this may be done at compile time (efficient) or
     code may be generated and computation left to run time.
     In an interpreter it has to be done!
     ********************************************************** */

    private Literal MakeLit(Literal Q1, Literal Q2, int C) {
        Literal R = null;
        /* ### Code ### */
        return R;
    }

    private char ResType(int Ty1, int Ty2) {
        return ((MyControl) Ctrl).GetResType(Ty1, Ty2);
    }

    private char GetResMode(int M1, int M2) {
        return ((MyControl) Ctrl).GetResMode(M1, M2);
    }
    /* **********************************************************
     Transfer a parameter to a function.
     The operand to transfer (P2) is taken from operand stack
     The parameter operand (P1) is taken from operator (function) P,
     the parameter has to be searched in the function to get the correct type.
     A system function may have a parameter with type "as-is", meaning the type
     of the actual parameter, actual type has to be passed on to the "search".
     Notice that if operand is in temporary this has to be released!
     ********************************************************** */

    public void Transfer(Operator P) {
        MyOperand P1, P2;
        Adr A1, A2;
        /*	Something like this ?????????
         P2=(MyOperand)GetOperand();
         P1=(MyOperand)((FunPar)P).GetParOp(P2.GetType());
         A2=P2.Address(0);
         A1=P1.Address(0);
         if(P1.GetMode()==M_Reg){Env.TransfStdPar(A2);}
         else{Env.SetValue(A1,A2);}
         if(P2 instanceof Temporary) {ReleaseTemp();} 
         */
        P2=(MyOperand)GetOperand();
        P1=(MyOperand)((FunPar)P).GetParOp(P2.Type);
        A2=P2.Address(0);
        A1=P1.Address(0);
        Env.SetValue(A1,A2);
        System.out.println("");
        System.out.println("Transfer");
        System.out.print("Adress 1  type " + P1.Type); A1.Debug();
        System.out.print("Adress 2  type " + P1.Type); A2.Debug();
        System.out.println("");
        if(P2 instanceof Temporary)
            UnallocateTemporary(((Temporary)P2).Rel);
        
        
    }
    /* ******************************************************** 
     Get an operator of type Function Paren

     Used by MyControl, a left paren after a function operator
     is regarded as the start of a parameter list and not just
     as a paren.
     ******************************************************** */

    public Operator NewFunPar(Operator P) {
        return new FunPar(P);
    }

    /* *******************************************************************
     *  Search the symbol tables for a given symbol.
     If not found in current table the SF-chain is followed until
     symbol found. I not in tables the system functions are searched
     and if not there null is returned.
     The level in the chain may be retrieved from SymSearch object.
     ******************************************************************* */
    Op DSearch(Token T) {
        int id;
        int level;
        
        Symbol S;
        Op P = null;
        
        id = T.GetId();
        
        S = SM.Search(id);
        level = SM.GetLevel();
        
            int k;
            k = S.GetKind();
            if(k == K_Simple || k == K_Ref)
            {
                P = new ActivationRecord(S, level);
            }
            else if(k == K_Func)
            {
                P = new UserFunction(S, level);
            }
            else if(k == K_Funcval)
            {
                if(LastInputOp == null)
                {
                    P = new ActivationRecord(S, level);
                }
                else
                {
                    S = SM.ContSearch(id);
                    if(S!=null)
                    {
                        k=S.GetKind();
                    }
                    else
                    {
                        k = 0;
                    }
                    
                    level = SM.GetLevel();
                    if(k==K_Func)
                    {
                        P = new UserFunction(S, level);
                    }
                }
            }
            
            return P;
        
            
    }

    Op GetPar(int BlkId, int Nr) {
        return new ActivationRecordParameter(SM.ParSearch(BlkId, Nr), 0);
    }

    /* *******************************************************************
     *		Definition of Operands and Operators
     *	A type hierarchy of operators and operands is defined.
     *
     *	Op
     *		Operand
     *			MyOperand
     *				ActivationRecord
     *				Temporary
     *				Literal
     *				Label
     *		Operator
     *			ConstOperator
     *				Lpar
     *				Rpar
     *				Stop
     *				Comma
     *			Become
     *			ArithmOp
     *			Compare
     *			UserFunction
     *			SystemFunction
     *			FunPar
     *
     *		?More ?????
     ******************************************************************* */
    private Lpar TheLpar = new Lpar();
    private Rpar TheRpar = new Rpar();
    private Stop TheStop = new Stop();
    private Comma TheComma = new Comma();

    abstract class MyOperand extends Operand {

        int Mode, Garp, Rel, Type;
        Symbol Sym;

        public int GetIndex() {
            return 0;
        }

        public void Address() {
        }

        public Adr Address(int d) { 
            return new Adr(Mode, Garp, Rel, Type);
        }
    }

    class ActivationRecord extends MyOperand {
        ActivationRecord(Symbol s, int level)
        {
            this.Sym = s;
            this.Garp = level;
            this.Rel = s.GetRel();
            this.Type = s.GetType();
            this.Mode = M_Ar;
        }
    }
    
    class ActivationRecordParameter extends MyOperand {
        ActivationRecordParameter(Symbol s, int type)
        {
                this.Garp = -1;
                this.Mode = M_Ar;
                this.Sym = s;
            if(s!=null)
            {
                this.Rel = s.GetRel();
                this.Type = s.GetType();
            }
            else
            {
               this.Rel = 0;
               this.Type = type;
            }
            
        }
    }

    class Temporary extends MyOperand {
        Temporary(int rel)
        {
            Rel = rel; //Rel is used in Env for SetValue
            Mode = M_Tmp;
            //Val = value;
            Garp = 0;
            Type = C_Integer;
        }
        
        int GetRel()
        {
            return Rel;
        }
    }

    class IndirectTemporary extends MyOperand {
        /* ### Constructor(s) ### */
    }

    class Literal extends MyOperand {

        String Val;
        
        Literal(String value, int level, int type)
        {
            Val = value;
            Garp = level;
            Rel = Integer.parseInt(value);
            Type = type;
            Mode = M_Lit;
        }

        public String GetVal() {
            return Val;
        }
    }

    class Lab extends MyOperand {
        /* ### Constructor(s) ### */
    }

    abstract class ConstOperator extends Operator {

        public void Exec() {
        }

        public void Prolog(Op P) {
        }
        /*  ??? More ??? */
    }

    class Lpar extends ConstOperator {

        public int GetIndex() {
            return AM_Lpar;
        }
    }

    class Rpar extends ConstOperator {

        public int GetIndex() {
            return AM_Rpar;
        }
    }

    class Stop extends ConstOperator {

        public int GetIndex() {
            return AM_Empty;
        }
    }

    class Comma extends ConstOperator {

        public int GetIndex() {
            return AM_Comma;
        }
    }

    class Become extends Operator {

        public int GetIndex() {
            return AM_Become;
        }

        public void Exec() {
            System.out.println("Become:");
            Op op2 = GetOperand();
            ActivationRecord op1 = (ActivationRecord)GetOperand(); //op1 will always be AR
            
            if(op2 instanceof Literal)
            {
                System.out.println("\top2(Literal) = " + ((Literal)op2).GetVal());
                System.out.println("\top1(" + op1.Sym.Tok.Text + ") = " + Env.GetVal(op1.Address(0)));
                
                Env.SetVal(Integer.parseInt(((Literal)op2).GetVal()), op1.Address(0));
            }
            else if (op2 instanceof ActivationRecord)
            {
                System.out.println("\top2(AR) = " + Env.GetVal(op1.Address(0)));
                System.out.println("\top1(" + op1.Sym.Tok.Text + ") = " + Env.GetVal(op1.Address(0)));
                
                Env.SetValue(op1.Address(0), ((ActivationRecord)op2).Address(0));
            }
            else if (op2 instanceof Temporary)
            {
                System.out.println("\top2(Temp) = " + Env.GetVal(((Temporary)op2).Address(0)));
                System.out.println("\top1(" + op1.Sym.Tok.Text + ") = " + Env.GetVal(op1.Address(0)));
                
                Env.SetValue(op1.Address(0), ((Temporary)op2).Address(0));
                
                UnallocateTemporary(((Temporary)op2).Rel);
            }
            
            System.out.println("\top1: " + Env.GetVal(op1.Address(0)));
        }

        public void Prolog(Op P) {
        }
    }

    class ArithmOp extends Operator {
        int type;
        
        ArithmOp(int type)
        {
            this.type = type;
        }

        public int GetIndex() {
            if(type == TC_Plus || type == TC_Minus)
                return AM_Add;
            else if (type == TC_Mul || type == TC_Div)
                return AM_Mul;
            else
                return -1;
        } // Change this, its more complex!

        public void Exec() 
        {
            switch(type)
            {
                case TC_Plus:
                    System.out.println("ArithmOp(TC_Plus)");
                    break;
                case TC_Minus:
                    System.out.println("ArithmOp(TC_Minus)");
                    break;
                case TC_Div:
                    System.out.println("ArithmOp(TC_Div)");
                    break;
                case TC_Mul:
                    System.out.println("ArithmOp(TC_Mul)");
                    break;
            }
            
            MyOperand op2 = (MyOperand)GetOperand();
            MyOperand op1 = (MyOperand)GetOperand();
            
            System.out.println("\top1 = " + Env.GetVal(op1.Address(0)));
            System.out.println("\top2 = " + Env.GetVal(op2.Address(0)));
            
            MyOperand resOp = MakeResOp(op1, op2, C_G0);
            
            Env.Calc(type - 9, op1.Address(0), op2.Address(0), resOp.Address(0));
            
            System.out.println("\tresOp = " + Env.GetVal(resOp.Address(0)));
            
            PutOperand(resOp);
        }

        public void Prolog(Op P) {
        }
    }

    class Compare extends Operator {

        int type;
        public Compare(int type)
        {
            this.type = type;
        }
        public int GetIndex() {
            return AM_Cmp;
            
        }

        public void Exec() { 
        /* ### Code ### */  
            MyOperand op2 = (MyOperand)GetOperand();
            MyOperand op1 = (MyOperand)GetOperand();
            Env.cmp(type, op1.Address(0), op2.Address(0));
        }

        public void Prolog(Op P) {
        }
    }

    class UserFunction extends Operator {	/* ### Constructor ### */

        int paramNumber;
        int blockNumber;
        int type;
        int level;
        
        Symbol symbol;
        
        UserFunction(Symbol s, int level)
        {
            symbol = s;
            paramNumber = s.GetInf1();
            blockNumber = s.GetInf2();
            type = s.GetType();
            this.level = level;
        }
        public int GetIndex() {
            return AM_Func;
        } 

        public void Exec()
        {
            System.out.println("UserFunction::Exec");
            Env.Execute(IO.GetNextTokNr() - 1);
            if(type != 0)
            {               
                MyOperand R = new Temporary(AllocateTemporary());
                MyOperand P = new ActivationRecordParameter(null, type);
                                
                Env.SetValue(R.Address(0), P.Address(0));
                PutOperand(R);
            }
            Env.Retur();
            Env.DeAllocate();
            
            //Nån har lagt lite bottom operatorer på stacken som måste elimineras
            //-.-
            Operator topOperator = GetTopOperator();
            
            while(!(topOperator instanceof Become) &&
                  !(topOperator instanceof ArithmOp))
            {
                GetOperator();
                topOperator = GetTopOperator();
            }
        }

        

        public void Prolog(Op P) {	/* ### Code ### */
            /* You have to do something here!!! */
            Env.Enter(blockNumber, IO.GetSize(blockNumber));
            Env.Allocate(blockNumber, level);
        }
    }

    class SystemFunction extends Operator {

        public int GetIndex() {
            return AM_Func;
        }

        public void Exec() {/* ### Code ### */        }

        public void Prolog(Op P) {
        }
    }

    class FunPar extends Operator {

        Op function;
        int blockNumber;
        int numberParam;
        int currentParam;
        
        FunPar(Op F) {
            Symbol symbol;
            function = F;
            
            symbol = ((UserFunction)F).symbol;
            blockNumber = symbol.GetInf2();
            numberParam = symbol.GetInf1();
            currentParam = 1;
        }
        Op GetParOp(int type)
        {
            MyOperand retVal = (MyOperand)GetPar(blockNumber, currentParam);
            currentParam++;
            return retVal;
        }
        public int GetIndex() {
            return AM_Fpar;
        }

        public void Exec() {
        }

        public void Prolog(Op P) {
        }

        /* ????? */
    }
}
