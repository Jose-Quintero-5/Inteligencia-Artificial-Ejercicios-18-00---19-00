package principal;

/*
 * Árbol binario de búsqueda
 * Alumno: Jose de Jesus Quintero Noris
 * No. de control: 23170288
 */

public class Nodo {
    private String nombre;   // Dato guardado en el nodo.
    private Nodo izquierdo;  // Hijo izquierdo (nombres menores).
    private Nodo derecho;    // Hijo derecho (nombres mayores).

    public Nodo(String nombre) {
        this.nombre = nombre;
        this.izquierdo = null;
        this.derecho = null;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Nodo getIzquierdo() {
        return izquierdo;
    }

    public void setIzquierdo(Nodo izquierdo) {
        this.izquierdo = izquierdo;
    }

    public Nodo getDerecho() {
        return derecho;
    }

    public void setDerecho(Nodo derecho) {
        this.derecho = derecho;
    }

    // Un nodo es hoja si no tiene hijos.
    public boolean esHoja() {
        return izquierdo == null && derecho == null;
    }
}
