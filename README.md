# Compose Image Cropper
Image cropping library written with Jetpack Compose with other Composables such as
`ScalableImage` and `ImageWithThumbnail`

## ⚠️ Work in Progress

* `ScalableImage`  displays `ImageBitmap` like `androidx.compose.foundation.Image`
but unlike `Image` it also returns position of `ImageBitmap`. This is helpful when aspect ratio of `Bitmap` does not match
the `ScalableImage`.

* `ImageWithThumbnail` shows zoomed thumbnail at top left or top right corner of the image that is being displayed

