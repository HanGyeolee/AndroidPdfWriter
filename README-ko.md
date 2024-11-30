# AndroidPdfWriter
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.hangyeolee/androidpdfwriter/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/io.github.hangyeolee/androidpdfwriter) [![API](https://img.shields.io/badge/API-14%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=14)

안드로이드를 위한 손쉬운 PDF 라이브러리
- [English README.md](./README.md)

## 목차
0. [변경사항](#변경사항)
1. [설정](#설정)
   1. [Gradle 설정](#gradle-설정)
   2. [Maven 설정](#maven-설정)
2. [간단한 사용법](#간단한-사용법)
3. [예시 이미지](#예시-이미지)
4. [설명](#설명)
5. [라이선스](#라이선스)

## 변경사항
### v1.1.2
투명 배경 이미지를 지원합니다. 알파값을 소프트 마스크 이미지로 표현함으로서, 작은 크기의 이미지일 경우 가장자리에 검은 색 선이 그려질 수 있습니다.
Xml Vector Drawable 또한 PDFImage 로 그리려고 시도하면, 최소 256x256 px 크기의 알파값이 있는 비트맵으로 변경합니다.

Zoomable 싱글톤 객체 때문에 여러 PDF를 동시에 그릴 수 없는 문제를 해결하기 위해, PDFPageLayout 객체를 통해 현재 PDF 의 크기를 참조합니다.

### v1.1.1
기존 버전은 각 페이지에 보이는 컴포넌트들을 비트맵에 그려내는 방식입니다.
이 방식의 단점은 캔버스의 크기가 5페이지를 초과하는 순간부터 메모리 부족으로 인해 앱이 튕길 수 있습니다.

버전 1.1.1 부터는 모든 컴포넌트가 PDF 바이너리 형식으로 변환됩니다.
즉, 텍스트와 이미지를 바이너리 형식으로 최적화하여 출력되는 PDF 파일의 용량을 줄입니다.

임베딩 폰트는 아직 `.ttf` 확장자만 지원합니다.

### v1.1.0
레이아웃 컴포넌트의 위치 측정 알고리즘 이슈
부동 소수점의 특징로 인해 측정 알고리즘 무한 루프 동작 이슈
폰트 서브 세팅 오류 이슈
이미지 리사이징 시 원본 미참조 이슈

## 설정
### Gradle 설정
``` gradle
dependencies {
  implementation 'io.github.hangyeolee:androidpdfwriter:1.1.2'
}
```

### Maven 설정
``` xml
<dependency>
    <groupId>io.github.hangyeolee</groupId>
    <artifactId>androidpdfwriter</artifactId>
    <version>1.1.2</version>
</dependency>
```

## 간단한 사용법
``` Java
// PDF Builder의 파라미터는 72dpi를 기준으로 가로 길이와 세로 길이입니다.
// A4 용지의 가로:595.3px 세로:841.9px
// PDF 페이지의 내용이 들어가지 않는 여백입니다. 가로, 세로 순서로 넣으시면 됩니다.
PDFBuilder builder = new PDFBuilder(PageLayoutFactory.createLayout(Paper.A4, 30, 30));
```

#### 테스트용 PDF 페이지를 만드는 코드입니다:
``` Java
PDFLinearLayout root = PDFLinearLayout.build(Orientation.Vertical)
        .setBackgroundColor(Color.BLUE)
        .addChild(PDFImage.fromResource(context, resourceId)
                .setCompress(true)
                .setHeight(200f)
                .setFit(Fit.CONTAIN))
        .addChild(PDFH1.build("Title", PDFFont.HELVETICA_BOLD)
                .setBackgroundColor(Color.RED)
                .setTextAlign(TextAlign.Center))
        .addChild(PDFGridLayout.horizontal(3)
                .setMargin(10, 10, 10, 10)
                .setBackgroundColor(Color.WHITE)
                .setBorder(border -> border
                        .setLeft(4, Color.BLACK)
                        .setTop(4, Color.RED)
                        .setRight(4, Color.GREEN)
                        .setBottom(4, Color.MAGENTA))
                .addCell(PDFH3.build("Number").wrapGridCell())
                .addCell(PDFH3.build("Name")
                        .setFontFromAsset(context, "Pretendard-Bold.ttf")
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
                .addCell(4, 1, PDFImage.fromResource(context, resourceId)
                        .setCompress(true)
                        .setHeight(150)
                        .setFit(Fit.CONTAIN)
                        .wrapGridCell())
                .addCell(4, 2,  PDFH3.build(
                        "It's a very, very long content, and the vertical height of the table layout is the same. It's a very, very long content, and the vertical height of the table layout is the same.")
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
                                .setBottom(4, Color.MAGENTA))
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
                                "It's a very long content. According to this content, the vertical height of Table layout is the same. It also has Span applied, and if you do well, you can also go over the page.")
                        .setBackgroundColor(Color.BLACK)
                        .setTextColor(Color.WHITE)
                        .setTextAlign(TextAlign.Center)
                        .wrapGridCell()
                        .setRowSpan(2))
                .addCell(2, 0, PDFH3.build(
                                "PDFH3 It's a very long content. According to this content, the vertical height of Table layout is the same. It also has Span applied, and if you do well, you can also go over the page.")
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
```

#### 작성된 PDF를 파일로 저장하고 싶을 때:
``` Java
// 해당 레이아웃을 그려냅니다.
builder.draw(root);
// 다운로드 폴더에 저장될 것 입니다.
builder.save(context, StandardDirectory.DIRECTORY_DOWNLOADS, "result.pdf");
```

## 예시 이미지
#### 저장된 PDF 파일은 하단과 같습니다:

![저장된 PDF 파일 예시](./android-pdf-writer/src/androidTest/res/drawable/pdftabletest_resultimage.png)


## 설명
PDF 내에 들어 가는 이미지에 대한 품질 값을 설정 합니다. JPEG 압축 품질을 변경할 수 있으며, 품질 기본 설정 값은 `85` 입니다.
``` Java
builder.setQuality(85);
```

## 라이선스
Copyright 2023 HanGyeol Choi

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

> http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an **"as is" basis, without warranties or conditions of any kind**, either express or implied. See the License for the specific language governing permissions and limitations under the License.
