package com.hangyeolee.androidpdfwriter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Layout;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.hangyeolee.androidpdfwriter.components.PDFH1;
import com.hangyeolee.androidpdfwriter.components.PDFH3;
import com.hangyeolee.androidpdfwriter.components.PDFLinearLayout;
import com.hangyeolee.androidpdfwriter.utils.Anchor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class PDFBuilderTest {
    Context context;
    PDFBuilder<PDFLinearLayout> builder;

    @Before
    public void setUp(){
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        int a4X = 595;
        int a4Y = 842;
        builder = new PDFBuilder<>(a4X, a4Y);
        builder.root = PDFLinearLayout.build()
                .setOrientation(PDFLinearLayout.Column)
                .setPadding(10,10,10,10)
                .setBackgroundColor(Color.BLUE)
                .addChild(PDFH1.build("제목")
                        .setBackgroundColor(Color.WHITE)
                        .setTextAlign(Layout.Alignment.ALIGN_CENTER))
                .addChild(PDFLinearLayout.build()
                        .setOrientation(PDFLinearLayout.Row)
                        .setMargin(10, 10, 10, 10)
                        .setBackgroundColor(Color.WHITE)
                        .setBorder(border -> border
                                .setLeft(4, Color.BLACK)
                                .setTop(4, Color.RED)
                                .setRight(4, Color.GREEN)
                                .setBottom(4, Color.BLUE)
                        )
                        .addChild(PDFH3.build("번호"))
                        .addChild(PDFH3.build("이름")
                                .setBackgroundColor(Color.YELLOW)
                                .setTextAlign(Layout.Alignment.ALIGN_CENTER))
                        .addChild(PDFH3.build("내용")
                                .setBackgroundColor(Color.BLACK)
                                .setTextColor(Color.WHITE)
                                .setTextAlign(Layout.Alignment.ALIGN_OPPOSITE))
                );
    }

    @Test
    public void testSave() {
        builder.draw();
        System.out.println(builder.root.getTotalHeight());
        builder.save(context, "Download/result.pdf");
    }
}