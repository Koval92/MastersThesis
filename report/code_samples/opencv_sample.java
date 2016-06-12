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