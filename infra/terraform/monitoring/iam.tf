data "aws_iam_policy_document" "monitoring" {
  statement {
    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type = "Federated"
      identifiers = [data.terraform_remote_state.oidc.outputs.k3s_oidc_arn]
    }

    condition {
      test     = "StringEquals"
      variable = "k3s.staircrusher.club:sub"
      values = ["system:serviceaccount:monitoring:loki"]
    }
  }
}

data "aws_iam_policy_document" "scc_loki_storage_full_access" {
  statement {
    actions = [
      "s3:*",
    ]
    resources = [
      aws_s3_bucket.scc_loki_storage.arn,
      "${aws_s3_bucket.scc_loki_storage.arn}/*",
    ]
  }
}

resource "aws_iam_role" "monitoring" {
  name               = "monitoring"
  assume_role_policy = data.aws_iam_policy_document.monitoring.json
}

resource "aws_iam_policy" "scc_loki_storage_full_access" {
  name   = "scc-loki-storage-full-access"
  policy = data.aws_iam_policy_document.scc_loki_storage_full_access.json
}

resource "aws_iam_role_policy_attachment" "scc_loki_storage_full_access" {
  role       = aws_iam_role.monitoring.name
  policy_arn = aws_iam_policy.scc_loki_storage_full_access.arn
}

data "aws_iam_policy_document" "monitoring_deploy_secret" {
  statement {
    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type = "Federated"
      identifiers = [data.terraform_remote_state.oidc.outputs.k3s_oidc_arn]
    }

    condition {
      test     = "StringEquals"
      variable = "k3s.staircrusher.club:sub"
      values = ["system:serviceaccount:monitoring:monitoring-deploy-secret"]
    }
  }
}

data "aws_iam_policy_document" "monitoring_deploy_secret_kms_access" {
  statement {
    actions = [
      "kms:Encrypt",
      "kms:Decrypt",
      "kms:ReEncrypt*",
      "kms:GenerateDataKey*",
      "kms:DescribeKey"
    ]
    resources = [
      data.terraform_remote_state.kms.outputs.sops_kms_key_arn
    ]
  }
}

resource "aws_iam_role" "monitoring_deploy_secret" {
  name               = "monitoring-deploy-secret"
  assume_role_policy = data.aws_iam_policy_document.monitoring_deploy_secret.json
}

resource "aws_iam_policy" "monitoring_deploy_secret_kms_access" {
  name   = "monitoring-deploy-secret-kms-access"
  policy = data.aws_iam_policy_document.monitoring_deploy_secret_kms_access.json
}

resource "aws_iam_role_policy_attachment" "monitoring_deploy_secret_kms_read_access" {
  role       = aws_iam_role.monitoring_deploy_secret.name
  policy_arn = aws_iam_policy.monitoring_deploy_secret_kms_access.arn
}
