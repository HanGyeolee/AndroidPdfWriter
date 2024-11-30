package com.hangyeolee.androidpdfwriter;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import com.hangyeolee.androidpdfwriter.components.PDFEmpty;
import com.hangyeolee.androidpdfwriter.components.PDFGridCell;
import com.hangyeolee.androidpdfwriter.components.PDFGridLayout;
import com.hangyeolee.androidpdfwriter.components.PDFH1;
import com.hangyeolee.androidpdfwriter.components.PDFH2;
import com.hangyeolee.androidpdfwriter.components.PDFH3;
import com.hangyeolee.androidpdfwriter.components.PDFH4;
import com.hangyeolee.androidpdfwriter.components.PDFH5;
import com.hangyeolee.androidpdfwriter.components.PDFH6;
import com.hangyeolee.androidpdfwriter.components.PDFImage;
import com.hangyeolee.androidpdfwriter.components.PDFLinearLayout;
import com.hangyeolee.androidpdfwriter.font.PDFFont;
import com.hangyeolee.androidpdfwriter.utils.Anchor;
import com.hangyeolee.androidpdfwriter.utils.Fit;
import com.hangyeolee.androidpdfwriter.utils.Orientation;
import com.hangyeolee.androidpdfwriter.utils.PageLayoutFactory;
import com.hangyeolee.androidpdfwriter.utils.Paper;
import com.hangyeolee.androidpdfwriter.utils.StandardDirectory;
import com.hangyeolee.androidpdfwriter.utils.TextAlign;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.InputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Formatter;
import java.util.Locale;

@RunWith(AndroidJUnit4.class)
public class PDFTableTest {
    Context context;

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
    }

    @Test
    public void testTableSave() {
        PDFBuilder builder = new PDFBuilder(PageLayoutFactory.createLayout(Paper.A4, 30, 30)).setQuality(90);

        PDFLinearLayout root = PDFLinearLayout.build(Orientation.Vertical)
                .setBackgroundColor(Color.BLUE)
                .addChild(PDFImage.fromResource(context, com.hangyeolee.androidpdfwriter.test.R.drawable.test)
                        .setCompress(true)
                        .setHeight(200f)
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
                        .addCell(4, 1, PDFImage.fromResource(context, com.hangyeolee.androidpdfwriter.test.R.drawable.test)
                                .setCompress(true)
                                .setHeight(150)
                                .setFit(Fit.CONTAIN)
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
                                        "PDFH3 It's a very long content. According to this content, the vertical height of Table layout is the same." +
                                                "It also has Span applied, and if you do well, you can also go over the page.")
                                .setBackgroundColor(Color.BLACK)
                                .setTextColor(Color.WHITE)
                                .setTextAlign(TextAlign.Center)
                                .wrapGridCell()
                                .setRowSpan(2))
                        .addCell(2, 1, PDFH3.build(
                                        "PDFH3 It's a content without Span.")
                                .setBackgroundColor(Color.GRAY)
                                .setTextColor(Color.WHITE)
                                .setTextAlign(TextAlign.Center)
                                .wrapGridCell())
                        .addCell(3, 1, PDFH3.build(
                                        "PDFH3 It's a content without Span.")
                                .setBackgroundColor(Color.RED)
                                .setTextColor(Color.WHITE)
                                .setTextAlign(TextAlign.Center)
                                .wrapGridCell()))
            ;
        builder.draw(root);
        builder.save(context, StandardDirectory.DIRECTORY_DOWNLOADS, "test_GridLayout.pdf");
    }

    @Test
    public void testKoreanTableSave() {
        PDFBuilder builder = new PDFBuilder(PageLayoutFactory.createLayout(Paper.A4, 30, 30)).setQuality(90);

            PDFLinearLayout root = PDFLinearLayout.build(Orientation.Vertical)
                    .setBackgroundColor(Color.BLUE)
                    .addChild(PDFImage.fromResource(context, com.hangyeolee.androidpdfwriter.test.R.drawable.test)
                            .setCompress(true)
                            .setHeight(200f)
                            .setFit(Fit.CONTAIN))
                    .addChild(PDFH1.build("제목")
                            .setFontFromAsset(context, "Pretendard-Bold.ttf")
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
                            .addCell(PDFH3.build("번호")
                                    .setFontFromAsset(context, "Pretendard-Bold.ttf").wrapGridCell())
                            .addCell(PDFH3.build("이름")
                                    .setFontFromAsset(context, "Pretendard-Bold.ttf")
                                    .setBackgroundColor(Color.YELLOW)
                                    .setTextAlign(TextAlign.Center)
                                    .wrapGridCell())
                            .addCell(PDFH3.build("내용")
                                    .setFontFromAsset(context, "Pretendard-Bold.ttf")
                                    .setBackgroundColor(Color.BLACK)
                                    .setTextColor(Color.WHITE)
                                    .setTextAlign(TextAlign.Center)
                                    .wrapGridCell())
                            .addCell(1, 0, PDFH3.build("001")
                                    .setFontFromAsset(context, "Pretendard-Regular.ttf")
                                    .setBackgroundColor(Color.GREEN)
                                    .wrapGridCell()
                                    .setBackgroundColor(Color.BLACK))
                            .addCell(2, 1, PDFH3.build("홍길동")
                                    .setFontFromAsset(context, "Pretendard-Regular.ttf")
                                    .setBackgroundColor(Color.YELLOW)
                                    .setTextAlign(TextAlign.Center)
                                    .wrapGridCell()
                                    .setBackgroundColor(Color.BLACK))
                            .addCell(3, 2, PDFH3.build("어떤 내용이 적혀있다.")
                                    .setFontFromAsset(context, "Pretendard-Regular.ttf")
                                    .setBackgroundColor(Color.BLACK)
                                    .setTextColor(Color.WHITE)
                                    .setTextAlign(TextAlign.Center)
                                    .wrapGridCell()
                                    .setBackgroundColor(Color.GREEN))
                            .addCell(4, 1, PDFImage.fromResource(context, com.hangyeolee.androidpdfwriter.test.R.drawable.test)
                                    .setCompress(true)
                                    .setHeight(150)
                                    .setFit(Fit.CONTAIN)
                                    .wrapGridCell())
                            .addCell(4, 2,  PDFH3.build(
                                            "이건 정말 매우 매우 긴 내용입니다. 그리드 레이아웃의 세로 높이는 동일하게 커집니다." +
                                                    "It's a very, very long content, and the vertical height of the table layout is the same.")
                                    .setFontFromAsset(context, "Pretendard-Regular.ttf")
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
                                            "PDFH3 It's a very long content. According to this content, the vertical height of Table layout is the same." +
                                                    "It also has Span applied, and if you do well, you can also go over the page.")
                                    .setBackgroundColor(Color.BLACK)
                                    .setTextColor(Color.WHITE)
                                    .setTextAlign(TextAlign.Center)
                                    .wrapGridCell()
                                    .setRowSpan(2))
                            .addCell(2, 1, PDFH3.build(
                                            "PDFH3 It's a content without Span.")
                                    .setBackgroundColor(Color.GRAY)
                                    .setTextColor(Color.WHITE)
                                    .setTextAlign(TextAlign.Center)
                                    .wrapGridCell())
                            .addCell(3, 1, PDFH3.build(
                                            "PDFH3 It's a content without Span.")
                                    .setBackgroundColor(Color.RED)
                                    .setTextColor(Color.WHITE)
                                    .setTextAlign(TextAlign.Center)
                                    .wrapGridCell()))
            ;

        builder.draw(root);
        builder.save(context, StandardDirectory.DIRECTORY_DOWNLOADS, "test_GridLayout_korea.pdf");
    }

    final int TITLE = 26;                   //제목
    final int SUB_TITLE = 18;               //부제목
    final int ANIMAL_INFORMATION = 14;      //동물 정보
    final int SUBJECT = 16;                 //주제
    final int CONTENTS = 12;                //내용
    final int TABLE_CHART = 10;             //표
    final int PARAGRAPH = 8;                //워드 단락 나누기 포인트 설정값.
    final float SPACING = 1.08f;            //줄간격 = 폰트 사이즈 *1.08 => 워드 설정값.
    @Test
    public void testReport() {
        PDFH1.fontSize = convertWordPointToPixel(TITLE);
        PDFH2.fontSize = convertWordPointToPixel(SUB_TITLE);
        PDFH3.fontSize = convertWordPointToPixel(SUBJECT);
        PDFH4.fontSize = convertWordPointToPixel(ANIMAL_INFORMATION);
        PDFH5.fontSize = convertWordPointToPixel(CONTENTS);
        PDFH6.fontSize = convertWordPointToPixel(TABLE_CHART);

        PDFBuilder builder = new PDFBuilder(PageLayoutFactory.createLayout(Paper.A4, 36, 36)).setQuality(70);

            PDFLinearLayout root = PDFLinearLayout.build(Orientation.Vertical)
                    .addChild(PDFEmpty.build()
                            .setPadding(0, (int) convertWordPointToPixel(22 * (1 + SPACING) * 6), 0, 0) )
                    .addChild(PDFLinearLayout.build(Orientation.Vertical)
                            .addChild(PDFImage.fromResource(context, com.hangyeolee.androidpdfwriter.test.R.drawable.logo)     //(폰트사이즈 22 + 줄간격 22*1.08) * 단락 나누기(엔터) 6회
                                    .setBackgroundColor(Color.WHITE)
                                    .setHeight(70f)
                                    .setFit(Fit.CONTAIN)
                                    .setAnchor(Anchor.Start, null)
                            )
                            .addChild(PDFH1.build("동물 심전도(ECG) 검진 보고서")
                                    .setFontFromAsset(context, "Pretendard-Bold.ttf")
                                    .setPadding(0, 0, 0, (int) convertWordPointToPixel(TITLE * SPACING))
                                    .setTextAlign(TextAlign.Start))
                            .addChild(PDFH2.build("2024.11.27 08:30")
                                    .setFontFromAsset(context, "Pretendard-Bold.ttf")
                                    .setPadding(0, 0, 0, (int) convertWordPointToPixel(SUB_TITLE * SPACING)))
                            .addChild(PDFH2.build("정기 심전도 검사")
                                    .setFontFromAsset(context, "Pretendard-Bold.ttf")
                                    .setPadding(0, 0, 0, (int) convertWordPointToPixel(SUB_TITLE * SPACING)))
                    )
                    .addChild(PDFEmpty.build()
                            .setPadding(0, (int) convertWordPointToPixel(10 * (1 + SPACING)), 0, 0) )
                    .addChild(PDFEmpty.build()
                            .setPadding(0, (int) convertWordPointToPixel(10 * (1 + SPACING)), 0, 0) );

        builder.draw(root);
        builder.save(context, StandardDirectory.DIRECTORY_DOWNLOADS, "test_report.pdf");
    }
    private float mm2px(float mm){return mm * 2.8348472f;}
    private float convertWordPointToPixel(float wordPoint){
        return mm2px(wordPoint * 0.35f);
    }
}