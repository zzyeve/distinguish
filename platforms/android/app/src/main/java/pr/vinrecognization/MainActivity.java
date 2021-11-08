package pr.vinrecognization;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.eastime.distinguish.R;

import pr.Constant;
import pr.vinrecognization.view.VinEditText;


public class MainActivity extends Activity {
   VinEditText et_vin;
    TextView tv_hint;
    Button vinOcr;

    private int REQUEST_VIN_OCR = 1;
    private int REQUEST_PERMISSION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initView();
//        new VinKeyboard(this, et_vin);
        initClick();
    }

    private void initView() {
        et_vin = findViewById(R.id.et_vin);
        tv_hint = findViewById(R.id.tv_hint);
        vinOcr = findViewById(R.id.vinOcr);
    }

    private void initClick() {
        et_vin.addTextChangedListener(new VinEditText.VinTextWatcher(et_vin, new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int length = s.length();
                tv_hint.setText("已输入${l}位，还差${17 - l}位");

                // todo something
            }
        }));

        vinOcr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                        ||ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                ){
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},
                            110);
                    return;
                }
                startOcr();
            }
        });
    }

    private void startOcr() {
        startActivityForResult(new Intent(this, VinOcrActivity.class), REQUEST_VIN_OCR);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 110) {
            if (grantResults.length > 0) {
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(MainActivity.this,"请开启权限",Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                startOcr();
                //其他逻辑(这里当权限都同意的话就执行打电话逻辑)
//                addCameraView(this);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;
        if (requestCode != REQUEST_VIN_OCR) return;
        String vin = data.getStringExtra(Constant.EXTRA_VIN_CODE);
        et_vin.setText(vin);
    }
}
