# - Miriam Alejandra Bazaldua Machado
# - Daniel Jimenez Duarte
# - José de Jesús Quintero Noris
# - Rodrigo Villegas Valdez
#
# Programa que abre la camara, detecta los rostros y usa la CNN entrenada
# para decir de quien es cada uno, en tiempo real.
#
# Archivos necesarios en esta misma carpeta (los genera el cuaderno de Colab):
#   modelo_cnn_rostros.keras
#   modelo_info.json
#
# Controles:
#   Q o ESC -> salir

import os

os.environ.setdefault("TF_CPP_MIN_LOG_LEVEL", "2")  # menos mensajes de TensorFlow

import json
import unicodedata
from collections import deque

import cv2
import numpy as np
import keras

# ---------------- Configuracion ----------------
CARPETA = os.path.dirname(os.path.abspath(__file__))
RUTA_MODELO = os.path.join(CARPETA, "modelo_cnn_rostros.keras")
RUTA_INFO = os.path.join(CARPETA, "modelo_info.json")
INDICE_CAMARA = 0          # 0 = camara principal; probar con 1 si no abre
UMBRAL_CONFIANZA = 0.70    # por debajo de esta confianza se muestra "Desconocido"
MARGEN = 0.0               # igual que en capturaRostros.py (recorte exacto a la cara)
CUADROS_PROMEDIO = 8       # cuantos cuadros se promedian para que el nombre no parpadee
# -----------------------------------------------

NOMBRE_VENTANA = "Reconocimiento de rostros"


def quitar_acentos(texto):
    # OpenCV no puede escribir acentos en pantalla, asi que se quitan
    normal = unicodedata.normalize("NFKD", texto)
    return "".join(c for c in normal if not unicodedata.combining(c))


def cargar_modelo():
    # Carga la CNN y la informacion que se guardo al entrenar (nombres y tamano de imagen)
    modelo = keras.models.load_model(RUTA_MODELO)
    with open(RUTA_INFO, encoding="utf-8") as archivo:
        info = json.load(archivo)
    return modelo, info["clases"], info["tamano"], "normalizar_iluminacion" in info


def normalizar_iluminacion(imagen_bgr):
    # Igual que en el cuaderno: CLAHE sobre el canal de luminosidad para igualar el contraste
    lab = cv2.cvtColor(imagen_bgr, cv2.COLOR_BGR2LAB)
    l, a, b = cv2.split(lab)
    clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8))
    return cv2.cvtColor(cv2.merge((clahe.apply(l), a, b)), cv2.COLOR_LAB2BGR)


class Seguimiento:
    # Sigue cada cara entre cuadros y promedia sus ultimas probabilidades.
    # Asi un solo cuadro con mala luz no cambia el nombre de golpe.
    def __init__(self, cuadros=CUADROS_PROMEDIO, maximo_perdido=10):
        self.cuadros = cuadros
        self.maximo_perdido = maximo_perdido
        self.pistas = []  # cada una: {"centro", "tam", "historial", "perdido"}

    def actualizar(self, cajas, probabilidades):
        for pista in self.pistas:
            pista["perdido"] += 1
        promedios = []
        for (x, y, ancho, alto), probs in zip(cajas, probabilidades):
            centro = (x + ancho / 2, y + alto / 2)
            tam = max(ancho, alto)
            # Busca la pista mas cercana que aun no se uso en este cuadro
            mejor, mejor_dist = None, 0.6 * tam
            for pista in self.pistas:
                if pista["perdido"] == 0:
                    continue
                dist = np.hypot(centro[0] - pista["centro"][0], centro[1] - pista["centro"][1])
                if dist < mejor_dist:
                    mejor, mejor_dist = pista, dist
            if mejor is None:
                mejor = {"historial": deque(maxlen=self.cuadros)}
                self.pistas.append(mejor)
            mejor["centro"], mejor["tam"], mejor["perdido"] = centro, tam, 0
            mejor["historial"].append(probs)
            promedios.append(np.mean(mejor["historial"], axis=0))
        self.pistas = [p for p in self.pistas if p["perdido"] <= self.maximo_perdido]
        return promedios


def preparar_rostro(cuadro, caja, tamano, normalizar=False):
    # Recorta la cara del cuadro y la deja como la espera la red (tamano x tamano, RGB)
    x, y, ancho, alto = [int(v) for v in caja]
    mx, my = int(ancho * MARGEN), int(alto * MARGEN)
    x1, y1 = max(x - mx, 0), max(y - my, 0)
    x2, y2 = min(x + ancho + mx, cuadro.shape[1]), min(y + alto + my, cuadro.shape[0])

    rostro = cuadro[y1:y2, x1:x2]
    if normalizar:
        rostro = normalizar_iluminacion(rostro)  # solo si el modelo se entreno con CLAHE
    rostro = cv2.cvtColor(rostro, cv2.COLOR_BGR2RGB)  # OpenCV usa BGR y la red se entreno con RGB
    rostro = cv2.resize(rostro, (tamano, tamano))
    return rostro.astype("float32")  # la red ya normaliza por dentro


def procesar_cuadro(cuadro, detector, modelo, clases, tamano, seguimiento=None, normalizar=False):
    # Detecta los rostros del cuadro, los clasifica y los dibuja con su nombre
    gris = cv2.cvtColor(cuadro, cv2.COLOR_BGR2GRAY)
    gris = cv2.equalizeHist(gris)  # ayuda al detector cuando hay poca luz
    cajas = detector.detectMultiScale(gris, scaleFactor=1.2, minNeighbors=6, minSize=(80, 80))
    if len(cajas) == 0:
        if seguimiento is not None:
            seguimiento.actualizar([], [])  # envejece las pistas
        return cuadro

    # Clasifica todos los rostros del cuadro de una sola vez
    lote = np.stack([preparar_rostro(cuadro, caja, tamano, normalizar) for caja in cajas])
    probabilidades = modelo(lote, training=False).numpy()
    if seguimiento is not None:
        probabilidades = seguimiento.actualizar(cajas, probabilidades)  # promedio de los ultimos cuadros

    for (x, y, ancho, alto), probs in zip(cajas, probabilidades):
        indice = int(np.argmax(probs))
        confianza = float(probs[indice])

        if confianza >= UMBRAL_CONFIANZA:
            nombre = quitar_acentos(clases[indice])
            color = (0, 200, 0)    # verde: persona reconocida
        else:
            nombre = "Desconocido"
            color = (0, 0, 220)    # rojo: la red no esta segura

        texto = f"{nombre} {confianza:.0%}" if confianza >= UMBRAL_CONFIANZA else nombre
        cv2.rectangle(cuadro, (x, y), (x + ancho, y + alto), color, 2)
        (ancho_texto, alto_texto), _ = cv2.getTextSize(texto, cv2.FONT_HERSHEY_SIMPLEX, 0.7, 2)
        cv2.rectangle(cuadro, (x, y - alto_texto - 12), (x + ancho_texto + 8, y), color, -1)
        cv2.putText(cuadro, texto, (x + 4, y - 7), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (255, 255, 255), 2)
    return cuadro


def main():
    for ruta in (RUTA_MODELO, RUTA_INFO):
        if not os.path.exists(ruta):
            print("Falta el archivo:", ruta)
            print("Entrena primero con el cuaderno de Colab y copia los archivos a esta carpeta.")
            return

    modelo, clases, tamano, normalizar = cargar_modelo()
    seguimiento = Seguimiento()
    if not normalizar:
        print("Aviso: el modelo es de la version anterior (sin CLAHE). Reentrena con el cuaderno nuevo.")
    print("Modelo cargado. Personas que reconoce:", ", ".join(clases))

    # Detector de rostros que ya trae OpenCV
    detector = cv2.CascadeClassifier(cv2.data.haarcascades + "haarcascade_frontalface_default.xml")
    if detector.empty():
        print("No se pudo cargar el detector de rostros.")
        return

    camara = cv2.VideoCapture(INDICE_CAMARA)
    if not camara.isOpened():
        print("No se pudo abrir la camara. Prueba cambiando INDICE_CAMARA a 1.")
        return

    print("Camara abierta. Presiona Q o ESC para salir.")
    while True:
        ok, cuadro = camara.read()
        if not ok:
            print("No se pudo leer de la camara.")
            break

        cuadro = procesar_cuadro(cuadro, detector, modelo, clases, tamano, seguimiento, normalizar)
        cv2.imshow(NOMBRE_VENTANA, cuadro)

        tecla = cv2.waitKey(1) & 0xFF
        if tecla in (ord("q"), ord("Q"), 27):
            break
        if cv2.getWindowProperty(NOMBRE_VENTANA, cv2.WND_PROP_VISIBLE) < 1:
            break  # se cerro la ventana con la X

    camara.release()
    cv2.destroyAllWindows()


if __name__ == "__main__":
    main()
