package puzzle;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class SearchTree {
    Node root;
    String initialState;
    String goalState;

    public SearchTree(String initialState, String goalState) {
        this.initialState = initialState;
        this.goalState = goalState;
        this.root = new Node(initialState, null);
    }
    public void breadthFirstSearch() {
        //Crear estructura de datos para almacenar los nodos visitados
        Set<String> visited = new HashSet<String>();
        //1. Buscar el nodo raíz y agregarlo a la cola
        Node currentNode = this.root;
        Queue<Node> queue = new LinkedList<>();
        queue.add(currentNode);

        //2. Mientras la cola no esté vacía
        while (!queue.isEmpty()) {
            //3. Sacar el primer nodo de la cola y verificar si es el nodo objetivo
            currentNode = queue.poll();
            visited.add(currentNode.getState());
            System.out.println("Current Node: " + NodeUtils.formatState(currentNode.getState()));
            if (currentNode.getState().equals(this.goalState)) {
                System.out.println("Goal state found: " + currentNode.getState());
                //Imprimir la ruta desde el nodo raíz hasta el nodo objetivo
                return;
            }
            //4. Si no es el nodo objetivo, generar los nodos hijos y agregarlos a la cola
            List<Node> children = NodeUtils.generateChildren(currentNode);
            for (Node child : children) {
                if (!visited.contains(child.getState())) {
                    visited.add(child.getState());
                    queue.add(child);
                }
            }
            queue.add(currentNode)
        }
    }
}
