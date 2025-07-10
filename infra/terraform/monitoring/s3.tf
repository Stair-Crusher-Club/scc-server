resource "aws_s3_bucket" "scc_loki_storage" {
  bucket = "scc-loki-storage"
}

resource "aws_s3_bucket_lifecycle_configuration" "scc_loki_retention" {
  bucket = aws_s3_bucket.scc_loki_storage.id

  rule {
    id     = "scc-loki-data-expiration"
    status = "Enabled"

    filter {
      prefix = "fake/"
    }

    expiration {
      days = 30
    }
  }

  rule {
    id     = "scc-loki-index-expiration"
    status = "Enabled"

    filter {
      prefix = "index/"
    }

    expiration {
      days = 30
    }
  }
}
