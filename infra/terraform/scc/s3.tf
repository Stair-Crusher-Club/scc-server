resource "aws_s3_bucket" "dev_accessibility_images" {
  bucket = "scc-dev-accessibility-images-2"
}

resource "aws_s3_bucket_ownership_controls" "dev_accessibility_images" {
  bucket = aws_s3_bucket.dev_accessibility_images.id

  rule {
    object_ownership = "ObjectWriter"
  }
}

resource "aws_s3_bucket_public_access_block" "dev_accessibility_images" {
  bucket = aws_s3_bucket.dev_accessibility_images.id

  block_public_acls       = false
  block_public_policy     = false
  ignore_public_acls      = false
  restrict_public_buckets = false
}

resource "aws_s3_bucket" "accessibility_images" {
  bucket = "scc-prod-accessibility-images"
}

resource "aws_s3_bucket_ownership_controls" "accessibility_images" {
  bucket = aws_s3_bucket.accessibility_images.id

  rule {
    object_ownership = "ObjectWriter"
  }
}

resource "aws_s3_bucket_public_access_block" "accessibility_images" {
  bucket = aws_s3_bucket.accessibility_images.id

  block_public_acls       = false
  block_public_policy     = false
  ignore_public_acls      = false
  restrict_public_buckets = false
}

resource "aws_s3_bucket" "dev_accessibility_thumbnails" {
  bucket = "scc-dev-accessibility-thumbnails"
}

resource "aws_s3_bucket_ownership_controls" "dev_accessibility_thumbnails" {
  bucket = aws_s3_bucket.dev_accessibility_thumbnails.id

  rule {
    object_ownership = "ObjectWriter"
  }
}

resource "aws_s3_bucket_public_access_block" "dev_accessibility_thumbnails" {
  bucket = aws_s3_bucket.dev_accessibility_thumbnails.id

  block_public_acls       = false
  block_public_policy     = false
  ignore_public_acls      = false
  restrict_public_buckets = false
}

resource "aws_s3_bucket" "accessibility_thumbnails" {
  bucket = "scc-prod-accessibility-thumbnails"
}

resource "aws_s3_bucket_ownership_controls" "accessibility_thumbnails" {
  bucket = aws_s3_bucket.accessibility_thumbnails.id

  rule {
    object_ownership = "ObjectWriter"
  }
}

resource "aws_s3_bucket_public_access_block" "accessibility_thumbnails" {
  bucket = aws_s3_bucket.accessibility_thumbnails.id

  block_public_acls       = false
  block_public_policy     = false
  ignore_public_acls      = false
  restrict_public_buckets = false
}

resource "aws_s3_bucket" "dev_home_banners" {
  bucket = "scc-dev-home-banners"
}

resource "aws_s3_bucket_ownership_controls" "dev_home_banners" {
  bucket = aws_s3_bucket.dev_home_banners.id

  rule {
    object_ownership = "ObjectWriter"
  }
}

resource "aws_s3_bucket_public_access_block" "dev_home_banners" {
  bucket = aws_s3_bucket.dev_home_banners.id

  block_public_acls       = false
  block_public_policy     = false
  ignore_public_acls      = false
  restrict_public_buckets = false
}

resource "aws_s3_bucket_policy" "dev_home_banners" {
  bucket = aws_s3_bucket.dev_home_banners.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action    = "s3:GetObject"
        Effect    = "Allow"
        Principal = "*"
        Resource  = "${aws_s3_bucket.dev_home_banners.arn}/*"
      }
    ]
  })
}

resource "aws_s3_bucket_cors_configuration" "dev_home_banners" {
  bucket = aws_s3_bucket.dev_home_banners.id

  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["PUT"]
    allowed_origins = ["http://localhost:3066", "https://admin.dev.staircrusher.club"]
    max_age_seconds = 3000
  }
}

resource "aws_s3_bucket" "home_banners" {
  bucket = "scc-prod-home-banners"
}

resource "aws_s3_bucket_ownership_controls" "home_banners" {
  bucket = aws_s3_bucket.home_banners.id

  rule {
    object_ownership = "ObjectWriter"
  }
}

resource "aws_s3_bucket_public_access_block" "home_banners" {
  bucket = aws_s3_bucket.home_banners.id

  block_public_acls       = false
  block_public_policy     = false
  ignore_public_acls      = false
  restrict_public_buckets = false
}

resource "aws_s3_bucket_policy" "home_banners" {
  bucket = aws_s3_bucket.home_banners.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action    = "s3:GetObject"
        Effect    = "Allow"
        Principal = "*"
        Resource  = "${aws_s3_bucket.home_banners.arn}/*"
      }
    ]
  })
}

resource "aws_s3_bucket_cors_configuration" "home_banners" {
  bucket = aws_s3_bucket.home_banners.id

  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["PUT"]
    allowed_origins = ["https://admin.staircrusher.club"]
    max_age_seconds = 3000
  }
}

resource "aws_s3_bucket" "dev_crusher_labels" {
  bucket = "scc-dev-crusher-labels"
}

resource "aws_s3_bucket_ownership_controls" "dev_crusher_labels" {
  bucket = aws_s3_bucket.dev_crusher_labels.id

  rule {
    object_ownership = "ObjectWriter"
  }
}

resource "aws_s3_bucket_public_access_block" "dev_crusher_labels" {
  bucket = aws_s3_bucket.dev_crusher_labels.id

  block_public_acls       = false
  block_public_policy     = false
  ignore_public_acls      = false
  restrict_public_buckets = false
}

resource "aws_s3_bucket_policy" "dev_crusher_labels" {
  bucket = aws_s3_bucket.dev_crusher_labels.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action    = "s3:GetObject"
        Effect    = "Allow"
        Principal = "*"
        Resource  = "${aws_s3_bucket.dev_crusher_labels.arn}/*"
      }
    ]
  })
}

resource "aws_s3_bucket_cors_configuration" "dev_crusher_labels" {
  bucket = aws_s3_bucket.dev_crusher_labels.id

  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["PUT"]
    allowed_origins = ["http://localhost:3066", "https://admin.dev.staircrusher.club"]
    max_age_seconds = 3000
  }
}

resource "aws_s3_bucket" "crusher_labels" {
  bucket = "scc-prod-crusher-labels"
}

resource "aws_s3_bucket_ownership_controls" "crusher_labels" {
  bucket = aws_s3_bucket.crusher_labels.id

  rule {
    object_ownership = "ObjectWriter"
  }
}

resource "aws_s3_bucket_public_access_block" "crusher_labels" {
  bucket = aws_s3_bucket.crusher_labels.id

  block_public_acls       = false
  block_public_policy     = false
  ignore_public_acls      = false
  restrict_public_buckets = false
}

resource "aws_s3_bucket_policy" "crusher_labels" {
  bucket = aws_s3_bucket.crusher_labels.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action    = "s3:GetObject"
        Effect    = "Allow"
        Principal = "*"
        Resource  = "${aws_s3_bucket.crusher_labels.arn}/*"
      }
    ]
  })
}

resource "aws_s3_bucket_cors_configuration" "crusher_labels" {
  bucket = aws_s3_bucket.crusher_labels.id

  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["PUT"]
    allowed_origins = ["https://admin.staircrusher.club"]
    max_age_seconds = 3000
  }
}
