# Compose Image Cropper

Image cropping library written with Jetpack Compose with other Composables such
as `ImageWithConstraints` scales Bitmap it displays and returns position and bounds of Bitmap
and `ImageWithThumbnail` to display thumbnail of the image on selected corner.

## ⚠️ Work in Progress

* `ImageWithConstraints` displays `ImageBitmap`s as `androidx.compose.foundation.Image`
  but unlike `Image`, `ImageWithConstraints` returns `ImageScope` which contains width and height of
  drawing area(Canvas) in **dp**, and actual `IntRect` of `Imagebitmap`. This rectangle's bounds
  change depending on which section of the `ImageBitmap` is drawn. For instance, for 1000x1000px
  image with `ContentScale.Crop` IntRect can be such as `IntRect(250,250,500,500)`.
  Using `ImageWithConstraint`
  building other Composables like `ImageThumbnail` that require area of drawing and actual bounds
  of `ImageBİtmap` that is drawn can be achieved easily.

* `ImageWithThumbnail` shows zoomed thumbnail at top left or top right corner of the image that is
  being displayed

