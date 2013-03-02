#pragma once

#include "ofMain.h"
#include "ofxAndroid.h"

#include "ofxAccelerometer.h"
#include "ofxOsc.h"

#define HOST "192.168.1.255"
#define PORT 12345
class testApp : public ofxAndroidApp{
	
	public:
		
		void setup();
		void update();
		void draw();

		void keyPressed(int key);
		void keyReleased(int key);
		void windowResized(int w, int h);

		void touchDown(int x, int y, int id);
		void touchMoved(int x, int y, int id);
		void touchUp(int x, int y, int id);
		void touchDoubleTap(int x, int y, int id);
		void touchCancelled(int x, int y, int id);
		void swipe(ofxAndroidSwipeDir swipeDir, int id);

		void pause();
		void stop();
		void resume();
		void reloadTextures();

		bool backPressed();
		void okPressed();
		void cancelPressed();


		ofVec3f accel, normAccel, ort, normOrt;
		ofxOscSender sender;
		string uuid;
		float frame;

};
