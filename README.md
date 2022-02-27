# AccuraMRZ-FM-Android-SDK-OCFD

Accura MRZ is used for Optical character recognition.<br/>

Below steps to setup Accura MRZ SDK to your project.

## Install SDK in to your App

#### Step 1: Check the aar file:
    Make sure accura_mrz_fm_liveness-release.aar file is there inside your app/lib/ folder 

#### Step 2: Add dependency:
    Link the aar file through your app's gradle file.
    Also add "api 'com.google.code.gson:gson:2.8.6'" dependency and all other dependencies needed for your app

    android {
        compileOptions {
            sourceCompatibility JavaVersion.VERSION_1_8
            targetCompatibility JavaVersion.VERSION_1_8
        }
    }
    dependencies {
    	implementation fileTree(dir: "libs", include: ["*.jar"])
		implementation 'androidx.appcompat:appcompat:1.3.1'
		implementation 'androidx.constraintlayout:constraintlayout:2.0.4'
		testImplementation 'junit:junit:4.13.2'
		androidTestImplementation 'androidx.test.ext:junit:1.1.3'
		androidTestImplementation 'androidx.test.espresso:espresso-core:3.4.0'
		implementation 'com.github.bumptech.glide:glide:4.11.0'
		annotationProcessor 'com.github.bumptech.glide:compiler:4.11.0'
		implementation 'com.google.android.material:material:1.5.0-alpha01'
		
		api 'com.google.code.gson:gson:2.8.6'
	
		//Implement ACCURA_MRZ_FM_SDK AAR file
		implementation files('libs\\accura_mrz_fm_liveness-release.aar')
    }

#### Step 3: Add files to project assets folder:

* Create "assets" folder under `app/src/main` and Add `key.license` & 'accuraface.license' file in assets folder.

* Generate your Accura license from https://accurascan.com/developer/dashboard

## Setup Accura MRZ
* Require `key.license` to implement Accura MRZ in to your app

#### Step 1 : To initialize sdk on app start:

    RecogEngine recogEngine = new RecogEngine();
    RecogEngine.SDKModel sdkModel = recogEngine.initEngine(your activity context);

    if (sdkModel.i > 0) { // means license is valid
         if (sdkModel.isMRZEnable) // True if MRZ option is selected while creating license
    }

##### Update filters like below.</br>
  Call this function after initialize sdk if license is valid(sdkModel.i > 0)
   * Set Blur Percentage to allow blur on document

        ```
		//0 for clean document and 100 for Blurry document
		recogEngine.setBlurPercentage(Context context, int blurPercentage/*52*/);
		```
   * Set Face blur Percentage to allow blur on detected Face

        ```
		// 0 for clean face and 100 for Blurry face
		recogEngine.setFaceBlurPercentage(Context context, int faceBlurPercentage/*70*/);
        ```
   * Set Glare Percentage to detect Glare on document

        ```
		// Set min and max percentage for glare
		recogEngine.setGlarePercentage(Context context, int /*minPercentage*/6, int /*maxPercentage*/98);
		```
   * Set Photo Copy to allow photocopy document or not

        ```
		// Set min and max percentage for glare
		recogEngine.isCheckPhotoCopy(Context context, boolean /*isCheckPhotoCopy*/false);
		```
   * Set Hologram detection to verify the hologram on the face

        ```
		// true to check hologram on face
		recogEngine.SetHologramDetection(Context context, boolean /*isDetectHologram*/true);
		```
   * Set light tolerance to detect light on document

        ```
        // 0 for full dark document and 100 for full bright document
        recogEngine.setLowLightTolerance(Context context, int /*tolerance*/39);
        ```
   * Set motion threshold to detect motion on camera document
		```
        // 1 - allows 1% motion on document and
        // 100 - it can not detect motion and allow document to scan.
        recogEngine.setMotionThreshold(Context context, int /*motionThreshold*/18);
        ```

#### Step 2 : Set CameraView
```
Must have to extend com.accurascan.ocr.mrz.motiondetection.SensorsActivity to your activity.
- Make sure your activity orientation locked from Manifest. Because auto rotate not support.

private CameraView cameraView;

@Override
public void onCreate(Bundle savedInstanceState) {
    if (isPortrait) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // to set portarait mode
    } else {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // to set landscape mode
    }
    super.onCreate(savedInstanceState);
    setContentView(R.layout.your_layout);

    // initialized camera
    initCamera();
}

private void initCamera() {
    //<editor-fold desc="To get status bar height">
    Rect rectangle = new Rect();
    Window window = getWindow();
    window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
    int statusBarTop = rectangle.top;
    int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
    int statusBarHeight = contentViewTop - statusBarTop;
    //</editor-fold>

    RelativeLayout linearLayout = findViewById(R.id.ocr_root); // layout width and height is match_parent

    if (recogType == RecogType.MRZ) {
        // Also set MRZ document type to scan specific MRZ document
        // 1. ALL MRZ document       - MRZDocumentType.NONE        
        // 2. Passport MRZ document  - MRZDocumentType.PASSPORT_MRZ
        // 3. ID card MRZ document   - MRZDocumentType.ID_CARD_MRZ 
        // 4. Visa MRZ document      - MRZDocumentType.VISA_MRZ    
        cameraView.setMRZDocumentType(mrzDocumentType);
    }
    
    cameraView.setRecogType(recogType)//or RecogType.MRZ
            .setView(linearLayout) // To add camera view
            .setCameraFacing(0) // // To set selfie(1) or rear(0) camera.
            .setOcrCallback(this)  // To get feedback and Success Call back
            .setStatusBarHeight(statusBarHeight)  // To remove Height from Camera View if status bar visible
//                Option setup
//                .setEnableMediaPlayer(false) // false to disable default sound and true to enable sound and default it is true
//                .setCustomMediaPlayer(MediaPlayer.create(this, /*custom sound file*/)) // To add your custom sound and Must have to enable media player
            .init();  // initialized camera
}

/**
 * To handle camera on window focus update
 * @param hasFocus
 */
@Override
public void onWindowFocusChanged(boolean hasFocus) {
    if (cameraView != null) {
        cameraView.onWindowFocusUpdate(hasFocus);
    }
}

@Override
protected void onResume() {
    super.onResume();
    cameraView.onResume();
}

@Override
protected void onPause() {
    cameraView.onPause();
    super.onPause();
}

@Override
protected void onDestroy() {
    cameraView.onDestroy();
    super.onDestroy();
}

/**
 * To update your border frame according to width and height
 * it's different for different card
 * Call {@link CameraView#startOcrScan(boolean isReset)} To start Camera Preview
 * @param width    border layout width
 * @param height   border layout height
 */
@Override
public void onUpdateLayout(int width, int height) {
    if (cameraView != null) cameraView.startOcrScan(false);

    //<editor-fold desc="To set camera overlay Frame">
    ViewGroup.LayoutParams layoutParams = borderFrame.getLayoutParams();
    layoutParams.width = width;
    layoutParams.height = height;
    borderFrame.setLayoutParams(layoutParams);

    ViewGroup.LayoutParams lpRight = viewRight.getLayoutParams();
    lpRight.height = height;
    viewRight.setLayoutParams(lpRight);

    ViewGroup.LayoutParams lpLeft = viewLeft.getLayoutParams();
    lpLeft.height = height;
    viewLeft.setLayoutParams(lpLeft);
    //</editor-fold>
}

/**
 * Override this method after scan complete to get data from document
 *
 * @param result is scanned card data
 *
 */
 @Override
    public void onScannedComplete(Object result) {
        Runtime.getRuntime().gc(); // To clear garbage
        AccuraLog.loge(TAG, "onScannedComplete: ");
        if (result != null) {
                if (result instanceof RecogResult) {
                /**
                 *  @recogType is {@link RecogType#MRZ}*/
                RecogResult.setRecogResult((RecogResult) result);
                sendDataToResultActivity(RecogType.MRZ);
            }
            }
        else Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
    }


/**
 * @param titleCode to display scan card message on top of border Frame
 *
 * @param errorMessage To display process message.
 *                null if message is not available
 * @param isFlip  true to set your customize animation for scan back card alert after complete front scan
 *                and also used cameraView.flipImage(ImageView) for default animation
 */
  @Override
    public void onProcessUpdate(int titleCode, String errorMessage, boolean isFlip) {
        AccuraLog.loge(TAG, "onProcessUpdate :-> " + titleCode + "," + errorMessage + "," + isFlip);
        Message message;
        if (getTitleMessage(titleCode) != null) {
            
            message = new Message();
            message.what = 0;
            message.obj = getTitleMessage(titleCode);
            handler.sendMessage(message);
//            tvTitle.setText(title);
        }
        if (errorMessage != null) {
            message = new Message();
            message.what = 1;
            message.obj = getErrorMessage(errorMessage);
            handler.sendMessage(message);
//            tvScanMessage.setText(message);
        }
        if (isFlip) {
            message = new Message();
            message.what = 2;
            handler.sendMessage(message);//  to set default animation or remove this line to set your customize animation
        }

    }

@Override
public void onError(String errorMessage) {
    // display data on ui thread
    // stop ocr if failed
    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
}

private String getTitleMessage(int titleCode) {
    if (titleCode < 0) return null;
    switch (titleCode){
          case RecogEngine.SCAN_TITLE_MRZ_PDF417_FRONT:// for front side MRZ
              return "Scan Front Side of Document";
          case RecogEngine.SCAN_TITLE_MRZ_PDF417_BACK: // for back side MRZ
               return "Now Scan Back Side of Document";
        default:return "";
    }
}

private String getErrorMessage(String s) {
    switch (s) {
        case RecogEngine.ACCURA_ERROR_CODE_MOTION:
            return "Keep Document Steady";
        case RecogEngine.ACCURA_ERROR_CODE_PROCESSING:
            return "Processing...";
        case RecogEngine.ACCURA_ERROR_CODE_BLUR_DOCUMENT:
            return "Blur detect in document";
        case RecogEngine.ACCURA_ERROR_CODE_FACE_BLUR:
            return "Blur detected over face";
        case RecogEngine.ACCURA_ERROR_CODE_GLARE_DOCUMENT:
            return "Glare detect in document";
        case RecogEngine.ACCURA_ERROR_CODE_HOLOGRAM:
            return "Hologram Detected";
        case RecogEngine.ACCURA_ERROR_CODE_DARK_DOCUMENT:
            return "Low lighting detected";
        case RecogEngine.ACCURA_ERROR_CODE_PHOTO_COPY_DOCUMENT:
            return "Can not accept Photo Copy Document";
        case RecogEngine.ACCURA_ERROR_CODE_FACE:
            return "Face not detected";
        case RecogEngine.ACCURA_ERROR_CODE_MRZ:
            return "MRZ not detected";
        case RecogEngine.ACCURA_ERROR_CODE_PASSPORT_MRZ:
            return "Passport MRZ not detected";
        case RecogEngine.ACCURA_ERROR_CODE_ID_MRZ:
            return "ID card MRZ not detected";
        case RecogEngine.ACCURA_ERROR_CODE_VISA_MRZ:
            return "Visa MRZ not detected";
        default:
            return s;
    }
}

// After getting result to restart scanning you have to set below code onActivityResult
// when you use startActivityForResult(Intent, RESULT_ACTIVITY_CODE) to open result activity.
@Override
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    ...
    if (resultCode == RESULT_OK) {
        if (requestCode == RESULT_ACTIVITY_CODE) {
            //<editor-fold desc="Call CameraView#startOcrScan(true) to scan document again">

            if (cameraView != null) cameraView.startOcrScan(true);

            //</editor-fold>
        }
    }
}
```


## 2. Setup Accura Face Match
* Require `accuraface.license` to implement AccuraFaceMatch SDK in to your app

#### Step 1 : Add following code in Manifest.
    <manifest>
        ...
        <uses-permission android:name="android.permission.CAMERA" />
        <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    </manifest>

#### Step 2 : Open auto capture camera
    LivenessCustomization cameraScreenCustomization = new LivenessCustomization();
    
    cameraScreenCustomization.backGroundColor = getResources().getColor(R.color.fm_camera_Background);
    cameraScreenCustomization.closeIconColor = getResources().getColor(R.color.fm_camera_CloseIcon);
    cameraScreenCustomization.feedbackBackGroundColor = getResources().getColor(R.color.fm_camera_feedbackBg);
    cameraScreenCustomization.feedbackTextColor = getResources().getColor(R.color.fm_camera_feedbackText);
    cameraScreenCustomization.feedbackTextSize = 18;
    cameraScreenCustomization.feedBackframeMessage = "Frame Your Face";
    cameraScreenCustomization.feedBackAwayMessage = "Move Phone Away";
    cameraScreenCustomization.feedBackOpenEyesMessage = "Keep Your Eyes Open";
    cameraScreenCustomization.feedBackCloserMessage = "Move Phone Closer";
    cameraScreenCustomization.feedBackCenterMessage = "Move Phone Center";
    cameraScreenCustomization.feedBackMultipleFaceMessage = "Multiple Face Detected";
    cameraScreenCustomization.feedBackHeadStraightMessage = "Keep Your Head Straight";
    cameraScreenCustomization.feedBackBlurFaceMessage = "Blur Detected Over Face";
    cameraScreenCustomization.feedBackGlareFaceMessage = "Glare Detected";

    // 0 for clean face and 100 for Blurry face or set it -1 to remove blur filter
    cameraScreenCustomization.setBlurPercentage(80/*blurPercentage*/); // To allow blur on face
                                                    
    // Set min and max percentage for glare or set it -1 to remove glare filter
	cameraScreenCustomization.setGlarePercentage(6/*glareMinPercentage*/, 99/*glareMaxPercentage*/);
    
    Intent intent = SelfieCameraActivity.getFaceMatchCameraIntent(this, cameraScreenCustomization);
    startActivityForResult(intent, ACCURA_FACEMATCH_CAMERA);
    
    // Handle accura fm camera result.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ACCURA_LIVENESS_CAMERA && data != null) {
                AccuraFMCameraModel result = data.getParcelableExtra("Accura.fm");
                if (result == null) {
                    return;
                }
                if (result.getStatus().equals("1")) {
                    // result bitmap
                    Bitmap bitmap = result.getFaceBiometrics();
                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed" + result.getStatus(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

#### Step 3 : Implement face match code manually to your activity.

    Important Grant Camera and storage Permission.

    must have to implements FaceCallback, FaceHelper.FaceMatchCallBack to your activity
    ImageView image1,image2;

    // Initialized facehelper in onCreate.
    FaceHelper helper = new FaceHelper(this);

    TextView tvFaceMatch = findViewById(R.id.tvFM);
    tvFaceMatch.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
            ********** For faceMatch
            
            // To pass two image uri for facematch.
            // @params uri1 is for input image
            // @params uri2 is for match image
            
            helper.getFaceMatchScore(uri1, uri2);


            // also use some other method for for face match
            // must have to helper.setInputImage first and then helper.setMatchImage
            // helper.setInputImage(uri1);
            // helper.setMatchImage(uri2);
        
        }
    });
    // Override methods of FaceMatchCallBack

    @Override
    public void onFaceMatch(float score) {
        // get face match score
        System.out.println("Match Score : " + ss + " %");
    }

    @Override
    public void onSetInputImage(Bitmap src1) {
        // set Input image to your view
        image1.setImageBitmap(src1);
    }

    @Override
    public void onSetMatchImage(Bitmap src2) {
        // set Match image to your view
        image2.setImageBitmap(src2);
    }

    // Override methods for FaceCallback

    @Override
    public void onInitEngine(int ret) {
    }

    //call if face detect
    
    @Override
    public void onLeftDetect(FaceDetectionResult faceResult) {
        // must have to call helper method onLeftDetect(faceResult) to get faceMatch score.
        helper.onLeftDetect(faceResult);
    }

    //call if face detect
    @Override
    public void onRightDetect(FaceDetectionResult faceResult) {
        // must have to call helper method onRightDetect(faceResult) to get faceMatch score.
        helper.onRightDetect(faceResult);
    }

    @Override
    public void onExtractInit(int ret) {
    }

    And take a look ActivityFaceMatch.java for full working example.
    
#### Step 4 : Simple Usage to face match in your app.

    // Just add FaceMatchActivity to your manifest:
    <activity android:name="com.accurascan.facematch.ui.FaceMatchActivity"/>

    // Start Intent to open activity
    Intent intent = new Intent(this, FaceMatchActivity.class);
    startActivity(intent);

    Or follow the Manifest File of Demo Project to use FaceMatch.


