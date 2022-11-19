resource "aws_s3_bucket" "dev_accessibility_images" {
  bucket = "scc-dev-accessibility-images"
}

resource "aws_s3_bucket" "accessibility_images" {
  bucket = "scc-accessibility-images"
}
