#include "testApp.h"

//--------------------------------------------------------------
void testApp::setup() {
	// initialize the accelerometer
	ofxAccelerometer.setup();
	sender.setup("192.168.1.255", PORT);
	ofSetLineWidth(10);
	ofBackground(0, 0, 0);
}

//--------------------------------------------------------------
void testApp::update() {
	accel = ofxAccelerometer.getForce();
	normAccel = accel.getNormalized();

	//if (frame % 8 == 0) {
		ofxOscMessage b;
		b.setAddress("/godmode/device/uuid/accel");
		b.addFloatArg(normAccel.x);
		b.addFloatArg(normAccel.y);
		b.addFloatArg(normAccel.z);
		sender.sendMessage(b);
	//}

}

//--------------------------------------------------------------
void testApp::draw() {
	ofSetColor(255, 255, 0);
	ofDrawBitmapString("p.x = " + ofToString(normAccel.x), 10, 35);
	ofDrawBitmapString("p.y = " + ofToString(normAccel.y), 10, 50);
	ofDrawBitmapString("p.z = " + ofToString(normAccel.z), 10, 65);

	ofPushMatrix();
	ofTranslate(ofGetWidth() / 2, ofGetHeight() / 2);

	ofSetColor(255, 255, 0);
	ofLine(0, 0, normAccel.x * ofGetWidth() / 2, 0);
	ofSetColor(255, 0, 255);
	ofLine(0, 0, 0, -normAccel.y * ofGetHeight() / 2);
	// we don't draw z as the perspective might be confusing
	// but it's approximately one when the device is still and parallel
	// to the ground
	ofPopMatrix();
}

//--------------------------------------------------------------
void testApp::keyPressed(int key) {

}

//--------------------------------------------------------------
void testApp::keyReleased(int key) {

}

//--------------------------------------------------------------
void testApp::windowResized(int w, int h) {

}

//--------------------------------------------------------------
void testApp::touchDown(int x, int y, int id) {

}

//--------------------------------------------------------------
void testApp::touchMoved(int x, int y, int id) {

}

//--------------------------------------------------------------
void testApp::touchUp(int x, int y, int id) {

}

//--------------------------------------------------------------
void testApp::touchDoubleTap(int x, int y, int id) {

}

//--------------------------------------------------------------
void testApp::touchCancelled(int x, int y, int id) {

}

//--------------------------------------------------------------
void testApp::swipe(ofxAndroidSwipeDir swipeDir, int id) {

}

//--------------------------------------------------------------
void testApp::pause() {

}

//--------------------------------------------------------------
void testApp::stop() {

}

//--------------------------------------------------------------
void testApp::resume() {

}

//--------------------------------------------------------------
void testApp::reloadTextures() {

}

//--------------------------------------------------------------
bool testApp::backPressed() {
	return false;
}

//--------------------------------------------------------------
void testApp::okPressed() {

}

//--------------------------------------------------------------
void testApp::cancelPressed() {

}
