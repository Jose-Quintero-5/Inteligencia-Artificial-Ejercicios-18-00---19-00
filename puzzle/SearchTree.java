package puzzle;
import java.util.LinkedList;
import java.util.Queue;

public class SearchTree {
    Node root;
    String initialState;
    String goalState;

    public SearchTree(String initialState, String goalState) {
        this.initialState = initialState;
        this.goalState = goalState;
        this.root = new Node(initialState, null);
    }
    public static void breadthFirstSearch() {
        //1. Buscar el nodo raíz y agregarlo a la cola
        Node currentNode = root;
        Queue<Node> queue = new LinkedList<>();
        queue.add(currentNode);
        
        //2. Mientras la cola no esté vacía
        while (!queue.isEmpty()) {
            //3. Sacar el primer nodo de la cola y verificar si es el nodo objetivo
            currentNode = queue.poll();
            if (currentNode.getState().equals(goalState)) {
                System.out.println("Goal state found: " + currentNode.getState());
                return;
            }
            //4. Si no es el nodo objetivo, generar los nodos hijos y agregarlos a la cola
            
        }

    }

}
