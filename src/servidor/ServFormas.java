package servidor;

import Formas.*;

public class ServFormas extends IFormaPOA {
    private final ObjGrafico f;
    private final int miVersion;

    public ServFormas(ObjGrafico g, int version) {
        this.f = g;
        this.miVersion = version;
    }

    @Override
    public int obtVersion() {
        return miVersion;
    }

    @Override
    public ObjGrafico obtEstados() {
        return f;
    }
}
