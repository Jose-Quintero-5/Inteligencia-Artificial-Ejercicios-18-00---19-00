package puzzle;

import java.util.ArrayList;
import java.util.List;

public class NodeUtils {

    private static String swapPositions(String state, int pos1, int pos2) {
        char[] arr = state.toCharArray();
        char temp = arr[pos1];
        arr[pos1] = arr[pos2];
        arr[pos2] = temp;
        return new String(arr);
    }

    public static List<Node> generateChildren(Node parentNode) {
        List<Node> successors = new ArrayList<>();
        int zeroPos = parentNode.getState().indexOf(' ');

        int[][] adjacentPositions = {
            {1, 3},
            {0, 2, 4},
            {1, 5},
            {0, 4, 6},
            {1, 3, 5, 7},
            {2, 4, 8},
            {3, 7},
            {4, 6, 8},
            {5, 7}
        };

        for (int adjPos : adjacentPositions[zeroPos]) {
            String newState = swapPositions(parentNode.getState(), zeroPos, adjPos);
            successors.add(new Node(newState, parentNode));
        }

        return successors;
    }

    public static String formatState(String state) {
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < state.length(); i++) {
            char value = state.charAt(i);
            formatted.append(value == ' ' ? "_" : value).append(' ');
            if ((i + 1) % 3 == 0) {
                formatted.append('\n');
            }
        }
        return formatted.toString();
    }
}
