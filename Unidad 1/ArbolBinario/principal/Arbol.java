package principal;

/*
 * Árbol binario de búsqueda
 * Alumno: Jose de Jesus Quintero Noris
 * No. de control: 23170288
 */

import java.util.ArrayList;
import java.util.List;

public class Arbol {

    private Nodo raiz; // Raíz del árbol.

    public Arbol() {
        this.raiz = null;
    }

    public Nodo getRaiz() {
        return raiz;
    }

    public boolean vacio() {
        return raiz == null;
    }

    // ---------------------------------------------------------------
    // Insertar: los nombres menores van a la izquierda y los mayores a la derecha.
    // Devuelve false si el nombre ya estaba (no se permiten repetidos).
    // ---------------------------------------------------------------
    public boolean insertar(String nombre) {
        if (buscarNodo(nombre) != null) {
            return false;
        }
        raiz = insertar(raiz, nombre);
        return true;
    }

    private Nodo insertar(Nodo actual, String nombre) {
        if (actual == null) {
            return new Nodo(nombre); // Lugar vacío: aquí va el nuevo nodo.
        }
        if (nombre.compareToIgnoreCase(actual.getNombre()) < 0) {
            actual.setIzquierdo(insertar(actual.getIzquierdo(), nombre));
        } else {
            actual.setDerecho(insertar(actual.getDerecho(), nombre));
        }
        return actual;
    }

    // ---------------------------------------------------------------
    // Buscar: baja por el árbol comparando, sin recorrerlo completo.
    // Devuelve el nodo o null si no existe.
    // ---------------------------------------------------------------
    public Nodo buscarNodo(String nombre) {
        Nodo actual = raiz;
        while (actual != null) {
            int comparacion = nombre.compareToIgnoreCase(actual.getNombre());
            if (comparacion == 0) {
                return actual;
            }
            actual = (comparacion < 0) ? actual.getIzquierdo() : actual.getDerecho();
        }
        return null;
    }

    // ---------------------------------------------------------------
    // Eliminar: hay tres casos (hoja, un hijo, dos hijos).
    // Devuelve false si el nombre no existe.
    // ---------------------------------------------------------------
    public boolean eliminar(String nombre) {
        if (buscarNodo(nombre) == null) {
            return false;
        }
        raiz = eliminar(raiz, nombre);
        return true;
    }

    private Nodo eliminar(Nodo actual, String nombre) {
        if (actual == null) {
            return null;
        }
        int comparacion = nombre.compareToIgnoreCase(actual.getNombre());
        if (comparacion < 0) {
            actual.setIzquierdo(eliminar(actual.getIzquierdo(), nombre));
        } else if (comparacion > 0) {
            actual.setDerecho(eliminar(actual.getDerecho(), nombre));
        } else {
            // Caso 1 y 2: sin hijos o con un solo hijo, lo reemplaza ese hijo.
            if (actual.getIzquierdo() == null) {
                return actual.getDerecho();
            }
            if (actual.getDerecho() == null) {
                return actual.getIzquierdo();
            }
            // Caso 3: dos hijos. Toma el menor del subárbol derecho (el sucesor),
            // copia su nombre aquí y borra el sucesor de su lugar original.
            Nodo sucesor = minimo(actual.getDerecho());
            actual.setNombre(sucesor.getNombre());
            actual.setDerecho(eliminar(actual.getDerecho(), sucesor.getNombre()));
        }
        return actual;
    }

    // ---------------------------------------------------------------
    // Menor y mayor: siempre están en el extremo izquierdo y derecho.
    // ---------------------------------------------------------------
    public String minimo() {
        return raiz == null ? null : minimo(raiz).getNombre();
    }

    private Nodo minimo(Nodo actual) {
        while (actual.getIzquierdo() != null) {
            actual = actual.getIzquierdo();
        }
        return actual;
    }

    public String maximo() {
        if (raiz == null) {
            return null;
        }
        Nodo actual = raiz;
        while (actual.getDerecho() != null) {
            actual = actual.getDerecho();
        }
        return actual.getNombre();
    }

    // ---------------------------------------------------------------
    // Medidas del árbol.
    // ---------------------------------------------------------------
    public int contar() {
        return contar(raiz);
    }

    private int contar(Nodo actual) {
        if (actual == null) {
            return 0;
        }
        return 1 + contar(actual.getIzquierdo()) + contar(actual.getDerecho());
    }

    // Altura = cantidad de niveles (árbol vacío = 0, solo la raíz = 1).
    public int altura() {
        return altura(raiz);
    }

    private int altura(Nodo actual) {
        if (actual == null) {
            return 0;
        }
        return 1 + Math.max(altura(actual.getIzquierdo()), altura(actual.getDerecho()));
    }

    // ---------------------------------------------------------------
    // Recorridos. Cambia en qué momento se visita el nodo:
    //   enOrden:   izquierda, nodo, derecha  (sale ordenado alfabéticamente)
    //   preOrden:  nodo, izquierda, derecha  (sirve para copiar el árbol)
    //   postOrden: izquierda, derecha, nodo  (los hijos antes que el padre)
    // ---------------------------------------------------------------
    public List<String> enOrden() {
        List<String> lista = new ArrayList<>();
        enOrden(raiz, lista);
        return lista;
    }

    private void enOrden(Nodo actual, List<String> lista) {
        if (actual == null) {
            return;
        }
        enOrden(actual.getIzquierdo(), lista);
        lista.add(actual.getNombre());
        enOrden(actual.getDerecho(), lista);
    }

    public List<String> preOrden() {
        List<String> lista = new ArrayList<>();
        preOrden(raiz, lista);
        return lista;
    }

    private void preOrden(Nodo actual, List<String> lista) {
        if (actual == null) {
            return;
        }
        lista.add(actual.getNombre());
        preOrden(actual.getIzquierdo(), lista);
        preOrden(actual.getDerecho(), lista);
    }

    public List<String> postOrden() {
        List<String> lista = new ArrayList<>();
        postOrden(raiz, lista);
        return lista;
    }

    private void postOrden(Nodo actual, List<String> lista) {
        if (actual == null) {
            return;
        }
        postOrden(actual.getIzquierdo(), lista);
        postOrden(actual.getDerecho(), lista);
        lista.add(actual.getNombre());
    }

    // ---------------------------------------------------------------
    // Dibuja el árbol de lado: la raíz a la izquierda, los mayores arriba
    // y los menores abajo.
    // ---------------------------------------------------------------
    public String dibujar() {
        if (raiz == null) {
            return "(árbol vacío)\n";
        }
        StringBuilder texto = new StringBuilder();
        dibujar(raiz, 0, texto);
        return texto.toString();
    }

    private void dibujar(Nodo actual, int nivel, StringBuilder texto) {
        if (actual == null) {
            return;
        }
        dibujar(actual.getDerecho(), nivel + 1, texto);
        texto.append("    ".repeat(nivel)).append(actual.getNombre()).append("\n");
        dibujar(actual.getIzquierdo(), nivel + 1, texto);
    }
}
