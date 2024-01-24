package com.hangyeolee.androidpdfwriter;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import com.hangyeolee.androidpdfwriter.components.PDFH1;
import com.hangyeolee.androidpdfwriter.components.PDFH3;
import com.hangyeolee.androidpdfwriter.components.PDFImage;
import com.hangyeolee.androidpdfwriter.components.PDFLinearLayout;
import com.hangyeolee.androidpdfwriter.utils.DPI;
import com.hangyeolee.androidpdfwriter.utils.Fit;
import com.hangyeolee.androidpdfwriter.utils.Orientation;
import com.hangyeolee.androidpdfwriter.utils.Paper;
import com.hangyeolee.androidpdfwriter.utils.StandardDirectory;
import com.hangyeolee.androidpdfwriter.utils.TextAlign;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.InputStream;

@RunWith(AndroidJUnit4.class)
public class PDFBuilderTest {
    Context context;
    PDFBuilder<PDFLinearLayout> builder;

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Before
    public void setUp(){
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        InputStream stream = InstrumentationRegistry.getInstrumentation().getContext().getResources().openRawResource(
                com.hangyeolee.androidpdfwriter.test.R.drawable.sample_image);
        Bitmap b = BitmapFactory.decodeStream(stream);

        builder = new PDFBuilder<>(Paper.A4);
        builder.setQuality(100)
                .setDPI(DPI.Standard)
                .setDPI(1.0f);
        builder.root = PDFLinearLayout.build()
                .setOrientation(Orientation.Column)
                .setPadding(10,10,10,10)
                .setBackgroundColor(Color.BLUE)
                .addChild(PDFH1.build("제목")
                        .setBackgroundColor(Color.WHITE)
                        .setTextAlign(TextAlign.Center))
                .addChild(PDFLinearLayout.build()
                        .setOrientation(Orientation.Row)
                        .setMargin(10, 10, 10, 10)
                        .setBackgroundColor(Color.WHITE)
                        .setBorder(border -> border
                                .setLeft(4, Color.BLACK)
                                .setTop(4, Color.RED)
                                .setRight(4, Color.GREEN)
                                .setBottom(4, Color.MAGENTA)
                        )
                        .addChild(PDFH3.build("번호"))
                        .addChild(PDFH3.build("이름")
                                .setBackgroundColor(Color.YELLOW)
                                .setTextAlign(TextAlign.Center))
                        .addChild(PDFH3.build("내용")
                                .setBackgroundColor(Color.BLACK)
                                .setTextColor(Color.WHITE)
                                .setTextAlign(TextAlign.Center))
                )
                .addChild(PDFImage.build(b)
                        .setFit(Fit.CONTAIN));
    }

    @Test
    public void testSave() {
        builder.draw();
        Uri uri = builder.save(context, StandardDirectory.DIRECTORY_DOWNLOADS , "result.pdf");
    }
}