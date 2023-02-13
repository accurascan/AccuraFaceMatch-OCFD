package com.accurascan.accura.kyc;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.accurascan.facedetection.LivenessCustomization;
import com.accurascan.facedetection.SelfieCameraActivity;
import com.accurascan.facedetection.model.AccuraVerificationResult;
import com.accurascan.facematch.util.BitmapHelper;
import com.accurascan.ocr.mrz.model.RecogResult;
import com.bumptech.glide.Glide;
import com.docrecog.scan.RecogType;
import com.inet.facelock.callback.FaceCallback;
import com.inet.facelock.callback.FaceDetectionResult;
import com.inet.facelock.callback.FaceHelper;

public class OcrResultActivity extends BaseActivity implements FaceHelper.FaceMatchCallBack, FaceCallback {

    private final int ACCURA_LIVENESS_CAMERA = 101;
    private final int ACCURA_FACEMATCH_CAMERA = 102;
    Bitmap face1;
    TableLayout mrz_table_layout, front_table_layout, back_table_layout, usdl_table_layout, pdf417_table_layout, bank_table_layout;

    ImageView ivUserProfile, ivUserProfile2, iv_frontside, iv_backside;
    LinearLayout ly_back, ly_front;
    View ly_auth_container, ly_mrz_container, ly_front_container, ly_back_container, ly_security_container,
            ly_pdf417_container, ly_usdl_container, dl_plate_lout, ly_bank_container, ly_barcode_container, ly_checkbox_container;
    View loutImg, loutImg2, loutFaceImageContainer;
    private FaceHelper faceHelper;
    private TextView tvFaceMatchScore, tvLivenessScore, tv_security;
    private boolean isFaceMatch = false, isLiveness = false;

    protected void onCreate(Bundle savedInstanceState) {
        if (getIntent().getIntExtra("app_orientation", 1) != 0) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_result);

        initUI();
        RecogType recogType = RecogType.detachFrom(getIntent());

         if (recogType == RecogType.MRZ) {
            // RecogType.MRZ
            RecogResult g_recogResult = RecogResult.getRecogResult();
            if (g_recogResult != null) {
                setMRZData(g_recogResult);

                if (g_recogResult.docFrontBitmap != null) {
                    iv_frontside.setImageBitmap(g_recogResult.docFrontBitmap);
                } else {
                    ly_front.setVisibility(View.GONE);
                }

                if (g_recogResult.docBackBitmap != null) {
                    iv_backside.setImageBitmap(g_recogResult.docBackBitmap);
                } else {
                    ly_back.setVisibility(View.GONE);
                }


                if (g_recogResult.faceBitmap != null) {
                    face1 = g_recogResult.faceBitmap;
                }
            }
            setData();
        }

    }
    CheckBox cb_image,cb_video,cb_face,cb_fm;
    boolean isVideoRecording = true;
    private void initUI() {
        //initialize the UI
        ivUserProfile = findViewById(R.id.ivUserProfile);
        ivUserProfile2 = findViewById(R.id.ivUserProfile2);
        loutFaceImageContainer = findViewById(R.id.lyt_face_image_container);
        loutImg = findViewById(R.id.lyt_img_cover);
        loutImg2 = findViewById(R.id.lyt_img_cover2);
        tvLivenessScore = findViewById(R.id.tvLivenessScore);
        tvFaceMatchScore = findViewById(R.id.tvFaceMatchScore);
        loutImg2.setVisibility(View.GONE);

        tv_security = findViewById(R.id.tv_security);

        ly_back = findViewById(R.id.ly_back);
        ly_front = findViewById(R.id.ly_front);
        iv_frontside = findViewById(R.id.iv_frontside);
        iv_backside = findViewById(R.id.iv_backside);

        mrz_table_layout = findViewById(R.id.mrz_table_layout);
        front_table_layout = findViewById(R.id.front_table_layout);
        back_table_layout = findViewById(R.id.back_table_layout);
        pdf417_table_layout = findViewById(R.id.pdf417_table_layout);
        usdl_table_layout = findViewById(R.id.usdl_table_layout);
        bank_table_layout = findViewById(R.id.bank_table_layout);

        ly_auth_container = findViewById(R.id.layout_button_auth);
        ly_mrz_container = findViewById(R.id.ly_mrz_container);
        ly_front_container = findViewById(R.id.ly_front_container);
        ly_back_container = findViewById(R.id.ly_back_container);
        ly_security_container = findViewById(R.id.ly_security_container);
        ly_pdf417_container = findViewById(R.id.ly_pdf417_container);
        ly_usdl_container = findViewById(R.id.ly_usdl_container);
        dl_plate_lout = findViewById(R.id.dl_plate_lout);
        ly_bank_container = findViewById(R.id.ly_bank_container);
        ly_barcode_container = findViewById(R.id.ly_barcode_container);

        tvFaceMatchScore.setVisibility(View.GONE);
        tvLivenessScore.setVisibility(View.GONE);
        ly_security_container.setVisibility(View.GONE);
        ly_front_container.setVisibility(View.GONE);
        ly_back_container.setVisibility(View.GONE);
        ly_mrz_container.setVisibility(View.GONE);
        ly_pdf417_container.setVisibility(View.GONE);
        ly_usdl_container.setVisibility(View.GONE);
        dl_plate_lout.setVisibility(View.GONE);
        ly_bank_container.setVisibility(View.GONE);
        ly_barcode_container.setVisibility(View.GONE);
        ly_auth_container.setVisibility(View.GONE);

        ly_checkbox_container = findViewById(R.id.ly_checkbox_container);
        ly_checkbox_container.setVisibility(View.GONE);
        cb_video = findViewById(R.id.cb_video);
        cb_image = findViewById(R.id.cb_image);
        cb_face = findViewById(R.id.cb_face);
        cb_fm = findViewById(R.id.cb_fm);
        cb_video.setChecked(true);
        cb_image.setChecked(true);
        cb_face.setChecked(true);
        cb_fm.setChecked(true);
    }


    private void updateSecurityLayout(String s) {
        boolean isVerified = Boolean.parseBoolean(s);
        if (isVerified) {
            tv_security.setText("YES");
            tv_security.setTextColor(getResources().getColor(R.color.security_true));
        } else {
            tv_security.setTextColor(getResources().getColor(R.color.security_false));
            tv_security.setText("NO");
        }
        ly_security_container.setVisibility(View.VISIBLE);
    }

    private void addBankLayout(String key, String s) {
        if (TextUtils.isEmpty(s)) return;
        View layout1 = LayoutInflater.from(OcrResultActivity.this).inflate(R.layout.table_row, null);
        TextView tv_key1 = layout1.findViewById(R.id.tv_key);
        TextView tv_value1 = layout1.findViewById(R.id.tv_value);
        tv_key1.setText(key);
        tv_value1.setText(s);
        bank_table_layout.addView(layout1);
    }

    private void setMRZData(RecogResult recogResult) {

        ly_mrz_container.setVisibility(View.VISIBLE);
        try {
            addLayout("MRZ", recogResult.lines);
            addLayout("Document Type", recogResult.docType);
            addLayout("First Name", recogResult.givenname);
            addLayout("Last Name", recogResult.surname);
            addLayout("Document No.", recogResult.docnumber);
            addLayout("Document check No.", recogResult.docchecksum);
            addLayout("Correct Document check No.", recogResult.correctdocchecksum);
            addLayout("Country", recogResult.country);
            addLayout("Nationality", recogResult.nationality);
            String s = (recogResult.sex.equals("M")) ? "Male" : ((recogResult.sex.equals("F")) ? "Female" : recogResult.sex);
            addLayout("Sex", s);
            addLayout("Date of Birth", recogResult.birth);
            addLayout("Birth Check No.", recogResult.birthchecksum);
            addLayout("Correct Birth Check No.", recogResult.correctbirthchecksum);
            addLayout("Date of Expiry", recogResult.expirationdate);
            addLayout("Expiration Check No.", recogResult.expirationchecksum);
            addLayout("Correct Expiration Check No.", recogResult.correctexpirationchecksum);
            addLayout("Date Of Issue", recogResult.issuedate);
            addLayout("Department No.", recogResult.departmentnumber);
            addLayout("Other ID", recogResult.otherid);
            addLayout("Other ID Check", recogResult.otheridchecksum);
            addLayout("Other ID2", recogResult.otherid2);
            addLayout("Second Row Check No.", recogResult.secondrowchecksum);
            addLayout("Correct Second Row Check No.", recogResult.correctsecondrowchecksum);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addLayout(String key, String s) {
        if (TextUtils.isEmpty(s)) return;
        View layout1 = LayoutInflater.from(OcrResultActivity.this).inflate(R.layout.table_row, null);
        TextView tv_key1 = layout1.findViewById(R.id.tv_key);
        TextView tv_value1 = layout1.findViewById(R.id.tv_value);
        tv_key1.setText(key);
        tv_value1.setText(s);
        mrz_table_layout.addView(layout1);
    }

    private void setData() {
        if (face1 != null) {
            Glide.with(this).load(face1).centerCrop().into(ivUserProfile);
            ivUserProfile.setVisibility(View.VISIBLE);
        } else {
//            ivUserProfile.setVisibility(View.GONE);
//            loutFaceImageContainer.setVisibility(View.GONE);
        }
        ly_auth_container.setVisibility(View.VISIBLE);
        ly_checkbox_container.setVisibility(View.VISIBLE);
    }

    public void handleVerificationSuccessResult(final AccuraVerificationResult result) {
        if (result != null) {
//            showProgressDialog();
            Runnable runnable = new Runnable() {
                public void run() {

                    if (face1 != null) {
                        faceHelper.setInputImage(face1);
                    }

                    if (result.getFaceBiometrics() != null) {
                        if (result.getLivenessResult() == null) {
                            return;
                        }
                        if (result.getLivenessResult().getLivenessStatus()) {
                            Bitmap face2 = result.getFaceBiometrics();
                            if (face2 != null) {
//                                    Glide.with(OcrResultActivity.this).load(face2).centerCrop().into(ivUserProfile2);
                                faceHelper.setMatchImage(face2);
                            }
//                                Toast.makeText(OcrResultActivity.this, result.getVideoPath()+"", Toast.LENGTH_SHORT).show();
                            setLivenessData(result.getLivenessResult().getLivenessScore() * 100 + "");

//                                tvFaceMatchScore.setText(String.format(getString(R.string.score_formate), result.getFaceMatchScore()));
//                                if (result.getFaceResult() != null) {
//                                    try {
//                                        FaceDetectionResult faceDetectionResult = result.getFaceResult();
//                                        Bitmap face2 = BitmapHelper.createFromARGB(faceDetectionResult.getNewImg(), faceDetectionResult.getNewWidth(), faceDetectionResult.getNewHeight());
//                                        Glide.with(OcrResultActivity.this).load(faceDetectionResult.getFaceImage(face2)).centerCrop().into(ivUserProfile2);
//                                        loutImg2.setVisibility(View.VISIBLE);
//                                    } catch (Exception e) {
//                                        e.printStackTrace();
//                                    }
//                                }
                        }
                    }


                }
            };
            new Handler().postDelayed(runnable, 100);
        }
    }

    public void handleVerificationSuccessResultFM(final AccuraVerificationResult result) {
        if (result != null) {
//            showProgressDialog();
            Runnable runnable = new Runnable() {
                public void run() {
                    setLivenessData("0.00");
                    if (faceHelper!=null && face1 != null) {
                        faceHelper.setInputImage(face1);
                    }

                    if (result.getFaceBiometrics() != null) {
                        Bitmap nBmp = result.getFaceBiometrics();
                        faceHelper.setMatchImage(nBmp);
                    }
                }
            };
            new Handler().postDelayed(runnable, 100);
        }
    }

    //method for setting liveness data
    //parameter to pass : livenessScore
    private void setLivenessData(String livenessScore) {
        tvLivenessScore.setText(String.format("%s %%", livenessScore.length() > 5 ? livenessScore.substring(0, 5) : livenessScore));
        tvLivenessScore.setVisibility(View.VISIBLE);
        tvFaceMatchScore.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ACCURA_LIVENESS_CAMERA && data != null) {
                AccuraVerificationResult result = data.getParcelableExtra("Accura.liveness");
                if (result == null) {
                    return;
                }
                if (result.getStatus().equals("1")) {
                    handleVerificationSuccessResult(result);
                    Uri videoPath = result.getVideoPath();
                    if (videoPath != null) {
                        Log.e(OcrResultActivity.class.getSimpleName(), "Video Path: " + videoPath.getPath());
                    }
                    Uri imagePath = result.getImagePath();
                    if (imagePath != null) {
                        Log.e(OcrResultActivity.class.getSimpleName(), "Image Path: " + imagePath.getPath());
                    }
                } else {
                    if (result.getVideoPath() != null) {
                        Toast.makeText(this, "Video Path : " + result.getVideoPath() + "\n" + result.getErrorMessage(), Toast.LENGTH_LONG).show();
                        isVideoRecording = false;
                        cb_video.setChecked(false);
                    } else
                        Toast.makeText(this, result.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == 102) {
                AccuraVerificationResult result = data.getParcelableExtra("Accura.fm");
                if (result == null) {
                    return;
                }
                if (result.getStatus().equals("1")) {
                    handleVerificationSuccessResultFM(result);
                } else {
                    Toast.makeText(this, "Retry...", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (faceHelper != null) {
            faceHelper.closeEngine();
        }
        Runtime.getRuntime().gc();
    }

    @Override
    public void onBackPressed() {

        if (RecogType.detachFrom(getIntent()) == RecogType.MRZ && RecogResult.getRecogResult() != null) {
            try {
                RecogResult.getRecogResult().docFrontBitmap.recycle();
                RecogResult.getRecogResult().faceBitmap.recycle();
                RecogResult.getRecogResult().docBackBitmap.recycle();
            } catch (Exception e) {
            }
        }



        RecogResult.setRecogResult(null);
        //</editor-fold>

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setResult(RESULT_OK);
        finish();
    }



    public void onCLickFaceMatch(View view) {
        if (view.getId() == R.id.btn_fm) {
            isFaceMatch = true;
            isLiveness = false;
        } else if (view.getId() == R.id.btn_liveness) {
            isFaceMatch = false;
            isLiveness = true;
        }
        if (faceHelper == null) {
            faceHelper = new FaceHelper(this);
        } else {
            performClick(isFaceMatch, isLiveness);
        }

    }

    private void performClick(boolean isFaceMatch, boolean isLiveness) {
        if (isFaceMatch) openCamera();
        else if (isLiveness) openLivenessCamera();
    }

    private void openLivenessCamera() {
        LivenessCustomization livenessCustomization = new LivenessCustomization();

        livenessCustomization.backGroundColor = getResources().getColor(R.color.livenessBackground);
        livenessCustomization.closeIconColor = getResources().getColor(R.color.livenessCloseIcon);
        livenessCustomization.feedbackBackGroundColor = getResources().getColor(R.color.livenessfeedbackBg);
        livenessCustomization.feedbackTextColor = getResources().getColor(R.color.livenessfeedbackText);
        livenessCustomization.feedbackTextSize = 18;
        livenessCustomization.feedBackframeMessage = "Frame Your Face";
        livenessCustomization.feedBackAwayMessage = "Move Phone Away";
        livenessCustomization.feedBackOpenEyesMessage = "Keep Your Eyes Open";
        livenessCustomization.feedBackCloserMessage = "Move Phone Closer";
        livenessCustomization.feedBackCenterMessage = "Move Phone Center";
        livenessCustomization.feedBackMultipleFaceMessage = "Multiple Face Detected";
        livenessCustomization.feedBackHeadStraightMessage = "Keep Your Head Straight";
        livenessCustomization.feedBackLowLightMessage = "Low light detected";
        livenessCustomization.feedBackBlurFaceMessage = "Blur Detected Over Face";
        livenessCustomization.feedBackGlareFaceMessage = "Glare Detected";
        livenessCustomization.feedBackProcessingMessage = "Processing..";


//        livenessCustomization.feedBackVideoRecordingMessage = "Processing...";
        livenessCustomization.setLowLightTolerence(39);
        livenessCustomization.setBlurPercentage(80);
        livenessCustomization.setGlarePercentage(-1, -1);

        livenessCustomization.isSaveImage = cb_image.isChecked();
        // video length in seconds
//        livenessCustomization.isRecordVideo = cb_video.isChecked();
//        livenessCustomization.videoLengthInSecond = 5;
//        livenessCustomization.recordingTimerTextColor = getResources().getColor(R.color.livenessRecordingText);
//        livenessCustomization.recordingTimerTextSize = 45;
//        livenessCustomization.recordingMessage = "Scanning your face be steady";
//        livenessCustomization.recordingMessageTextColor = getResources().getColor(R.color.livenessRecordingText);
//        livenessCustomization.recordingMessageTextSize = 18;
//        livenessCustomization.enableFaceDetect = cb_face.isChecked();
//        livenessCustomization.fmScoreThreshold = 50;
//        livenessCustomization.enableFaceMatch = cb_fm.isChecked();
//        livenessCustomization.feedbackFMFailed = "Face not matched";

        livenessCustomization.feedBackStartMessage = getString(R.string.inside_oval);
        livenessCustomization.feedBackLookLeftMessage = getString(R.string.left_info);
        livenessCustomization.feedBackLookRightMessage = getString(R.string.right_info);
        livenessCustomization.feedBackOralInfoMessage = getString(R.string.oral_info);

        livenessCustomization.enableOralVerification = cb_video.isChecked();
        livenessCustomization.livenessAlertSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/raw/accura_liveness_verified");
        livenessCustomization.livenessVerifiedAlertSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/raw/accura_liveness_verified");
        livenessCustomization.livenessVerifiedAnimation = R.drawable.approved_sign;
        livenessCustomization.livenessLeftMoveAnimation = R.drawable.accura_liveness_face;
        livenessCustomization.livenessRightMoveAnimation = R.drawable.accura_liveness_face;
        livenessCustomization.voiceIcon = R.drawable.ic_mic;
        livenessCustomization.codeTextSize = 30;
        livenessCustomization.codeTextColor = Color.WHITE;

        Intent intent = SelfieCameraActivity.getLivenessCameraIntent(this, livenessCustomization, "your liveness url");
        startActivityForResult(intent, ACCURA_LIVENESS_CAMERA);
    }

    private void openCamera() {

        LivenessCustomization cameraScreenCustomization = new LivenessCustomization();

        cameraScreenCustomization.backGroundColor = getResources().getColor(R.color.camera_Background);
        cameraScreenCustomization.closeIconColor = getResources().getColor(R.color.camera_CloseIcon);
        cameraScreenCustomization.feedbackBackGroundColor = getResources().getColor(R.color.camera_feedbackBg);
        cameraScreenCustomization.feedbackTextColor = getResources().getColor(R.color.camera_feedbackText);
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
        cameraScreenCustomization.feedBackProcessingMessage = "Processing..";
        cameraScreenCustomization.feedbackDialogMessage = "Processing...";
        cameraScreenCustomization.setBlurPercentage(80);
        cameraScreenCustomization.setGlarePercentage(-1, -1);
        cameraScreenCustomization.showlogo=1;

       //cameraScreenCustomization.logoPath =R.drawable.test;

        Intent intent = SelfieCameraActivity.getFaceMatchCameraIntent(this, cameraScreenCustomization);
        startActivityForResult(intent, ACCURA_FACEMATCH_CAMERA);
    }

    @Override
    public void onFaceMatch(float score) {
        tvFaceMatchScore.setText(String.format(getString(R.string.score_formate), score));
        tvLivenessScore.setVisibility(View.VISIBLE);
        tvFaceMatchScore.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSetInputImage(Bitmap bitmap) {

    }

    @Override
    public void onSetMatchImage(Bitmap bitmap) {

    }

    @Override
    public void onInitEngine(int i) {
        Log.e(OcrResultActivity.class.getSimpleName(), "onInitEngine: " + i);
        if (i != -1) {
            performClick(isFaceMatch, isLiveness);
        }
    }

    @Override
    public void onLeftDetect(FaceDetectionResult faceDetectionResult) {
    }

    @Override
    public void onRightDetect(FaceDetectionResult faceDetectionResult) {
        if (faceDetectionResult != null) {
            try {
                Bitmap face2 = BitmapHelper.createFromARGB(faceDetectionResult.getNewImg(), faceDetectionResult.getNewWidth(), faceDetectionResult.getNewHeight());
                Glide.with(this).load(faceDetectionResult.getFaceImage(face2)).centerCrop().into(ivUserProfile2);
                loutImg2.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onExtractInit(int i) {

    }
}
