data "aws_cloudfront_cache_policy" "cdn_managed_caching_optimized_policy" {
  name = "Managed-CachingOptimized"
}

resource "aws_cloudfront_distribution" "scc_distribution" {
  origin {
    domain_name = aws_s3_bucket.accessibility_images.bucket_regional_domain_name
    origin_id   = aws_s3_bucket.accessibility_images.bucket_regional_domain_name
  }

  origin {
    domain_name = aws_s3_bucket.accessibility_thumbnails.bucket_regional_domain_name
    origin_id   = aws_s3_bucket.accessibility_thumbnails.bucket_regional_domain_name
  }

  enabled             = true
  comment             = "CDN for scc server"

  default_cache_behavior {
    allowed_methods  = ["GET", "HEAD"]
    cached_methods   = ["GET", "HEAD"]
    target_origin_id = aws_s3_bucket.accessibility_images.bucket_regional_domain_name
    viewer_protocol_policy = "allow-all"
    cache_policy_id = data.aws_cloudfront_cache_policy.cdn_managed_caching_optimized_policy.id
    compress = true
  }

  ordered_cache_behavior {
    path_pattern     = "/thumbnail_*"
    allowed_methods = ["GET", "HEAD"]
    cached_methods = ["GET", "HEAD"]
    target_origin_id = aws_s3_bucket.accessibility_thumbnails.bucket_regional_domain_name
    viewer_protocol_policy = "allow-all"
    cache_policy_id = data.aws_cloudfront_cache_policy.cdn_managed_caching_optimized_policy.id
    compress = true
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    cloudfront_default_certificate = true
  }
}

resource "aws_cloudfront_distribution" "scc_dev_distribution" {
  origin {
    domain_name = aws_s3_bucket.dev_accessibility_images.bucket_regional_domain_name
    origin_id   = aws_s3_bucket.dev_accessibility_images.bucket_regional_domain_name
  }

  origin {
    domain_name = aws_s3_bucket.dev_accessibility_thumbnails.bucket_regional_domain_name
    origin_id   = aws_s3_bucket.dev_accessibility_thumbnails.bucket_regional_domain_name
  }

  enabled             = true
  comment             = "CDN for scc dev server"

  default_cache_behavior {
    allowed_methods  = ["GET", "HEAD"]
    cached_methods   = ["GET", "HEAD"]
    target_origin_id = aws_s3_bucket.dev_accessibility_images.bucket_regional_domain_name
    viewer_protocol_policy = "allow-all"
    cache_policy_id = data.aws_cloudfront_cache_policy.cdn_managed_caching_optimized_policy.id
    compress = true
  }

  ordered_cache_behavior {
    path_pattern     = "thumbnail_*"
    allowed_methods = ["GET", "HEAD"]
    cached_methods = ["GET", "HEAD"]
    target_origin_id = aws_s3_bucket.dev_accessibility_thumbnails.bucket_regional_domain_name
    viewer_protocol_policy = "allow-all"
    cache_policy_id = data.aws_cloudfront_cache_policy.cdn_managed_caching_optimized_policy.id
    compress = true
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    cloudfront_default_certificate = true
  }
}
