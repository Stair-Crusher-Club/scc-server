resource "aws_iam_openid_connect_provider" "k3s_oidc" {
  url             = "https://s3-ap-northeast-2.amazonaws.com/scc-k3s-oidc"
  client_id_list  = ["sts.amazonaws.com"]
  thumbprint_list = ["9e99a48a9960b14926bb7f3b02e22da2b0ab7280"]
}
