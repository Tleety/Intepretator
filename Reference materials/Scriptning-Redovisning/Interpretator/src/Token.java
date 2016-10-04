
import file.*;
import komp.*;

class Token extends Lexeme {
    static final int T_Ignore = 0, T_Id = 1, T_Op = 5, T_Fix = 2, T_Float = 3, T_Text = 4;
    static final int T_Error = 6, T_Line = 7, T_Call = 10, T_Expr = 11, T_External = 12;
    static final int T_Dynamic = 13;
    static final int TC_No = 0, TC_Semi = 1, TC_Lpar = 2, TC_Rpar = 3, TC_Kol = 4, TC_Com = 5;
    static final int TC_Become = 6, TC_Equal = 7, TC_Neq = 8, TC_Less = 9, TC_Le = 10;
    static final int TC_Gt = 11, TC_Ge = 12, TC_Plus = 13, TC_Minus = 14, TC_Mul = 15;
    static final int TC_Div = 16, TC_Dot = 17;
    static final int ID_Begin = 1, ID_End = 2, ID_Array = 3, ID_Function = 4, ID_Integer = 5;
    static final int ID_Real = 6, ID_Text = 7, ID_While = 8, ID_Do = 9, ID_If = 10, ID_Then = 11;
    static final int ID_Else = 12, ID_Is = 13, ID_External = 14, ID_Object = 15;
    static final int ID_Pointer = 16, ID_New = 17, ID_Dynamic = 18, ID_Max = ID_Dynamic;

    int Type, Code;
    String Text;
    
    /* ********************* Constructors ************************************** */
    public Token() {
    }

    public Token(int Ty, int Cd, String S) {
        Type = Ty;
        Code = Cd;
        Text = S;
    }
    
    /* ********************* Methods ************************************** */
    public void Create(int Ty, int Cd, String S) {
        Type = Ty;
        Code = Cd;
        Text = S;
    }

    public int GetId() {
        if (Type == T_Id) {
            return Code;
        } else {
            return -1;
        }
    }

    public int GetType() {
        return Type;
    }

    public String GetName() {
        return Text;
    }

    public int Getint() {
        if (Type == T_Fix) {
            return Integer.parseInt(Text);
        } else {
            return Integer.MAX_VALUE;
        }
    }

    public int GetLine() {
        if (Type == T_Line) {
            return Code;
        } else {
            return -1;
        }
    }

    public int GetBlkNr() {
        if (Type == T_Call) {
            return Code;
        } else {
            return -1;
        }
    }

    public int GetSymNr() {
        if (Type == T_External || Type == T_Dynamic) {
            return Code;
        } else {
            return -1;
        }
    }

    public boolean Id() {
        return Type == T_Id;
    }

    public boolean UserId() {
        return Type == T_Id && Code > ID_Max;
    }

    public boolean Reserved() {
        return Type == T_Id && Code <= ID_Max;
    }

    public boolean Op() {
        return Type == T_Op;
    }

    public boolean Literal() {
        return Type == T_Fix || Type == T_Float || Type == T_Text;
    }

    public boolean Fix() {
        return Type == T_Fix;
    }

    public boolean Float() {
        return Type == T_Float;
    }

    public boolean Text() {
        return Type == T_Text;
    }

    public boolean Line() {
        return Type == T_Line;
    }

    public boolean Expr() {
        return Type == T_Expr;
    }

    public boolean Begin() {
        return Type == T_Id && Code == ID_Begin;
    }

    public boolean End() {
        return Type == T_Id && Code == ID_End;
    }

    public boolean If() {
        return Type == T_Id && Code == ID_If;
    }

    public boolean While() {
        return Type == T_Id && Code == ID_While;
    }

    public boolean Then() {
        return Type == T_Id && Code == ID_Then;
    }

    public boolean Else() {
        return Type == T_Id && Code == ID_Else;
    }

    public boolean Do() {
        return Type == T_Id && Code == ID_Do;
    }

    public boolean New() {
        return Type == T_Id && Code == ID_New;
    }

    public boolean Is() {
        return Type == T_Id && Code == ID_Is;
    }

    public boolean External() {
        return Type == T_Id && Code == ID_External;
    }

    public boolean Dynamic() {
        return Type == T_Id && Code == ID_Dynamic;
    }

    public boolean CallBlock() {
        return Type == T_Call;
    }

    public boolean CallExternal() {
        return Type == T_External;
    }

    public boolean CallDynamic() {
        return Type == T_Dynamic;
    }

    public boolean Error() {
        return Type == T_Error;
    }

    public boolean Func() {
        return Type == T_Id && Code == ID_Function;
    }

    public boolean Array() {
        return Type == T_Id && Code == ID_Array;
    }

    public boolean Obj() {
        return Type == T_Id && Code == ID_Object;
    }

    public boolean Object() {
        return Type == T_Id && Code == ID_Object;
    }

    public boolean Semi() {
        return Type == T_Op && Code == TC_Semi;
    }

    public boolean Lpar() {
        return Type == T_Op && Code == TC_Lpar;
    }

    public boolean Rpar() {
        return Type == T_Op && Code == TC_Rpar;
    }

    public boolean Comma() {
        return Type == T_Op && Code == TC_Com;
    }

    public boolean Colon() {
        return Type == T_Op && Code == TC_Kol;
    }

    public boolean Dot() {
        return Type == T_Op && Code == TC_Dot;
    }

    public boolean Become() {
        return Type == T_Op && Code == TC_Become;
    }

    public boolean Arithm() {
        return Type == T_Op && Code >= TC_Plus && Code <= TC_Div;
    }

    public boolean AddOp() {
        return Type == T_Op && (Code == TC_Plus || Code == TC_Minus);
    }

    public boolean MulOp() {
        return Type == T_Op && (Code == TC_Mul || Code == TC_Div);
    }

    public boolean Plus() {
        return Type == T_Op && Code == TC_Plus;
    }

    public boolean Minus() {
        return Type == T_Op && Code == TC_Minus;
    }

    public boolean Mul() {
        return Type == T_Op && Code == TC_Mul;
    }

    public boolean Div() {
        return Type == T_Op && Code == TC_Div;
    }

    public boolean Comp() {
        return Type == T_Op && Code >= TC_Equal && Code <= TC_Ge;
    }

    public boolean Eq() {
        return Type == T_Op && Code == TC_Equal;
    }

    public boolean Neq() {
        return Type == T_Op && Code == TC_Neq;
    }

    public boolean Less() {
        return Type == T_Op && Code == TC_Less;
    }

    public boolean Lesseq() {
        return Type == T_Op && Code == TC_Le;
    }

    public boolean Gt() {
        return Type == T_Op && Code == TC_Gt;
    }

    public boolean Gteq() {
        return Type == T_Op && Code == TC_Ge;
    }

    public boolean Eof() {
        return Type == 0 && Text.equals("EOF");
    }

    public boolean Decl() {
        return Type == T_Id
                && (Code == ID_Integer || Code == ID_Real || Code == ID_Text
                || Code == ID_Function || Code == ID_Object || Code == ID_Pointer);
    }

    public boolean Spec() {
        return Type == T_Id
                && (Code == ID_Integer || Code == ID_Real || Code == ID_Text
                || Code == ID_Pointer);
    }

    public void OutPut(outfile Fil) {
        Fil.Write(Code, 5);
        Fil.Write(Type, 5);
        Fil.Skip(5);
        Fil.Write(Text);
        Fil.WriteLine();
    }

    public void Debug() {
        System.out.println("Token typ=" + Type + " code=" + Code + " text=" + Text);
    }

}
