mCamera.startPreview();
mCamera.setFaceDetectionListener(new Camera.FaceDetectionListener() {
	@Override
	public void onFaceDetection(Camera.Face[] faces, Camera camera) {
		// no easy way to display it on image
		for(Camera.Face face : faces) {
			Log.i(FACE, face.rect.flattenToString());
		}
	}
});
mCamera.startFaceDetection();