resource "aws_kms_key" "sops" {
  description         = "KMS key for SOPS encryption"
  enable_key_rotation = true
}

# resource "aws_kms_grant" "sops" {
#   name              = "sops"
#   key_id            = aws_kms_key.sops.key_id
#   grantee_principal = aws_iam_role.sops.arn
#   operations        = ["Encrypt", "Decrypt", "GenerateDataKey"]
# }

resource "aws_kms_alias" "sops" {
  name          = "alias/sops"
  target_key_id = aws_kms_key.sops.key_id
}

