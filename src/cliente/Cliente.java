package cliente;

import Formas.*;
import java.util.Scanner;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

public class Cliente {
    public static void main(String[] args) {
        try {
            ORB orb = ORB.init(args, null);
            NamingContextExt nc = NamingContextExtHelper.narrow(
                    orb.resolve_initial_references("NameService"));
            IListaForma lista = IListaFormaHelper.narrow(nc.resolve_str("listaDeFormas"));

            Scanner sc = new Scanner(System.in);
            int op;
            do {
                System.out.println("\n===== MENU =====");
                System.out.println("1. Crear forma");
                System.out.println("2. Listar formas");
                System.out.println("3. Version de la lista");
                System.out.println("0. Salir");
                System.out.print("Opcion: ");
                op = Integer.parseInt(sc.nextLine().trim());

                switch (op) {
                    case 1: crear(lista, sc); break;
                    case 2: listar(lista); break;
                    case 3:
                        System.out.println("Version actual: " + lista.obtVersion());
                        break;
                    case 0: System.out.println("Fin."); break;
                    default: System.out.println("Opcion no valida");
                }
            } while (op != 0);
        } catch (Exception e) {
            System.out.println("Error en el cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void crear(IListaForma lista, Scanner sc) {
        System.out.print("Tipo (Rectangulo/Circulo/Linea): ");
        String tipo = sc.nextLine();
        System.out.print("x: ");      int x = Integer.parseInt(sc.nextLine().trim());
        System.out.print("y: ");      int y = Integer.parseInt(sc.nextLine().trim());
        System.out.print("ancho: ");  int w = Integer.parseInt(sc.nextLine().trim());
        System.out.print("alto: ");   int h = Integer.parseInt(sc.nextLine().trim());
        System.out.print("Relleno (s/n): ");
        boolean relleno = sc.nextLine().trim().equalsIgnoreCase("s");

        ObjGrafico g = new ObjGrafico(
                tipo,
                new Rectangulo(x, y, w, h),
                new Color((byte) 0, (byte) 0, (byte) 0),
                new Color((byte) 0, (byte) 0, (byte) 255),
                relleno);

        IForma f = lista.nuevaForma(g);
        System.out.println("Forma creada con version: " + f.obtVersion());
    }

    private static void listar(IListaForma lista) {
        IForma[] formas = lista.todaForma();
        if (formas.length == 0) {
            System.out.println("No hay formas registradas.");
            return;
        }
        for (IForma f : formas) {
            ObjGrafico o = f.obtEstados();
            System.out.println("Version " + f.obtVersion() + " -> " + o.tipo
                    + " [" + o.cerrado.x + ", " + o.cerrado.y + ", "
                    + o.cerrado.width + ", " + o.cerrado.height + "]"
                    + (o.esRelleno ? " con relleno" : " sin relleno"));
        }
    }
}
