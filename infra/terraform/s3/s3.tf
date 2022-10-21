resource "aws_s3_bucket" "scc_tf_remote_state" {
  bucket = "scc-tf-remote-state"
}

resource "aws_s3_bucket" "k3s_oidc" {
  bucket = "scc-k3s-oidc"
}

output "k3s_oidc_endpoint" {
  value = "s3-${aws_s3_bucket.k3s_oidc.region}.amazonaws.com/${aws_s3_bucket.k3s_oidc.bucket}"
}
