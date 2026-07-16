# Model Details — PPE Detection

This document describes the object-detection model bundled with this app, where it came from,
how it was validated, and how the app consumes it. Read this before swapping or retraining the model.

## Summary

| Property | Value |
|---|---|
| **Asset file** | `app/src/main/assets/ppe_css.tflite` |
| **Labels file** | `app/src/main/assets/labels_css.txt` |
| **Architecture** | YOLOv8s (Ultralytics), anchor-free detection head |
| **Parameters** | ~11.1 M |
| **File size** | 42.7 MB (FP32 TFLite) |
| **Task** | Object detection (PPE compliance) |
| **Classes** | 10 (see below) |
| **Input** | `[1, 3, 640, 640]` float32, **NCHW**, RGB, normalized 0–1 |
| **Output** | `[1, 14, 8400]` float32 — 4 box channels (cx, cy, w, h, normalized 0–1) + 10 class scores per anchor |
| **Source weights** | [VoxDroid/Construction-Site-Safety-PPE-Detection](https://github.com/VoxDroid/Construction-Site-Safety-PPE-Detection) — `yolov8s_ppe_css_200_epochs/weights/best.pt` |
| **Exported with** | Ultralytics 8.4.96, `yolo export format=tflite imgsz=640` (FP32, LiteRT) |
| **Export date** | 2026-07-16 |

## Classes

Class order below **must** match `labels_css.txt` line-for-line — the app maps output indices to
these names at runtime.

| Index | Class | Type |
|---|---|---|
| 0 | `Hardhat` | PPE worn |
| 1 | `Mask` | PPE worn |
| 2 | `NO-Hardhat` | **Violation** (bare head) |
| 3 | `NO-Mask` | **Violation** (uncovered face) |
| 4 | `NO-Safety Vest` | **Violation** (no vest on torso) |
| 5 | `Person` | Context |
| 6 | `Safety Cone` | Context |
| 7 | `Safety Vest` | PPE worn |
| 8 | `machinery` | Context |
| 9 | `vehicle` | Context |

The explicit `NO-*` violation classes are the key design property: a bare head is *positively
classified* as `NO-Hardhat` instead of being at risk of a false "helmet" guess. Note this model
does **not** cover gloves, goggles, or safety shoes.

## Training

- **Dataset:** [Construction Site Safety Image Dataset](https://www.kaggle.com/datasets/snehilsanyal/construction-site-safety-image-dataset-roboflow)
  (Roboflow) — 2,801 annotated images (2,605 train / 114 val / 82 test).
- **Recipe:** YOLOv8s fine-tuned from COCO-pretrained weights, 200 epochs, imgsz 640
  (trained by the VoxDroid project, not by us).

### Reported metrics (validation, from the source repo)

| Metric | Value |
|---|---|
| Precision | 0.95 |
| Recall | 0.80 |
| mAP@50 | 0.877 |
| mAP@50–95 | 0.615 |

## Our verification (2026-07-16)

We tested the exported TFLite directly (Python, `ai-edge-litert`), replicating the app's exact
preprocessing, before shipping it. Comparison against the previous model on photos of people
**without** PPE:

| Test image | Previous model (`model.tflite`) | This model |
|---|---|---|
| Two bare-headed men | helmet 0.41–0.67 (false positive) | NO-Hardhat 0.90 ✓ |
| Same image, brightness +30% | helmet 0.67 (false positive) | NO-Hardhat 0.89 ✓ |
| Worker wearing hard hat | — | Hardhat 0.80 ✓ |
| Worker wearing hard hat #2 | — | Hardhat 0.87 ✓ |
| Street scene, no PPE | helmet false positives | NO-Hardhat 0.85, NO-Safety Vest 0.93 ✓ |

## How the app consumes it

- `Detector.kt` reads tensor shapes at runtime (handles the NCHW input and `[1, 14, 8400]` output),
  applies confidence threshold **0.5** and NMS IoU threshold **0.5**.
- Preprocessing: frame resized (stretched) to 640×640, pixel values divided by 255.
- `MainActivity.kt` checklist: helmet/vest counted as DETECTED only if the positive class
  (`Hardhat` / `Safety Vest`) is present **and** no corresponding `NO-*` violation is in frame —
  one uncovered head anywhere means non-compliance.
- Asset paths are configured in `Constants.kt`.

## Previous model (kept in assets for rollback)

`model.tflite` + `labels.txt` — YOLOv8n, 6 classes (`Gloves, Vest, goggles, helmet, mask,
safety_shoe`), 10.9 MB, trained on a custom Roboflow PPE dataset. **Deprecated** because it was
trained without negative examples and systematically false-positives bare heads as `helmet` and
bare hands as `Gloves` (see verification table above). Revert `Constants.kt` to switch back.

## Licensing

- The source repository (VoxDroid) is **MIT** licensed.
- The weights are a fine-tune of Ultralytics YOLOv8, which is **AGPL-3.0**. Commercial
  distribution of this app requires either AGPL compliance or an
  [Ultralytics Enterprise License](https://ultralytics.com/license). (The previous model has the
  same obligation.)
- The training dataset is published on Roboflow/Kaggle under its own terms (CC BY 4.0).

## Regenerating the TFLite from source weights

```bash
pip install ultralytics
curl -LO https://github.com/VoxDroid/Construction-Site-Safety-PPE-Detection/raw/main/Model-Training/Outputs/runs/detect/yolov8s_ppe_css_200_epochs/weights/best.pt
yolo export model=best.pt format=tflite imgsz=640        # FP32 (this file)
# yolo export model=best.pt format=tflite imgsz=640 int8=True data=<dataset.yaml>  # ~11 MB INT8 variant
```

The exported file embeds its own metadata (class map, imgsz, version) — inspect it with
`unzip -p ppe_css.tflite metadata.json`.
