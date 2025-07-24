# scc
data "aws_iam_policy_document" "scc_dev" {
  statement {
    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type = "Federated"
      identifiers = [data.terraform_remote_state.oidc.outputs.k3s_oidc_arn]
    }

    condition {
      test     = "StringEquals"
      variable = "k3s.staircrusher.club:sub"
      values = ["system:serviceaccount:dev:scc-server"]
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

data "aws_iam_policy_document" "scc_dev_accessibility_thumbnails_full_access" {
  statement {
    actions = [
      "s3:*",
    ]
    resources = [
      aws_s3_bucket.dev_accessibility_thumbnails.arn,
      "${aws_s3_bucket.dev_accessibility_thumbnails.arn}/*",
    ]
  }
}

data "aws_iam_policy_document" "scc_dev_home_banners_full_access" {
  statement {
    actions = [
      "s3:*",
    ]
    resources = [
      aws_s3_bucket.dev_home_banners.arn,
      "${aws_s3_bucket.dev_home_banners.arn}/*",
    ]
  }
}

data "aws_iam_policy_document" "scc_dev_crusher_labels_full_access" {
  statement {
    actions = [
      "s3:*",
    ]
    resources = [
      aws_s3_bucket.dev_crusher_labels.arn,
      "${aws_s3_bucket.dev_crusher_labels.arn}/*",
    ]
  }
}

data "aws_iam_policy_document" "scc_dev_rekognition_access" {
  statement {
    actions = [
      "rekognition:DetectFaces",
    ]
    resources = ["*"]
  }
}

data "aws_iam_policy_document" "scc_dev_common_queue_full_access" {
  statement {
    actions = [
      "sqs:*",
    ]
    resources = [aws_sqs_queue.dev_common_queue.arn]
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

resource "aws_iam_policy" "scc_dev_accessibility_thumbnails_full_access" {
  name   = "scc-dev-accessibility-thumbnails-full-access"
  policy = data.aws_iam_policy_document.scc_dev_accessibility_thumbnails_full_access.json
}

resource "aws_iam_policy" "scc_dev_home_banners_full_access" {
  name   = "scc-dev-home-banners-full-access"
  policy = data.aws_iam_policy_document.scc_dev_home_banners_full_access.json
}

resource "aws_iam_policy" "scc_dev_crusher_labels_full_access" {
  name   = "scc-dev-crusher-labels-full-access"
  policy = data.aws_iam_policy_document.scc_dev_crusher_labels_full_access.json
}

resource "aws_iam_policy" "scc_dev_rekognition_access" {
  name   = "scc-dev-rekognition-access"
  policy = data.aws_iam_policy_document.scc_dev_rekognition_access.json
}

resource "aws_iam_policy" "scc_dev_common_queue_full_access" {
  name   = "scc-dev-common-queue-full-access"
  policy = data.aws_iam_policy_document.scc_dev_common_queue_full_access.json
}

resource "aws_iam_role_policy_attachment" "scc_dev_accessibility_images_full_access" {
  role       = aws_iam_role.scc_dev.name
  policy_arn = aws_iam_policy.scc_dev_accessibility_images_full_access.arn
}

resource "aws_iam_role_policy_attachment" "scc_dev_accessibility_thumbnails_full_access" {
  role       = aws_iam_role.scc_dev.name
  policy_arn = aws_iam_policy.scc_dev_accessibility_thumbnails_full_access.arn
}

resource "aws_iam_role_policy_attachment" "scc_dev_home_banners_full_access" {
  role       = aws_iam_role.scc_dev.name
  policy_arn = aws_iam_policy.scc_dev_home_banners_full_access.arn
}

resource "aws_iam_role_policy_attachment" "scc_dev_crusher_labels_full_access" {
  role       = aws_iam_role.scc_dev.name
  policy_arn = aws_iam_policy.scc_dev_crusher_labels_full_access.arn
}

resource "aws_iam_role_policy_attachment" "scc_dev_rekognition_access" {
  role       = aws_iam_role.scc_dev.name
  policy_arn = aws_iam_policy.scc_dev_rekognition_access.arn
}

resource "aws_iam_role_policy_attachment" "scc_dev_common_queue_full_access" {
  role       = aws_iam_role.scc_dev.name
  policy_arn = aws_iam_policy.scc_dev_common_queue_full_access.arn
}

data "aws_iam_policy_document" "scc_deploy_secret_dev" {
  statement {
    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type = "Federated"
      identifiers = [data.terraform_remote_state.oidc.outputs.k3s_oidc_arn]
    }

    condition {
      test     = "StringEquals"
      variable = "k3s.staircrusher.club:sub"
      values = ["system:serviceaccount:dev:scc-server-deploy-secret"]
    }
  }
}

data "aws_iam_policy_document" "scc_deploy_secret_dev_kms_access" {
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

data "aws_iam_policy_document" "scc_deploy_secret_dev_ecr_pull_access" {
  statement {
    actions = ["ecr:GetAuthorizationToken"]
    resources = ["*"]
  }

  statement {
    sid    = "AllowImagePull"
    actions = [
      "ecr:BatchCheckLayerAvailability",
      "ecr:GetDownloadUrlForLayer",
      "ecr:BatchGetImage"
    ]
    # Restrict permissions to a specific repository for security
    resources = [
      data.terraform_remote_state.ecr.outputs.scc_server_repository_arn,
      data.terraform_remote_state.ecr.outputs.scc_admin_frontend_repository_arn
    ]
  }
}

resource "aws_iam_role" "scc_deploy_secret_dev" {
  name               = "scc-deploy-secret-dev"
  assume_role_policy = data.aws_iam_policy_document.scc_deploy_secret_dev.json
}

resource "aws_iam_policy" "scc_deploy_secret_dev_kms_access" {
  name   = "scc-deploy-secret-dev-kms-access"
  policy = data.aws_iam_policy_document.scc_deploy_secret_dev_kms_access.json
}

resource "aws_iam_policy" "scc_deploy_secret_dev_ecr_pull_access" {
  name   = "scc-deploy-secret-dev-ecr-pull-access"
  policy = data.aws_iam_policy_document.scc_deploy_secret_dev_ecr_pull_access.json
}

resource "aws_iam_role_policy_attachment" "scc_deploy_secret_dev_kms_read_access" {
  role       = aws_iam_role.scc_deploy_secret_dev.name
  policy_arn = aws_iam_policy.scc_deploy_secret_dev_kms_access.arn
}

resource "aws_iam_role_policy_attachment" "scc_deploy_secret_dev_ecr_pull_access" {
  role       = aws_iam_role.scc_deploy_secret_dev.name
  policy_arn = aws_iam_policy.scc_deploy_secret_dev_ecr_pull_access.arn
}
