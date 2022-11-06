data "aws_iam_policy_document" "external_secret_role_policy" {
  statement {
    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type        = "Federated"
      identifiers = [data.terraform_remote_state.oidc.outputs.k3s_oidc_arn]
    }

    condition {
      test     = "StringEquals"
      variable = "k3s.staircrusher.club:sub"
      values   = ["system:serviceaccount:external-secret:external-secret"]
    }
  }
}

data "aws_iam_policy_document" "read_secret_manager" {
  statement {
    actions = [
      "secretsmanager:GetResourcePolicy",
      "secretsmanager:GetSecretValue",
      "secretsmanager:DescribeSecret",
      "secretsmanager:ListSecretVersionIds",
    ]
    resources = [data.terraform_remote_state.secret_manager.outputs.scc_secret_manager_arn]
  }
  statement {
    actions   = ["secretsmanager:ListSecrets"]
    resources = ["*"]
  }
}

resource "aws_iam_role" "external_secret_role" {
  name               = "external_secret_role"
  assume_role_policy = data.aws_iam_policy_document.external_secret_role_policy.json
}

resource "aws_iam_policy" "read_secret_manager" {
  name   = "read_secret_manager"
  policy = data.aws_iam_policy_document.read_secret_manager.json
}

resource "aws_iam_role_policy_attachment" "external_secret_role_attach" {
  role       = aws_iam_role.external_secret_role.name
  policy_arn = aws_iam_policy.read_secret_manager.arn
}
