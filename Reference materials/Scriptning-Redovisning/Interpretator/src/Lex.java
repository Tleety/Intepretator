
class Lex {

    static final int T_Ignore = 0, T_Id = 1, T_Op = 5, T_Fix = 2, T_Float = 3, T_Text = 4;
    static final int T_Error = 6, T_Line = 7, T_Call = 10, T_Expr = 11;
    static final int TC_No = 0, TC_Semi = 1, TC_Lpar = 2, TC_Rpar = 3, TC_Kol = 4, TC_Com = 5;
    static final int TC_Become = 6, TC_Equal = 7, TC_Neq = 8, TC_Less = 9, TC_Le = 10;
    static final int TC_Gt = 11, TC_Ge = 12, TC_Plus = 13, TC_Minus = 14, TC_Mul = 15;
    static final int TC_Div = 16, TC_Dot = 17;
    int EOF = 025;
    char Ch;
    Inmatning Inm;
    char NL = '\n';
    Filhantering Fil;
    IStruct Resultat;
    Namntab Namn;
    /* ******************************************************************
     Constructor
     ****************************************************************** */

    public Lex(IStruct Res, Filhantering F) {
        Namn = new Namntab();
        Fil = F;
        Resultat = Res;
        Inm = new Inmatning(Fil);
    }
    /* ******************************************************************
     Inner class Inmatning
     ****************************************************************** */

    class Inmatning {

        String Rad;
        Filhantering Fil;
        int RadNr, Pos, ItemPos, RadSz;
        boolean TomRad;
        char LastCh;

        Inmatning(Filhantering F) {
            TomRad = true;
            Fil = F;
        }

        char FirstChar() {
            Token Tk;
            if (TomRad) {
                Rad = Fil.GetLine();
                RadSz = Rad.length();
                RadNr = Fil.GetLineNumber();
                Tk = CreateToken(T_Line, RadNr, "Line#");
                Resultat.PutCode(Tk);
                Pos = 0;
                if (RadSz != 0) {
                    TomRad = false;
                } else {
                    TomRad = true;
                }
            }
            ItemPos = Pos;
            return GetChar();
        }

        char GetChar() {
            char Ch;
            if (Rad.equals("\025")) {
                Ch = '\025';
            } else if (TomRad) {
                Ch = NL;
            } else {
                Ch = Rad.charAt(Pos);
                Pos = Pos + 1;
                if (Pos == RadSz) {
                    TomRad = true;
                }
            }
            LastCh = Ch;
            return Ch;
        }

        String GetItem(boolean Backa) {
            String S;
            if (Backa) {
                if (LastCh == NL) {
                    TomRad = true;
                } else {
                    Pos = Pos - 1;
                    TomRad = false;
                }
                S = Rad.substring(ItemPos, Pos);
            } else {
                S = Rad.substring(ItemPos, Pos);
            }
            return S;
        }
    }
    /* ******************** End of inner class Inmatning************************ */

    /* ******************************************************************
     The only public method GetToken
     The next token is taken from the input. A token or a lexeme is
     extracted from input by the use of an automaton. The token has the
     following design:
     Type:		Identifier(1),Integer lit(2), Float lit(3), Text lit(4), Operator(5),
     Error(6), NewLine(7), CallBlock(10), Expression(11)
     Code:		identifies this token
     String:	the text content of the token, mainly for error handling
     ****************************************************************** */
    public Token GetToken() {
        Ch = Inm.FirstChar();
        return Q0(Ch);
    }
    /* ******************************************************************
     The Automaton
     Start state Q0
     Non-final states: Q0,Q1,Q2,Q3,Q4,Q5,Q6,Q7,Q8
     Normal terminating state CreateToken
     Error terminating state Error
     ****************************************************************** */

    private Token Q0(char C) {
        if (Character.isLetter(C)) {
            return Q1(Inm.GetChar());
        } else if (Character.isDigit(C)) {
            return Q2(Inm.GetChar());
        } else if (C == '"') {
            return Q4(Inm.GetChar());
        } else if (Simple(C)) {
            return CreateToken(T_Op, SimpCode(C), Inm.GetItem(false));
        } else if (C == ':') {
            return Q5(Inm.GetChar());
        } else if (C == '<') {
            return Q6(Inm.GetChar());
        } else if (C == '>') {
            return Q7(Inm.GetChar());
        } else if (Space(C)) {
            return Q0(Inm.FirstChar());
        } else if (C == '!') {
            return Q8(Inm.GetChar());
        } else if (C == EOF) {
            return CreateToken(T_Ignore, 0, "EOF");
        } else {
            return ErrorToken(0, "Fel");
        }
    }

    private Token Q1(char C) {
        if (Character.isLetterOrDigit(C)) {
            return Q1(Inm.GetChar());
        } else {
            String Str;
            int K;
            Str = Inm.GetItem(true);
            K = Namn.Getcode(Str); // Tag kod från namntabell
            return CreateToken(T_Id, K, Str);
        }
    }

    private Token Q2(char C) {
        if (Character.isDigit(C)) {
            return Q2(Inm.GetChar());
        } else if (C == '.') {
            return Q3(Inm.GetChar());
        } else {
            String Str;
            int I;
            Str = Inm.GetItem(true);
            I = Integer.parseInt(Str);
            return CreateToken(T_Fix, I, Str);
        }
    }

    private Token Q3(char C) {
        if (Character.isDigit(C)) {
            return Q3(Inm.GetChar());
        } else {
            String Str;
            Str = Inm.GetItem(true);
            return CreateToken(T_Float, 0, Str);
        }
    }

    private Token Q4(char C) {
        if (C == '"') {
            return CreateToken(T_Text, 0, Inm.GetItem(false));
        } else if (C == NL) {
            return ErrorToken(0, "");
        } else {
            return Q4(Inm.GetChar());
        }
    }

    private Token Q5(char C) {
        if (C == '=') {
            return CreateToken(T_Op, TC_Become, ":=");
        } else {
            return CreateToken(T_Op, 4, Inm.GetItem(true));
        }
    }

    private Token Q6(char C) {
        if (C == '>') {
            return CreateToken(T_Op, TC_Neq, "<>");
        } else if (C == '=') {
            return CreateToken(T_Op, TC_Le, "<=");
        } else {
            return CreateToken(T_Op, TC_Less, Inm.GetItem(true));
        }
    }

    private Token Q7(char C) {
        if (C == '=') {
            return CreateToken(T_Op, TC_Ge, ">=");
        } else {
            return CreateToken(T_Op, TC_Gt, Inm.GetItem(true));
        }
    }

    private Token Q8(char C) {
        if (C == ';') {
            return Q0(Inm.GetChar());
        } else if (C == NL) {
            return Q0(Inm.GetChar());
        } else {
            return Q8(Inm.GetChar());
        }
    }

    private boolean Simple(char C) {
        return C == '+' || C == '-' || C == '*' || C == '/'
                || C == '='
                || C == '(' || C == ')' || C == ',' || C == ';'
                || C == '.';
    }

    private int SimpCode(char C) {
        if (C == '+') {
            return TC_Plus;
        } else if (C == '-') {
            return TC_Minus;
        } else if (C == '*') {
            return TC_Mul;
        } else if (C == '/') {
            return TC_Div;
        } else if (C == '=') {
            return TC_Equal;
        } else if (C == '(') {
            return TC_Lpar;
        } else if (C == ')') {
            return TC_Rpar;
        } else if (C == ',') {
            return TC_Com;
        } else if (C == ';') {
            return TC_Semi;
        } else if (C == '.') {
            return TC_Dot;
        } else {
            return 0;
        }
    }

    private boolean Space(char C) {
        return C == ' ' || C == '\t' || C == NL;
    }

    private Token CreateToken(int ty, int c, String s) {
        return new Token(ty, c, s);
    }

    private Token ErrorToken(int Cd, String Str) {
        return new Token(T_Error, Cd, Str);
    }
}
