# AndroidPdfWriter
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.hangyeolee/androidpdfwriter/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/io.github.hangyeolee/androidpdfwriter) [![API](https://img.shields.io/badge/API-14%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=14)

안드로이드를 위한 손쉬운 PDF 라이브러리
- [English README.md](./README.md)

## 목차
1. [설정](#설정)
   1. [Gradle 설정](#gradle-설정)
   2. [Maven 설정](#maven-설정)
2. [간단한 사용법](#간단한-사용법)
3. [예시 이미지](#예시-이미지)
4. [설명](#설명)
5. [라이선스](#라이선스)


## 설정
### Gradle 설정
``` gradle
dependencies {
  implementation 'io.github.hangyeolee:androidpdfwriter:1.0.4'
}
```

### Maven 설정
``` xml
<dependency>
    <groupId>io.github.hangyeolee</groupId>
    <artifactId>androidpdfwriter</artifactId>
    <version>1.0.4</version>
</dependency>
```

## 간단한 사용법
``` Java
// 제네릭 형식은 최상단 레이아웃의 형식을 지정합니다.
// PDF Builder의 파라미터는 72dpi를 기준으로 가로 길이와 세로 길이입니다.
// A4 용지의 가로:595.3px 세로:841.9px
PDFBuilder<PDFLinearLayout> builder = new PDFBuilder<>(Paper.A4);

// PDF 페이지의 내용이 들어가지 않는 여백입니다. 가로, 세로 순서로 넣으시면 됩니다.
builder.setPagePadding(30, 30);
```

#### 테스트용 PDF 페이지를 만드는 코드입니다:
``` Java
builder.root = PDFLinearLayout.build()
    .setOrientation(Orientation.Column)
    .setBackgroundColor(Color.BLUE)
    .addChild(PDFImage.build(b)
        .setSize(null, 200f)
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
            .setSize(null, 50.0f))
        .addChild(2, 4, PDFH3.build(
            "아주아주아주 긴 내용입니다. 이 내용에 따라서 Table 레이아웃의 세로 높이는 동일하게 늘어납니다. 아주아주아주 긴 내용입니다. 이 내용에 따라서 Table 레이아웃의 세로 높이는 동일하게 늘어납니다.")
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
        .addChild(0, 0, PDFH3.build("Span 이 없는 내용입니다.")
            .setBackgroundColor(Color.BLACK)
            .setTextColor(Color.WHITE)
            .setTextAlign(TextAlign.Center))
        .addChild(0, 1, PDFH3.build("Span 이 없는 내용입니다.")
            .setBackgroundColor(Color.WHITE)
            .setTextColor(Color.BLACK)
            .setTextAlign(TextAlign.Center))
        .addChild(1, 0, 1, 2, PDFH3.build(
            "아주아주아주 긴 내용입니다. 이 내용에 따라서 Table 레이아웃의 세로 높이는 동일하게 늘어납니다. 또한 Span 이 적용되어 있으며, 잘하면 페이지를 넘어갈 수 도 있습니다.")
            .setBackgroundColor(Color.BLACK)
            .setTextColor(Color.WHITE)
            .setTextAlign(TextAlign.Center))
        .addChild(0, 2, 1, 2, PDFH3.build(
            "아주아주아주 긴 내용입니다. 이 내용에 따라서 Table 레이아웃의 세로 높이는 동일하게 늘어납니다. 또한 Span 이 적용되어 있으며, 잘하면 페이지를 넘어갈 수 도 있습니다.")
            .setBackgroundColor(Color.BLACK)
            .setTextColor(Color.WHITE)
            .setTextAlign(TextAlign.Center))
        .addChild(1, 2, 1, 1, PDFH3.build("Span 이 없는 내용입니다.")
            .setBackgroundColor(Color.GRAY)
            .setTextColor(Color.WHITE)
            .setTextAlign(TextAlign.Center))
        .addChild(1, 3,1, 1, PDFH3.build("Span 이 없는 내용입니다.")
            .setBackgroundColor(Color.RED)
            .setTextColor(Color.WHITE)
            .setTextAlign(TextAlign.Center))
        );
```

#### 작성된 PDF를 파일로 저장하고 싶을 때:
``` Java
builder.draw();
// 다운로드 폴더에 저장될 것 입니다.
builder.save(context, StandardDirectory.DIRECTORY_DOWNLOADS, "result.pdf");
```

## 예시 이미지
#### 저장된 PDF 파일은 하단과 같습니다.:

![저장된 PDF 파일 예시](./android-pdf-writer/src/androidTest/res/drawable/pdftabletest_resultimage.png)

## 설명
이 라이브러리는 사용자가 설정한 화면을 비트맵으로 그려 JPEG 형식으로 압축한 후, PDF 파일로 출력합니다. 페이지 당 1장의 이미지만 포함합니다. JPEG 압축 품질을 변경할 수 있으며, 품질 기본 설정값은 `60` 입니다.
``` Java
builder.setQuality(60);
```

DPI는 pdf로 생성할 이미지의 해상도입니다. 해상도가 높을수록 이미지의 용량이 증가할 것 입니다. 디바이스 dpi와 상관없이 오직 pdf의 72dpi를 기준으로 해상도가 증가합니다. DPI 기본 설정값은 `DPI.M5`로 360dpi 입니다.
``` Java
builder.setDPI(DPI.M5);
```

PDF 페이지의 여백이 클수록 PDFComponents의 최대 너비 및 높이가 작아집니다. 여백 기본 설정값은 `(0,0)` 입니다. 프린트할 경우를 대비해 대략 `(30, 30)` 정도의 여백을 추가하는 것을 추천합니다.
``` Java
builder.setPagePadding(30, 30);
```

## 라이선스
Copyright 2023 HanGyeol Choi

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

> http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an **"as is" basis, without warranties or conditions of any kind**, either express or implied. See the License for the specific language governing permissions and limitations under the License.
