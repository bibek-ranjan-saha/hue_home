# TensorFlow Lite Model Setup

## Required Model

HueHome AR uses **DeepLab v3+** for semantic segmentation of walls, doors, and windows.

### Download Model

1. **Option 1: Pre-trained PASCAL VOC Model**
   ```bash
   # Download from TensorFlow Hub
   wget https://tfhub.dev/tensorflow/lite-model/deeplabv3/1/metadata/2?lite-format=tflite -O deeplabv3_513_mv_gpu.tflite
   ```

2. **Option 2: Custom Trained Model**
   - Train on interior scenes dataset
   - Export to TensorFlow Lite format
   - Optimize with GPU delegate

### Installation

1. Create assets directory:
   ```bash
   mkdir -p app/src/main/assets/models
   ```

2. Copy model file:
   ```bash
   cp deeplabv3_513_mv_gpu.tflite app/src/main/assets/models/
   ```

### Model Specifications

- **Input**: 513x513x3 (RGB image)
- **Output**: 513x513 (segmentation map)
- **Classes**: 21 (PASCAL VOC)
  - Class 15: Wall/Building
  - Class 8: Door
  - Class 20: Window (approximate)
  - Class 9: Floor
- **Format**: TensorFlow Lite with GPU delegate support
- **Size**: ~2.7 MB

### Alternative Models

For better accuracy on interior scenes, consider:
- **ADE20K trained model** - More indoor classes
- **Custom model** - Train on interior dataset
- **Quantized model** - Smaller size, faster inference

### Performance

- **Inference time**: ~100-200ms on GPU
- **FPS**: 5-10 (suitable for AR use case)
- **Accuracy**: ~70-80% mIOU on PASCAL VOC

## Troubleshooting

If model file is missing, the app will throw `SegmentationException` on initialization.

Check logcat for errors:
```bash
adb logcat | grep SegmentationEngine
```
