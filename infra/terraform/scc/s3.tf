resource "aws_s3_bucket" "dev_accessibility_images" {
  bucket = "scc-dev-accessibility-images-2"
}

resource "aws_s3_bucket" "accessibility_images" {
  bucket = "scc-prod-accessibility-images"
}
