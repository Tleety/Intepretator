
class Ipass1 {

    /* *************** Constants ********************************************* */
    static final int C_Asis = 0, C_Any = 0, C_Void = 0;
    static final int C_Integer = 1, C_Real = 2, C_Boolean = 4, C_Text = 3, C_Pointer = 5;
    /* ****** Kind codes ****** */
    static final int K_Simple = 0, K_Array = 1, K_Func = 2, K_Funcval = 3, K_Class = 4, K_Ref = 5;
    static final int K_Std = 10;

    static final int T_Ignore = 0, T_Id = 1, T_Op = 5, T_Fix = 2, T_Float = 3, T_Text = 4;
    static final int T_Expr = 11, T_Call = 10, T_External = 12, T_Dynamic = 13;

    /* ****** Error codes ****** */
    static final int E_Sem = 1, E_Id = 2, E_Lpar = 3, E_Rpar = 4, E_ResId = 5;
    static final int E_FunHead = 6, E_Ext = 7, E_Obj = 8, E_MissSpec = 9, E_OverSpec = 10;
    static final int E_ArrLim = 11, E_MultDec = 12;

    /* ****** Identifier codes ****** */
    static final int Id_Integer = 5, Id_Real = 6, Id_Text = 7, Id_Pointer = 16;

    Lex lexanalyzer;
    IStruct Resultat;
    int Bid;

    /* ******************************************************************
			Constructors
    Ipass1(Lex,IStruct)	used to interprete a complete program
    Ipass1(Lex,IStruct,boolean) used to interprete statement by statement
    ****************************************************************** */
    public Ipass1(Lex L, IStruct R) {
        lexanalyzer = L;
        Resultat = R;
        Enhet();
    }

    public Ipass1(Lex L, IStruct R, boolean S) {
        lexanalyzer = L;
        Resultat = R;
        CreateBlock();
        InterpretStatement();
    }

    /* ******************************************************************
    *		Main compiler method
    *
    *   Grammar:
    *       Enhet 	-->	Program | Separat
    *       Program 	-->	Block | Sats
    *       Separat 	-->	Procedur | Typ Procedur
    ****************************************************************** */

    private void Enhet() {
        Token T;
        T = GetToken();
        if (T.Begin()) {
            CreateBlock();
            T = GetToken();
            while (T.Decl()) {
                T = Deklaration(T);
                if (T.Semi()) {
                    T = GetToken();
                } else {
                    Error(E_Sem, 0);
                }
            }
            while (!(T.End() || T.Eof())) {
                T = Sats(T);
            }
            CloseBlock(T);
        } else {
            if (T.Decl()) {
                CreateBlock();
                T = Deklaration(T); // Not yet implemented.
                CloseBlock(T);
            } else {
                CreateBlock();
                T = Sats(T);
                CloseBlock(T);
            }
        }
    }

    /* **** Only one statement or declaration **** */
    private void InterpretStatement() {
        Token T;
        T = GetToken();
        if (T.Decl()) {
            T = Deklaration(T);
        } else {
            T = Sats(T);
        }
    }

    /* **** Parse next statement, called from Ipass2 **** */
    public boolean ParseNext() {
        Token T;
        boolean B;
        T = GetToken();
        if (T.End()) {
            return false;
        }
        if (T.Decl()) {
            T = Deklaration(T);
            System.out.println("Dec");
        } else {
            T = Sats(T);
        }
        return true;
    }

    /* ******************************************************************
    *		Declarations
    *
    * Grammar:
    *	Deklaration	-->	Typ Idlist | Typ array Arrlist
    *	Typ			-->	Simptyp | Objtyp
    *	Idlist		-->	Id | Id , Idlist
    *	Arrlist		-->	Arr | Arr , Arrlist
    *	Simptyp		-->	integer | real | text
    *	Objtyp		-->	Id Idlist
    *	Procedur		-->	function Id Par ; Speclist Kropp
    *	Arr is defined further down.
    ****************************************************************** */
    private Token Deklaration(Token T) {
        if (T.Func()) {
            T = Funktion(C_Void, 0);
        } else {
            if (T.Obj()) {
                T = Dobject();
            } else {
                if (T.Object()) {
                    int Q, Sid;
                    Q = 0;
                    T = GetToken();
                    if (!T.Lpar()) {
                        Error(E_Lpar, 0);
                    } else {
                        T = GetToken();
                    }
                    if (T.UserId()) {
                        Q = T.GetId();
                    } else {
                        Error(E_Id, 0);
                    }
                    T = GetToken();
                    if (T.Rpar()) {
                        T = GetToken();
                    } else {
                        Error(E_Rpar, 0);
                    }
                    if (T.UserId()) {
                        Sid = T.GetId();
                        CreateDecl(Bid, Sid, C_Pointer, K_Ref, T);
                        FillInfo(Sid, 0, 0, Q);
                        T = GetToken();
                        while (T.Comma()) {
                            T = GetToken();
                            Sid = T.GetId();
                            CreateDecl(Bid, Sid, C_Pointer, K_Ref, T);
                            FillInfo(Sid, 0, 0, Q);
                            T = GetToken();
                        }
                    } else {
                        if (T.Func()) {
                            T = Funktion(C_Pointer, Q);
                        } else {
                            if (T.Array()) {
                                T = ArrayDecl(C_Pointer, Q);
                            } else {
                                Error(E_ResId, 0);
                            }
                        }
                    }
                    if (T.Semi()) {
                    } else {
                        T = GetToken();
                    }
                } else {
                    Token S;
                    int Sid, Tid, Typ;
                    S = GetToken();
                    Sid = S.GetId();
                    Tid = T.GetId();
                    Typ = GetType(Tid);
                    if (S.UserId()) {
                        CreateDecl(Bid, Sid, Typ, K_Simple, S);
                        T = GetToken();
                        while (T.Comma()) {
                            S = GetToken();
                            Sid = S.GetId();
                            if (S.UserId()) {
                                CreateDecl(Bid, Sid, Typ, K_Simple, S);
                            } else {
                                Error(E_ResId, 0);
                            }
                            T = GetToken();
                        }
                    } else if (S.Func()) {
                        T = Funktion(Typ, 0);
                    } else {
                        if (S.Array()) {
                            T = ArrayDecl(Typ, 0);
                            while (T.Comma()) {
                                T = ArrayDecl(Typ, 0);
                            }
                        } else {
                            Error(E_ResId, 0);
                        }
                    }
                }
            }
        }
        return T;
    }

    /* ***********************************************************
    *			Procedures
    * function or type function already found by Deklaration
    * Gramma:
    *	Procedur -->function Id Par Semicolon Speclist [ExtKropp | Kropp]
    *	Par	--->	( Plista ) | Empty
    *	Plista--->	id | id , Plista
    *	ExtKropp--->is external Name
****************************************************************** */
    private Token Funktion(int Ty, int Qual) {
        int Sid, NofPar, BNr;
        Token T, Namn;
        T = GetToken();
        Namn = T;
        Sid = T.GetId();
        BNr = CreateBlock();
        if (Ty != C_Void) {
            CreateDecl(Bid, Sid, Ty, K_Funcval, T);
        }
        T = GetToken();
        if (T.Lpar()) {
            NofPar = Parameters();
            T = Specifikationer(NofPar);
        } else {
            if (T.Semi()) {
                NofPar = 0;
                T = GetToken();
            } else {
                NofPar = 0;
                Error(E_FunHead, 0);
            }
        }
        if (T.Is()) {
            String N;
            T = GetToken();
            if (T.External()) {
                T = GetToken();
                N = T.GetName();
                T = GetToken();
                PutExternal(Sid);
                CloseBlock(null);
                CreateFuncDecl(Bid, Sid, Ty, K_Func, NofPar, BNr, Qual, Namn);
                PutExternal(Sid, N);
                return T;
            } else {
                if (T.Dynamic()) {
                    T = GetToken();
                    PutDynamic(Sid);
                    CloseBlock(null);
                    CreateFuncDecl(Bid, Sid, Ty, K_Func, NofPar, BNr, Qual, Namn);
                    return T;
                } else {
                    Error(E_Ext, 0);
                    return null;
                }
            }
        } else {
            T = Kropp(T);
            CloseBlock(null);
            CreateFuncDecl(Bid, Sid, Ty, K_Func, NofPar, BNr, Qual, Namn);
            return T;
        }
    }

    /* ************************************************************
    *			Class
    * class already found by Deklaration
    * Gramma:
    *	Object  -->     class Id Par Semicolon Speclist  Kropp
    *	Par	--->	( Plista ) | Empty
    *	Plista  --->	id | id , Plista
    ****************************************************************** */
    private Token Dobject() {
        int Sid, NofPar, BNr;
        Token T, S;
        S = GetToken();
        Sid = S.GetId();
        BNr = CreateBlock();
        T = GetToken();
        if (T.Lpar()) {
            NofPar = Parameters();
            T = Specifikationer(NofPar);
        } else {
            if (T.Semi()) {
                NofPar = 0;
                T = GetToken();
            } else {
                NofPar = 0;
                Error(E_Obj, 0);
            }
        }
        T = Kropp(T);
        CloseBlock(null);
        CreateFuncDecl(Bid, Sid, C_Void, K_Class, NofPar, BNr, 0, S);
        return T;
    }

    /* ************************************************************
    *			Parameters
    * parsing the parameter list of a function or object declaration
    * Gramma:
    *	Par	--->	( Plista ) | Empty
    *	Plista  --->	id | id , Plista
    ****************************************************************** */
    private int Parameters() {
        Token T;
        int Num;
        T = ParId();
        Num = 1;
        while (T.Comma()) {
            T = ParId();
            Num = Num + 1;
        }
        if (!T.Rpar()) {
            Error(E_Rpar, 0);
        }
        T = GetToken();
        if (!T.Semi()) {
            Error(E_Sem, 0);
        }
        return Num;
    }

    private Token ParId() {
        Token T;
        T = GetToken();
        if (T.UserId()) {
            CreateDecl(Bid, T.GetId(), 0, 0, T);
            return GetToken();
        } else {
            Error(E_Id, 0);
            return null;
        }
    }

    /* ************************************************************
    *			Parameter specification
    * after having parsed the parameter list of a function 
    * or object declaration the specifications are parsed
    * Gramma:
    *	Speclist	--->	Spec Semicolon Speclist | Empty
    *	Spec		--->	Typ Id | Typ array Id
    ****************************************************************** */
    private Token Specifikationer(int N) {
        int M;
        Token T;
        Count C;
        C = new Count();
        T = GetToken();
        while (T.Spec()) {
            T = Spec(GetType(T.GetId()), C);
        }
        M = C.GetCount();
        if (M != N) {
            if (M < N) {
                Error(E_MissSpec, 0);
            } else {
                Error(E_OverSpec, 0);
            }
        }
        return T;
    }

    private Token Spec(int Ty, Count C) {
        Token T;
        if (Ty == C_Pointer) {
            int Q, Sid;
            T = GetToken();
            if (!T.Lpar()) {
                Error(E_Lpar, 0);
            } else {
                T = GetToken();
            }
            if (T.UserId()) {
                Q = T.GetId();
            } else {
                Q = 0;
                Error(E_Id, 0);
            }
            T = GetToken();
            if (T.Rpar()) {
                T = GetToken();
            } else {
                Error(E_Rpar, 0);
            }
            if (T.UserId()) {
                Sid = T.GetId();
                Specify(Sid, C_Pointer, K_Ref);
                FillInfo(Sid, 0, 0, Q);
                C.Increment();
                T = GetToken();
                while (T.Comma()) {
                    T = GetToken();
                    if (T.UserId()) {
                    } else {
                        Error(0, 0);
                    }
                    Sid = T.GetId();
                    Specify(Sid, C_Pointer, K_Ref);
                    FillInfo(Sid, 0, 0, Q);
                    C.Increment();
                    T = GetToken();
                }
            } else {
                Error(E_Id, 0);
            }
        } else {
            T = GetToken();
            if (T.UserId()) {
                Specify(T.GetId(), Ty, K_Simple);
                C.Increment();
                T = GetToken();
                while (T.Comma()) {
                    T = GetToken();
                    if (T.UserId()) {
                    } else {
                        Error(0, 0);
                    }
                    Specify(T.GetId(), Ty, K_Simple);
                    C.Increment();
                    T = GetToken();
                }
            } else {
                if (T.Array()) {
                    T = GetToken();
                    Specify(T.GetId(), Ty, K_Array);
                    C.Increment();
                    T = GetToken();
                    while (T.Comma()) {
                        T = GetToken();
                        if (T.UserId()) {
                        } else {
                            Error(0, 0);
                        }
                        Specify(T.GetId(), Ty, K_Array);
                        C.Increment();
                        T = GetToken();
                    }
                }
            }
        }
        if (T.Semi()) {
            return GetToken();
        } else {
            return T;
        }
    }

    class Count {

        int Cnt;

        Count() {
            Cnt = 0;
        }

        void Increment() {
            Cnt = Cnt + 1;
        }

        int GetCount() {
            return Cnt;
        }
    }

    /* ************************************************************
****************************************************************** */
    private Token Kropp(Token T) {
        if (T.Semi()) {
        } else {
            if (T.Begin()) {
                T = GetToken();
                while (T.Decl()) {
                    T = Deklaration(T);
                    if (T.Semi()) {
                        T = GetToken();
                    } else {
                        Error(E_Sem, 0);
                    }
                }
                while (!T.End()) {
                    T = Sats(T);
                }
                PutCode(Bid, T);
                T = GetToken();
            } else {
                T = Sats(T);
            }
        }
        return T;
    }

    /* ************************************************************
    *			Array
    * Type array already found by Deklaration
    * Gramma:
    *	Arr     --> 		Id ( Alimits 
    *	Alimits -->	Lim , Alimits | )
    *	Lim	-->	Expr : Expr
    ****************************************************************** */
    private Token ArrayDecl(int Ty, int Qual) {
        Token T;
        int Sid;
        boolean OK;
        T = GetToken();
        Sid = T.GetId();
        OK = CreateDecl(Bid, Sid, Ty, K_Array, T);
        FillInfo(Sid, 0, 0, Qual);
        T = GetToken();
        if (!T.Lpar()) {
            Error(E_Lpar, 0);
        } else {
            T = Alimits(Sid, Qual, OK);
        }
        if (!T.Rpar()) {
            Error(E_Rpar, 0);
        } else {
            T = GetToken();
        }
        return T;
    }

    private Token Alimits(int Sid, int Qual, boolean OK) {
        Token T, S;
        int H, L, Ind;
        boolean Klar;
        Klar = false;
        T = null;
        Ind = 1;
        while (!Klar) {
            H = Integer.MAX_VALUE;
            L = Integer.MAX_VALUE;
            T = GetToken();
            S = GetToken();
            if (S.Colon()) {
                int K;
                K = T.GetType();
                if (K == T_Fix) {
                    L = T.Getint();
                } else {
                    if (K == T_Id) {
                        CreateExprCall();
                        PutCode(Bid, T);
                        PutSemi(Bid);
                    } else {
                        Error(E_ArrLim, 0);
                    }
                }
            } else {
                CreateExprCall();
                PutCode(Bid, T);
                PutCode(Bid, S);
                T = GetToken();
                while (!T.Colon()) {
                    PutCode(Bid, T);
                    T = GetToken();
                }
                PutSemi(Bid);
            }
            T = GetToken();
            S = GetToken();
            if (S.Comma() || S.Rpar()) {
                int K;
                K = T.GetType();
                if (K == T_Fix) {
                    H = T.Getint();
                    T = S;
                } else {
                    if (K == T_Id) {
                        CreateExprCall();
                        PutCode(Bid, T);
                        PutSemi(Bid);
                        T = S;
                    }
                }
            } else {
                CreateExprCall();
                PutCode(Bid, T);
                PutCode(Bid, S);
                T = GetToken();
                while (!T.Comma() || T.Rpar()) {
                    PutCode(Bid, T);
                    T = GetToken();
                }
                PutSemi(Bid);
            }
            if (OK) {
                SetArrayLimit(Sid, Ind, L, H);
            }
            if (T.Rpar()) {
                Klar = true;
            } else {
                Ind = Ind + 1;
            }
        }
        FillInfo(Sid, Ind, 0, Qual);
        return T;
    }

    /* ************************************************************
    *			Sats
    *	Gramma:
    *       Sats    --->        Block | begin Satslist end |
    *				Anrop | Medan | Villkor | Tillordn | Tomt
    *       Tomt    ---> 
    ****************************************************************** */
    private Token Sats(Token T) {
        if (T.Begin()) {
            Token S;
            S = GetToken();
            if (S.Decl()) {
                CreateBlockCall(Block(S));
                T = GetToken();
                if (T.Semi()) {
                    PutCode(Bid, T);
                }
            } else {
                PutCode(Bid, T);
                T = Sammansatt(S);
                if (T.Semi()) {
                    PutCode(Bid, T);
                }
            }
            return T;
        } else {
            if (T.End()) {
                return T;
            } else {
                if (T.Semi()) {
                    return GetToken();
                } else {
                    if (T.Eof()) {
                        return T;
                    } else {
                        T = EnkelSats(T);
                        if (T.Semi()) {
                            PutCode(Bid, T);
                        }
                        return T;
                    }
                }
            }
        }
    }

    private Token Sammansatt(Token T) {
        while (!T.End()) {
            T = Sats(T);
        }
        PutCode(Bid, T);
        return GetToken();
    }

    private Token EnkelSats(Token T) {
        while (!T.Semi() && !T.End() && !T.Eof()) {
            if (T.Begin()) {
                T = Sats(T);
            } else {
                PutCode(Bid, T);
                T = GetToken();
            }
        }
        return T;
    }


    /* ************************************************************
    *			Block
    *	Gramma:
    *		Block       --->	begin Deklist Statlist end
    *		Deklist     --->	Dekl ; Deklist | Dekl ;
    *		Satslist    --->	Sats ; Satslist | Sats
    *
    ****************************************************************** */
    private int Block(Token T) {
        int N;
        N = CreateBlock();
        Deklaration(T);
        T = GetToken();
        while (T.Decl()) {
            Deklaration(T);
            T = GetToken();
        }
        while (!T.End() && !T.Eof()) {
            while (T.Semi()) {
                T = GetToken();
            }
            T = Sats(T);
        }
        CloseBlock(T);
        return N;
    }

    /* ****************** Interface ********************************** */

    private Token GetToken() {;
        return lexanalyzer.GetToken();
    }

    private int CreateBlock() {
        return Resultat.CreateBlock();
    }

    private void CloseBlock(Token T) {
        if (T == null) {
        } else {
            if (T.GetType() == T_Ignore) {
            } else {
                Resultat.PutCode(T);
            }
        }
        Resultat.CloseBlock();
    }

    private boolean CreateDecl(int B, int S, int Ty, int K, Token Tk) {
        boolean OK;
        OK = Resultat.CreateDecl(S, Ty, K, Tk);
        if (OK) {
            return true;
        } else {
            Error(E_MultDec, 0);
            return false;
        }
    }

    private void CreateFuncDecl(int Bid, int Sid, int Ty, int K, int NofPar, int B, int Qual, Token Namn) {
        boolean OK;
        OK = Resultat.CreateDecl(Sid, Ty, K, Namn);
        if (OK) {
            Resultat.PutInf(Sid, NofPar, B, Qual);
        } else {
            Error(E_MultDec, 0);
        }
    }

    private void Specify(int Sid, int Ty, int K) {
        Resultat.Specify(Sid, Ty, K);
    }

    private void FillInfo(int B, int I1, int I2, int I3) {
        Resultat.PutInf(B, I1, I2, I3);
    }

    private void SetArrayLimit(int Sid, int Ind, int L, int H) {
        Resultat.PutArrayLimit(Sid, Ind, L, H);
    }

    private void PutCode(int B, Token T) {
        Resultat.PutCode(T);
    }

    private void PutSemi(int B) {
        Token T;
        T = new Token();
        T.Create(T_Op, 1, ";");
        Resultat.PutCode(T);
    }

    private void CreateExprCall() {
        Token T;
        T = new Token();
        T.Create(T_Expr, 0, "Array limit");
        Resultat.PutCode(T);
    }

    private void CreateBlockCall(int B) {
        Token T;
        T = new Token();
        T.Create(T_Call, B, "Callblock");
        Resultat.PutCode(T);
    }

    private void PutExternal(int B, String N) {
        Resultat.PutExternal(B, N);
    }

    private void PutExternal(int B) {
        Token T;
        T = new Token();
        T.Create(T_External, B, "Callexternal");
        Resultat.PutCode(T);
    }

    private void PutDynamic(int B) {
        Token T;
        T = new Token();
        T.Create(T_Dynamic, B, "Calldynamic");
        Resultat.PutCode(T);
    }

    private int GetType(int I) {
        if (I == Id_Integer) {
            return C_Integer;
        } else {
            if (I == Id_Real) {
                return C_Real;
            } else {
                if (I == Id_Text) {
                    return C_Text;
                } else {
                    if (I == Id_Pointer) {
                        return C_Pointer;
                    } else {
                        return C_Any;
                    }
                }
            }
        }
    }

    private void Error(int Fel, int I) {
        System.out.println("Error: " + Fel + " : " + I);
    }

}
