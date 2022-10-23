data "external" "thumbprint" {
  program = [
    "${path.module}/thumbprint.sh",
    data.terraform_remote_state.lightsail.outputs.k3s_ip,
  ]
}

resource "aws_iam_openid_connect_provider" "k3s_oidc" {
  url             = "https://k3s.staircrusher.club"
  client_id_list  = ["sts.amazonaws.com"]
  thumbprint_list = [data.external.thumbprint.result.thumbprint]
}
