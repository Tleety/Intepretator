
import file.*;
import komp.*;

class IStruct extends Interface {

    private static final int K_Simple = 0, K_Array = 1, K_Func = 2, K_Funcval = 3, K_Class = 4;
    private Block Blocks, LastBlock, CurrentBlock;
    private int NumberofBlocks, CurBlkNum, Line;
    static final int Max = 100;
    /* ******************************************
     *		inner class Block in class IStruct
     *	Internal type used to hold the result
     *	describing a Block
     ****************************************** */

    class Block {

        private int BlockNr, SFBlockNr, NextAdr;
        private int NumTok, NumDecl;
        private DeclNode SymTab, LastSym, CurrentSym, LastDN;
        private CodeNode BlockCode, CurrentCode;
        private int TokNr;   // The index of the token in the complete block.
        private Block Nxt, SFBlock;
        private int LastKind;
        /* ***********************
         *		inner class DeclNode in class Block
         * internal type in Block 
         * holding a symbol node.
         *********************** */

        class DeclNode {

            DeclNode Nxt;
            Symbol Info;

            void SetInfo(Symbol S) {
                Info = S;
            }

            Symbol GetSym() {
                return Info;
            }

            DeclNode Insert(DeclNode P) {
                Nxt = P;
                return P;
            }

            DeclNode Search(int Id) {
                if (Info.Equal(Id)) {
                    return this;
                } else if (Nxt == null) {
                    return null;
                } else {
                    return Nxt.Search(Id);
                }
            }

            Symbol Par(int N) {
                if (Info.GetKind() == K_Funcval) {
                    return Nxt.Par(N);
                } else if (N == 1) {
                    return Info;
                } else if (Nxt != null) {
                    return Nxt.Par(N - 1);
                } else {
                    return null;
                }
            }

            DeclNode GetSlagSym(int K) {
                if (Info.GetKind() == K) {
                    return this;
                } else if (Nxt != null) {
                    return Nxt.GetSlagSym(K);
                } else {
                    return null;
                }
            }

            void PutInf(int I1, int I2, int I3) {
                if (Info != null) {
                    Info.Set(Symbol.S_Inf1, I1);
                    Info.Set(Symbol.S_Inf2, I2);
                    Info.Set(Symbol.S_Inf3, I3);
                } else {
                }
            }

            void PutSpec(int Type, int Kind) {
                if (Info != null) {
                    Info.Set(Symbol.S_Type, Type);
                    Info.Set(Symbol.S_Kind, Kind);
                } else {
                }
            }

            void PutLim(int Ind, int Low, int High) {
                if (Info != null) {
                    Info.SetLim(Ind, Low, High);
                } else {
                }
            }

            void PutString(String T) {
                if (Info != null) {
                    Info.SetString(T);
                } else {
                }
            }
        }
        /* ***********************
         *		inner class CodeNode in class Block
         * internal type in Block 
         * holding the code of the
         * Block.
         *********************** */

        class CodeNode {

            CodeNode Nxt;
            int Cur;
            Token[] Code = new Token[Max];

            CodeNode P2Init() {
                Cur = 0;
                return this;
            }

            CodeNode Reset(int b, int i) {
                if (b == 0) {
                    Cur = i;
                    return this;
                } else if (Nxt == null) {
                    return null;
                } else {
                    return Nxt.Reset(b - 1, i);
                }
            }

            CodeNode Put(Token Tk) {
                Code[Cur] = Tk;
                Cur = Cur + 1;
                if (Cur >= Max) {
                    Cur = Max + 1;
                    Nxt = new CodeNode();
                    return Nxt;
                } else {
                    return this;
                }
            }

            Token Get() {
                Token T;
                T = Code[Cur];
                Cur = Cur + 1;
                TokNr = TokNr + 1;
                if (Cur == Max) {
                    CurrentCode = Nxt.P2Init();
                }
                if (T != null && T.Line()) {
                    Line = T.GetLine();
                    return CurrentCode.Get();
                } else {
                    return T;
                }
            }

            int GetCur() {
                return Cur;
            }

            void SetCur(int I) {
                Cur = I;
            }

            void OutPut(outfile Fil) {
                for (int I = 0; I < Max; I = I + 1) {
                    if (Code[I] != null) {
                        Code[I].OutPut(Fil);
                    }
                }
            }
        }

        /* ***********************
         *		Constructor of Block
         * 
         *********************** */
        Block() {
            SFBlock = CurrentBlock;
            BlockCode = new CodeNode();
            CurrentCode = BlockCode;
            BlockNr = GetNextBlockNumber();
            NextAdr = 0;
            NumDecl = 0;
            NumTok = 0;
        }
        /* ***********************
         *		Methods of Block
         * 
         *********************** */

        Block Insert(Block P) {
            Nxt = P;
            return P;
        }

        Block Search(int Bid) {
            if (Bid == BlockNr) {
                return this;
            } else if (Nxt != null) {
                return Nxt.Search(Bid);
            } else {
                return null;
            }
        }

        void SetSF(Block P) {
            if (P == null) {
                SFBlockNr = -1;
            } else {
                SFBlockNr = P.GetBn();
            }
        }

        int GetSFBn() {
            return SFBlockNr;
        }

        Block GetSF() {
            return SFBlock;
        }

        int GetBn() {
            return BlockNr;
        }

        Symbol DeclSearch(int Sid) {
            DeclNode P;
            if (SymTab != null) {
                P = SymTab.Search(Sid);
            } else {
                P = null;
            }
            if (P != null) {
                return P.GetSym();
            } else {
                return null;
            }
        }

        Symbol GetSlagSym(int Knd) {
            DeclNode P;
            if (Knd != LastKind) {
                LastDN = SymTab;
                LastKind = Knd;
            }
            if (LastDN != null) {
                P = LastDN.GetSlagSym(Knd);
            } else {
                P = null;
            }
            if (P == null) {
                Knd = 0;
                LastDN = SymTab;
                return null;
            } else {
                LastDN = P.Nxt;
                return P.GetSym();
            }
        }

        boolean CreateDecl(int SymId, int Typ, int Knd, Token T) {
            DeclNode P;
            Symbol S;
            int Adr;
            boolean Res;
            Adr = CompRel(Knd);
            Res = true;
            if (SymTab == null) {
                P = new DeclNode();
                S = new Symbol();
                S.Create(SymId, Typ, Knd, Adr, T);
                NumDecl = NumDecl + 1;
                P.SetInfo(S);
                SymTab = P;
                LastSym = P;
            } else {
                P = SymTab.Search(SymId);
                if (P == null) {
                    P = new DeclNode();
                    S = new Symbol();
                    S.Create(SymId, Typ, Knd, Adr, T);
                    NumDecl = NumDecl + 1;
                    P.SetInfo(S);
                    LastSym = LastSym.Insert(P);
                } else {
                    Res = false;
                }
            }
            return Res;
        }

        void PutInf(int SymId, int Inf1, int Inf2, int Inf3) {
            DeclNode P;
            P = SymTab.Search(SymId);
            if (P != null) {
                P.PutInf(Inf1, Inf2, Inf3);
            } else {
            }
        }

        void Specify(int SymId, int Typ, int Knd) {
            DeclNode P;
            P = SymTab.Search(SymId);
            if (P != null) {
                P.PutSpec(Typ, Knd);
            } else {
            }
        }

        void PutArrayLimit(int SymId, int Ind, int Low, int High) {
            DeclNode P;
            P = SymTab.Search(SymId);
            if (P != null) {
                P.PutLim(Ind, Low, High);
            } else {
            }
        }

        void PutExternal(int SymId, String Ename) {
            DeclNode P;
            P = SymTab.Search(SymId);
            if (P != null) {
                P.PutString(Ename);
            } else {
            }
        }

        void PutCode(Token Tk) {
            CurrentCode = CurrentCode.Put(Tk);
            NumTok = NumTok + 1;
        }

        void InitCode() {
            CurrentCode = BlockCode;
            TokNr = 0;
            CurrentCode.P2Init();
        }

        void ResetCode(int I) {
            int Cb, ind;
            CodeNode C;
            TokNr = I;
            Cb = I / Max; // Code node index.
            ind = I % Max; // token in this block.
            C = BlockCode;
            CurrentCode = C.Reset(Cb, ind);
        }

        Token GetCode() {
            return CurrentCode.Get();
        }

        int GetCurTokNr() {
            return TokNr;
        }

        Symbol GetPar(int N) {
            return SymTab.Par(N);
        }

        int GetSize() {
            return NextAdr;
        }
        /*		*** Help Routines *** */

        private int CompRel(int Kind) {
            if (Kind == 0 || Kind == 1 || Kind == 3 || Kind == 5) {
                int i;
                i = NextAdr;
                NextAdr = NextAdr + 1;
                return i;
            } else {
                return 0;
            }
        }

        void OutPut(outfile Fil) {
            DeclNode P;
            CodeNode Q;
            Fil.Write("##BLOCK##");
            Fil.WriteLine();
            Fil.Write(BlockNr, 3);
            Fil.Write(SFBlockNr, 3);
            Fil.Write(NextAdr, 3);
            Fil.Write(NumDecl, 3);
            Fil.Write(NumTok, 3);
            Fil.WriteLine();
            Fil.Write("#DEKLARATIONER#");
            Fil.WriteLine();
            P = SymTab;
            while (P != null) {
                P.GetSym().OutPut(Fil);
                P = P.Nxt;
            }
            Fil.Write("#KOD#");
            Fil.WriteLine();
            Q = BlockCode;
            while (Q != null) {
                Q.OutPut(Fil);
                Q = Q.Nxt;
            }
            Fil.Write("##BLOCKSLUT##");
            Fil.WriteLine();
        }
    }
    /* ******************************************
     *		Methods of IStruct
     ****************************************** */
    /* *** Constructor *** */

    IStruct() {
        Blocks = null;
        LastBlock = null;
        CurrentBlock = null;
        NumberofBlocks = 0;
    }
    /* ******************** Pass 1 methods ************************* */

    /* -------------------------------------------
     !	CreateBlock
     !
     !	A new block in the result structure is created,
     !	inserted in the block list and its static father
     !	is set.
     !------------------------------------------ */
    public int CreateBlock() {
        Block P;
        P = new Block();
        if (Blocks == null) {
            Blocks = P;
            LastBlock = P;
            P.SetSF(null);
        } else {
            LastBlock = LastBlock.Insert(P);
            P.SetSF(CurrentBlock);
        }
        CurrentBlock = P;
        return P.BlockNr;
    }
    /*-------------------------------------------
     !	CreateDecl
     !
     !	A new declaration is created in current block,
     !	Its Symbol identification, type and kind is
     !	set. For error handling the token connected to
     !	the symbol is also registered.
     !------------------------------------------ */

    public boolean CreateDecl(int SymId, int Type, int Kind, Token Tok) {
        return CurrentBlock.CreateDecl(SymId, Type, Kind, Tok);
    }
    /*-------------------------------------------
     !	PutInf
     !	For a symbol identified by SymId, the Inf1-3
     !	fields will be set.
     !------------------------------------------ */

    public void PutInf(int SymId, int Inf1, int Inf2, int Inf3) {
        CurrentBlock.PutInf(SymId, Inf1, Inf2, Inf3);
    }
    /*-------------------------------------------
     !	Specify
     !	Used to handle a specification. The symbol
     !	is already created, but type and kind wasn't
     !	available at that time. Type and kind is sent
     !	to the symbol identified by SymId
     !------------------------------------------ */

    public void Specify(int SymId, int Typ, int Knd) {
        CurrentBlock.Specify(SymId, Typ, Knd);
    }
    /*-------------------------------------------
     !	PutArrayLimit
     !	Set the index limits for an index (Index) in
     !	the array identified by SymId. For dynamic
     !	limits maxint is used.
     !------------------------------------------ */

    public void PutArrayLimit(int SymId, int Index, int Low, int High) {
        CurrentBlock.PutArrayLimit(SymId, Index, Low, High);
    }
    /*-------------------------------------------
     !
     !------------------------------------------ */

    public void PutExternal(int SymId, String T) {
        CurrentBlock.PutExternal(SymId, T);
    }
    /*-------------------------------------------
     !	PutCode
     !	Put a token in the code token sequence.
     !------------------------------------------ */

    public void PutCode(Token Tk) {
        if (CurrentBlock != null) {
            CurrentBlock.PutCode(Tk);
        }
    }
    /*-------------------------------------------
     !	CloseBlock
     !	The current block is left and its static father will
     !	be the new current block.
     !------------------------------------------ */

    public void CloseBlock() {
        CurrentBlock = CurrentBlock.GetSF();
    }
    /* ********************** Pass 2 methods ***************************** */

    /* ***************************************************
     *** Init Block ***
     The block is opened for interpretation, set the block
     as current and all pointers at start of block.
     *************************************************** */
    public int InitBlock(int N) {
        if (Blocks != null) {
            CurrentBlock = Blocks.Search(N);
        } else {
            CurrentBlock = null;
        }
        if (CurrentBlock != null) {
            CurrentBlock.InitCode();
        }
        CurBlkNum = N;
        return N;
    }
    /* ***************************************************
     *** Continue Block ***
     The block is re-opened for interpretation, set the block
     as current and all pointers are reset to restart inter-
     pretation where it was interrupted.
     N - block number
     nr- index of token in block
     *************************************************** */

    public int ContBlock(int N, int nr) {
        if (Blocks != null) {
            CurrentBlock = Blocks.Search(N);
            CurrentBlock.ResetCode(nr);
        } else {
            CurrentBlock = null;
        }
        CurBlkNum = N;
        return N;
    }

    public void Jump(int nr) {
        CurrentBlock.ResetCode(nr);
    }
    /* ***************************************************

     *************************************************** */

    public Lexeme GetToken() {
        Lexeme T;
        T = CurrentBlock.GetCode();
        return T;
    }

    public int GetNextTokNr() {
        return CurrentBlock.GetCurTokNr();
    }
    /* ***************************************************

     *************************************************** */

    public Symbol Search(int BId, int SId) {
        Block Q;
        if (Blocks != null) {
            Q = Blocks.Search(BId);
        } else {
            Q = null;
        }
        if (Q != null) {
            return Q.DeclSearch(SId);
        } else {
            return null;
        }
    }

    public int GetSF(int Bid) {
        Block Q;
        if (Blocks != null) {
            Q = Blocks.Search(Bid);
        } else {
            Q = null;
        }
        if (Q != null) {
            return Q.GetSFBn();
        } else {
            return -1;
        }
    }

    public int GetNumberofBlocks() {
        return NumberofBlocks;
    }

    public int GetSize(int Bnr) {
        Block B;
        B = Blocks.Search(Bnr);
        return B.GetSize();
    }

    public int GetLine() {
        return Line;
    }

    public Symbol GetPar(int Bid, int N) {
        Block Q;
        Q = Blocks.Search(Bid);
        if (Q != null) {
            return Q.GetPar(N);
        } else {
            return null;
        }
    }

    public Symbol GetSlagSym(int Bid, int Knd) {
        Block Q;
        Q = Blocks.Search(Bid);
        return Q.GetSlagSym(Knd);
    }
    /* *** Help Methods *** */

    private int GetNextBlockNumber() {
        int B;
        B = NumberofBlocks;
        NumberofBlocks = NumberofBlocks + 1;
        return B;
    }

    public void OutPut(outfile Fil) {
        Block P;
        Fil.Write("###PROGRAM###");
        Fil.WriteLine();
        Fil.Write(NumberofBlocks, 3);
        Fil.WriteLine();
        P = Blocks;
        while (P != null) {
            P.OutPut(Fil);
            P = P.Nxt;
        }
        Fil.Write("###PROGRAMSLUT###");
        Fil.WriteLine();
    }
}
