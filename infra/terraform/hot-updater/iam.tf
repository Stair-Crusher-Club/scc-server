resource "aws_iam_user" "hot_update_user" {
  name = "hot-update-user"
}

resource "aws_iam_policy" "hot_updater_s3_policy" {
  name = "hot-updater-s3-policy"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:PutObject",
          "s3:ListBucket",
          "s3:DeleteObject"
        ]
        Resource = [
          aws_s3_bucket.prod_scc_hot_updater_storage.arn,
          "${aws_s3_bucket.prod_scc_hot_updater_storage.arn}/*",
          aws_s3_bucket.dev_scc_hot_updater_storage.arn,
          "${aws_s3_bucket.dev_scc_hot_updater_storage.arn}/*"
        ]
      }
    ]
  })
}

resource "aws_iam_user_policy_attachment" "hot_update_s3" {
  user       = aws_iam_user.hot_update_user.name
  policy_arn = aws_iam_policy.hot_updater_s3_policy.arn
}

resource "aws_iam_role" "hot_updater_edge" {
  name = "hot-updater-edge-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Service = [
            "lambda.amazonaws.com",
            "edgelambda.amazonaws.com",
          ]
        }
        Action = "sts:AssumeRole"
      }
    ]
  })
  description = "Role for Lambda@Edge to access S3"
}
