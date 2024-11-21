package com.hangyeolee.androidpdfwriter;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import com.hangyeolee.androidpdfwriter.components.PDFGridCell;
import com.hangyeolee.androidpdfwriter.components.PDFGridLayout;
import com.hangyeolee.androidpdfwriter.components.PDFH1;
import com.hangyeolee.androidpdfwriter.components.PDFH3;
import com.hangyeolee.androidpdfwriter.components.PDFImage;
import com.hangyeolee.androidpdfwriter.components.PDFLinearLayout;
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
public class PDFTableTest {
    Context context;
    PDFBuilder builder;

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    /**
     * The first page of the pdf file that is output by executing the code below is as follows:
     * com.hangyeolee.androidpdfwriter.test.R.drawable.pdftabletest_resultimage
     */
    @Before
    public void setUp() {
        PDFBuilder.DEBUG = true;

        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        InputStream stream = InstrumentationRegistry.getInstrumentation().getContext().getResources().openRawResource(com.hangyeolee.androidpdfwriter.test.R.drawable.test);
        Bitmap b = BitmapFactory.decodeStream(stream);

        builder = new PDFBuilder(Paper.A4);
        builder.setPagePadding(30, 30)
                .setQuality(100)
                .setPagePadding(30.0f, 30.0f, 30.0f, 30.0f)
                .setQuality(100);
        {
            builder.root = PDFLinearLayout.build(Orientation.Vertical)
                    .setBackgroundColor(Color.BLUE)
                    .addChild(PDFImage.build(b)
                            .setHeight(120f)
                            .setFit(Fit.CONTAIN))
                    .addChild(PDFH1.build("Title")
                            .setBackgroundColor(Color.RED)
                            .setTextAlign(TextAlign.Center))
                    .addChild(PDFGridLayout.horizontal(3)
                            .setMargin(10, 10, 10, 10)
                            .setBackgroundColor(Color.WHITE)
                            .setBorder(border -> border
                                    .setLeft(4, Color.BLACK)
                                    .setTop(4, Color.RED)
                                    .setRight(4, Color.GREEN)
                                    .setBottom(4, Color.MAGENTA)
                            )
                            .addCell(PDFH3.build("Number").wrapGridCell())
                            .addCell(PDFH3.build("Name")
                                    .setBackgroundColor(Color.YELLOW)
                                    .setTextAlign(TextAlign.Center)
                                    .wrapGridCell())
                            .addCell(PDFH3.build("Content")
                                    .setBackgroundColor(Color.BLACK)
                                    .setTextColor(Color.WHITE)
                                    .setTextAlign(TextAlign.Center)
                                    .wrapGridCell())
                            .addCell(1, 0, PDFH3.build("001")
                                    .setBackgroundColor(Color.GREEN)
                                    .wrapGridCell()
                                    .setBackgroundColor(Color.BLACK))
                            .addCell(2, 1, PDFH3.build("Hong Gil-Dong")
                                    .setBackgroundColor(Color.YELLOW)
                                    .setTextAlign(TextAlign.Center)
                                    .wrapGridCell()
                                    .setBackgroundColor(Color.BLACK))
                            .addCell(3, 2, PDFH3.build("Some content had been existed.")
                                    .setBackgroundColor(Color.BLACK)
                                    .setTextColor(Color.WHITE)
                                    .setTextAlign(TextAlign.Center)
                                    .wrapGridCell()
                                    .setBackgroundColor(Color.GREEN))
                            .addCell(4, 1, PDFImage.build(b)
                                    .setCompress(true)
                                    .setBackgroundColor(Color.RED)
                                    .setFit(Fit.FILL)
                                    .setHeight(50.0f)
                                    .wrapGridCell())
                            .addCell(4, 2,  PDFH3.build(
                                    "It's a very, very long content, and the vertical height of the table layout is the same." +
                                    "It's a very, very long content, and the vertical height of the table layout is the same.")
                                    .setTextColor(Color.BLACK)
                                    .setTextAlign(TextAlign.Center)
                                    .wrapGridCell()))
                    .addChild(PDFGridLayout.horizontal(2)
                                    .setMargin(10, 10, 10, 10)
                                    .setBackgroundColor(Color.WHITE)
                                    .setBorder(border -> border
                                            .setLeft(4, Color.BLACK)
                                            .setTop(4, Color.RED)
                                            .setRight(4, Color.GREEN)
                                            .setBottom(4, Color.MAGENTA)
                                    )
                            .addCell(0, 0, PDFH3.build(
                                            "It's a content without Span.")
                                    .setBackgroundColor(Color.BLACK)
                                    .setTextColor(Color.WHITE)
                                    .setTextAlign(TextAlign.Center)
                                    .wrapGridCell())
                            .addCell(1, 0, PDFH3.build(
                                            "It's a content without Span.")
                                    .setBackgroundColor(Color.WHITE)
                                    .setTextColor(Color.BLACK)
                                    .setTextAlign(TextAlign.Center)
                                    .wrapGridCell())
                            .addCell(0, 1, PDFH3.build(
                                            "It's a very long content. According to this content, the vertical height of Table layout is the same." +
                                            "It also has Span applied, and if you do well, you can also go over the page.")
                                    .setBackgroundColor(Color.BLACK)
                                    .setTextColor(Color.WHITE)
                                    .setTextAlign(TextAlign.Center)
                                    .wrapGridCell()
                                    .setRowSpan(2))
                            .addCell(2, 0, PDFH3.build(
                                            "It's a very long content. According to this content, the vertical height of Table layout is the same." +
                                                    "It also has Span applied, and if you do well, you can also go over the page.")
                                    .setBackgroundColor(Color.BLACK)
                                    .setTextColor(Color.WHITE)
                                    .setTextAlign(TextAlign.Center)
                                    .wrapGridCell()
                                    .setRowSpan(2))
                            .addCell(2, 1, PDFH3.build(
                                            "It's a content without Span.")
                                    .setBackgroundColor(Color.GRAY)
                                    .setTextColor(Color.WHITE)
                                    .setTextAlign(TextAlign.Center)
                                    .wrapGridCell())
                            .addCell(3, 1, PDFH3.build(
                                            "It's a content without Span.")
                                    .setBackgroundColor(Color.RED)
                                    .setTextColor(Color.WHITE)
                                    .setTextAlign(TextAlign.Center)
                                    .wrapGridCell()))
            ;
        }
    }

    @Test
    public void testTableSave() {
        builder.draw();
        builder.save(context, StandardDirectory.DIRECTORY_DOWNLOADS, "test_GridLayout.pdf");
    }
}