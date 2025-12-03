package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class VisualizadorImplicaciones extends JPanel {
    private Set<ConversorClausulas.Clausula> clausulas;
    private Map<String, Point> posiciones;
    private Map<String, List<String>> grafo;
    private Set<String> nodos;
    private int centroX, centroY, radio;

    public VisualizadorImplicaciones(Set<ConversorClausulas.Clausula> clausulas) {
        this.clausulas = clausulas;
        this.posiciones = new HashMap<>();
        this.grafo = new HashMap<>();
        this.nodos = new HashSet<>();

        construirGrafoReal();
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.WHITE);
    }

    private void construirGrafoReal() {
        // PASO 1: Recolectar SOLO literales que aparecen en las cláusulas
        Set<String> literalesEnClausulas = new HashSet<>();

        for (ConversorClausulas.Clausula c : clausulas) {
            if (!c.esUnitaria()) {
                literalesEnClausulas.add(c.literal1);
                literalesEnClausulas.add(c.literal2);
            }
        }

        System.out.println("Literales en cláusulas: " + literalesEnClausulas);

        // PASO 2: Construir grafo de implicaciones
        for (ConversorClausulas.Clausula c : clausulas) {
            if (!c.esUnitaria()) {
                // Para cláusula (A ∨ B), tenemos:
                // ¬A → B  y  ¬B → A

                String complementoA = obtenerComplemento(c.literal1);
                String complementoB = obtenerComplemento(c.literal2);

                // Solo crear nodos para complementos si el complemento
                // está en las cláusulas originales
                boolean crearComplementoA = literalesEnClausulas.contains(complementoA);
                boolean crearComplementoB = literalesEnClausulas.contains(complementoB);

                // Agregar nodos que SÍ existen
                nodos.add(c.literal1);
                nodos.add(c.literal2);

                // Agregar arista ¬A → B (solo si ¬A existe como literal)
                if (crearComplementoA || esComplementoNecesario(complementoA, clausulas)) {
                    nodos.add(complementoA);
                    grafo.computeIfAbsent(complementoA, k -> new ArrayList<>()).add(c.literal2);
                }

                // Agregar arista ¬B → A (solo si ¬B existe como literal)
                if (crearComplementoB || esComplementoNecesario(complementoB, clausulas)) {
                    nodos.add(complementoB);
                    grafo.computeIfAbsent(complementoB, k -> new ArrayList<>()).add(c.literal1);
                }
            }
        }

        System.out.println("Nodos finales en grafo: " + nodos);
        System.out.println("Grafo construido: " + grafo);

        calcularPosicionesCirculares();
    }

    private boolean esComplementoNecesario(String complemento, Set<ConversorClausulas.Clausula> clausulas) {
        // Un complemento es necesario si aparece en el lado izquierdo de alguna implicación
        for (ConversorClausulas.Clausula c : clausulas) {
            if (!c.esUnitaria()) {
                String comp1 = obtenerComplemento(c.literal1);
                String comp2 = obtenerComplemento(c.literal2);

                if (complemento.equals(comp1) || complemento.equals(comp2)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String obtenerComplemento(String literal) {
        return literal.startsWith("-") ? literal.substring(1) : "-" + literal;
    }

    private void calcularPosicionesCirculares() {
        List<String> listaNodos = new ArrayList<>(nodos);
        int n = listaNodos.size();

        centroX = 400;
        centroY = 300;
        radio = Math.min(250, 200 + n * 15); // Ajustar radio según cantidad de nodos

        for (int i = 0; i < n; i++) {
            double angulo = 2 * Math.PI * i / n;
            int x = centroX + (int)(radio * Math.cos(angulo));
            int y = centroY + (int)(radio * Math.sin(angulo));
            posiciones.put(listaNodos.get(i), new Point(x, y));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Título
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Grafo de Implicaciones", 20, 30);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("Cláusulas: " + clausulas.size() + " | Nodos: " + nodos.size(), 20, 50);

        // Dibujar aristas primero
        g2d.setStroke(new BasicStroke(1.5f));
        for (Map.Entry<String, List<String>> entry : grafo.entrySet()) {
            Point p1 = posiciones.get(entry.getKey());
            if (p1 == null) continue;

            for (String destino : entry.getValue()) {
                Point p2 = posiciones.get(destino);
                if (p2 == null) continue;

                // Color según tipo de implicación
                if (entry.getKey().startsWith("-")) {
                    g2d.setColor(new Color(200, 0, 0, 180)); // Rojo semitransparente
                } else {
                    g2d.setColor(new Color(0, 100, 0, 180)); // Verde semitransparente
                }

                dibujarFlecha(g2d, p1.x, p1.y, p2.x, p2.y);
            }
        }

        // Dibujar nodos después (encima de las aristas)
        int radioNodo = 25;
        for (Map.Entry<String, Point> entry : posiciones.entrySet()) {
            String nodo = entry.getKey();
            Point p = entry.getValue();

            // Color del nodo según si es positivo o negativo
            if (nodo.startsWith("-")) {
                g2d.setColor(new Color(255, 200, 200)); // Rosa claro para negativos
            } else {
                g2d.setColor(new Color(200, 230, 255)); // Azul claro para positivos
            }

            g2d.fillOval(p.x - radioNodo/2, p.y - radioNodo/2, radioNodo, radioNodo);

            // Borde
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(p.x - radioNodo/2, p.y - radioNodo/2, radioNodo, radioNodo);

            // Texto
            g2d.setColor(Color.BLACK);
            FontMetrics fm = g2d.getFontMetrics();
            int anchoTexto = fm.stringWidth(nodo);
            int altoTexto = fm.getHeight();
            g2d.drawString(nodo, p.x - anchoTexto/2, p.y + altoTexto/4);
        }

        // Leyenda
        dibujarLeyenda(g2d);
    }

    private void dibujarFlecha(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        // Calcular punto de control para curva
        int cx = (x1 + x2) / 2;
        int cy = (y1 + y2) / 2;

        // Desplazar para curva
        double angulo = Math.atan2(y2 - y1, x2 - x1);
        int desplazamiento = 30;
        cx += (int)(desplazamiento * Math.cos(angulo + Math.PI/2));
        cy += (int)(desplazamiento * Math.sin(angulo + Math.PI/2));

        // Dibujar curva
        g2d.draw(new java.awt.geom.QuadCurve2D.Float(x1, y1, cx, cy, x2, y2));

        // Flecha al final
        double anguloFlecha = Math.atan2(y2 - cy, x2 - cx);
        int tamañoFlecha = 12;

        int x3 = (int)(x2 - tamañoFlecha * Math.cos(anguloFlecha - Math.PI/6));
        int y3 = (int)(y2 - tamañoFlecha * Math.sin(anguloFlecha - Math.PI/6));
        int x4 = (int)(x2 - tamañoFlecha * Math.cos(anguloFlecha + Math.PI/6));
        int y4 = (int)(y2 - tamañoFlecha * Math.sin(anguloFlecha + Math.PI/6));

        g2d.fillPolygon(
                new int[]{x2, x3, x4},
                new int[]{y2, y3, y4},
                3
        );
    }

    private void dibujarLeyenda(Graphics2D g2d) {
        int y = 550;
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));

        // Literal positivo
        g2d.setColor(new Color(200, 230, 255));
        g2d.fillRect(20, y, 15, 15);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(20, y, 15, 15);
        g2d.drawString("Literal positivo", 40, y + 12);

        // Literal negativo
        g2d.setColor(new Color(255, 200, 200));
        g2d.fillRect(150, y, 15, 15);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(150, y, 15, 15);
        g2d.drawString("Literal negativo", 170, y + 12);

        // Implicación positiva
        g2d.setColor(new Color(0, 100, 0));
        g2d.drawLine(300, y + 7, 320, y + 7);
        g2d.drawString("¬positivo → algo", 325, y + 12);

        // Implicación negativa
        g2d.setColor(new Color(200, 0, 0));
        g2d.drawLine(470, y + 7, 490, y + 7);
        g2d.drawString("¬negativo → algo", 495, y + 12);
    }

    public static void mostrarGrafo(Set<ConversorClausulas.Clausula> clausulas) {
        JFrame frame = new JFrame("Grafo de Implicaciones");
        VisualizadorImplicaciones panel = new VisualizadorImplicaciones(clausulas);

        frame.add(new JScrollPane(panel));
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}