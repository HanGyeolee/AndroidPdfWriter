package com.hangyeolee.pdf.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import com.hangyeolee.pdf.core.utils.Anchor;
import com.hangyeolee.pdf.core.utils.Orientation;
import com.hangyeolee.pdf.core.utils.Paper;
import com.hangyeolee.pdf.core.utils.StandardDirectory;
import com.hangyeolee.pdf.core.utils.TextAlign;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class PDFTextTest {
    private final String TAG = "TEST";
    Context context;
    Bitmap b = null;

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Before
    public void setUp(){
        PDFBuilder.DEBUG = true;

        Log.d(TAG, "Starting test setup...");

        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Log.d(TAG, "Context initialized");
    }

    @Test
    public void testLinearLayoutNText() {
        PDFBuilder builder = new PDFBuilder(Paper.A4);
        builder.setQuality(85);
        builder.setPagePadding(10, 10);
        builder.root = PDFLinearLayout.build(Orientation.Vertical)
                .setBackgroundColor(Color.TRANSPARENT)
                .addChild(PDFH1.build("Title")
                        //.setFontFromAsset(context, "Pretendard-Bold.ttf")
//                        .setBackgroundColor(Color.WHITE)
                        .setTextAlign(TextAlign.Center)
                        .setAnchor(Anchor.Center, Anchor.Center));
        Log.d(TAG, "PDF Builder setup completed");
        builder.draw();
        Log.d(TAG, "builder draw");
        Uri uri = builder.save(context, StandardDirectory.DIRECTORY_DOWNLOADS , "test_Text.pdf");
        Log.d(TAG, "builder save");

        assertNotNull("Generated PDF URI should not be null", uri);
    }

    @Test
    public void testKoreanText() {
        PDFBuilder builder = new PDFBuilder(Paper.A4);
        builder.setQuality(85);
        builder.setPagePadding(10, 10);
        builder.root = PDFLinearLayout.build(Orientation.Vertical)
                .setBackgroundColor(Color.TRANSPARENT)
                .addChild(PDFH1.build("다람쥐 헌 쳇바퀴에 타고파.1234567890")
                        .setTextColor(Color.BLACK)
                        .setFontFromAsset(context, "Pretendard-Bold.ttf")
                        .setTextAlign(TextAlign.Center)
                        .setAnchor(Anchor.Center, Anchor.Center));
        Log.d(TAG, "PDF Builder setup completed");
        builder.draw();
        Log.d(TAG, "builder draw");
        Uri uri = builder.save(context, StandardDirectory.DIRECTORY_DOWNLOADS , "test_KoreanText_subset.pdf");
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