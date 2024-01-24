# AndroidPdfWriter
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.hangyeolee/androidpdfwriter/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/io.github.hangyeolee/androidpdfwriter) [![API](https://img.shields.io/badge/API-14%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=14)

A easy to create PDF file library for Android.
- [한국어 README.md](./README-ko.md)

## Table of Contents
1. [Setup](#setup)
   1. [Gradle Setup](#gradle-setup)
   2. [Maven Setup](#maven-setup)
2. [Quick Start](#quick-start)
3. [Example Image](#example-image)
4. [Description](#description)
5. [License](#license)


## Setup
### Gradle Setup
``` gradle
dependencies {
  implementation 'io.github.hangyeolee:androidpdfwriter:1.0.4'
}
```

### Maven Setup
``` xml
<dependency>
    <groupId>io.github.hangyeolee</groupId>
    <artifactId>androidpdfwriter</artifactId>
    <version>1.0.4</version>
</dependency>
```

## Quick Start
``` Java
// Generics specify the format of the root layout.
// The parameters of the PDF Builder are horizontal and vertical lengths based on 72 dpi.
// A4 paper width:595.3px height:841.9px
PDFBuilder<PDFLinearLayout> builder = new PDFBuilder<>(Paper.A4);

// set PDF page padding, vertical and horizontal
builder.setPagePadding(30, 30);
```

#### this is test pdf page:
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

#### When you want to save a created file:
``` Java
builder.draw();
// Saved in the Download folder.
builder.save(context, StandardDirectory.DIRECTORY_DOWNLOADS, "result.pdf");
```

## Example Image
#### PDF File looks like below:

![PDF File looks like this image](./android-pdf-writer/src/androidTest/res/drawable/pdftabletest_resultimage.png)

## Description
The library draws the screen set by the user in Bitmap, compresses it in JPEG format, and outputs it as a PDF file. One image is entered per page. Can change compress quality. Default quality is `60`.
``` Java
builder.setQuality(60);
```

DPI is the resolution of the image to be created in pdf. The higher the resolution, the higher the capacity of the image will be. Regardless of the device dpi, the resolution is increased based solely on the dpi of the pdf. Default Dpi is `DPI.M5`, as 360dpi.
``` Java
builder.setDPI(DPI.M5);
```

The larger the padding of the page that is not related to PDFComponents, the smaller the maximum width and height of the components. Default Padding is `(0, 0)`. In case of printing, it is recommended to add about `(30, 30)` padding.
``` Java
builder.setPagePadding(30, 30);
```

## License
Copyright 2023 HanGyeol Choi

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

> http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an **"as is" basis, without warranties or conditions of any kind**, either express or implied. See the License for the specific language governing permissions and limitations under the License.
