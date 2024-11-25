package com.hangyeolee.androidpdfwriter.font;

import static org.junit.Assert.*;

import android.Manifest;
import android.content.Context;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import com.hangyeolee.androidpdfwriter.components.FontExtractor;
import com.hangyeolee.androidpdfwriter.components.PDFText;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

@RunWith(AndroidJUnit4.class)
public class TTFSubsetterTest {
    private final String TAG = "TEST";
    Context context;
    FontExtractor.FontInfo info;

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        PDFText text = PDFText.build("다람쥐 헌 쳇바퀴에 타고파.1234567890").setFontFromAsset(context, "Pretendard-Bold.ttf");
        Field infoField = PDFText.class.getDeclaredField("info");
        infoField.setAccessible(true);

        info = (FontExtractor.FontInfo) infoField.get(text);
    }

    @After
    public void tearDown() throws Exception {
        info = null;
    }

    @Test
    public void subsetTest() {
        TTFSubsetter subsetter = new TTFSubsetter(info);
        byte[] subsetFont = subsetter.subset();
        if (subsetFont != null) {
            try (FileOutputStream fos = new FileOutputStream("subset_font.ttf")) {
                fos.write(subsetFont);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "파일 없음", e);
            } catch (IOException e) {
                Log.e(TAG, "출력 오류", e);
            }
        }
    }
}