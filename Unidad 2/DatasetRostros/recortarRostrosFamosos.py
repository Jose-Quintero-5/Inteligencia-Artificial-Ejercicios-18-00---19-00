import os
import cv2
import tkinter as tk
from tkinter import filedialog

def procesar_con_selector():
    print("========================================")
    print("  RECORTADOR DE ROSTROS CON VENTANA")
    print("========================================")
    
    # Ocultar la ventana principal de Tkinter para que solo aparezca el explorador
    root = tk.Tk()
    root.withdraw()

    print("👉 Se abrirá una ventana. Selecciona las fotos que quieras recortar (puedes elegir varias a la vez con Ctrl o Shift).")
    
    # Abrir el explorador de archivos para seleccionar las imágenes
    archivos_seleccionados = filedialog.askopenfilenames(
        title="Selecciona las fotos de los famosos",
        filetypes=[("Imágenes", "*.jpg *.jpeg *.png *.webp")]
    )

    if not archivos_seleccionados:
        print("No seleccionaste ninguna foto. Saliendo del programa.")
        return

    carpeta_salida = input("Ingresa el nombre de la carpeta donde quieres guardar los recortes (ej. dataset_proyecto/famoso_1): ").strip()
    os.makedirs(carpeta_salida, exist_ok=True)

    # Clasificador de rostros de OpenCV
    ruta_cascada = cv2.data.haarcascades + "haarcascade_frontalface_default.xml"
    detector = cv2.CascadeClassifier(ruta_cascada)
    
    if detector.empty():
        print("Error al cargar el detector de rostros de OpenCV.")
        return

    print(f"\nProcesando {len(archivos_seleccionados)} imágenes seleccionadas...")
    
    exitosos = 0
    fallidos = 0
    margen = 0.1  # 10% de margen alrededor de la cara
    tamano_salida = (250, 250) # Tamaño cuadrado estándar para la IA

    for ruta_imagen in archivos_seleccionados:
        imagen = cv2.imread(ruta_imagen)
        
        if imagen is None:
            fallidos += 1
            continue

        gris = cv2.cvtColor(imagen, cv2.COLOR_BGR2GRAY)
        rostros = detector.detectMultiScale(
            gris, scaleFactor=1.1, minNeighbors=5, minSize=(50, 50)
        )

        if len(rostros) > 0:
            x, y, w, h = rostros[0]

            mx, my = int(w * margen), int(h * margen)
            x1, y1 = max(x - mx, 0), max(y - my, 0)
            x2 = min(x + w + mx, imagen.shape[1])
            y2 = min(y + h + my, imagen.shape[0])

            recorte = imagen[y1:y2, x1:x2]
            if tamano_salida:
                recorte = cv2.resize(recorte, tamano_salida)

            exitosos += 1
            nombre_salida = os.path.join(carpeta_salida, f"rostro_{exitosos:03d}.jpg")
            cv2.imwrite(nombre_salida, recorte)
        else:
            fallidos += 1

    print("\n========================================")
    print(f"¡Proceso completado!")
    print(f"Rostros recortados y guardados: {exitosos}")
    print(f"Imágenes sin rostro detectado (descartadas): {fallidos}")
    print(f"Guardado en: {os.path.abspath(carpeta_salida)}")
    print("========================================")

if __name__ == "__main__":
    procesar_con_selector()