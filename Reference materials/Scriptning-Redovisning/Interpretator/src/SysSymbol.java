
import komp.*;

public class SysSymbol {

    static final int MaxStd = 5, C_O0 = 8;
    String Namn;
    int NumberofPar, Type, LabId;
    ParDescr[] Pars;
    FunVector Funs;

    SysSymbol(int N, String Id, int Ty, int L) {
        Namn = Id;
        Type = Ty;
        LabId = L;
        NumberofPar = N;
        Pars = new ParDescr[N + 1];
    }

    public void CreatePar(int I, int T) {
        Pars[I] = new ParDescr(T, I);
    }

    public void CreateFun(int I, int K) {
        Funs = new FunVector(I, K);
    }

    public void Set(int I, int Id) {
        Funs.Set(I, Id);
    }

    public void SetFun(int I) {
        LabId = Funs.Get(I);
    }

    public int GetId() {
        return LabId;
    }

    public int GetType() {
        return Type;
    }

    public int GetNumPar() {
        return NumberofPar;
    }

    public SysPar GetPar(int I) {
        return Pars[I].GetPar();
    }

    public boolean Equal(String N) {
        return N.equals(Namn);
    }

    class ParDescr {

        int Type, Num;

        ParDescr(int Ty, int N) {
            Type = Ty;
            Num = N;
        }

        public SysPar GetPar() {
            return new SysPar(Type, Num - 1 + C_O0);
        }
    }

    class FunVector {

        int StartInd, EndInd;
        int[] Fun;

        FunVector(int I, int K) {
            StartInd = I;
            EndInd = K;
            Fun = new int[K - I + 1];
        }

        void Set(int I, int Id) {
            Fun[I - StartInd] = Id;
        }

        int Get(int I) {
            return Fun[I - StartInd];
        }
    }
}
