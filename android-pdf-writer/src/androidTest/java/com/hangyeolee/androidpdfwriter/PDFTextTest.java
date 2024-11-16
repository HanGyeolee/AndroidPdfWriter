package com.hangyeolee.androidpdfwriter;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import com.hangyeolee.androidpdfwriter.components.PDFH1;
import com.hangyeolee.androidpdfwriter.components.PDFH3;
import com.hangyeolee.androidpdfwriter.components.PDFImage;
import com.hangyeolee.androidpdfwriter.components.PDFLinearLayout;
import com.hangyeolee.androidpdfwriter.font.PDFFont;
import com.hangyeolee.androidpdfwriter.utils.Fit;
import com.hangyeolee.androidpdfwriter.utils.Orientation;
import com.hangyeolee.androidpdfwriter.utils.Paper;
import com.hangyeolee.androidpdfwriter.utils.StandardDirectory;
import com.hangyeolee.androidpdfwriter.utils.TextAlign;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.InputStream;

@RunWith(AndroidJUnit4.class)
public class PDFTextTest {
    private final String TAG = "TEST";
    Context context;
    PDFBuilder builder;
    Bitmap b;

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Before
    public void setUp(){
        Log.d(TAG, "Starting test setup...");

        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Log.d(TAG, "Context initialized");

        /*InputStream stream = InstrumentationRegistry.getInstrumentation().getContext().getResources().openRawResource(
                com.hangyeolee.androidpdfwriter.test.R.drawable.sample_image);
        Log.d(TAG, "Image stream opened");

        b = BitmapFactory.decodeStream(stream);
        Log.d(TAG, "Bitmap decoded: " + (b != null));*/

        builder = new PDFBuilder(Paper.A4);
        builder.setQuality(85);
        builder.setPagePadding(10, 10);
        builder.root = PDFLinearLayout.build()
                .setOrientation(Orientation.Column)
                .setBackgroundColor(Color.BLUE)
                .addChild(PDFH1.build("제목")
                        .setFontFromAsset(context, "Pretendard-Bold.ttf")
                        .setBackgroundColor(Color.WHITE)
                        .setTextAlign(TextAlign.Center));
        Log.d(TAG, "PDF Builder setup completed");
    }

    @Test
    public void testSave() {
        Log.d(TAG, "실행");
        builder.draw();
        Log.d(TAG, "builder draw");
        Uri uri = builder.save(context, StandardDirectory.DIRECTORY_DOWNLOADS , "result.pdf");
        Log.d(TAG, "builder save");

        assertNotNull("Generated PDF URI should not be null", uri);
    }

    @After
    public void tearDown(){
        if(b != null && !b.isRecycled()){
            b.recycle();
        }
    }
}