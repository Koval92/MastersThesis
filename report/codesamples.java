// FaceDetector API
void detectFaces(Bitmap image) {
	// detection
	FaceDetector face_detector = new FaceDetector(image.getWidth(), image.getHeight(), MAX_FACES);
	FaceDetector.Face[] faces = new FaceDetector.Face[MAX_FACES];
	int faceCount = face_detector.findFaces(image, faces);
	// drawing
	Canvas canvas = new Canvas(image);
	canvas.drawBitmap(image, 0, 0, null);
	PointF tmp_point = new PointF();
	Paint tmp_paint = new Paint();
	tmp_paint.setColor(COLOR);
	tmp_paint.setAlpha(ALPHA);
	for (int i = 0; i < facesCount; i++) {
		FaceDetector.Face face = faces[i];
		face.getMidPoint(tmp_point);
		canvas.drawCircle(tmp_point.x, tmp_point.y, face.eyesDistance(), tmp_paint);
	}
}

// Camera API
mCamera.startPreview();
mCamera.setFaceDetectionListener(new Camera.FaceDetectionListener() {
	@Override
	public void onFaceDetection(Camera.Face[] faces, Camera camera) {
		// no easy way to display it
		for(Camera.Face face : faces) {
			Log.i(FACE, duration + " ms: " +face.rect.flattenToString());
		}
	}
});
mCamera.startFaceDetection();

// openCV Java
@Override // from CameraBridgeViewBase.CvCameraViewListener
public Mat onCameraFrame(final Mat aInputFrame) {
	// convert to grayscale
	Mat grayscaleImage;
	Imgproc.cvtColor(aInputFrame, grayscaleImage, Imgproc.COLOR_RGBA2RGB);
	MatOfRect faces = new MatOfRect();
	// detect faces
	cascadeClassifier.detectMultiScale(grayscaleImage, faces);
	// mark faces on frame from camera
	for (Rect faceRect : faces.toArray) {
		Imgproc.rectangle(aInputFrame, faceRect.tl(), faceRect.br(), COLOR, THICKNESS);
	}
	return aInputFrame;
}