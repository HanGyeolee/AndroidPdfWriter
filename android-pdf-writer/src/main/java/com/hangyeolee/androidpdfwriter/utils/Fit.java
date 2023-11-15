package com.hangyeolee.androidpdfwriter.utils;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Fit {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({FILL, CONTAIN, COVER, NONE, SCALE_DOWN})
    public @interface FitInt {}

    /**
     * 요소 콘텐츠 박스 크기에 맞춰 대체 콘텐츠의 크기를 조절합니다.<br>
     * 콘텐츠가 콘텐츠 박스를 가득 채웁니다. 서로의 가로세로비가 일치하지 않으면 콘텐츠가 늘어납니다.<br>
     * Sizes alternative content to match the size of the element content box.<br>
     * The content fills the content box. If the aspect ratio of each other does not match, the content increases.
     *
     * @author  HanGyeol Choi
     */
    public static final int FILL = 0;

    /**
     * 대체 콘텐츠의 가로세로비를 유지하면서, 요소의 콘텐츠 박스 내부에 들어가도록 크기를 맞춤 조절합니다.<br>
     * 콘텐츠가 콘텐츠 박스 크기에 맞도록 하면서도 가로세로비를 유지하게 되므로, 서로의 가로세로비가 일치하지 않으면 객체가 "레터박스"처럼 됩니다.<br>
     * Size to fit inside the content box of an element while maintaining the aspect ratio of the alternative content.<br>
     * Because the content matches the size of the content box while maintaining the aspect ratio, the objects become like "letter boxes" if they do not match each other.
     *
     * @author  HanGyeol Choi
     */
    public static final int CONTAIN = 1;

    /**
     * 대체 콘텐츠의 가로세로비를 유지하면서, 요소 콘텐츠 박스를 가득 채웁니다.<br>
     * 서로의 가로세로비가 일치하지 않으면 객체 일부가 잘려나갑니다.<br>
     * Fill the element content box while maintaining the aspect ratio of the alternative content.<br>
     * If the aspect ratio of each other does not match, some of the objects are truncated.
     *
     * @author  HanGyeol Choi
     */
    public static final int COVER = 2;

    /**
     * 대체 콘텐츠의 크기를 조절하지 않습니다.<br>
     * Do not size alternative content.
     *
     * @author  HanGyeol Choi
     */
    public static final int NONE = 3;

    /**
     * {@code @NONE}과 {@code @CONTAIN} 중 대체 콘텐츠의 크기가 더 작아지는 값을 선택합니다.<br>
     * Choose {@code @NONE} or {@code @CONTAIN}, which values the alternate content will be smaller in size.
     *
     * @author  HanGyeol Choi
     */
    public static final int SCALE_DOWN = 4;
}
