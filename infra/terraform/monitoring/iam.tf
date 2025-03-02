data "aws_iam_policy_document" "loki" {
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

resource "aws_iam_role" "loki" {
  name               = "loki"
  assume_role_policy = data.aws_iam_policy_document.loki.json
}

resource "aws_iam_policy" "scc_loki_storage_full_access" {
  name   = "scc-loki-storage-full-access"
  policy = data.aws_iam_policy_document.scc_loki_storage_full_access.json
}

resource "aws_iam_role_policy_attachment" "scc_loki_storage_full_access" {
  role       = aws_iam_role.loki.name
  policy_arn = aws_iam_policy.scc_loki_storage_full_access.arn
}
