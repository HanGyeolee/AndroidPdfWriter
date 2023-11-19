# AndroidPdfWriter
This project is a library for easy creation of PDF file on Android.
## Table of Contents
1. Quick Start
2. Documentation

### Gradle Setup
``` gradle
dependencies {
  implementation 'io.github.hangyeolee:androidpdfwriter:0.0.2'
}
```

### Maven Setup
``` xml
<!-- <dependencies> section of pom.xml -->
<dependency>
    <groupId>io.github.hangyeolee</groupId>
    <artifactId>androidpdfwriter</artifactId>
    <version>0.0.2</version>
</dependency>
```

## Quick Start
``` java
// Generics specify the format of the root layout.
// The parameters of the PDF Builder are horizontal and vertical lengths based on 72 dpi.
// A4 paper width:595.3f length:841.9f
PDFBuilder<PDFLinearLayout> builder = new PDFBuilder<>(595.3f, 841.9f);

/*
// set PDF page dpi D2, Standard, M2, M3, ... etc
builder.setDPI(DPI.M3);
// set PDF page padding, vertical and horizontal
builder.setPagePadding(30, 30);
//*/
builder.setDPI(DPI.M3).setPagePadding(30, 30);
```


this is for test pdf page:
``` java
builder.root = PDFLinearLayout.build()
                  .setOrientation(Orientation.Column)
                  .setPadding(10, 10, 10, 10)
                  .setBackgroundColor(Color.BLUE)
                  .addChild(PDFH1.build("제목")
                          .setBackgroundColor(Color.WHITE)
                          .setTextAlign(TextAlign.Center))
                  .addChild(PDFTableLayout.build(3, 12)
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
                                  .setAnchor(Anchor.Start, null)
                                  .setFit(Fit.FILL)
                                  .setSize(null, 200.0f))
                          .addChild(2, 4, PDFH3.build("이미지랑 비교")
                                  .setTextColor(Color.BLACK)
                                  .setTextAlign(TextAlign.Center))
                          .addChild(1, 5, PDFImage.build(b)
                                  .setBackgroundColor(Color.RED)
                                  .setAnchor(Anchor.Start, null)
                                  .setFit(Fit.FILL)
                                  .setSize(null, 200.0f))
                          .addChild(0, 11, PDFH3.build("030"))
                          .addChild(1, 11, PDFH3.build("김철수")
                                  .setBackgroundColor(Color.YELLOW)
                                  .setTextAlign(TextAlign.Center))
                          .addChild(2, 11, PDFH3.build(
                                          "아주아주아주 긴 내용입니다. 이 내용에 따라서 Table 레이아웃의 세로 높이는 동일하게 늘어납니다.")
                                  .setBackgroundColor(Color.BLACK)
                                  .setTextColor(Color.WHITE)
                                  .setTextAlign(TextAlign.Center))
                  );
```

to save just draw and save:
``` java
builder.draw();
builder.save(context, "result.pdf");
```
