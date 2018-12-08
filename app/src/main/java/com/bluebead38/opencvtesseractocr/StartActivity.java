package com.bluebead38.opencvtesseractocr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class StartActivity extends Activity {

        @Override
        protected void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);

            try {
                Thread.sleep(3000);

            }catch (InterruptedException e){
                e.printStackTrace();
            }
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }

}
