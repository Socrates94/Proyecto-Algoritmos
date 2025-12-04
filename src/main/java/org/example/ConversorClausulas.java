package org.example;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;

import java.util.*;

public class ConversorClausulas {

    //clase para representar cualquier cláusula (unitaria o binaria)
    public static class Clausula {

        String literal1;
        String literal2;  // Puede ser null para cláusulas unitarias

        //clausula unaria
        public Clausula(String lit1) {
            this.literal1 = lit1;
            this.literal2 = null;
        }

        //clausula binaria
        public Clausula(String lit1, String lit2) {
            this.literal1 = lit1;
            this.literal2 = lit2;
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

        //para comparar los objetos si son diferentes en memoria
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

        //para trabajar con estructuras como hashset o hashmap
        @Override
        public int hashCode() {
            if (esUnitaria()) {
                return Objects.hash(literal1, "unitaria");
            } else {
                return Objects.hash(literal1, literal2) + Objects.hash(literal2, literal1);
            }
        }
    }


    //1. Metodos principales de conversion y visualizacion==================================
    //convertir la relacion en cláusulas unarias y binarias
    public static Set<Clausula> convertirRelacionAClausulas(Set<Par> relacion) {
        Set<Clausula> clausulas = new HashSet<>();

        for (Par par : relacion) {

            if (par.y == null) {
                // CASO 1: Par individual (x) - CLAÚSULA UNITARIA
                // (x) representa reflexividad: x debe ser verdadero
                String xPositivo = String.valueOf(par.x);
                clausulas.add(new Clausula(xPositivo));

//                System.out.println("Para (" + par.x + "):");
//                System.out.println("  Reflexividad: " + xPositivo + " debe ser verdadero");
//                System.out.println("  Cláusula unitaria: (" + xPositivo + ")");

            } else {
                // CASO 2: Par completo (x,y) - CLAÚSULAS BINARIAS
                String xPositivo = String.valueOf(par.x);
                String yPositivo = String.valueOf(par.y);

                // Para (x,y) generamos:
                // -x → y  ≡  x ∨ y
                clausulas.add(new Clausula(xPositivo, yPositivo));

//                System.out.println("Para (" + par.x + "," + par.y + "):");
//                System.out.println("  -" + par.x + " → " + par.y + " ≡ " + xPositivo + " ∨ " + yPositivo);
//                System.out.println("  -" + par.y + " → " + par.x + " ≡ " + yPositivo + " ∨ " + xPositivo);
            }

        }
        System.out.println("\n");
        return clausulas;
    }

    //metodo para mostrar las cláusulas de forma organizada
    public static void mostrarClausulas(Set<Clausula> clausulas) {
        System.out.println("\n=====================================================");
        System.out.println("=== CLAÚSULAS GENERADAS ===");

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
            System.out.println("Cláusulas Unitarias:");
            for (int i = 0; i < unitarias.size(); i++) {
                System.out.println("U" + (i+1) + ": " + unitarias.get(i));
            }
        }

        // Mostrar cláusulas binarias
        if (!binarias.isEmpty()) {
            System.out.println("Cláusulas Binarias:");
            for (int i = 0; i < binarias.size(); i++) {
                System.out.println("C" + (i+1) + ": " + binarias.get(i));
            }
        }

        System.out.println("Total: " + unitarias.size() + " unitarias, " +
                binarias.size() + " binarias");
        System.out.println("=====================================================\n");
    }

    //procesar y mostrar
    public static void procesarYMostrarClausulas(Set<Par> relacion) {
        //System.out.println("\n=====================================================");
        //System.out.println("=== CONVERSIÓN A LÓGICA PROPOSICIONAL ===");
        //System.out.println("Relación original: " + relacion);

        Set<Clausula> clausulas = convertirRelacionAClausulas(relacion);
        mostrarClausulas(clausulas);

        // DEPURAR ANTES DE GRAFICAR
        depurarClausulas(clausulas);

        // Análisis de implicaciones
        analizarImplicaciones(clausulas);

        // MOSTRAR GRÁFICO
        graficarImplicaciones(clausulas);

    }//Fin de metodos principales======================================================


    //2. Analizar implicaciones========================================================
    // Análisis adicional de lo que significan las cláusulas
    public static void analizarImplicaciones(Set<Clausula> clausulas) {
        System.out.println("\n=====================================================");
        System.out.println("=== ANÁLISIS DE IMPLICACIONES ===");

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
        System.out.println("=====================================================\n");
    }

    //muestra detalles de depuracion para las clausulas binarias
    public static void depurarClausulas(Set<Clausula> clausulas) {
        System.out.println("\n=== DEPURACIÓN DE CLAÚSULAS ===");
        for (Clausula c : clausulas) {
            if (!c.esUnitaria()) {
                System.out.println("Cláusula: " + c);
                System.out.println("  literal1: " + c.literal1 + " → complemento: " + obtenerComplemento(c.literal1));
                System.out.println("  literal2: " + c.literal2 + " → complemento: " + obtenerComplemento(c.literal2));
            }
        }
    }// Fin analilzar implicaciones==========================================================


    //3. Metodos de resolucion 2-SAT
    //determina si el conjunto de clausulas es satisfactible
    public static boolean resolver2SAT(Set<Par> relacion) {
        System.out.println("\n=====================================================");
        System.out.println("=== RESOLUCIÓN 2-SAT ===");

        // Convertir la relación a cláusulas
        Set<Clausula> clausulas = convertirRelacionAClausulas(relacion);

        // 1. PRIMERO: Verificar asignaciones forzadas por cláusulas unitarias
        Map<String, Boolean> forzadas = extraerAsignacionesForzadas(clausulas);
        if (forzadas == null) {
            System.out.println("NO SATISFACIBLE (contradicción en cláusulas unitarias)");
            System.out.println("=====================================================\n");
            return false;  // Hay (A) y (¬A) al mismo tiempo → imposible
        }

        // 2. Obtener TODAS las variables únicas
        Set<String> variables = new HashSet<>();
        for (Clausula c : clausulas) {
            if (c.esUnitaria()) {
                variables.add(extraerVariable(c.literal1));
            } else {
                variables.add(extraerVariable(c.literal1));
                variables.add(extraerVariable(c.literal2));
            }
        }

        System.out.println("Variables encontradas: " + variables);
        System.out.println("Asignaciones forzadas por cláusulas unitarias: " + forzadas);
        System.out.println("Número de cláusulas: " + clausulas.size());

        // Si no hay variables, es trivialmente satisfacible
        if (variables.isEmpty()) {
            System.out.println("¡SATISFACIBLE! (sin variables)");
            return true;
        }

        // 3. REMOVER variables ya asignadas (optimización)
        variables.removeAll(forzadas.keySet());
        System.out.println("Variables por probar (después de quitar forzadas): " + variables);

        // 4. Intentar combinaciones para las variables RESTANTES
        int n = variables.size();
        List<String> listaVariables = new ArrayList<>(variables);

        System.out.println("Probando " + (1 << n) + " combinaciones posibles...");

        for (int i = 0; i < (1 << n); i++) {
            // 4a. Empezar con las asignaciones forzadas
            Map<String, Boolean> asignacion = new HashMap<>(forzadas);

            // 4b. Agregar valores para variables no forzadas
            for (int j = 0; j < n; j++) {
                String variable = listaVariables.get(j);
                boolean valor = ((i >> j) & 1) == 1;
                asignacion.put(variable, valor);
            }

            // 4c. Verificar si satisface TODAS las cláusulas
            if (esSatisfacible(clausulas, asignacion)) {
                System.out.println("¡SATISFACIBLE!");
                System.out.println("Asignación que satisface:");

                // Ordenar para mejor visualización
                List<String> claves = new ArrayList<>(asignacion.keySet());
                Collections.sort(claves);
                for (String clave : claves) {
                    System.out.println("  " + clave + " = " + asignacion.get(clave));
                }

                // Verificación detallada (opcional)
                System.out.println("\nVerificación por cláusula:");
                for (Clausula c : clausulas) {
                    boolean resultado = evaluarClausula(c, asignacion);
                    System.out.println("  " + c + " = " + resultado +
                            (c.esUnitaria() ? " (unitaria)" : ""));
                }

                return true;
            }
        }

        System.out.println("NO SATISFACIBLE");
        System.out.println("No existe asignación que satisfaga todas las cláusulas");
        System.out.println("=====================================================\n");
        return false;
    }

    // Optimización: procesar cláusulas unitarias primero
    private static Map<String, Boolean> extraerAsignacionesForzadas(Set<Clausula> clausulas) {
        Map<String, Boolean> forzadas = new HashMap<>();

        for (Clausula c : clausulas) {
            if (c.esUnitaria()) {
                String variable = extraerVariable(c.literal1);
                boolean valor;

                // Determinar valor forzado
                if (c.literal1.startsWith("-") || c.literal1.startsWith("¬")) {
                    valor = false;  // (¬A) fuerza A = false
                } else {
                    valor = true;   // (A) fuerza A = true
                }

                // Verificar contradicción
                if (forzadas.containsKey(variable)) {
                    boolean valorAnterior = forzadas.get(variable);
                    if (valorAnterior != valor) {
                        System.out.println("CONTRADICCIÓN: " + variable +
                                " debe ser " + valorAnterior + " y " + valor + " al mismo tiempo");
                        System.out.println("Por las cláusulas: (" +
                                (valorAnterior ? variable : "-" + variable) +
                                ") y (" + (valor ? variable : "-" + variable) + ")");
                        return null;  // Insatisfacible
                    }
                }

                forzadas.put(variable, valor);
                System.out.println("Cláusula unitaria " + c + " fuerza " +
                        variable + " = " + valor);
            }
        }

        return forzadas;
    }

    //metodo auxiliar para evaluar una cláusula con una asignación dada
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

    // metodo auxiliar para verificar si una asignación satisface todas las cláusulas
    private static boolean esSatisfacible(Set<Clausula> clausulas, Map<String, Boolean> asignacion) {
        for (Clausula c : clausulas) {
            if (!evaluarClausula(c, asignacion)) {
                return false; // Si una cláusula no se satisface, toda la fórmula falla
            }
        }
        return true;
    }// fin de metodos para resolucion 2-SAT==========================================================


    //4. Metodos de graficos de implicacioens
    // prepara y llama al vizualizar el grafo
    public static void graficarImplicaciones(Set<Clausula> clausulas) {
        System.out.println("\n=== CREANDO GRÁFICO DE IMPLICACIONES ===");

        try {
            // Mostrar información de depuración
            System.out.println("Cláusulas a graficar: " + clausulas);

            // Filtrar cláusulas binarias (las que generan implicaciones)
            Set<Clausula> clausulasBinarias = new HashSet<>();
            for (Clausula c : clausulas) {
                if (!c.esUnitaria()) {
                    clausulasBinarias.add(c);
                }
            }

            System.out.println("Cláusulas binarias: " + clausulasBinarias.size());

            if (clausulasBinarias.isEmpty()) {
                System.out.println("No hay cláusulas binarias para graficar");
                return;
            }

            // Mostrar grafo
            VisualizadorImplicaciones.mostrarGrafo(clausulasBinarias);

        } catch (Exception e) {
            System.out.println("Error al crear gráfico: " + e.getMessage());
            e.printStackTrace();
            mostrarGrafoTexto(clausulas);
        }
    }

    //metodo de respaldo para mostrar el grafo en texto
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


    //5. Metodos para clausulas transitivas
    //calcula y muestra clausulas transitivas por resolucion
    public static void calcularClausurasTransitivas(Set<Par> relacion) {
        System.out.println("\n=====================================================");
        System.out.println("=== CLAUSURAS TRANSITIVAS POR RESOLUCIÓN ===");

        Set<Clausula> clausulas = convertirRelacionAClausulas(relacion);

        // Obtener todos los literales únicos
        Set<String> todosLiterales = new HashSet<>();
        for (Clausula c : clausulas) {
            if (!c.esUnitaria()) {
                todosLiterales.add(c.literal1);
                todosLiterales.add(c.literal2);
                todosLiterales.add(obtenerComplemento(c.literal1));
                todosLiterales.add(obtenerComplemento(c.literal2));
            } else {
                todosLiterales.add(c.literal1);
                todosLiterales.add(obtenerComplemento(c.literal1));
            }
        }

        // Calcular clausura para cada literal
        List<String> listaLiterales = new ArrayList<>(todosLiterales);
        Collections.sort(listaLiterales);

        for (String literal : listaLiterales) {

            if (!literal.startsWith("-")) {
                // Solo calcular para literales positivos para evitar duplicados
                Set<String> clausuraPositivo = calcularClausuraLiteral(literal, clausulas);
                Set<String> clausuraNegativo = calcularClausuraLiteral("-" + literal, clausulas);

                System.out.println("T(" + literal + ") = " + clausuraPositivo);
                System.out.println("T(-" + literal + ") = " + clausuraNegativo);

                // Verificar inconsistencias
                verificarInconsistencias(clausuraPositivo, literal);
                verificarInconsistencias(clausuraNegativo, "-" + literal);
                System.out.println(); // Línea en blanco para separar
            }

        }
        System.out.println("=====================================================\n");
    }

    //calcula clausulas de un literal
    public static Set<String> calcularClausuraLiteral(String literalInicial, Set<Clausula> clausulas) {
        Set<String> clausura = new HashSet<>();
        clausura.add(literalInicial);

        boolean cambio;
        do {
            cambio = false;
            Set<String> nuevosLiterales = new HashSet<>();

            // Aplicar resolución unitaria con todos los literales actuales
            for (String literal : clausura) {
                for (Clausula c : clausulas) {
                    if (!c.esUnitaria()) {
                        // Si tenemos ¬A y la cláusula (A ∨ B), entonces podemos derivar B
                        String complemento = obtenerComplemento(literal);

                        if (c.literal1.equals(complemento) && !clausura.contains(c.literal2)) {
                            nuevosLiterales.add(c.literal2);
                            cambio = true;
                        }
                        if (c.literal2.equals(complemento) && !clausura.contains(c.literal1)) {
                            nuevosLiterales.add(c.literal1);
                            cambio = true;
                        }
                    }
                }
            }

            clausura.addAll(nuevosLiterales);

        } while (cambio);

        return clausura;
    }

    //detecta inconsistencias dentro de una clausula
    private static void verificarInconsistencias(Set<String> clausura, String literal) {

        for (String l : clausura) {
            String complemento = obtenerComplemento(l);
            if (clausura.contains(complemento)) {
                System.out.println("   X  INCONSISTENCIA en T(" + literal + "): contiene " + l + " y " + complemento);
            }
        }

    }// fin de metodos para las clusulas transitivas


    //6. Metodos auxuliares
    //metodo auxiliar para obtener complemento. Devuelve el complemento de un literal (¬A ↔ A)
    private static String obtenerComplemento(String literal) {
        if (literal.startsWith("-")) {
            return literal.substring(1);
        } else {
            return "-" + literal;
        }
    }

    //extrae el nombre de la variable sin negacion
    private static String extraerVariable(String literal) {
        // Manejar tanto "1" como "-1", "¬1", etc.
        if (literal.startsWith("-") || literal.startsWith("¬")) {
            return literal.substring(1);
        }
        return literal;
    }// fin de metodos auxiliares========================================

}