package puzzle;

import java.util.List;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // El espacio representa el cuadro vacio.
        String initialState = "7621 3458";
        String goalState = "12345678 ";

        SearchTree searchTree = new SearchTree(initialState, goalState);

        System.out.println("===== 8 PUZZLE =====");
        System.out.println("\nEstado inicial:");
        System.out.println(NodeUtils.formatState(initialState));
        System.out.println("Estado objetivo:");
        System.out.println(NodeUtils.formatState(goalState));

        System.out.println("Seleccione el algoritmo:");
        System.out.println("1. Primero en anchura (BFS)");
        System.out.println("2. Costo uniforme");
        System.out.println("3. Primero en profundidad (DFS)");
        System.out.println("4. Profundidad limitada");
        System.out.println("5. Profundidad iterativa");
        System.out.println("6. Bidireccional");
        System.out.print("Opcion: ");

        int option = scanner.nextInt();
        SearchResult result;

        switch (option) {
            case 1:
                result = searchTree.breadthFirstSearch();
                break;
            case 2:
                result = searchTree.uniformCostSearch();
                break;
            case 3:
                result = searchTree.depthFirstSearch();
                break;
            case 4:
                System.out.print("Limite de profundidad: ");
                int limit = scanner.nextInt();
                result = searchTree.depthLimitedSearch(limit);
                break;
            case 5:
                result = searchTree.iterativeDeepeningSearch();
                break;
            case 6:
                result = searchTree.bidirectionalSearch();
                break;
            default:
                System.out.println("Opcion no valida.");
                scanner.close();
                return;
        }

        if (result.isFound()) {
            System.out.println("Profundidad de la solucion: " + result.getGoalNode().getDepth());
            System.out.println("\nCamino de solucion:");
            printPath(result.getGoalNode());
        }

        scanner.close();

        System.out.println("\n===== RESULTADO =====");
        System.out.println("Solucion encontrada: " + (result.isFound() ? "Si" : "No"));
        System.out.println("Nodos expandidos: " + result.getNodesExpanded());
        System.out.println("Nodos generados: " + result.getNodesGenerated());
        System.out.printf("Tiempo: %.6f segundos%n", result.getTimeSeconds());
    }

    private static void printPath(Node goalNode) {
        List<Node> path = new java.util.ArrayList<>();
        Node current = goalNode;
        while (current != null) {
            path.add(current);
            current = current.getParent();
        }
        java.util.Collections.reverse(path);

        for (int i = 0; i < path.size(); i++) {
            System.out.println("Paso " + i + ":");
            System.out.println(NodeUtils.formatState(path.get(i).getState()));
        }
    }
}
