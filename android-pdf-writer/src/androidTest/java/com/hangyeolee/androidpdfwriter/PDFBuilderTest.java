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

import com.hangyeolee.androidpdfwriter.components.PDFGridLayout;
import com.hangyeolee.androidpdfwriter.components.PDFH1;
import com.hangyeolee.androidpdfwriter.components.PDFH2;
import com.hangyeolee.androidpdfwriter.components.PDFH3;
import com.hangyeolee.androidpdfwriter.components.PDFH4;
import com.hangyeolee.androidpdfwriter.components.PDFH5;
import com.hangyeolee.androidpdfwriter.components.PDFH6;
import com.hangyeolee.androidpdfwriter.components.PDFImage;
import com.hangyeolee.androidpdfwriter.components.PDFLinearLayout;
import com.hangyeolee.androidpdfwriter.components.PDFText;
import com.hangyeolee.androidpdfwriter.utils.Anchor;
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

import java.io.InputStream;

@RunWith(AndroidJUnit4.class)
public class PDFBuilderTest {
    private final String TAG = "TEST";
    Context context;
    Bitmap testImage;

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Before
    public void setUp(){
        PDFBuilder.DEBUG = true;

        Log.d(TAG, "Starting test setup...");

        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Log.d(TAG, "Context initialized");

        InputStream stream = InstrumentationRegistry.getInstrumentation().getContext().getResources().openRawResource(
                com.hangyeolee.androidpdfwriter.test.R.drawable.test);
        Log.d(TAG, "Image stream opened");

        testImage = BitmapFactory.decodeStream(stream);
        Log.d(TAG, "Bitmap decoded: " + (testImage != null));
    }

    @Test
    public void testImage(){
        PDFBuilder builder = new PDFBuilder(Paper.A4).setQuality(85).setPagePadding(30, 30);
        {
            builder.root = PDFLinearLayout.build(Orientation.Vertical)
                    .setSize(null, 300)
                    .setBackgroundColor(Color.BLUE)
                    .addChild(PDFImage.build(testImage)
                            .setCompress(true)
                            .setFit(Fit.CONTAIN)
                            .setAnchor(Anchor.Start, Anchor.Start)
                            .setBackgroundColor(Color.GRAY)
                            .setHeight(200.0f));
        }
        Log.d(TAG, "PDF Builder setup completed");

        Log.d(TAG, "실행");
        builder.draw();
        Log.d(TAG, "builder draw");
        Uri uri = builder.save(context, StandardDirectory.DIRECTORY_DOWNLOADS , "test_Image.pdf");
        Log.d(TAG, "builder save");

        assertNotNull("Generated PDF URI should not be null", uri);
    }

    @Test
    public void testLinearLayoutBiggerSave(){
        PDFBuilder builder = new PDFBuilder(Paper.A4).setQuality(85).setPagePadding(30, 30);
        {
            builder.root = PDFLinearLayout.build(Orientation.Vertical)
                    .setBackgroundColor(Color.GRAY)
                    .addChild(PDFH1.build("H1 Title Long Content")
                            .setBackgroundColor(Color.YELLOW)
                            .setTextAlign(TextAlign.Center))
                    .addChild(PDFLinearLayout.build(Orientation.Horizontal)
                            .setMargin(10, 10, 10, 10)
                            .setBackgroundColor(Color.WHITE)
                            .setBorder(4, Color.BLACK)
                            .addChild(PDFH2.build("H2 Name")
                                    .setBackgroundColor(Color.RED))
                            .addChild(PDFH3.build("H3 Glyph")
                                    .setBorder(border ->
                                            border.setLeft(4, Color.YELLOW)
                                            .setRight(4, Color.CYAN))
                                    .setBackgroundColor(Color.GREEN))
                            .addChild(PDFH4.build("H4 Content")
                                    .setTextColor(Color.WHITE)
                                    .setBackgroundColor(Color.BLUE))
                    )
                    .addChild(PDFLinearLayout.build(Orientation.Horizontal)
                            .setBackgroundColor(Color.LTGRAY)
                            .setSize(null, 300)
                            .addChild(PDFH5.build("H5 Image"))
                            .addChild(PDFImage.build(testImage)
                                    .setCompress(true)
                                    .setFit(Fit.NONE)
                                    .setHeight(520.0f)));
        }
        Log.d(TAG, "PDF Builder setup completed");

        Log.d(TAG, "실행");
        builder.draw();
        Log.d(TAG, "builder draw");
        Uri uri = builder.save(context, StandardDirectory.DIRECTORY_DOWNLOADS , "test_LinearLayout_300_520_None.pdf");
        Log.d(TAG, "builder save");

        assertNotNull("Generated PDF URI should not be null", uri);
    }

    @Test
    public void testLinearLayoutOver(){
        PDFBuilder builder = new PDFBuilder(Paper.A4).setQuality(85).setPagePadding(30, 30);
        {
            builder.root = PDFLinearLayout.build(Orientation.Vertical)
                    .setBackgroundColor(Color.GRAY)
                    .addChild(PDFH1.build("H1 Title Long Content")
                            .setBackgroundColor(Color.YELLOW)
                            .setTextAlign(TextAlign.Center))
                    .addChild(PDFLinearLayout.build(Orientation.Horizontal)
                            .setMargin(10, 10, 10, 10)
                            .setBackgroundColor(Color.WHITE)
                            .setBorder(4, Color.BLACK)
                            .addChild(PDFH2.build("H2 Name")
                                    .setBackgroundColor(Color.RED))
                            .addChild(PDFH3.build("H3 Glyph")
                                    .setBorder(border ->
                                            border.setLeft(4, Color.YELLOW)
                                                    .setRight(4, Color.CYAN))
                                    .setBackgroundColor(Color.GREEN))
                            .addChild(PDFH4.build("H4 Content")
                                    .setTextColor(Color.WHITE)
                                    .setBackgroundColor(Color.BLUE))
                    )
                    .addChild(PDFLinearLayout.build(Orientation.Horizontal)
                            .setBackgroundColor(Color.LTGRAY)
                            .setSize(null, 200)
                            .addChild(PDFH5.build("H5 Image")
                                    .setPadding(4)
                                    .setBackgroundColor(Color.WHITE)
                                    .setSize(60, null)
                                    .setAnchor(Anchor.Center, Anchor.Center)
                                    .setTextAlign(TextAlign.End))
                            .addChild(PDFImage.build(testImage)
                                    .setCompress(true)
                                    .setFit(Fit.CONTAIN)
                                    .setAnchor(Anchor.Start, Anchor.Center)
                                    .setHeight(50.0f)))
                    .addChild(PDFH1.build("ABCDEFGHIJKLMNOPQRSTUVWXYZ"))
                    .addChild(PDFH2.build("ABCDEFGHIJKLMNOPQRSTUVWXYZ"))
                    .addChild(PDFH3.build("ABCDEFGHIJKLMNOPQRSTUVWXYZ"))
                    .addChild(PDFH4.build("ABCDEFGHIJKLMNOPQRSTUVWXYZ"))
                    .addChild(PDFH5.build("ABCDEFGHIJKLMNOPQRSTUVWXYZ"))
                    .addChild(PDFH6.build("ABCDEFGHIJKLMNOPQRSTUVWXYZ"))
                    .addChild(PDFH1.build("abcdefghijklmnopqrstuvwxyz"))
                    .addChild(PDFH2.build("abcdefghijklmnopqrstuvwxyz"))
                    .addChild(PDFH3.build("abcdefghijklmnopqrstuvwxyz"))
                    .addChild(PDFH4.build("abcdefghijklmnopqrstuvwxyz"))
                    .addChild(PDFH5.build("abcdefghijklmnopqrstuvwxyz"))
                    .addChild(PDFH6.build("abcdefghijklmnopqrstuvwxyz"))
                    .addChild(PDFH1.build("ABCDEFGHIJKLMNOPQRSTUVWXYZ"))
                    .addChild(PDFH2.build("ABCDEFGHIJKLMNOPQRSTUVWXYZ"))
                    .addChild(PDFH3.build("ABCDEFGHIJKLMNOPQRSTUVWXYZ"))
                    .addChild(PDFH4.build("ABCDEFGHIJKLMNOPQRSTUVWXYZ"))
                    .addChild(PDFH5.build("ABCDEFGHIJKLMNOPQRSTUVWXYZ"))
                    .addChild(PDFH6.build("ABCDEFGHIJKLMNOPQRSTUVWXYZ"))
                    .addChild(PDFH1.build("abcdefghijklmnopqrstuvwxyz"))
                    .addChild(PDFH2.build("abcdefghijklmnopqrstuvwxyz"))
                    .addChild(PDFH3.build("abcdefghijklmnopqrstuvwxyz"))
                    .addChild(PDFH4.build("abcdefghijklmnopqrstuvwxyz"))
                    .addChild(PDFH5.build("abcdefghijklmnopqrstuvwxyz"))
                    .addChild(PDFH6.build("abcdefghijklmnopqrstuvwxyz"))
                    .addChild(PDFH1.build("ABCDEFGHIJKLMNOPQRSTUVWXYZ"))
                    .addChild(PDFH2.build("ABCDEFGHIJKLMNOPQRSTUVWXYZ"))
                    .addChild(PDFH3.build("ABCDEFGHIJKLMNOPQRSTUVWXYZ"))
                    .addChild(PDFH4.build("ABCDEFGHIJKLMNOPQRSTUVWXYZ"))
                    .addChild(PDFH5.build("ABCDEFGHIJKLMNOPQRSTUVWXYZ"))
                    .addChild(PDFH6.build("ABCDEFGHIJKLMNOPQRSTUVWXYZ"))
                    .addChild(PDFH1.build("abcdefghijklmnopqrstuvwxyz"))
                    .addChild(PDFH2.build("abcdefghijklmnopqrstuvwxyz"))
                    .addChild(PDFH3.build("abcdefghijklmnopqrstuvwxyz"))
                    .addChild(PDFH4.build("abcdefghijklmnopqrstuvwxyz"))
                    .addChild(PDFH5.build("abcdefghijklmnopqrstuvwxyz"))
                    .addChild(PDFH6.build("abcdefghijklmnopqrstuvwxyz"));
        }
        Log.d(TAG, "PDF Builder setup completed");

        Log.d(TAG, "실행");
        builder.draw();
        Log.d(TAG, "builder draw");
        Uri uri = builder.save(context, StandardDirectory.DIRECTORY_DOWNLOADS , "test_LinearLayout_Over.pdf");
        Log.d(TAG, "builder save");

        assertNotNull("Generated PDF URI should not be null", uri);
    }

    @Test
    public void testLinearLayoutTextOver(){
        PDFBuilder builder = new PDFBuilder(Paper.A4).setQuality(85).setPagePadding(30, 30);
        {
            builder.root = PDFLinearLayout.build(Orientation.Vertical)
                    .setBackgroundColor(Color.GRAY)
                    .addChild(PDFH1.build("H1 Title Long Content")
                            .setBackgroundColor(Color.YELLOW)
                            .setTextAlign(TextAlign.Center))
                    .addChild(PDFLinearLayout.build(Orientation.Horizontal)
                            .setMargin(10, 10, 10, 10)
                            .setBackgroundColor(Color.WHITE)
                            .setBorder(4, Color.BLACK)
                            .addChild(PDFH2.build("H2 Name")
                                    .setBackgroundColor(Color.RED))
                            .addChild(PDFH3.build("H3 Glyph")
                                    .setBorder(border ->
                                            border.setLeft(4, Color.YELLOW)
                                                    .setRight(4, Color.CYAN))
                                    .setBackgroundColor(Color.GREEN))
                            .addChild(PDFH4.build("H4 Content")
                                    .setTextColor(Color.WHITE)
                                    .setBackgroundColor(Color.BLUE))
                    )
                    .addChild(PDFLinearLayout.build(Orientation.Horizontal)
                            .setBackgroundColor(Color.LTGRAY)
                            .setSize(null, 200)
                            .addChild(PDFH5.build("H5 Image")
                                    .setPadding(4)
                                    .setBackgroundColor(Color.WHITE)
                                    .setSize(60, null)
                                    .setAnchor(Anchor.Center, Anchor.Center)
                                    .setTextAlign(TextAlign.End))
                            .addChild(PDFImage.build(testImage)
                                    .setCompress(true)
                                    .setFit(Fit.CONTAIN)
                                    .setAnchor(Anchor.Start, Anchor.Center)
                                    .setHeight(50.0f)))
                    .addChild(PDFH1.build(
                            PDFText.Lorem + PDFText.Lorem
                    ));
        }
        Log.d(TAG, "PDF Builder setup completed");

        Log.d(TAG, "실행");
        builder.draw();
        Log.d(TAG, "builder draw");
        Uri uri = builder.save(context, StandardDirectory.DIRECTORY_DOWNLOADS , "test_LinearLayout_Lorem.pdf");
        Log.d(TAG, "builder save");

        assertNotNull("Generated PDF URI should not be null", uri);
    }

    @Test
    public void testGridLayoutTextOver(){
        PDFBuilder builder = new PDFBuilder(Paper.A4).setQuality(85).setPagePadding(30, 30);
        {
            builder.root = PDFLinearLayout.build(Orientation.Vertical)
                    .setBackgroundColor(Color.GRAY)
                    .addChild(PDFLinearLayout.build(Orientation.Horizontal)
                            .setBackgroundColor(Color.LTGRAY)
                            .setSize(null, 200)
                            .addChild(PDFH5.build("H5 Image")
                                    .setPadding(4)
                                    .setBackgroundColor(Color.WHITE)
                                    .setSize(60, null)
                                    .setAnchor(Anchor.Center, Anchor.Center)
                                    .setTextAlign(TextAlign.End))
                            .addChild(PDFImage.build(testImage)
                                    .setCompress(true)
                                    .setFit(Fit.CONTAIN)
                                    .setAnchor(Anchor.Start, Anchor.Center)
                                    .setHeight(50.0f)))
                    .addChild(PDFGridLayout.horizontal(2)
                            .setMargin(10)
                            .setBackgroundColor(Color.WHITE)
                            .addCell(PDFH3.build(PDFText.Lorem).wrapGridCell())
                            .addCell(PDFH3.build(PDFText.Lorem).wrapGridCell())
                            .addCell(PDFH3.build(PDFText.Lorem).wrapGridCell())
                            .addCell(PDFH3.build(PDFText.Lorem).wrapGridCell())
                            .addCell(PDFH4.build(PDFText.Lorem).wrapGridCell())
                            .addCell(PDFH4.build(PDFText.Lorem).wrapGridCell())
                    );

        }
        Log.d(TAG, "PDF Builder setup completed");

        Log.d(TAG, "실행");
        builder.draw();
        Log.d(TAG, "builder draw");
        Uri uri = builder.save(context, StandardDirectory.DIRECTORY_DOWNLOADS , "test_GridLayout_Lorem.pdf");
        Log.d(TAG, "builder save");

        assertNotNull("Generated PDF URI should not be null", uri);
    }

    @After
    public void tearDown(){
        if(testImage != null && !testImage.isRecycled()){
            testImage.recycle();
        }
    }
}