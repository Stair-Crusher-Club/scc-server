resource "aws_iam_user" "hot_update_user" {
  name = "hot-update-user"
  tags = {
    "AKIAUH5PXDMFXDWJFIHW" = "hot_updater_deploy"
  }
  tags_all = {
    "AKIAUH5PXDMFXDWJFIHW" = "hot_updater_deploy"
  }
}

resource "aws_iam_user_policy_attachment" "hot_update_s3" {
  user       = aws_iam_user.hot_update_user.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonS3FullAccess"
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
