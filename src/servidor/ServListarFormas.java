package servidor;

import Formas.*;
import java.util.ArrayList;
import java.util.List;
import org.omg.PortableServer.POA;

public class ServListarFormas extends IListaFormaPOA {
    private final POA poa;
    private final List<IForma> laLista = new ArrayList<>();
    private int version = 0;

    public ServListarFormas(POA poa) {
        this.poa = poa;
    }

    @Override
    public synchronized IForma nuevaForma(ObjGrafico g) {
        version++;
        try {
            ServFormas servant = new ServFormas(g, version);
            org.omg.CORBA.Object ref = poa.servant_to_reference(servant);
            IForma forma = IFormaHelper.narrow(ref);
            laLista.add(forma);
            System.out.println("SE ACABA DE AGREGAR EL OBJETO GRAFICO CON VERSION: " + version);
            return forma;
        } catch (Exception e) {
            throw new org.omg.CORBA.INTERNAL("Error creando la forma: " + e.getMessage());
        }
    }

    @Override
    public synchronized IForma[] todaForma() {
        return laLista.toArray(new IForma[0]);
    }

    @Override
    public synchronized int obtVersion() {
        return version;
    }
}