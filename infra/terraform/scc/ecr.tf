data "aws_iam_policy_document" "scc_server_ecr_pull_access" {
  statement {
    actions = ["ecr:GetAuthorizationToken"]
    resources = ["*"]
  }

  statement {
    actions = [
      "ecr:BatchCheckLayerAvailability",
      "ecr:GetDownloadUrlForLayer",
      "ecr:BatchGetImage"
    ]
    resources = [
      data.terraform_remote_state.ecr.outputs.scc_server_repository_arn,
      "${data.terraform_remote_state.ecr.outputs.scc_server_repository_arn}/*"
    ]
  }
}

resource "aws_iam_policy" "scc_server_ecr_pull_access" {
  name   = "scc-server-ecr-pull-access"
  policy = data.aws_iam_policy_document.scc_server_ecr_pull_access.json
}

data "aws_iam_policy_document" "scc_admin_frontend_ecr_pull_access" {
  statement {
    actions = ["ecr:GetAuthorizationToken"]
    resources = ["*"]
  }

  statement {
    actions = [
      "ecr:BatchCheckLayerAvailability",
      "ecr:GetDownloadUrlForLayer",
      "ecr:BatchGetImage"
    ]
    resources = [
      data.terraform_remote_state.ecr.outputs.scc_admin_frontend_repository_arn,
      "${data.terraform_remote_state.ecr.outputs.scc_admin_frontend_repository_arn}/*"
    ]
  }
}

resource "aws_iam_policy" "scc_admin_frontend_ecr_pull_access" {
  name   = "scc-admin-frontend-ecr-pull-access"
  policy = data.aws_iam_policy_document.scc_admin_frontend_ecr_pull_access.json
}
