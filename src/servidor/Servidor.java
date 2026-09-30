package servidor;

import Formas.*;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class Servidor {
    public static void main(String[] args) {
        try {
            System.out.println("Proceso servidor en ALISTAMIENTO");

            ORB orb = ORB.init(args, null);

            POA rootPoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootPoa.the_POAManager().activate();

            ServListarFormas servant = new ServListarFormas(rootPoa);
            org.omg.CORBA.Object ref = rootPoa.servant_to_reference(servant);
            IListaForma href = IListaFormaHelper.narrow(ref);
            System.out.println("Se ha creado objeto servidor");

            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            NameComponent[] path = ncRef.to_name("listaDeFormas");
            ncRef.rebind(path, href);

            System.out.println("Servidor de listado de formas O.K.");
            orb.run();
        } catch (Exception e) {
            System.out.println("Error en proceso de creacion y activacion del objeto servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
