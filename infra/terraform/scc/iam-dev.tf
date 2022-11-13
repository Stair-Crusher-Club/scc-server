# scc
data "aws_iam_policy_document" "scc_dev" {
  statement {
    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type        = "Federated"
      identifiers = [data.terraform_remote_state.oidc.outputs.k3s_oidc_arn]
    }

    condition {
      test     = "StringEquals"
      variable = "k3s.staircrusher.club:sub"
      values   = ["system:serviceaccount:dev:scc-server"]
    }
  }
}

data "aws_iam_policy_document" "scc_dev_accessibility_images_full_access" {
  statement {
    actions = [
      "s3:*",
    ]
    resources = [
      aws_s3_bucket.dev_accessibility_images.arn,
      "${aws_s3_bucket.dev_accessibility_images.arn}/*",
    ]
  }
}

resource "aws_iam_role" "scc_dev" {
  name               = "scc-dev"
  assume_role_policy = data.aws_iam_policy_document.scc_dev.json
}

resource "aws_iam_policy" "scc_dev_accessibility_images_full_access" {
  name   = "scc-dev-accsseibility-images-pull-access"
  policy = data.aws_iam_policy_document.scc_dev_accessibility_images_full_access.json
}

resource "aws_iam_role_policy_attachment" "scc_dev_accessibility_images_full_access" {
  role       = aws_iam_role.scc_dev.name
  policy_arn = aws_iam_policy.scc_dev_accessibility_images_full_access.arn
}

data "aws_iam_policy_document" "scc_deploy_secret_dev" {
  statement {
    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type        = "Federated"
      identifiers = [data.terraform_remote_state.oidc.outputs.k3s_oidc_arn]
    }

    condition {
      test     = "StringEquals"
      variable = "k3s.staircrusher.club:sub"
      values   = ["system:serviceaccount:dev:scc-server-deploy-secret"]
    }
  }
}

data "aws_iam_policy_document" "scc_deploy_secret_dev_kms_read_access" {
  statement {
    actions = [
      "kms:DescribeCustomKeyStores",
      "kms:DescribeKey",
      "kms:GetKeyPolicy",
      "kms:GetKeyRotationStatus",
      "kms:GetParametersForImport",
      "kms:GetPublicKey"
    ]
    resources = [
      data.terraform_remote_state.kms.outputs.sops_kms_key_arn
    ]
  }
}

resource "aws_iam_role" "scc_deploy_secret_dev" {
  name               = "scc-deploy-secret-dev"
  assume_role_policy = data.aws_iam_policy_document.scc_deploy_secret_dev.json
}

resource "aws_iam_policy" "scc_deploy_secret_dev_kms_read_access" {
  name   = "scc-deploy-secret-dev-kms-read-access"
  policy = data.aws_iam_policy_document.scc_deploy_secret_dev_kms_read_access.json
}

resource "aws_iam_role_policy_attachment" "scc_deploy_secret_dev_kms_read_access" {
  role       = aws_iam_role.scc_dev.name
  policy_arn = aws_iam_policy.scc_deploy_secret_dev_kms_read_access.arn
}
