package org.example;

import java.util.*;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;
import org.graphstream.ui.view.Viewer;
import javax.swing.*;
import java.util.Set;

public class ClausuraTransitivaReflexiva {

    public static void inputs(){

        Scanner in = new Scanner(System.in);

        //conjunto
        Set<Integer> conjunto = new HashSet<>();
        Set<Par> relacion = new HashSet<>();
        int opc = 0;

        do{

            try{
                System.out.println("\n====== MENU ======");
                System.out.println("Selecciona una opcion del menu.");
                System.out.println("1.- Ingresar un conjunto con su relacion.");
                System.out.println("2.- Salir.");
                System.out.print("Ingresa una opcion: ");
                opc = in.nextInt();
                in.nextLine();

            }catch (InputMismatchException e){
                System.out.print("FATAL ERROR: Debe ingresar un número entero de la opcion del menu. ");
                in.nextLine(); //limpiar el buffer
                continue; //si estás en un loop, volver al inicio

            }

            //ingresa los elementos del conjunto, los valida y construye la relacion que tambien se valida correctametne
            switch (opc){
                case 1:

                    //ingreso dinámico del conjunto
                    System.out.println("=== INGRESO DEL CONJUNTO ===");
                    System.out.println("Ingrese los elementos del conjunto (ingrese 'fin' para terminar).");

                    while (true) {
                        System.out.print("\nElemento (número entero): ");
                        String input = in.nextLine().trim();

                        if (input.equalsIgnoreCase("fin")) {
                            break;
                        }

                        try {
                            int elemento = Integer.parseInt(input);
                            if (conjunto.add(elemento)) {
                                System.out.print(" | Elemento " + elemento + " agregado.");
                            } else {
                                System.out.print(" | El elemento " + elemento + " ya existe en el conjunto.");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Por favor, ingrese un número entero válido o 'fin' para terminar.");
                        }
                    }

                    if (conjunto.isEmpty()) {
                        System.out.println("El conjunto no puede estar vacío. Saliendo...");
                        in.close();
                        return;
                    }

                    System.out.println("\nConjunto final: " + conjunto);

                    //ingreso dinámico de la relación
                    System.out.println("\n=== INGRESO DE LA RELACIÓN ===");
                    System.out.println("Ingrese los pares de la relación (formato: 'x,y' o 'x').");
                    System.out.println("Ingrese 'fin' para terminar.\n");

                    while (true) {
                        System.out.print("\nPar (formato 'x,y' para pares o 'x' para individual): ");
                        String input = in.nextLine().trim();

                        if (input.equalsIgnoreCase("fin")) {
                            break;
                        }

                        try {
                            //separamos por la coma
                            String[] partes = input.split(",");
                            Par nuevoPar = null;

                            //caso 1: par con una literal
                            if (partes.length == 1) {
                                //evitar entradas vacías si el usuario solo da Enter
                                if(partes[0].trim().isEmpty()) continue;

                                int x = Integer.parseInt(partes[0].trim());

                                //validar que 'x' exista en el conjunto universo
                                if (!conjunto.contains(x)) {
                                    System.out.println("Error: El elemento " + x + " no existe en el conjunto definido previamente.");
                                    continue;
                                }

                                //usamos el constructor nuevo de un solo argumento
                                nuevoPar = new Par(x);

                                //caso 2: par con dos literales
                            } else if (partes.length == 2) {
                                int x = Integer.parseInt(partes[0].trim());
                                int y = Integer.parseInt(partes[1].trim());

                                //validar que AMBOS existan en el conjunto universo
                                if (!conjunto.contains(x) || !conjunto.contains(y)) {
                                    System.out.println("Error: Ambos elementos deben estar en el conjunto definido.");
                                    continue;
                                }

                                //usamos el constructor original de dos argumentos
                                nuevoPar = new Par(x, y);

                            } else {
                                //si meten un formato que no sea x,y o x
                                System.out.println("Formato incorrecto. Use: 'x,y' o solo 'x'");
                                continue;
                            }

                            //agregar a la relacion (Común para ambos casos)
                            if (relacion.add(nuevoPar)) {
                                System.out.print(" | " + nuevoPar + " agregado.");
                            } else {
                                System.out.print(" | " + nuevoPar + " ya existe en la relación.");
                            }

                        } catch (NumberFormatException e) {
                            System.out.println("Por favor, ingrese números válidos.");
                        } catch (Exception e) {
                            System.out.println("Error en la entrada: " + e.getMessage());
                        }
                    }

                    //mostrar resultados
                    System.out.println("\n=======================================================");
                    System.out.println("=== RESULTADOS ===");
                    System.out.println("Conjunto: " + conjunto);
                    System.out.println("Relación: " + relacion);
                    System.out.println("=======================================================\n");

                    if (relacion.isEmpty()) {
                        System.out.println("\nLA RELACION ESTA VACIA:");
                    }

                    //1. Conversion de clausulas
                    ConversorClausulas.procesarYMostrarClausulas(relacion);

                    //calcularClausura(relacion);

                    // calcular clausuras transitivas (NUEVO)
                    ConversorClausulas.calcularClausurasTransitivas(relacion);

                    // RESOLVER 2-SAT (NUEVO)
                    boolean esSatisfacible = ConversorClausulas.resolver2SAT(relacion);

                    // mostrar resultado final
                    System.out.println("\n" + "=".repeat(50));
                    if (esSatisfacible) {
                        System.out.println("RESULTADO 2-SAT:  SATISFACIBLE");
                    } else {
                        System.out.println("RESULTADO 2-SAT:  NO SATISFACIBLE");
                    }
                    System.out.println("=".repeat(50));


                    conjunto.clear();
                    relacion.clear();

                    break;

                case 2:
                    System.out.println("Nos vemos pronto...");
                    System.out.println("Programa terminado.");
                    System.exit(0);
                    break;

                default:
                    System.out.println("Seleccione una opcion del menu...");
            }

        }while(opc != 2);

        in.close();
    }

    public static Set<Par> calcularClausura(Set<Par> relacion) {

        if (relacion.isEmpty()) {
            return new HashSet<>();
        }

        //recolectar elementos
        Set<Integer> elementos = new HashSet<>();
        for (Par p : relacion) {
            elementos.add(p.x);
            if (p.y != null) {
                elementos.add(p.y);
            }
        }

        List<Integer> listaElementos = new ArrayList<>(elementos);
        Collections.sort(listaElementos);
        Map<Integer, Integer> elementoAIndice = new HashMap<>();
        for (int i = 0; i < listaElementos.size(); i++) {
            elementoAIndice.put(listaElementos.get(i), i);
        }

        int n = listaElementos.size();
        boolean[][] A = new boolean[n][n];

        // construir matriz inicial incluyendo reflexividad
        for (Par p : relacion) {
            if (p.y != null) {
                //par normal (x,y)
                int i = elementoAIndice.get(p.x);
                int j = elementoAIndice.get(p.y);
                A[i][j] = true;
            } else {
                //par reflexivo individual (x) - representa (x,x)
                int i = elementoAIndice.get(p.x);
                A[i][i] = true;  // ← ¡IMPORTANTE! No olvidar esto
            }
        }

        //mostrar matriz inicial
        System.out.println("\n=====================================================");
        System.out.println("=== MATRIZ INICIAL ===");
        mostrarMatriz(A, listaElementos);

        //Warshall mejorado con mensajes de progreso
        boolean[][] clausuraMatriz = calcularClausuraWarshallMejorado(A, listaElementos);

        //mostrar matriz resultante
        System.out.println("\n=== MATRIZ CLAUSURA ===");
        mostrarMatriz(clausuraMatriz, listaElementos);

        //convertir a conjunto de pares - CORRECTO
        Set<Par> clausura = new HashSet<>();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (clausuraMatriz[i][j]) {
                    clausura.add(new Par(listaElementos.get(i), listaElementos.get(j)));
                }
            }
        }

        System.out.println("=====================================================\n");
        return clausura;
    }

    private static boolean[][] calcularClausuraWarshallMejorado(boolean[][] A, List<Integer> elementos) {

        int n = A.length;
        boolean[][] R = new boolean[n][n];

        //paso 1: Inicializar matriz R (copia de A + diagonal reflexiva)
        for (int i = 0; i < n; i++) {
            System.arraycopy(A[i], 0, R[i], 0, n);
            R[i][i] = true; // Reflexividad garantizada
        }

        System.out.println("\n--- PROCESO WARSHALL ---");

        //paso 2: Algoritmo de Warshall
        for (int k = 0; k < n; k++) {
            System.out.println("Iteración k = " + k + " (elemento: " + elementos.get(k) + ")");
            int cambios = 0;

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (!R[i][j] && R[i][k] && R[k][j]) {
                        R[i][j] = true;
                        cambios++;
                        System.out.println("  + R[" + i + "][" + j + "] = true (" +
                                elementos.get(i) + " → " + elementos.get(k) + " → " + elementos.get(j) + ")");
                    }
                }
            }

            System.out.println("  Cambios en iteración " + k + ": " + cambios);

            //mostrar matriz intermedia (opcional, para n pequeños)
            if (n <= 6) {
                System.out.println("  Matriz intermedia:");
                for (int i = 0; i < n; i++) {
                    System.out.print("    ");
                    for (int j = 0; j < n; j++) {
                        System.out.print(R[i][j] ? "1 " : "0 ");
                    }
                    System.out.println();
                }
            }
        }
        System.out.println("\n");
        return R;
    }

    private static void mostrarMatriz(boolean[][] matriz, List<Integer> elementos) {

        System.out.print("    ");
        for (int elem : elementos) {
            System.out.printf("%4d", elem);
        }
        System.out.println();

        for (int i = 0; i < matriz.length; i++) {
            System.out.printf("%4d", elementos.get(i));
            for (int j = 0; j < matriz[i].length; j++) {
                System.out.print(matriz[i][j] ? "   1" : "   0");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {

        inputs();

    }

}