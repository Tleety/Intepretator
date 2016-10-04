
import java.util.ArrayList;
import komp.*;
import struct.List;
/* **************************************************************
 This is not complete!
 ************************************************************** */

class SymSearch {

    static final int M_Unspec = 0, M_Readint = 1, M_Readreal = 2, M_Readtext = 7;
    static final int M_Writeint = 3, M_Writereal = 4, M_Writetext = 5, M_Writeln = 6;
    static final int C_Asis = 0, C_Any = 0, C_Void = 0;
    static final int C_Integer = 1, C_Real = 2, C_Boolean = 4, C_Text = 3, C_Pointer = 5;
    static final int MaxStd = 5;

    int BlockNumber, Level, SF;
    IStruct P1Res;
    SysSymbol[] STD = new SysSymbol[MaxStd + 1];

    public SymSearch(IStruct S) {
        P1Res = S;
        SysSetup();
    }
    public int GetBlockNr()
    {
        return BlockNumber;
    }
    public void SetBlock(int Bid) {
        BlockNumber = Bid;
    }

    public void ResetBlock(int Bid) {
        BlockNumber = Bid;
    }
    
    /* Search for symbol until found
    continue in static father if necessary
    do not forget to set level! */
    public Symbol Search(int Id) 
     {
        Symbol s = P1Res.Search(BlockNumber, Id);
        SF = P1Res.GetSF(BlockNumber);
        Level = 0;
        
        while(s == null)
        {
            s = P1Res.Search(SF, Id);
            SF = P1Res.GetSF(SF);
            Level++;
        }
        
        return s;
    }

    public Symbol ContSearch(int Id) {	
        Symbol s = null;
        
        while(s == null)
        {
            s = P1Res.Search(SF, Id);
            SF = P1Res.GetSF(SF);
            Level++;
        }
        
        return s;
    }

    public Symbol ParSearch(int Id, int N) {
        return P1Res.GetPar(Id, N);
    }

    public SysSymbol StdSearch(String NID) {	/* Search for a system function/procedure */

        return null;
    }

    public int GetLevel() {
        return Level;
    }

    /* ************************************************
     *		Set up standard routine library interface
     *
     ************************************************ */
    public void SysSetup() {
        STD[1] = new SysSymbol(0, "readint", C_Integer, M_Readint);
        STD[2] = new SysSymbol(0, "readreal", C_Real, M_Readreal);
        STD[3] = new SysSymbol(1, "write", C_Asis, M_Unspec);
        STD[3].CreatePar(1, 0);
        STD[3].CreateFun(C_Integer, C_Text);
        STD[3].Set(C_Integer, M_Writeint);
        STD[3].Set(C_Real, M_Writereal);
        STD[3].Set(C_Text, M_Writetext);
        STD[4] = new SysSymbol(0, "writln", C_Void, M_Writeln);
        STD[5] = new SysSymbol(0, "readtext", C_Text, M_Readtext);
    }

}
