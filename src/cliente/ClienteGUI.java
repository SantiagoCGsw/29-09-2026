package cliente;

import Formas.IForma;
import Formas.IListaForma;
import Formas.IListaFormaHelper;
import Formas.ObjGrafico;
import Formas.Rectangulo;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

public class ClienteGUI extends JFrame {

    private IListaForma lista;
    private final List<ObjGrafico> objetos = new ArrayList<>();

    private final JComboBox<String> cmbTipo =
            new JComboBox<>(new String[]{"Rectangulo", "Circulo", "Linea"});
    private final JSpinner spX = new JSpinner(new SpinnerNumberModel(40, 0, 600, 5));
    private final JSpinner spY = new JSpinner(new SpinnerNumberModel(40, 0, 600, 5));
    private final JSpinner spW = new JSpinner(new SpinnerNumberModel(120, 1, 600, 5));
    private final JSpinner spH = new JSpinner(new SpinnerNumberModel(80, 1, 600, 5));
    private final JCheckBox chkRelleno = new JCheckBox("Con relleno", true);

    private Color colorLinea = Color.BLACK;
    private Color colorRelleno = new Color(80, 140, 220);
    private final JPanel swLinea = crearMuestra(colorLinea);
    private final JPanel swRelleno = crearMuestra(colorRelleno);

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Version", "Tipo", "x", "y", "Ancho", "Alto", "Relleno"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final PanelDibujo panel = new PanelDibujo();
    private final JLabel lblEstado = new JLabel(" ");

    public ClienteGUI(String[] args) {
        super("Formas distribuidas - CORBA");
        conectar(args);
        construirInterfaz();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        refrescar("Conectado al servidor");
    }

    private void conectar(String[] args) {
        try {
            ORB orb = ORB.init(args, null);
            NamingContextExt nc = NamingContextExtHelper.narrow(
                    orb.resolve_initial_references("NameService"));
            lista = IListaFormaHelper.narrow(nc.resolve_str("listaDeFormas"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "No se pudo conectar al servidor CORBA.\n"
                    + "Verifique que orbd y el servidor esten en ejecucion.\n\n" + e,
                    "Error de conexion", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void construirInterfaz() {
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Tipo"));          form.add(cmbTipo);
        form.add(new JLabel("Posicion x"));    form.add(spX);
        form.add(new JLabel("Posicion y"));    form.add(spY);
        form.add(new JLabel("Ancho"));         form.add(spW);
        form.add(new JLabel("Alto"));          form.add(spH);
        form.add(new JLabel("Relleno"));       form.add(chkRelleno);

        JButton btnLinea = new JButton("Elegir");
        btnLinea.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Color de linea", colorLinea);
            if (c != null) { colorLinea = c; swLinea.setBackground(c); }
        });
        JButton btnRelleno = new JButton("Elegir");
        btnRelleno.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Color de relleno", colorRelleno);
            if (c != null) { colorRelleno = c; swRelleno.setBackground(c); }
        });
        form.add(new JLabel("Color de linea"));   form.add(filaColor(swLinea, btnLinea));
        form.add(new JLabel("Color de relleno")); form.add(filaColor(swRelleno, btnRelleno));

        JButton btnCrear = new JButton("Crear forma");
        btnCrear.addActionListener(e -> crear());
        JButton btnActualizar = new JButton("Actualizar lista");
        btnActualizar.addActionListener(e -> refrescar("Lista actualizada"));
        JButton btnVersion = new JButton("Ver version");
        btnVersion.addActionListener(e -> {
            try {
                JOptionPane.showMessageDialog(this,
                        "Version actual de la lista: " + lista.obtVersion(),
                        "Version", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) { error(ex); }
        });

        JPanel botones = new JPanel(new GridLayout(0, 1, 8, 8));
        botones.add(btnCrear);
        botones.add(btnActualizar);
        botones.add(btnVersion);

        JPanel izq = new JPanel(new BorderLayout(0, 12));
        izq.setBorder(BorderFactory.createTitledBorder("Nueva forma"));
        izq.add(form, BorderLayout.NORTH);
        izq.add(botones, BorderLayout.SOUTH);

        tabla.getSelectionModel().addListSelectionListener(e -> panel.repaint());
        JScrollPane sp = new JScrollPane(tabla);
        sp.setPreferredSize(new Dimension(520, 150));

        JPanel centro = new JPanel(new BorderLayout(0, 8));
        centro.add(panel, BorderLayout.CENTER);
        centro.add(sp, BorderLayout.SOUTH);

        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(izq, BorderLayout.WEST);
        add(centro, BorderLayout.CENTER);
        add(lblEstado, BorderLayout.SOUTH);
    }

    private void crear() {
        try {
            ObjGrafico g = new ObjGrafico(
                    (String) cmbTipo.getSelectedItem(),
                    new Rectangulo((Integer) spX.getValue(), (Integer) spY.getValue(),
                            (Integer) spW.getValue(), (Integer) spH.getValue()),
                    aCorba(colorLinea), aCorba(colorRelleno), chkRelleno.isSelected());
            IForma f = lista.nuevaForma(g);
            refrescar("Forma creada con version " + f.obtVersion());
        } catch (Exception e) {
            error(e);
        }
    }

    private void refrescar(String mensaje) {
        try {
            IForma[] formas = lista.todaForma();
            modelo.setRowCount(0);
            objetos.clear();
            for (IForma f : formas) {
                ObjGrafico o = f.obtEstados();
                objetos.add(o);
                modelo.addRow(new Object[]{f.obtVersion(), o.tipo, o.cerrado.x, o.cerrado.y,
                        o.cerrado.width, o.cerrado.height, o.esRelleno ? "Si" : "No"});
            }
            panel.repaint();
            lblEstado.setText(mensaje + "   |   Version de la lista: " + lista.obtVersion()
                    + "   |   Formas: " + formas.length);
        } catch (Exception e) {
            error(e);
        }
    }

    private void error(Exception e) {
        JOptionPane.showMessageDialog(this, "Error en la llamada CORBA:\n" + e,
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static Formas.Color aCorba(Color c) {
        return new Formas.Color((byte) c.getRed(), (byte) c.getGreen(), (byte) c.getBlue());
    }

    private static Color deCorba(Formas.Color c) {
        return new Color(c.r & 0xFF, c.g & 0xFF, c.b & 0xFF);
    }

    private static JPanel crearMuestra(Color c) {
        JPanel p = new JPanel();
        p.setOpaque(true);
        p.setBackground(c);
        p.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        p.setPreferredSize(new Dimension(28, 24));
        return p;
    }

    private static JPanel filaColor(JPanel muestra, JButton boton) {
        JPanel fila = new JPanel(new BorderLayout(6, 0));
        fila.add(muestra, BorderLayout.WEST);
        fila.add(boton, BorderLayout.CENTER);
        return fila;
    }

    private class PanelDibujo extends JPanel {
        PanelDibujo() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(520, 340));
            setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int sel = tabla.getSelectedRow();
            for (int i = 0; i < objetos.size(); i++) {
                ObjGrafico o = objetos.get(i);
                int x = o.cerrado.x, y = o.cerrado.y, w = o.cerrado.width, h = o.cerrado.height;
                String t = o.tipo.trim().toLowerCase();
                boolean circulo = t.startsWith("c");
                boolean linea = t.startsWith("l");
                if (linea) {
                    g.setColor(deCorba(o.linea));
                    g.drawLine(x, y, x + w, y + h);
                } else {
                    if (o.esRelleno) {
                        g.setColor(deCorba(o.relleno));
                        if (circulo) g.fillOval(x, y, w, h); else g.fillRect(x, y, w, h);
                    }
                    g.setColor(deCorba(o.linea));
                    if (circulo) g.drawOval(x, y, w, h); else g.drawRect(x, y, w, h);
                }
                if (i == sel) {
                    g.setColor(Color.RED);
                    g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT,
                            BasicStroke.JOIN_MITER, 10f, new float[]{4f, 4f}, 0f));
                    g.drawRect(x - 3, y - 3, w + 6, h + 6);
                    g.setStroke(new BasicStroke(1f));
                }
            }
            g.dispose();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignorada) { }
        SwingUtilities.invokeLater(() -> new ClienteGUI(args).setVisible(true));
    }
}
