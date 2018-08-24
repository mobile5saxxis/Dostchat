package com.dostchat.dost.fragments.home;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.cameraview.AspectRatio;
import com.google.android.cameraview.CameraView;
import com.dostchat.dost.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import java.util.Set;

public class CameraFragment extends Fragment implements
        ActivityCompat.OnRequestPermissionsResultCallback,
        AspectRatioFragment.Listener {
    private static final String TAG = "MainActivity";

    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private static final String FRAGMENT_DIALOG = "dialog";

    private static final int[] FLASH_OPTIONS = {
            CameraView.FLASH_AUTO,
            CameraView.FLASH_OFF,
            CameraView.FLASH_ON,
    };

    private static final int[] FLASH_ICONS = {
            R.drawable.ic_flash_auto,
            R.drawable.ic_flash_off,
            R.drawable.ic_flash_on,
    };

    private static final int[] FLASH_TITLES = {
            R.string.flash_auto,
            R.string.flash_off,
            R.string.flash_on,
    };

    private int mCurrentFlash;

    private CameraView mCameraView;

    private Handler mBackgroundHandler;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.take_picture:
                    if (mCameraView != null) {
                        mCameraView.takePicture();
                    }
                    break;
            }
        }
    };

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, null, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCameraView = (CameraView) view.findViewById(R.id.camera);

        if (mCameraView != null) {
            mCameraView.addCallback(mCallback);
        }

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.take_picture);

        if (fab != null) {
            fab.setOnClickListener(mOnClickListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            mCameraView.start();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.CAMERA)) {
            ConfirmationDialogFragment.newInstance(R.string.camera_permission_confirmation,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION, R.string.camera_permission_not_granted)
                    .show(getActivity().getSupportFragmentManager(), FRAGMENT_DIALOG);
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onPause() {
        mCameraView.stop();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBackgroundHandler != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mBackgroundHandler.getLooper().quitSafely();
            } else {
                mBackgroundHandler.getLooper().quit();
            }
            mBackgroundHandler = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (permissions.length != 1 || grantResults.length != 1) {
                    throw new RuntimeException("Error on requesting camera permission.");
                }
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getContext(), R.string.camera_permission_not_granted,
                            Toast.LENGTH_SHORT).show();
                }
                // No need to start camera here; it is handled by onResume
                break;
        }
    }

    @Override
    public void onAspectRatioSelected(@NonNull AspectRatio ratio) {
        if (mCameraView != null) {
            Toast.makeText(getContext(), ratio.toString(), Toast.LENGTH_SHORT).show();
            mCameraView.setAspectRatio(ratio);
        }
    }

    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }

    private CameraView.Callback mCallback
            = new CameraView.Callback() {

        @Override
        public void onCameraOpened(CameraView cameraView) {
        }

        @Override
        public void onCameraClosed(CameraView cameraView) {
        }

        @Override
        public void onPictureTaken(CameraView cameraView, final byte[] data) {
            Toast.makeText(cameraView.getContext(), R.string.picture_taken, Toast.LENGTH_SHORT)
                    .show();
            getBackgroundHandler().post(new Runnable() {
                @Override
                public void run() {
                    String root = Environment.getExternalStorageDirectory().toString();
                    File myDir = new File(root + "/DostChat/DostChat images");
                    if (!myDir.exists()) {
                        myDir.mkdirs();
                    }
                    Random generator = new Random();
                    int n = 10000;
                    n = generator.nextInt(n);
                    String fname = "Image-"+ n +".jpg";
                    File file = new File (myDir, fname);
                    if (file.exists ())
                        file.delete ();
                    try {
                        FileOutputStream os = new FileOutputStream(file);
                        os.write(data);
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    };

    public void setAspectRatio() {
        FragmentManager fragmentManager = getActivity().getFragmentManager();
        if (mCameraView != null
                && fragmentManager.findFragmentByTag(FRAGMENT_DIALOG) == null) {
            final Set<AspectRatio> ratios = mCameraView.getSupportedAspectRatios();
            final AspectRatio currentRatio = mCameraView.getAspectRatio();
            AspectRatioFragment aspectRatioFragment=  AspectRatioFragment.newInstance(ratios, currentRatio);
            aspectRatioFragment.setListener(this);
            aspectRatioFragment.show(fragmentManager, FRAGMENT_DIALOG);
        }
    }

    public void setFlash(MenuItem item) {
        if (mCameraView != null) {
            mCurrentFlash = (mCurrentFlash + 1) % FLASH_OPTIONS.length;
            item.setTitle(FLASH_TITLES[mCurrentFlash]);
            item.setIcon(FLASH_ICONS[mCurrentFlash]);
            mCameraView.setFlash(FLASH_OPTIONS[mCurrentFlash]);
        }
    }

    public void switchCamera() {
        if (mCameraView != null) {
            int facing = mCameraView.getFacing();
            mCameraView.setFacing(facing == CameraView.FACING_FRONT ?
                    CameraView.FACING_BACK : CameraView.FACING_FRONT);
        }
    }

    public static class ConfirmationDialogFragment extends DialogFragment {

        private static final String ARG_MESSAGE = "message";
        private static final String ARG_PERMISSIONS = "permissions";
        private static final String ARG_REQUEST_CODE = "request_code";
        private static final String ARG_NOT_GRANTED_MESSAGE = "not_granted_message";

        public static ConfirmationDialogFragment newInstance(@StringRes int message,
                                                             String[] permissions, int requestCode, @StringRes int notGrantedMessage) {
            ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_MESSAGE, message);
            args.putStringArray(ARG_PERMISSIONS, permissions);
            args.putInt(ARG_REQUEST_CODE, requestCode);
            args.putInt(ARG_NOT_GRANTED_MESSAGE, notGrantedMessage);
            fragment.setArguments(args);
            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Bundle args = getArguments();
            return new AlertDialog.Builder(getActivity())
                    .setMessage(args.getInt(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String[] permissions = args.getStringArray(ARG_PERMISSIONS);
                                    if (permissions == null) {
                                        throw new IllegalArgumentException();
                                    }
                                    ActivityCompat.requestPermissions(getActivity(),
                                            permissions, args.getInt(ARG_REQUEST_CODE));
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(getActivity(),
                                            args.getInt(ARG_NOT_GRANTED_MESSAGE),
                                            Toast.LENGTH_SHORT).show();
                                }
                            })
                    .create();
        }

    }

}

