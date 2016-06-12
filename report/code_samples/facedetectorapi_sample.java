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