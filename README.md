# Compose Image Cropper

Image cropping library written with Jetpack Compose with other Composables such
as `ImageWithConstraints` scales Bitmap it displays and returns position and bounds of Bitmap
and `ImageWithThumbnail` to display thumbnail of the image on selected corner.

## ⚠️ Work in Progress

* `ImageWithConstraints`  displays `ImageBitmap` like `androidx.compose.foundation.Image`
  but unlike `Image` it also bounds of `ImageBitmap`, and width and height of the canvas image is
  drawn. This is helpful when aspect ratio of `Bitmap` does not match the `ImageWithConstraints`.

* `ImageWithThumbnail` shows zoomed thumbnail at top left or top right corner of the image that is
  being displayed

