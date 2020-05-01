# Mobile_Yolov3-on-Android

## 简介

本仓库在 `Android` 实现了一个基于 opencv 4.0 + Mobile-Yolov3 网络的识别程序。

其在 **小米8** 手机上，对 $3*416*416$ 大小的输入，实现了单张图片 `0.32s ± 0.2s` 的检测速度。

## 需要：

- opencv >= 3.4.8
- yolov3.cfg, yolov3-tiny.cfg
- mobilenet_yolov3.weights


## TODO

- [x] 实现了对 Asset 内附带图片的检测;

- [x] 实现了对 相册 内附带图片的检测;

- [x] mobileNet_yolov3 基于 VOC 的初步训练权重;

- [ ] mobileNet_yolov3 对 COCO 数据集的训练;

- [ ] 对视频的检测;

## 权重下载
mobileNet_yolov3.caffemodel:

[Baidu_Disk, 提取码：44u7](https://pan.baidu.com/s/1rNwxlLVFKMv_dsodu6Mvnw)

## 使用：
1. 下载仓库
2. 导入 opencv 到项目中，具体可参考：[Android Studio 配置 openCV](http://soultop.top/2020/03/20/Android-Studio-%E9%85%8D%E7%BD%AE-openCV/)
3. 下载 [mobileNet_yolov3.caffemodel, 提取码：44u7](https://pan.baidu.com/s/1rNwxlLVFKMv_dsodu6Mvnw)
4. 将下载的权重文件移动到 `app/assets/` 目录下
5. 编译运行

---
Reference：

界面设计严重参考引用 pytorch 官方的例程: https://github.com/pytorch/android-demo-app

网络的实现参考以下部分：

yolov3 / yolov3-tiny: http://pjreddie.com/
https://github.com/eric612/MobileNet-YOLO
https://github.com/eric612/Caffe-YOLOv3-Windows
SSD: https://github.com/chuanqi305/MobileNet-SSD

Android 端的实现参考：

https://github.com/matteomedioli/AndroidObjectDetection-OpenCV
