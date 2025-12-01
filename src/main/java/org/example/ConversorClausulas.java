package org.example;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;
import org.graphstream.ui.view.Viewer;

import java.util.*;

public class ConversorClausulas {

    // Clase para representar cualquier cláusula (unitaria o binaria)
    public static class Clausula {
        String literal1;
        String literal2;  // Puede ser null para cláusulas unitarias

        public Clausula(String lit1) {
            this.literal1 = lit1;
            this.literal2 = null;  // Cláusula unitaria
        }

        public Clausula(String lit1, String lit2) {
            this.literal1 = lit1;
            this.literal2 = lit2;  // Cláusula binaria
        }

        public boolean esUnitaria() {
            return literal2 == null;
        }

        @Override
        public String toString() {
            if (esUnitaria()) {
                return "(" + literal1 + ")";  // Cláusula unitaria
            } else {
                return "(" + literal1 + " ∨ " + literal2 + ")";  // Cláusula binaria
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Clausula other = (Clausula) obj;

            if (this.esUnitaria() && other.esUnitaria()) {
                return Objects.equals(literal1, other.literal1);
            } else if (!this.esUnitaria() && !other.esUnitaria()) {
                return (Objects.equals(literal1, other.literal1) &&
                        Objects.equals(literal2, other.literal2)) ||
                        (Objects.equals(literal1, other.literal2) &&
                                Objects.equals(literal2, other.literal1));
            }
            return false;
        }

        @Override
        public int hashCode() {
            if (esUnitaria()) {
                return Objects.hash(literal1, "unitaria");
            } else {
                return Objects.hash(literal1, literal2) + Objects.hash(literal2, literal1);
            }
        }
    }

    // Convierte una relación en cláusulas (unitarias y binarias)
    public static Set<Clausula> convertirRelacionAClausulas(Set<Par> relacion) {
        Set<Clausula> clausulas = new HashSet<>();

        for (Par par : relacion) {
            if (par.y == null) {
                // CASO 1: Par individual (x) - CLAÚSULA UNITARIA
                // (x) representa reflexividad: x debe ser verdadero
                String xPositivo = String.valueOf(par.x);
                clausulas.add(new Clausula(xPositivo));

                System.out.println("Para (" + par.x + "):");
                System.out.println("  Reflexividad: " + xPositivo + " debe ser verdadero");
                System.out.println("  Cláusula unitaria: (" + xPositivo + ")");

            } else {
                // CASO 2: Par completo (x,y) - CLAÚSULAS BINARIAS
                String xPositivo = String.valueOf(par.x);
                String yPositivo = String.valueOf(par.y);

                // Para (x,y) generamos:
                // -x → y  ≡  x ∨ y
                clausulas.add(new Clausula(xPositivo, yPositivo));

                System.out.println("Para (" + par.x + "," + par.y + "):");
                System.out.println("  -" + par.x + " → " + par.y + " ≡ " + xPositivo + " ∨ " + yPositivo);
                System.out.println("  -" + par.y + " → " + par.x + " ≡ " + yPositivo + " ∨ " + xPositivo);
            }
        }

        return clausulas;
    }

    // Método para mostrar las cláusulas de forma organizada
    public static void mostrarClausulas(Set<Clausula> clausulas) {
        System.out.println("\n=== CLAÚSULAS GENERADAS ===");

        // Separar cláusulas unitarias y binarias
        List<Clausula> unitarias = new ArrayList<>();
        List<Clausula> binarias = new ArrayList<>();

        for (Clausula c : clausulas) {
            if (c.esUnitaria()) {
                unitarias.add(c);
            } else {
                binarias.add(c);
            }
        }

        // Ordenar
        unitarias.sort((c1, c2) -> c1.literal1.compareTo(c2.literal1));
        binarias.sort((c1, c2) -> {
            int cmp = c1.literal1.compareTo(c2.literal1);
            return cmp != 0 ? cmp : c1.literal2.compareTo(c2.literal2);
        });

        // Mostrar cláusulas unitarias
        if (!unitarias.isEmpty()) {
            System.out.println("\nCláusulas Unitarias:");
            for (int i = 0; i < unitarias.size(); i++) {
                System.out.println("U" + (i+1) + ": " + unitarias.get(i));
            }
        }

        // Mostrar cláusulas binarias
        if (!binarias.isEmpty()) {
            System.out.println("\nCláusulas Binarias:");
            for (int i = 0; i < binarias.size(); i++) {
                System.out.println("C" + (i+1) + ": " + binarias.get(i));
            }
        }

        System.out.println("\nTotal: " + unitarias.size() + " unitarias, " +
                binarias.size() + " binarias");
    }

    // MÉTODO SIMPLE PARA GRAFICAR LAS IMPLICACIONES - VERSIÓN CORREGIDA
    public static void graficarImplicaciones(Set<Clausula> clausulas) {

        System.out.println("\n=== CREANDO GRÁFICO DE IMPLICACIONES ===");

        try {
            System.setProperty("org.graphstream.ui", "swing");
            Graph graph = new SingleGraph("Implicaciones Lógicas");

            // ESTILO MEJORADO - Deshabilitar layout automático
//            graph.setAttribute("ui.stylesheet",
//                    "node { fill-color: blue; size: 30px; text-mode: normal; text-size: 18; }" +
//                            "edge { fill-color: red; arrow-size: 20px, 8px; }");
//            graph.setAttribute("ui.stylesheet",
//                    "graph { fill-color: black; }" +
//                            "node { fill-color: white; size: 30px; text-mode: normal; text-size: 18; text-color: white; }" +
//                            "edge { fill-color: red; arrow-size: 20px, 8px; }");

            graph.setAttribute("ui.stylesheet",
                    "graph { fill-color: white; }" +                    // FONDO BLANCO
                            "node { fill-color: black; size: 30px; text-mode: normal; text-size: 18; text-color: white; }" +  // NODO NEGRO, TEXTO BLANCO
                            "edge { fill-color: red; arrow-size: 20px, 8px; }"); // ARISTAS ROJAS

            // DESHABILITAR LAYOUT AUTOMÁTICO
            graph.setAttribute("layout.quality", 0);
            graph.setAttribute("ui.quality");

            Set<String> literalesUnicos = new HashSet<>();

            // Primero recolectar todos los literales únicos
            for (Clausula c : clausulas) {
                if (!c.esUnitaria()) {
                    literalesUnicos.add(c.literal1);
                    literalesUnicos.add(c.literal2);
                    // También agregar sus complementos
                    literalesUnicos.add(obtenerComplemento(c.literal1));
                    literalesUnicos.add(obtenerComplemento(c.literal2));
                }
            }

            // Crear nodos con POSICIÓN MANUAL
            int posX = 0;
            int posY = 0;
            List<String> listaLiterales = new ArrayList<>(literalesUnicos);
            Collections.sort(listaLiterales);

            for (String literal : listaLiterales) {
                if (graph.getNode(literal) == null) {
                    Node node = graph.addNode(literal);
                    node.setAttribute("ui.label", literal);

                    // POSICIÓN MANUAL para evitar el problema del layout
                    node.setAttribute("x", posX);
                    node.setAttribute("y", posY);
                    node.setAttribute("z", 0);

                    posX += 2; // Espaciar horizontalmente
                    if (posX > 6) {
                        posX = 0;
                        posY += 2;
                    }
                }
            }

            // Crear aristas de implicación
            int aristasCreadas = 0;
            for (Clausula c : clausulas) {
                if (!c.esUnitaria()) {
                    // Para cláusula (A ∨ B), crear: ¬A → B y ¬B → A
                    String arista1 = crearAristaImplicacion(graph, c.literal1, c.literal2);
                    String arista2 = crearAristaImplicacion(graph, c.literal2, c.literal1);

                    if (arista1 != null) aristasCreadas++;
                    if (arista2 != null) aristasCreadas++;

                    System.out.println("Cláusula " + c + " → " + arista1 + " y " + arista2);
                }
            }

            System.out.println("Grafo creado: " + graph.getNodeCount() + " nodos, " + aristasCreadas + " aristas");

            // Mostrar el grafo SIN layout automático
            Viewer viewer = graph.display();
            viewer.disableAutoLayout(); // IMPORTANTE: Deshabilitar layout automático

        } catch (Exception e) {
            System.out.println("Error al crear gráfico: " + e.getMessage());
            System.out.println("Mostrando representación textual en su lugar:");
            mostrarGrafoTexto(clausulas);
        }
    }

    // Método auxiliar para crear una arista de implicación
    private static String crearAristaImplicacion(Graph graph, String literalA, String literalB) {
        String desde = obtenerComplemento(literalA);
        String hacia = literalB;
        String edgeId = desde + "→" + hacia;

        try {
            if (graph.getNode(desde) != null && graph.getNode(hacia) != null &&
                    graph.getEdge(edgeId) == null) {
                graph.addEdge(edgeId, desde, hacia, true);
                return edgeId;
            }
        } catch (Exception e) {
            // Ignorar aristas duplicadas
        }
        return null;
    }

    // Método auxiliar para obtener complemento
    private static String obtenerComplemento(String literal) {
        if (literal.startsWith("-")) {
            return literal.substring(1);
        } else {
            return "-" + literal;
        }
    }

    // Método de respaldo para mostrar el grafo en texto
    private static void mostrarGrafoTexto(Set<Clausula> clausulas) {
        System.out.println("\n=== REPRESENTACIÓN TEXTUAL DEL GRAFO ===");

        Map<String, Set<String>> grafo = new HashMap<>();

        // Construir grafo en memoria
        for (Clausula c : clausulas) {
            if (!c.esUnitaria()) {
                String implicacion1 = obtenerComplemento(c.literal1) + " → " + c.literal2;
                String implicacion2 = obtenerComplemento(c.literal2) + " → " + c.literal1;

                String desde1 = obtenerComplemento(c.literal1);
                String desde2 = obtenerComplemento(c.literal2);

                grafo.computeIfAbsent(desde1, k -> new HashSet<>()).add(c.literal2);
                grafo.computeIfAbsent(desde2, k -> new HashSet<>()).add(c.literal1);

                System.out.println("  " + implicacion1);
                System.out.println("  " + implicacion2);
            }
        }

        System.out.println("\nResumen del grafo:");
        for (Map.Entry<String, Set<String>> entry : grafo.entrySet()) {
            System.out.println("  " + entry.getKey() + " → " + entry.getValue());
        }
    }

    // Procesar y mostrar todo INCLUYENDO GRÁFICO
    public static void procesarYMostrarClausulas(Set<Par> relacion) {
        System.out.println("\n=== CONVERSIÓN A LÓGICA PROPOSICIONAL ===");
        System.out.println("Relación original: " + relacion);

        Set<Clausula> clausulas = convertirRelacionAClausulas(relacion);
        mostrarClausulas(clausulas);

        // Análisis de implicaciones
        analizarImplicaciones(clausulas);

        // MOSTRAR GRÁFICO
        graficarImplicaciones(clausulas);
    }

    // Análisis adicional de lo que significan las cláusulas
    public static void analizarImplicaciones(Set<Clausula> clausulas) {
        System.out.println("\n=== ANÁLISIS DE IMPLICACIONES ===");

        for (Clausula c : clausulas) {
            if (c.esUnitaria()) {
                System.out.println(c + " significa: " + c.literal1 + " debe ser VERDADERO");
            } else {
                // Para cláusula (A ∨ B), es equivalente a -A → B y -B → A
                System.out.println(c + " ≡ " +
                        "(¬" + c.literal1 + " → " + c.literal2 + ") ∧ " +
                        "(¬" + c.literal2 + " → " + c.literal1 + ")");
            }
        }
    }

    // En tu clase ConversorClausulas.java, agrega este método:

    public static boolean resolver2SAT(Set<Par> relacion) {
        System.out.println("\n=== RESOLUCIÓN 2-SAT ===");

        // Convertir la relación a cláusulas
        Set<Clausula> clausulas = convertirRelacionAClausulas(relacion);

        // Obtener todas las variables únicas
        Set<String> variables = new HashSet<>();
        for (Clausula c : clausulas) {
            if (!c.esUnitaria()) {
                variables.add(extraerVariable(c.literal1));
                variables.add(extraerVariable(c.literal2));
            }
        }

        System.out.println("Variables encontradas: " + variables);
        System.out.println("Número de cláusulas: " + clausulas.size());

        // Intentar todas las combinaciones posibles (fuerza bruta)
        // Para n variables, hay 2^n combinaciones
        int n = variables.size();
        List<String> listaVariables = new ArrayList<>(variables);

        System.out.println("Probando " + (1 << n) + " combinaciones posibles...");

        for (int i = 0; i < (1 << n); i++) {
            Map<String, Boolean> asignacion = new HashMap<>();

            // Crear asignación para esta combinación
            for (int j = 0; j < n; j++) {
                String variable = listaVariables.get(j);
                boolean valor = ((i >> j) & 1) == 1;
                asignacion.put(variable, valor);
            }

            // Verificar si esta asignación satisface todas las cláusulas
            if (esSatisfacible(clausulas, asignacion)) {
                System.out.println("✅ ¡SATISFACIBLE!");
                System.out.println("Asignación que satisface:");
                for (Map.Entry<String, Boolean> entry : asignacion.entrySet()) {
                    System.out.println("  " + entry.getKey() + " = " + entry.getValue());
                }
                return true;
            }
        }

        System.out.println("❌ NO SATISFACIBLE");
        System.out.println("No existe asignación que satisfaga todas las cláusulas");
        return false;
    }

    // Método auxiliar para extraer el nombre de la variable (sin el negativo)
    private static String extraerVariable(String literal) {
        if (literal.startsWith("-")) {
            return literal.substring(1);
        }
        return literal;
    }

    // Método auxiliar para evaluar una cláusula con una asignación dada
    private static boolean evaluarClausula(Clausula c, Map<String, Boolean> asignacion) {
        if (c.esUnitaria()) {
            // Cláusula unitaria: (A)
            String variable = extraerVariable(c.literal1);
            boolean valor = asignacion.get(variable);
            if (c.literal1.startsWith("-")) {
                return !valor; // Si es -A, entonces debe ser false
            } else {
                return valor;  // Si es A, entonces debe ser true
            }
        } else {
            // Cláusula binaria: (A ∨ B)
            String var1 = extraerVariable(c.literal1);
            String var2 = extraerVariable(c.literal2);

            boolean val1 = asignacion.get(var1);
            boolean val2 = asignacion.get(var2);

            // Aplicar negaciones si es necesario
            if (c.literal1.startsWith("-")) val1 = !val1;
            if (c.literal2.startsWith("-")) val2 = !val2;

            return val1 || val2; // OR lógico
        }
    }

    // Método auxiliar para verificar si una asignación satisface todas las cláusulas
    private static boolean esSatisfacible(Set<Clausula> clausulas, Map<String, Boolean> asignacion) {
        for (Clausula c : clausulas) {
            if (!evaluarClausula(c, asignacion)) {
                return false; // Si una cláusula no se satisface, toda la fórmula falla
            }
        }
        return true;
    }



}