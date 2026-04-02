package com.surendramaran.yolov8tflite

/**
 * One detected object after decoding (and NMS), in **normalized** image coordinates.
 *
 * @property x1,y1,x2,y2 Corners of the axis-aligned box in 0–1 relative to bitmap width/height.
 * @property cx,cy,w,h Center and size as produced by the model (also normalized).
 * @property cnf Confidence score for the assigned class.
 * @property cls Class index in the label list.
 * @property clsName Human-readable label from the labels asset ([Constants.LABELS_PATH]).
 */
data class BoundingBox(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val cx: Float,
    val cy: Float,
    val w: Float,
    val h: Float,
    val cnf: Float,
    val cls: Int,
    val clsName: String
)