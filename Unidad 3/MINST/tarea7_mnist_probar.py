# Alumno: Jose de Jesus Quintero Noris
# No. de control: 23170288
#
# Tarea 7. Aplicación para probar en tiempo real el modelo entrenado con MNIST.
# Se dibuja un dígito con el mouse y el modelo (cargado desde el archivo .keras)
# dice cuál cree que es y con qué probabilidad.
#
# Controles:
#   Clic izquierdo y arrastrar -> dibujar
#   + / -                      -> trazo más grueso / más delgado
#   C                          -> borrar el dibujo
#   Q o ESC                    -> salir
#
# Uso:  python tarea7_mnist_probar.py [ruta_del_modelo.keras]

import os
import sys

os.environ.setdefault("TF_CPP_MIN_LOG_LEVEL", "2")  # menos mensajes de TensorFlow

import cv2
import numpy as np
import keras

# ---------------- Configuración ----------------
NOMBRE_MODELO = "modelo_digitos.keras"
LIENZO = 280            # el lienzo mide 280x280 (10 veces una imagen de MNIST)
GROSOR_INICIAL = 18     # grosor del trazo, en píxeles
GROSOR_MIN, GROSOR_MAX = 8, 36
ANCHO_PANEL = 300       # ancho del panel de resultados
ALTO_BARRA = 36         # alto de la barra de instrucciones
VENTANA = "Tarea 7 - Reconocedor de digitos"
# -----------------------------------------------


def buscar_modelo():
    """Busca el archivo del modelo: argumento, carpeta del script o carpeta actual."""
    candidatos = []
    if len(sys.argv) > 1:
        candidatos.append(sys.argv[1])
    candidatos.append(os.path.join(os.path.dirname(os.path.abspath(__file__)), NOMBRE_MODELO))
    candidatos.append(os.path.join(os.getcwd(), NOMBRE_MODELO))
    for ruta in candidatos:
        if os.path.exists(ruta):
            return ruta
    return None


def imagen_para_modelo(lienzo):
    """Convierte el dibujo (280x280) en una imagen de 28x28 parecida a las de MNIST.

    Las imágenes de MNIST tienen el dígito dentro de una caja de 20x20 píxeles y
    centrado (por su centro de masa) en una imagen de 28x28. Si se le da al modelo
    un dibujo sin esa preparación, acierta mucho menos.
    Devuelve None si no hay nada dibujado.
    """
    puntos = cv2.findNonZero(lienzo)
    if puntos is None:
        return None

    # 1. Recorta la caja que rodea al trazo
    x, y, ancho, alto = cv2.boundingRect(puntos)
    trazo = lienzo[y:y + alto, x:x + ancho]

    # 2. Lo reduce para que el lado más largo mida 20 px (sin deformarlo)
    factor = 20.0 / max(ancho, alto)
    nuevo_ancho = max(1, int(round(ancho * factor)))
    nuevo_alto = max(1, int(round(alto * factor)))
    trazo = cv2.resize(trazo, (nuevo_ancho, nuevo_alto), interpolation=cv2.INTER_AREA)

    # 3. Lo pone en el centro de un cuadro negro de 28x28
    imagen = np.zeros((28, 28), dtype=np.uint8)
    x0 = (28 - nuevo_ancho) // 2
    y0 = (28 - nuevo_alto) // 2
    imagen[y0:y0 + nuevo_alto, x0:x0 + nuevo_ancho] = trazo

    # 4. Lo mueve para que su centro de masa quede en el centro de la imagen
    m = cv2.moments(imagen)
    if m["m00"] > 0:
        cx = m["m10"] / m["m00"]
        cy = m["m01"] / m["m00"]
        mover = np.float32([[1, 0, 13.5 - cx], [0, 1, 13.5 - cy]])
        imagen = cv2.warpAffine(imagen, mover, (28, 28))
    return imagen


def predecir(modelo, imagen):
    """Devuelve las 10 probabilidades (una por dígito)."""
    # El modelo ya normaliza dentro (capa Rescaling), así que se le dan valores 0-255
    entrada = imagen.reshape(1, 28, 28).astype("float32")
    return modelo(entrada, training=False).numpy()[0]


def dibujar_interfaz(lienzo, imagen, probabilidades, grosor):
    """Arma la ventana: lienzo a la izquierda, resultados a la derecha, instrucciones abajo."""
    vista = np.zeros((LIENZO + ALTO_BARRA, LIENZO + ANCHO_PANEL, 3), dtype=np.uint8)
    fuente = cv2.FONT_HERSHEY_SIMPLEX

    # Lienzo con borde
    vista[:LIENZO, :LIENZO] = cv2.cvtColor(lienzo, cv2.COLOR_GRAY2BGR)
    cv2.rectangle(vista, (0, 0), (LIENZO - 1, LIENZO - 1), (90, 90, 90), 1)

    # Panel de resultados
    x0 = LIENZO + 16
    cv2.putText(vista, "Lo que ve el modelo (28x28):", (x0, 22), fuente, 0.45, (180, 180, 180), 1)

    if probabilidades is None:
        cv2.putText(vista, "Dibuja un digito", (x0, 110), fuente, 0.8, (150, 150, 150), 2)
    else:
        # Vista previa de la imagen de 28x28 que recibe el modelo
        previa = cv2.resize(imagen, (84, 84), interpolation=cv2.INTER_NEAREST)
        vista[34:34 + 84, x0:x0 + 84] = cv2.cvtColor(previa, cv2.COLOR_GRAY2BGR)

        # Dígito predicho y su probabilidad
        digito = int(np.argmax(probabilidades))
        cv2.putText(vista, str(digito), (x0 + 110, 100), fuente, 3.0, (0, 255, 120), 5)
        cv2.putText(vista, f"{probabilidades[digito]:.1%} seguro", (x0 + 110, 125), fuente, 0.55, (0, 220, 255), 1)

        # Los 3 dígitos más probables, con barras
        cv2.putText(vista, "Los 3 mas probables:", (x0, 155), fuente, 0.5, (200, 200, 200), 1)
        mejores = np.argsort(probabilidades)[::-1][:3]
        for fila, d in enumerate(mejores):
            y = 170 + fila * 32
            color = (0, 255, 120) if fila == 0 else (160, 160, 160)
            largo = max(2, int(probabilidades[d] * 190))
            cv2.putText(vista, str(int(d)), (x0, y + 17), fuente, 0.7, color, 2)
            cv2.rectangle(vista, (x0 + 24, y + 3), (x0 + 24 + largo, y + 20), color, -1)
            cv2.putText(vista, f"{probabilidades[d]:.1%}", (x0 + 24 + 195, y + 17), fuente, 0.5, color, 1)

    # Barra inferior
    texto = f"Mouse: dibujar | +/-: trazo ({grosor}px) | C: borrar | Q/ESC: salir"
    cv2.putText(vista, texto, (8, LIENZO + 23), fuente, 0.48, (200, 200, 200), 1)
    return vista


def main():
    ruta = buscar_modelo()
    if ruta is None:
        print(f"No se encontró el modelo '{NOMBRE_MODELO}'.")
        print("Entrena primero con el cuaderno y deja el archivo junto a este programa,")
        print("o escribe su ruta:  python tarea7_mnist_probar.py ruta/al/modelo.keras")
        return

    modelo = keras.models.load_model(ruta)
    print(f"Modelo cargado desde: {ruta}")
    print("Dibuja un dígito en la ventana.")

    lienzo = np.zeros((LIENZO, LIENZO), dtype=np.uint8)
    estado = {"dibujando": False, "ultimo": None, "cambio": False, "grosor": GROSOR_INICIAL}

    def mouse(evento, x, y, flags, parametros):
        if evento == cv2.EVENT_LBUTTONDOWN:
            estado["dibujando"] = True
            estado["ultimo"] = (x, y)
            cv2.circle(lienzo, (x, y), estado["grosor"] // 2, 255, -1)
            estado["cambio"] = True
        elif evento == cv2.EVENT_MOUSEMOVE and estado["dibujando"]:
            cv2.line(lienzo, estado["ultimo"], (x, y), 255, estado["grosor"])
            estado["ultimo"] = (x, y)
            estado["cambio"] = True
        elif evento == cv2.EVENT_LBUTTONUP:
            estado["dibujando"] = False

    cv2.namedWindow(VENTANA)
    cv2.setMouseCallback(VENTANA, mouse)

    imagen = None
    probabilidades = None
    while True:
        # Solo predice cuando el dibujo cambió
        if estado["cambio"]:
            imagen = imagen_para_modelo(lienzo)
            probabilidades = predecir(modelo, imagen) if imagen is not None else None
            estado["cambio"] = False

        cv2.imshow(VENTANA, dibujar_interfaz(lienzo, imagen, probabilidades, estado["grosor"]))
        tecla = cv2.waitKey(15) & 0xFF

        if tecla in (ord("c"), ord("C")):
            lienzo[:] = 0
            imagen, probabilidades = None, None
        elif tecla in (ord("+"), ord("=")):
            estado["grosor"] = min(GROSOR_MAX, estado["grosor"] + 4)
        elif tecla in (ord("-"), ord("_")):
            estado["grosor"] = max(GROSOR_MIN, estado["grosor"] - 4)
        elif tecla in (ord("q"), ord("Q"), 27):
            break
        if cv2.getWindowProperty(VENTANA, cv2.WND_PROP_VISIBLE) < 1:
            break  # se cerró la ventana con la X

    cv2.destroyAllWindows()


if __name__ == "__main__":
    main()
