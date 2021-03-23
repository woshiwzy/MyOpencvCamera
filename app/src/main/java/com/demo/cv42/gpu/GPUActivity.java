package com.demo.cv42.gpu;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.demo.cv42.R;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageView;

public class GPUActivity extends Activity implements View.OnClickListener{

    private GPUImageView gpuimage;

    private ImageView resultIv;
    private Button btn1,btn2;
    private TextView countTv;
    private SeekBar seekbar;

    GPUImage gpuImage;
    Bitmap bitmap;

    private int i = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpu);

        btn1 = (Button)this.findViewById(R.id.btn1);
        btn2 = (Button)this.findViewById(R.id.btn2);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        resultIv = (ImageView)this.findViewById(R.id.resultIv);
        countTv = (TextView)this.findViewById(R.id.countTv);

        seekbar = (SeekBar)this.findViewById(R.id.seekbar);
        seekbar.setMax(10);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //调整饱和度亮度
                GPUImageUtil.changeSaturation(progress);
                resultIv.setImageBitmap(GPUImageUtil.getGPUImageFromAssets(getApplicationContext(),gpuImage,42));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        // 在ImageView中显示处理后的图像
        resultIv.setImageBitmap(GPUImageUtil.getGPUImageFromAssets(this,gpuImage,i));

    }

    /**
     * 左右按钮点击切换滤镜
     * @param v
     */
    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.btn1:
                i = i - 1;
                resultIv.setImageBitmap(GPUImageUtil.getGPUImageFromAssets(this,gpuImage,i));
                countTv.setText(i+"");
                break;
            case R.id.btn2:
                i = i + 1;
                resultIv.setImageBitmap(GPUImageUtil.getGPUImageFromAssets(this,gpuImage,i));
                countTv.setText(i+"");
                break;
        }
    }
}
