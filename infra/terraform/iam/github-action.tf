resource "aws_iam_openid_connect_provider" "github_action" {
  url             = "https://token.actions.githubusercontent.com"
  client_id_list  = ["sts.amazonaws.com"]
  thumbprint_list = ["f879abce0008e4eb126e0097e46620f5aaae26ad"]
}

resource "aws_iam_policy" "github_action_ci_cd_policy" {
  name   = "github-action-ci-cd-policy"
  policy = jsonencode({
    "Version": "2012-10-17",
    "Statement": [
      {
        "Effect": "Allow",
        "Action": [
          "ecr-public:DescribeImageTags",
          "ecr-public:DescribeImages",
          "ecr-public:UploadLayerPart",
          "ecr-public:UntagResource",
          "ecr-public:GetRepositoryCatalogData",
          "ecr-public:TagResource",
          "ecr-public:CompleteLayerUpload",
          "ecr-public:GetRepositoryPolicy",
          "ecr-public:InitiateLayerUpload",
          "ecr-public:DescribeRepositories",
          "ecr-public:PutImage",
          "ecr-public:ListTagsForResource",
          "ecr-public:BatchCheckLayerAvailability"
        ],
        "Resource": "arn:aws:ecr-public::833004893731:repository/*"
      },
      {
        "Effect": "Allow",
        "Action": [
          "ecr-public:GetAuthorizationToken",
          "sts:GetServiceBearerToken"
        ],
        "Resource": "*"
      }
    ]
  })
}

resource "aws_iam_role" "github_action_ci_cd" {
  name               = "github-action-ci-cd"
  assume_role_policy = jsonencode({
    "Version": "2012-10-17",
    "Statement": [
      {
        "Effect": "Allow",
        "Principal": {
          "Federated": aws_iam_openid_connect_provider.github_action.arn
        },
        "Action": "sts:AssumeRoleWithWebIdentity",
        "Condition": {
          "StringLike": {
            "token.actions.githubusercontent.com:sub": "repo:Stair-Crusher-Club/scc-server:*"
          },
          "StringEquals": {
            "token.actions.githubusercontent.com:aud": "sts.amazonaws.com"
          }
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "github_action_ci_cd" {
  role       = aws_iam_role.github_action_ci_cd.name
  policy_arn = aws_iam_policy.github_action_ci_cd_policy.arn
}
