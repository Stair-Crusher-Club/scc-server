# scc
data "aws_iam_policy_document" "scc" {
  statement {
    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type = "Federated"
      identifiers = [data.terraform_remote_state.oidc.outputs.k3s_oidc_arn]
    }

    condition {
      test     = "StringEquals"
      variable = "k3s.staircrusher.club:sub"
      values = ["system:serviceaccount:scc:scc-server"]
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

data "aws_iam_policy_document" "scc_accessibility_thumbnails_full_access" {
  statement {
    actions = [
      "s3:*",
    ]
    resources = [
      aws_s3_bucket.accessibility_thumbnails.arn,
      "${aws_s3_bucket.accessibility_thumbnails.arn}/*",
    ]
  }
}

data "aws_iam_policy_document" "scc_home_banners_full_access" {
  statement {
    actions = [
      "s3:*",
    ]
    resources = [
      aws_s3_bucket.home_banners.arn,
      "${aws_s3_bucket.home_banners.arn}/*",
    ]
  }
}

data "aws_iam_policy_document" "scc_partner_labels_full_access" {
  statement {
    actions = [
      "s3:*",
    ]
    resources = [
      aws_s3_bucket.partner_labels.arn,
      "${aws_s3_bucket.partner_labels.arn}/*",
    ]
  }
}

data "aws_iam_policy_document" "scc_rekognition_access" {
  statement {
    actions = [
      "rekognition:DetectFaces",
    ]
    resources = ["*"]
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

resource "aws_iam_policy" "scc_accessibility_thumbnails_full_access" {
  name   = "scc-accessibility-thumbnails-full-access"
  policy = data.aws_iam_policy_document.scc_accessibility_thumbnails_full_access.json
}

resource "aws_iam_policy" "scc_home_banners_full_access" {
  name   = "scc-home-banners-full-access"
  policy = data.aws_iam_policy_document.scc_home_banners_full_access.json
}

resource "aws_iam_policy" "scc_partner_labels_full_access" {
  name   = "scc-partner-labels-full-access"
  policy = data.aws_iam_policy_document.scc_partner_labels_full_access.json
}

resource "aws_iam_policy" "scc_rekognition_access" {
  name   = "scc-rekognition-access"
  policy = data.aws_iam_policy_document.scc_rekognition_access.json
}

resource "aws_iam_role_policy_attachment" "scc_accessibility_images_full_access" {
  role       = aws_iam_role.scc.name
  policy_arn = aws_iam_policy.scc_accessibility_images_full_access.arn
}

resource "aws_iam_role_policy_attachment" "scc_accessibility_thumbnails_full_access" {
  role       = aws_iam_role.scc.name
  policy_arn = aws_iam_policy.scc_accessibility_thumbnails_full_access.arn
}

resource "aws_iam_role_policy_attachment" "scc_home_banners_full_access" {
  role       = aws_iam_role.scc.name
  policy_arn = aws_iam_policy.scc_home_banners_full_access.arn
}

resource "aws_iam_role_policy_attachment" "scc_rekognition_access" {
  role       = aws_iam_role.scc.name
  policy_arn = aws_iam_policy.scc_rekognition_access.arn
}

data "aws_iam_policy_document" "scc_deploy_secret" {
  statement {
    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type = "Federated"
      identifiers = [data.terraform_remote_state.oidc.outputs.k3s_oidc_arn]
    }

    condition {
      test     = "StringEquals"
      variable = "k3s.staircrusher.club:sub"
      values = [
        "system:serviceaccount:scc:scc-server-deploy-secret",
        "system:serviceaccount:scc-redash:scc-redash-deploy-secret",
      ]
    }
  }
}

data "aws_iam_policy_document" "scc_deploy_secret_kms_access" {
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

resource "aws_iam_role" "scc_deploy_secret" {
  name               = "scc-deploy-secret"
  assume_role_policy = data.aws_iam_policy_document.scc_deploy_secret.json
}

resource "aws_iam_policy" "scc_deploy_secret_kms_access" {
  name   = "scc-deploy-secret-kms-access"
  policy = data.aws_iam_policy_document.scc_deploy_secret_kms_access.json
}

resource "aws_iam_role_policy_attachment" "scc_deploy_secret_kms_read_access" {
  role       = aws_iam_role.scc_deploy_secret.name
  policy_arn = aws_iam_policy.scc_deploy_secret_kms_access.arn
}
