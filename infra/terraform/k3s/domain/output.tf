output "k3s_oidc_arn" {
  value = aws_iam_openid_connect_provider.k3s_oidc.arn
}
