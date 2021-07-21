# Camera2

Camera App write with API 2:无论是预览还是拍照，都是基于ImageReader，因此一定要给build设置一个ImageReader的surface

1. CaptureRequest contains unconfigured Input/Output Surface!
cameraCaptureSession.capture(createCaptureImageRequest(), cameraCaptureSessionCaptureCallback, null);
这个captureRequest参数需要addTarget一个surface，
而这个surface必须要是cameraDevice.createCaptureSession(previewSurfaceList, CameraCaptureSessionStateCallback, null);
这个#previewSurfaceList的子集，否则就报异常了



