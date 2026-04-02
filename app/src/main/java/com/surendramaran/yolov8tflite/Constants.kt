package com.surendramaran.yolov8tflite

/**
 * Central place for asset paths under `app/src/main/assets/`.
 *
 * Swap these when you add a new exported model or label file without renaming on disk.
 */
object Constants {
    /** Filename of the TensorFlow Lite model in assets. */
    const val MODEL_PATH = "model.tflite"
    /** Text file with one class name per line, order matching the model’s class indices. */
    const val LABELS_PATH = "labels.txt"
}
