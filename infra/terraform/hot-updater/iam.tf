resource "aws_iam_user" "hot_update_user" {
  name = "hot-update-user"
}

resource "aws_iam_user_policy_attachment" "hot_update_s3" {
  user       = aws_iam_user.hot_update_user.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonS3FullAccess"
}

resource "aws_iam_user_policy_attachment" "hot_update_lambda" {
  user       = aws_iam_user.hot_update_user.name
  policy_arn = "arn:aws:iam::aws:policy/AWSLambda_FullAccess"
}

resource "aws_iam_user_policy_attachment" "hot_update_cloudfront" {
  user       = aws_iam_user.hot_update_user.name
  policy_arn = "arn:aws:iam::aws:policy/CloudFrontFullAccess"
}

resource "aws_iam_user_policy_attachment" "hot_update_iam" {
  user       = aws_iam_user.hot_update_user.name
  policy_arn = "arn:aws:iam::aws:policy/IAMFullAccess"
}
