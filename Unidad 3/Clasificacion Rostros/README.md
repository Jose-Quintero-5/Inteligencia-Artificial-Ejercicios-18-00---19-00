# Tarea 8. CNN para clasificar rostros

Materia: Inteligencia Artificial
Alumno: Daniel Jimenez Duarte
No. de control: 23170198

Equipo:

- Miriam Alejandra Bazaldua Machado
- Daniel Jimenez Duarte
- José de Jesús Quintero Noris
- Rodrigo Villegas Valdez

## ¿De qué trata?

Entrenamos una red neuronal convolucional (CNN) con el dataset de rostros que armamos en la Tarea 6 para que
reconozca de quién es cada cara. Después usamos el modelo en un programa que abre la cámara y dice quién es cada
persona en tiempo real.

## Archivos de esta carpeta

| Archivo | Para qué sirve |
|---|---|
| `entrenar_cnn_rostros.ipynb` | Cuaderno de Colab donde entrenamos la CNN |
| `reconocer_rostros.py` | Programa que usa la cámara y la CNN para identificar personas |
| `modelo_cnn_rostros.keras` | Modelo ya entrenado (se genera con el cuaderno) |
| `modelo_info.json` | Nombres de las personas y tamaño de imagen (se genera con el cuaderno) |
| `requirements.txt` | Librerías que necesita el programa local |

## Cómo usarlo

### 1. Entrenar la CNN (Google Colab)

1. Abrimos `entrenar_cnn_rostros.ipynb` en Colab (Archivo > Subir cuaderno).
2. Activamos la GPU para que sea más rápido: Entorno de ejecución > Cambiar tipo de entorno de ejecución > T4 GPU.
3. Ejecutamos las celdas en orden. Cuando lo pida, subimos el `fotos.zip` de la Tarea 6.
4. Al final se descargan `modelo_cnn_rostros.keras` y `modelo_info.json`. Los copiamos a esta carpeta, junto a
   `reconocer_rostros.py`.

### 2. Reconocer rostros con la cámara

```bash
pip install -r requirements.txt
python reconocer_rostros.py
```

Se abre la cámara y alrededor de cada rostro aparece un cuadro con el nombre de la persona y el porcentaje de
confianza. Si la red no está segura (menos del 70 %) muestra **Desconocido**. Se sale con Q o ESC.

Para que el nombre no cambie de un cuadro a otro, el programa sigue cada cara y promedia sus últimas 8 predicciones
(`CUADROS_PROMEDIO`).

Este programa usa la cámara y abre una ventana, así que se corre en una computadora normal, no en Codespaces ni en
Colab. TensorFlow necesita una versión de Python compatible (en general de la 3.10 a la 3.13).

## La CNN

En el cuaderno se elige con la variable `MODO`:

- `'transfer'` (el que usamos): **Transfer Learning con MobileNetV2** preentrenada con ImageNet. Se le cambia la capa de
  salida por una con una neurona por persona. Primero se entrena solo la capa nueva y después se hace fine-tuning de las
  últimas 30 capas con una tasa de aprendizaje pequeña.
- `'scratch'`: una CNN diseñada por nosotros desde cero.

| Capa (modo `'scratch'`) | Qué hace |
|---|---|
| Aumento de datos | Gira, rota, acerca y cambia un poco el contraste de las imágenes al azar, solo al entrenar |
| Rescaling | Pasa los píxeles de 0-255 a 0-1 (en MobileNetV2 a [-1, 1]) |
| Conv2D 32/64/128/128 | Detectan desde bordes hasta partes de la cara |
| BatchNormalization | Estabiliza los valores después de cada convolución |
| MaxPooling2D | Reduce la imagen a la mitad quedándose con lo más importante |
| Flatten | Convierte todo en una sola lista de números |
| Dropout 0.5 | Apaga la mitad de las neuronas al entrenar para evitar el sobreajuste |
| Dense 128 (ReLU) | Combina los rasgos |
| Dense (softmax) | Una probabilidad por cada persona |

Las imágenes se ajustan a 128 x 128 píxeles en color. Antes de entrenar, de cada foto se generan 5 variantes de luz
(gamma de 0.5 a 2.0) y a todas se les aplica CLAHE (igualar el contraste local). El programa de la cámara aplica el
mismo CLAHE a cada cara. El 80 % / 20 % se separa por foto original, no por variante.

Se entrena con el optimizador Adam y la pérdida `categorical_crossentropy` (etiquetas en one-hot). Los callbacks son
EarlyStopping (se detiene si deja de mejorar) y ModelCheckpoint (guarda el mejor modelo en `mejor_modelo.keras`). En el
cuaderno están las gráficas de precisión y pérdida, el reporte por persona, la precisión según la luz y la matriz de confusión.

## Cosas que notamos

- Las fotos de cada integrante las tomamos en ráfaga con la cámara, así que muchas se parecen entre sí. Por eso
  la precisión de validación puede salir más alta que en la vida real. La prueba de verdad es el programa con la cámara.
- La detección de la cara usa Haar Cascade, que funciona mejor con la persona de frente y con buena luz.
- El programa recorta la cara igual que `capturaRostros.py` (sin margen extra) para que se parezca a las fotos
  con las que se entrenó.
- Al principio la red cambiaba de nombre según la iluminación. Para mejorarlo agregamos las variantes de luz, CLAHE y
  el promedio de cuadros. Si aun así se confunde, lo que más ayuda es tomar más fotos con distinta luz reales
  (ventana, lámpara, sombra) y volver a entrenar.
- Un modelo entrenado con la versión anterior del cuaderno no usa CLAHE; el programa avisa y hay que reentrenar.
