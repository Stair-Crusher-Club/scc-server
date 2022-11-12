# external secret
data "aws_iam_policy_document" "external_secret" {
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

  statement {
    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type        = "Federated"
      identifiers = [data.terraform_remote_state.oidc.outputs.k3s_oidc_arn]
    }

    condition {
      test     = "StringEquals"
      variable = "k3s.staircrusher.club:sub"
      values   = ["system:serviceaccount:scc:scc-server"]
    }
  }
}

data "aws_iam_policy_document" "secret_manager_read" {
  statement {
    actions = [
      "secretsmanager:GetResourcePolicy",
      "secretsmanager:GetSecretValue",
      "secretsmanager:DescribeSecret",
      "secretsmanager:ListSecretVersionIds",
    ]
    resources = [data.terraform_remote_state.secret_manager.outputs.scc_secret_manager_arn]
  }
}

resource "aws_iam_role" "external_secret" {
  name               = "external-secret"
  assume_role_policy = data.aws_iam_policy_document.external_secret.json
}

resource "aws_iam_policy" "secret_manager_read" {
  name   = "secret_manager_read"
  policy = data.aws_iam_policy_document.secret_manager_read.json
}

resource "aws_iam_role_policy_attachment" "external_secret_role_attach" {
  role       = aws_iam_role.external_secret.name
  policy_arn = aws_iam_policy.secret_manager_read.arn
}

# scc
data "aws_iam_policy_document" "scc" {
  statement {
    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type        = "Federated"
      identifiers = [data.terraform_remote_state.oidc.outputs.k3s_oidc_arn]
    }

    condition {
      test     = "StringEquals"
      variable = "k3s.staircrusher.club:sub"
      values   = ["system:serviceaccount:scc:scc-server"]
    }
  }
}

data "aws_iam_policy_document" "scc_accessibility_images_full_access" {
  statement {
    actions = [
      "s3:*",
    ]
    resources = [
      aws_s3_bucket.accessibility_images.arn,
      "${aws_s3_bucket.accessibility_images.arn}/*",
    ]
  }
}

resource "aws_iam_role" "scc" {
  name               = "scc"
  assume_role_policy = data.aws_iam_policy_document.scc.json
}

resource "aws_iam_policy" "scc_accessibility_images_full_access" {
  name   = "scc-accsseibility-images-pull-access"
  policy = data.aws_iam_policy_document.scc_accessibility_images_full_access.json
}

resource "aws_iam_role_policy_attachment" "scc_accessibility_images_full_access" {
  role       = aws_iam_role.scc.name
  policy_arn = aws_iam_policy.scc_accessibility_images_full_access.arn
}
