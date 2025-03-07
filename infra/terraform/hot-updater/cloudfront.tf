resource "aws_cloudfront_distribution" "hot_updater_distribution" {
  origin {
    connection_attempts = 3
    connection_timeout  = 10
    domain_name         = "scc-hot-updater-storage.s3.ap-northeast-2.amazonaws.com"
    origin_id           = "scc-hot-updater-storage"
  }

  comment             = "Hot Updater CloudFront distribution"
  default_root_object = "index.html"
  is_ipv6_enabled     = true

  enabled = true

  default_cache_behavior {
    allowed_methods        = ["GET", "HEAD"]
    cached_methods         = ["GET", "HEAD"]
    target_origin_id       = "scc-hot-updater-storage"
    viewer_protocol_policy = "redirect-to-https"

    forwarded_values {
      headers                 = []
      query_string            = false
      query_string_cache_keys = []

      cookies {
        forward           = "none"
        whitelisted_names = []
      }
    }

    lambda_function_association {
      event_type   = "viewer-request"
      include_body = false
      lambda_arn   = "arn:aws:lambda:us-east-1:291889421067:function:hot-updater-edge:1"
    }
  }

  ordered_cache_behavior {
    allowed_methods = [
      "GET",
      "HEAD",
    ]
    cached_methods = [
      "GET",
      "HEAD",
    ]
    compress               = false
    default_ttl            = 0
    max_ttl                = 0
    min_ttl                = 0
    path_pattern           = "/api/*"
    smooth_streaming       = false
    target_origin_id       = "scc-hot-updater-storage"
    trusted_key_groups     = []
    trusted_signers        = []
    viewer_protocol_policy = "redirect-to-https"

    forwarded_values {
      headers = [
        "x-app-platform",
        "x-app-version",
        "x-bundle-id",
      ]
      query_string            = false
      query_string_cache_keys = []

      cookies {
        forward           = "none"
        whitelisted_names = []
      }
    }

    grpc_config {
      enabled = false
    }

    lambda_function_association {
      event_type   = "viewer-request"
      include_body = false
      lambda_arn   = "arn:aws:lambda:us-east-1:291889421067:function:hot-updater-edge:1"
    }
  }

  viewer_certificate {
    cloudfront_default_certificate = true
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }
}

resource "aws_cloudfront_distribution" "dev_hot_updater_distribution" {
  origin {
    connection_attempts = 3
    connection_timeout  = 10
    domain_name         = "dev-scc-hot-updater-storage.s3.ap-northeast-2.amazonaws.com"
    origin_id           = "dev-scc-hot-updater-storage"
  }

  comment             = "Dev Hot Updater CloudFront distribution"
  default_root_object = "index.html"
  is_ipv6_enabled     = true

  enabled = true

  default_cache_behavior {
    allowed_methods        = ["GET", "HEAD"]
    cached_methods         = ["GET", "HEAD"]
    target_origin_id       = "dev-scc-hot-updater-storage"
    viewer_protocol_policy = "redirect-to-https"

    forwarded_values {
      headers                 = []
      query_string            = false
      query_string_cache_keys = []

      cookies {
        forward           = "none"
        whitelisted_names = []
      }
    }

    lambda_function_association {
      event_type   = "viewer-request"
      include_body = false
      lambda_arn   = "arn:aws:lambda:us-east-1:291889421067:function:dev-hot-updater-edge:1"
    }
  }

  ordered_cache_behavior {
    allowed_methods = [
      "GET",
      "HEAD",
    ]
    cached_methods = [
      "GET",
      "HEAD",
    ]
    compress               = false
    default_ttl            = 0
    max_ttl                = 0
    min_ttl                = 0
    path_pattern           = "/api/*"
    smooth_streaming       = false
    target_origin_id       = "dev-scc-hot-updater-storage"
    trusted_key_groups     = []
    trusted_signers        = []
    viewer_protocol_policy = "redirect-to-https"

    forwarded_values {
      headers = [
        "x-app-platform",
        "x-app-version",
        "x-bundle-id",
      ]
      query_string            = false
      query_string_cache_keys = []

      cookies {
        forward           = "none"
        whitelisted_names = []
      }
    }

    grpc_config {
      enabled = false
    }

    lambda_function_association {
      event_type   = "viewer-request"
      include_body = false
      lambda_arn   = "arn:aws:lambda:us-east-1:291889421067:function:dev-hot-updater-edge:1"
    }
  }

  viewer_certificate {
    cloudfront_default_certificate = true
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }
}
