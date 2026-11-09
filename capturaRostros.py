"""
captura_rostros.py
Toma ~100 fotos con la cámara web, detecta el rostro en cada una,
recorta la imagen justo al margen de la cara y la guarda en una carpeta.

Requisitos:
    pip install opencv-python

Controles (en la ventana de la cámara):
    ESPACIO -> iniciar / pausar la captura automática
    Q o ESC -> salir
"""

import os
import time
import cv2

# ---------------- CONFIGURACIÓN ----------------
CARPETA_SALIDA = "fotos_rostro"   # carpeta donde se guardan los recortes
TOTAL_FOTOS = 100                 # cantidad de fotos a tomar
INTERVALO = 0.3                   # segundos entre cada foto

# URL de conexión directa a DroidCam (cambia la IP si es necesario)
CAMARA_URL = "http://192.168.1.8:4747/video" 

MARGEN = 0.0                      # 0.0 = recorte exacto a la cara; 0.1 = 10% extra alrededor
TAMANO_SALIDA = None              # None = tamaño original del recorte; o por ejemplo (200, 200)
# ------------------------------------------------


def main():
    os.makedirs(CARPETA_SALIDA, exist_ok=True)

    # Clasificador de rostros incluido con OpenCV (no requiere descargas)
    ruta_cascada = cv2.data.haarcascades + "haarcascade_frontalface_default.xml"
    detector = cv2.CascadeClassifier(ruta_cascada)
    if detector.empty():
        print("No se pudo cargar el detector de rostros.")
        return

    camara = cv2.VideoCapture(CAMARA_URL)
    if not camara.isOpened():
        print("No se pudo abrir la cámara. Verifica que DroidCam esté abierto y la IP sea correcta.")
        return

    # Continuar la numeración si ya hay fotos en la carpeta
    existentes = [f for f in os.listdir(CARPETA_SALIDA) if f.lower().endswith(".jpg")]
    contador = len(existentes)
    meta = contador + TOTAL_FOTOS

    capturando = False
    ultima_captura = 0.0

    print("Listo. Presiona ESPACIO para iniciar la captura, Q o ESC para salir.")

    while contador < meta:
        ok, frame = camara.read()
        if not ok:
            print("No se pudo leer de la cámara.")
            break

        gris = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        rostros = detector.detectMultiScale(
            gris, scaleFactor=1.2, minNeighbors=6, minSize=(80, 80)
        )

        vista = frame.copy()
        for (x, y, w, h) in rostros:
            cv2.rectangle(vista, (x, y), (x + w, y + h), (0, 255, 0), 2)

        # Solo guardamos si hay exactamente un rostro (evita recortes erróneos)
        if capturando and len(rostros) == 1:
            ahora = time.time()
            if ahora - ultima_captura >= INTERVALO:
                x, y, w, h = rostros[0]

                # Margen opcional alrededor de la cara
                mx, my = int(w * MARGEN), int(h * MARGEN)
                x1, y1 = max(x - mx, 0), max(y - my, 0)
                x2 = min(x + w + mx, frame.shape[1])
                y2 = min(y + h + my, frame.shape[0])

                recorte = frame[y1:y2, x1:x2]
                if TAMANO_SALIDA:
                    recorte = cv2.resize(recorte, TAMANO_SALIDA)

                contador += 1
                nombre = os.path.join(CARPETA_SALIDA, f"rostro_{contador:03d}.jpg")
                cv2.imwrite(nombre, recorte)
                ultima_captura = ahora

        estado = "CAPTURANDO" if capturando else "PAUSADO (ESPACIO para iniciar)"
        cv2.putText(vista, f"{estado}  |  Fotos: {contador - len(existentes)}/{TOTAL_FOTOS}",
                    (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 255, 255), 2)
        if capturando and len(rostros) != 1:
            cv2.putText(vista, "Se necesita exactamente 1 rostro visible",
                        (10, 60), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 0, 255), 2)

        cv2.imshow("Captura de rostros", vista)

        tecla = cv2.waitKey(1) & 0xFF
        if tecla == 32:            # ESPACIO
            capturando = not capturando
        elif tecla in (27, ord("q")):  # ESC o Q
            break

    camara.release()
    cv2.destroyAllWindows()
    print(f"Terminado. Fotos guardadas en: {os.path.abspath(CARPETA_SALIDA)}")


if __name__ == "__main__":
    main()