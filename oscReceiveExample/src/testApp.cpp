#include "testApp.h"

//--------------------------------------------------------------
void testApp::setup(){
	// listen on the given port
	cout << "listening for osc messages on port " << PORT << "\n";
	receiver.setup(PORT);
    
	current_msg_string = 0;
	mouseX = 0;
	mouseY = 0;
	mouseButtonState = "";
    
	ofBackground(4,5,6);
    
    
    p.x = 0;
    p.y = 0;
    p.z = 0;
}

//--------------------------------------------------------------
void testApp::update(){
    
	// hide old messages
	for(int i = 0; i < NUM_MSG_STRINGS; i++){
		if(timers[i] < ofGetElapsedTimef()){
			msg_strings[i] = "";
		}
	}
    
	// check for waiting messages
	while(receiver.hasWaitingMessages()){
		// get the next message
		ofxOscMessage m;
		receiver.getNextMessage(&m);
        
		// check for mouse moved message
		if(m.getAddress() == "/godmode/device/uuid/accel"){
			// both the arguments are int32's
            p.x = m.getArgAsFloat(0);
            p.y = m.getArgAsFloat(1);
            p.z = m.getArgAsFloat(2);
            
            points.push_back(p);
            if(points.size()>ofGetWidth()){
                points.pop_front();
            }
        }
		else{
			// unrecognized message: display on the bottom of the screen
			string msg_string;
			msg_string = m.getAddress();
			msg_string += ": ";
			for(int i = 0; i < m.getNumArgs(); i++){
				// get the argument type
				msg_string += m.getArgTypeName(i);
				msg_string += ":";
				// display the argument - make sure we get the right type
				if(m.getArgType(i) == OFXOSC_TYPE_INT32){
					msg_string += ofToString(m.getArgAsInt32(i));
				}
				else if(m.getArgType(i) == OFXOSC_TYPE_FLOAT){
					msg_string += ofToString(m.getArgAsFloat(i));
				}
				else if(m.getArgType(i) == OFXOSC_TYPE_STRING){
					msg_string += m.getArgAsString(i);
				}
				else{
					msg_string += "unknown";
				}
			}
			// add to the list of strings to display
			msg_strings[current_msg_string] = msg_string;
			timers[current_msg_string] = ofGetElapsedTimef() + 5.0f;
			current_msg_string = (current_msg_string + 1) % NUM_MSG_STRINGS;
			// clear the next line
			msg_strings[current_msg_string] = "";
		}
	}
}


//--------------------------------------------------------------
void testApp::draw(){
    
	string buf;
	buf = "listening for osc messages on port" + ofToString(PORT);
	ofDrawBitmapString(buf, 10, 20);
    
	
	for(int i = 0; i < NUM_MSG_STRINGS; i++){
		ofDrawBitmapString(msg_strings[i], 10, 40 + 15 * i);
	}
    
    ofDrawBitmapString("p.x = " + ofToString(p.x), 10, 35);
	ofDrawBitmapString("p.y = " + ofToString(p.y), 10, 50);
	ofDrawBitmapString("p.z = " + ofToString(p.z), 10, 65);
    
    
    ofPushMatrix();
	ofTranslate(ofGetWidth() / 2, ofGetHeight() / 2);
	ofSetColor(255, 255, 0);
	ofLine(0, 0, p.x * ofGetHeight() / 2, 0);
	ofSetColor(255, 0, 255);
	ofLine(0, 0, 0, -p.y * ofGetWidth() / 2);
	// we don't draw z as the perspective might be confusing
	// but it's approximately one when the device is still and parallel
	// to the ground
    ofSetColor(255, 0, 0);
    ofBox(p.x*ofGetWidth()/2, -p.y*ofGetHeight()/2, p.z*45, 10);
    
	ofPopMatrix();
    
    ofPushMatrix();
    ofTranslate(0,3*ofGetHeight()/4);
    drawCurve(points, 50, 0, 0);
}


void testApp::drawCurve(deque<ofPoint> curve, float scale, float min, float max) {
    ofNoFill();
    ofBeginShape();
    for(int i = 0; i < curve.size(); i++) {
        ofSetColor(255, 0, 0);
        ofVertex(i, curve[i].x*scale);
    }
    ofEndShape();
    
    ofBeginShape();
    for(int i = 0; i < curve.size(); i++) {
        ofSetColor(255, 255, 0);
        ofVertex(i, curve[i].y*scale);
    }
    ofEndShape();
    
    ofBeginShape();
    for(int i = 0; i < curve.size(); i++) {
        ofSetColor(0, 255, 255);
        ofVertex(i, curve[i].z*scale);
    }
    ofEndShape();
	
}

//--------------------------------------------------------------
void testApp::keyPressed(int key){
    
}

//--------------------------------------------------------------
void testApp::keyReleased(int key){
    
}

//--------------------------------------------------------------
void testApp::mouseMoved(int x, int y){
    
}

//--------------------------------------------------------------
void testApp::mouseDragged(int x, int y, int button){
    
}

//--------------------------------------------------------------
void testApp::mousePressed(int x, int y, int button){
}

//--------------------------------------------------------------
void testApp::mouseReleased(int x, int y, int button){
    
}

//--------------------------------------------------------------
void testApp::windowResized(int w, int h){
    
}

//--------------------------------------------------------------
void testApp::gotMessage(ofMessage msg){
    
}

//--------------------------------------------------------------
void testApp::dragEvent(ofDragInfo dragInfo){
    
}
