package principal;

/*
    Árbol binario de búsqueda
    Alumno: Jose de Jesus Quintero Noris
    No. de control: 23170288
*/

public class App {
    public static void main(String[] args) {
        Arbol arbol = new Arbol();

        System.out.println("=== Árbol recién creado ===");
        System.out.println("¿Está vacío? " + arbol.vacio());
        System.out.println("Mínimo: " + arbol.minimo() + " | Máximo: " + arbol.maximo());

        // Inserta los nombres (el primero queda como raíz).
        String[] nombres = { "Jose", "Maria", "Carlos", "Ana", "Pedro", "Luis", "Sofia", "Beto" };
        for (String nombre : nombres) {
            arbol.insertar(nombre);
        }

        System.out.println();
        System.out.println("=== Después de insertar " + nombres.length + " nombres ===");
        System.out.println("¿Está vacío? " + arbol.vacio());
        System.out.println("Nodo raíz: " + arbol.getRaiz().getNombre());
        System.out.println("Cantidad de nodos: " + arbol.contar());
        System.out.println("Altura: " + arbol.altura());
        System.out.println("Menor: " + arbol.minimo() + " | Mayor: " + arbol.maximo());
        System.out.println();
        System.out.print(arbol.dibujar());

        // Los tres recorridos.
        System.out.println();
        System.out.println("=== Recorridos ===");
        System.out.println("En orden : " + arbol.enOrden());
        System.out.println("Pre orden: " + arbol.preOrden());
        System.out.println("Post orden: " + arbol.postOrden());

        // Repetidos: no se agregan.
        System.out.println();
        System.out.println("=== Insertar un nombre repetido ===");
        System.out.println("Insertar Ana otra vez -> " + (arbol.insertar("Ana") ? "agregado" : "ya existía, no se agregó"));
        System.out.println("Cantidad de nodos: " + arbol.contar());

        // Búsquedas.
        System.out.println();
        System.out.println("=== Búsquedas ===");
        buscar(arbol, "Carlos");
        buscar(arbol, "Beto");
        buscar(arbol, "Sofia");
        buscar(arbol, "Zoe");

        // Eliminar: hoja, un hijo y dos hijos.
        System.out.println();
        System.out.println("=== Eliminar ===");
        eliminar(arbol, "Beto");    // hoja (sin hijos)
        eliminar(arbol, "Carlos");  // un hijo (solo le queda Ana)
        eliminar(arbol, "Maria");   // dos hijos (Luis y Pedro)
        eliminar(arbol, "Zoe");     // no existe
        System.out.println();
        System.out.print(arbol.dibujar());
        System.out.println("En orden: " + arbol.enOrden());
        System.out.println("Cantidad de nodos: " + arbol.contar() + " | Altura: " + arbol.altura());
    }

    private static void buscar(Arbol arbol, String nombre) {
        Nodo encontrado = arbol.buscarNodo(nombre);
        if (encontrado == null) {
            System.out.println("  " + nombre + " no está en el árbol");
        } else {
            System.out.println("  " + nombre + " encontrado | hijo izquierdo: "
                    + textoHijo(encontrado.getIzquierdo()) + " | hijo derecho: "
                    + textoHijo(encontrado.getDerecho()));
        }
    }

    private static void eliminar(Arbol arbol, String nombre) {
        boolean eliminado = arbol.eliminar(nombre);
        System.out.println("  Eliminar " + nombre + " -> " + (eliminado ? "eliminado" : "no existe"));
    }

    // Muestra el nombre del hijo o un guion si no tiene.
    private static String textoHijo(Nodo hijo) {
        return hijo == null ? "-" : hijo.getNombre();
    }
}
