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

    //1. Metodo estatico de entrada principal
    //metodo estatico que inicia la vizualizacion
    public static void mostrarGrafo(Set<ConversorClausulas.Clausula> clausulas) {
        JFrame frame = new JFrame("Grafo de Implicaciones");
        VisualizadorImplicaciones panel = new VisualizadorImplicaciones(clausulas);

        frame.add(new JScrollPane(panel));
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }


    //2. Metodos de construccion y configuracion del grafo
    //inicializa el panel con las clausulas
    public VisualizadorImplicaciones(Set<ConversorClausulas.Clausula> clausulas) {
        this.clausulas = clausulas;
        this.posiciones = new HashMap<>();
        this.grafo = new HashMap<>();
        this.nodos = new HashSet<>();

        construirGrafoReal();
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.WHITE);
    }

    //convierte las clausulas en estructura de grafo de implicaciones
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

    //decide si un complemento debe incluirse como nodo
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

    //devuelve el complemento de una literal
    private String obtenerComplemento(String literal) {
        return literal.startsWith("-") ? literal.substring(1) : "-" + literal;
    }

    //distribuye los nodos en circulo
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


    //3. Metodos de renderizado grafico
    //dibuja el grafo completo, nodos, aristas texto.
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

                // En el metodo paintComponent, modifica la parte donde dibujas las aristas:
                // Reemplaza la parte del color por esto:
                if (entry.getKey().startsWith("-")) {
                    g2d.setColor(new Color(200, 0, 0, 220)); // Rojo más opaco
                } else {
                    g2d.setColor(new Color(0, 100, 0, 220)); // Verde más opaco
                }

                // Aumenta el grosor de la línea para mejor visibilidad
                g2d.setStroke(new BasicStroke(2.0f));

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

    //dibuja leyenda explicativa en la parte inferior
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


    //4. Metodo de dibujar las flechas y formas
    //dibuja una arista curva en todos los nodos
    private void dibujarFlecha(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        // Calcular el ángulo de la línea
        double angle = Math.atan2(y2 - y1, x2 - x1);

        // Acortar la línea para que no empiece/termine en el borde del nodo
        int radioNodo = 25;
        int x1a = (int)(x1 + radioNodo/2 * Math.cos(angle));
        int y1a = (int)(y1 + radioNodo/2 * Math.sin(angle));
        int x2a = (int)(x2 - radioNodo/2 * Math.cos(angle));
        int y2a = (int)(y2 - radioNodo/2 * Math.sin(angle));

        // Calcular punto de control para curva (si es una autorreferencia)
        if (x1 == x2 && y1 == y2) {
            // Caso especial: autorreferencia (bucle)
            g2d.drawArc(x1 - radioNodo, y1 - 2*radioNodo, 2*radioNodo, 2*radioNodo, 0, 360);

            // Dibujar flecha en el bucle
            int arrowX = x1;
            int arrowY = y1 - 2*radioNodo;
            drawArrowHead(g2d, arrowX, arrowY, Math.PI/2);
            return;
        }

        // Calcular punto de control para curva suave
        double distancia = Math.sqrt(Math.pow(x2a - x1a, 2) + Math.pow(y2a - y1a, 2));
        int desplazamiento = Math.max(20, (int)(distancia * 0.3));

        int cx = (x1a + x2a) / 2;
        int cy = (y1a + y2a) / 2;

        // Perpendicular al vector dirección
        double perpendicularAngle = angle + Math.PI/2;
        cx += (int)(desplazamiento * Math.cos(perpendicularAngle));
        cy += (int)(desplazamiento * Math.sin(perpendicularAngle));

        // Dibujar la curva con Bezier cuadrática
        java.awt.geom.QuadCurve2D curve = new java.awt.geom.QuadCurve2D.Float();
        curve.setCurve(x1a, y1a, cx, cy, x2a, y2a);
        g2d.draw(curve);

        // Calcular punto en la curva cerca del final para la flecha
        double t = 0.8; // Punto al 80% de la curva
        double curveX = Math.pow(1-t, 2)*x1a + 2*(1-t)*t*cx + Math.pow(t, 2)*x2a;
        double curveY = Math.pow(1-t, 2)*y1a + 2*(1-t)*t*cy + Math.pow(t, 2)*y2a;

        // Derivada para la tangente
        double dx = 2*(1-t)*(cx - x1a) + 2*t*(x2a - cx);
        double dy = 2*(1-t)*(cy - y1a) + 2*t*(y2a - cy);
        double tangentAngle = Math.atan2(dy, dx);

        // Dibujar la flecha en el punto correcto de la curva
        drawArrowHead(g2d, (int)curveX, (int)curveY, tangentAngle);
    }

    //dibuja la punta de la arista en el extremo
    private void drawArrowHead(Graphics2D g2d, int x, int y, double angle) {
        int arrowSize = 12;

        // Calcular puntos de la flecha
        int x2 = (int)(x - arrowSize * Math.cos(angle - Math.PI/6));
        int y2 = (int)(y - arrowSize * Math.sin(angle - Math.PI/6));

        int x3 = (int)(x - arrowSize * Math.cos(angle + Math.PI/6));
        int y3 = (int)(y - arrowSize * Math.sin(angle + Math.PI/6));

        // Crear polígono de flecha
        Polygon arrowHead = new Polygon();
        arrowHead.addPoint(x, y);
        arrowHead.addPoint(x2, y2);
        arrowHead.addPoint(x3, y3);

        // Dibujar flecha
        g2d.fill(arrowHead);
        g2d.setColor(g2d.getColor().darker()); // Color más oscuro para el borde
        g2d.draw(arrowHead);
    }

}

