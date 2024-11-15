package com.hangyeolee.androidpdfwriter;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

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
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        InputStream stream = InstrumentationRegistry.getInstrumentation().getContext().getResources().openRawResource(com.hangyeolee.androidpdfwriter.test.R.drawable.test);
        Bitmap b = BitmapFactory.decodeStream(stream);

        builder = new PDFBuilder(Paper.A4);
        builder.setPagePadding(30, 30)
                .setQuality(100)
                .setPagePadding(30.0f, 30.0f, 30.0f, 30.0f)
                .setQuality(100);
        {
            builder.root = PDFLinearLayout.build()
                    .setOrientation(Orientation.Column)
                    .setBackgroundColor(Color.BLUE)
                    .addChild(PDFImage.build(b)
                            .setSize(1200f)
                            .setFit(Fit.CONTAIN))
                    .addChild(PDFH1.build("제목")
                            .setBackgroundColor(Color.RED)
                            .setTextAlign(TextAlign.Center))
                    .addChild(PDFGridLayout.build(3, 5)
                            .setMargin(10, 10, 10, 10)
                            .setBackgroundColor(Color.WHITE)
                            .setBorder(border -> border
                                    .setLeft(4, Color.BLACK)
                                    .setTop(4, Color.RED)
                                    .setRight(4, Color.GREEN)
                                    .setBottom(4, Color.MAGENTA)
                            )
                            .addChild(0, 0, PDFH3.build("번호"))
                            .addChild(1, 0, PDFH3.build("이름")
                                    .setBackgroundColor(Color.YELLOW)
                                    .setTextAlign(TextAlign.Center))
                            .addChild(2, 0, PDFH3.build("내용")
                                    .setBackgroundColor(Color.BLACK)
                                    .setTextColor(Color.WHITE)
                                    .setTextAlign(TextAlign.Center))
                            .addChild(0, 1, PDFH3.build("001"))
                            .addChild(1, 2, PDFH3.build("홍길동")
                                    .setBackgroundColor(Color.YELLOW)
                                    .setTextAlign(TextAlign.Center))
                            .addChild(2, 3, PDFH3.build("어떤 내용이 담겨있다.")
                                    .setBackgroundColor(Color.BLACK)
                                    .setTextColor(Color.WHITE)
                                    .setTextAlign(TextAlign.Center))
                            .addChild(1, 4, PDFImage.build(b)
                                    .setBackgroundColor(Color.RED)
                                    .setFit(Fit.FILL)
                                    .setSize(50.0f))
                            .addChild(2, 4, PDFH3.build(
                                    "아주아주아주 긴 내용입니다. 이 내용에 따라서 Table 레이아웃의 세로 높이는 동일하게 늘어납니다." +
                                    "아주아주아주 긴 내용입니다. 이 내용에 따라서 Table 레이아웃의 세로 높이는 동일하게 늘어납니다.")
                                    .setTextColor(Color.BLACK)
                                    .setTextAlign(TextAlign.Center)))
                    .addChild(PDFGridLayout.build(2, 4)
                                    .setMargin(10, 10, 10, 10)
                                    .setBackgroundColor(Color.WHITE)
                                    .setBorder(border -> border
                                            .setLeft(4, Color.BLACK)
                                            .setTop(4, Color.RED)
                                            .setRight(4, Color.GREEN)
                                            .setBottom(4, Color.MAGENTA)
                                    )
                            .addChild(0, 0, PDFH3.build(
                                            "Span 이 없는 내용입니다.")
                                    .setBackgroundColor(Color.BLACK)
                                    .setTextColor(Color.WHITE)
                                    .setTextAlign(TextAlign.Center))
                            .addChild(0, 1, PDFH3.build(
                                            "Span 이 없는 내용입니다.")
                                    .setBackgroundColor(Color.WHITE)
                                    .setTextColor(Color.BLACK)
                                    .setTextAlign(TextAlign.Center))
                            .addChild(1, 0, 1, 2, PDFH3.build(
                                            "아주아주아주 긴 내용입니다. 이 내용에 따라서 Table 레이아웃의 세로 높이는 동일하게 늘어납니다." +
                                            "또한 Span 이 적용되어 있으며, 잘하면 페이지를 넘어갈 수 도 있습니다.")
                                    .setBackgroundColor(Color.BLACK)
                                    .setTextColor(Color.WHITE)
                                    .setTextAlign(TextAlign.Center))
                            .addChild(0, 2, 1, 2, PDFH3.build(
                                            "아주아주아주 긴 내용입니다. 이 내용에 따라서 Table 레이아웃의 세로 높이는 동일하게 늘어납니다." +
                                                    "또한 Span 이 적용되어 있으며, 잘하면 페이지를 넘어갈 수 도 있습니다.")
                                    .setBackgroundColor(Color.BLACK)
                                    .setTextColor(Color.WHITE)
                                    .setTextAlign(TextAlign.Center))
                            .addChild(1, 2, 1, 1, PDFH3.build(
                                            "Span 이 없는 내용입니다.")
                                    .setBackgroundColor(Color.GRAY)
                                    .setTextColor(Color.WHITE)
                                    .setTextAlign(TextAlign.Center))
                            .addChild(1, 3,1, 1, PDFH3.build(
                                            "Span 이 없는 내용입니다.")
                                    .setBackgroundColor(Color.RED)
                                    .setTextColor(Color.WHITE)
                                    .setTextAlign(TextAlign.Center)))
            ;
        }
    }

    @Test
    public void testTableSave() {
        builder.draw();
        builder.save(context, StandardDirectory.DIRECTORY_DOWNLOADS, "result.pdf");
    }
}