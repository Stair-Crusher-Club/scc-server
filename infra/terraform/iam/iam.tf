# iam policy documents

data "aws_iam_policy_document" "developer" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "AWS"
      identifiers = ["arn:aws:iam::${var.account_id}:root"]
    }
  }
}

# Permission to rotate api keys
data "aws_iam_policy_document" "rotate_keys" {
  statement {
    actions = [
      "iam:*LoginProfile",
      "iam:*AccessKey*",
      "iam:*SSHPublicKey*",
    ]
    resources = [
      "arn:aws:iam::${var.account_id}:user/&{aws:username}",
    ]
  }

  statement {
    actions = [
      "iam:ListAccount*",
      "iam:GetAccountSummary",
      "iam:GetAccountPasswordPolicy",
      "iam:ListUsers",
    ]
    resources = [
      "*",
    ]
  }
}

# Permission to self managed MFA
data "aws_iam_policy_document" "self_manage_mfa" {
  statement {
    actions = [
      "iam:*VirtualMFADevice",
    ]
    resources = [
      "arn:aws:iam::${var.account_id}:mfa/&{aws:username}"
    ]
  }

  statement {
    actions = [
      "iam:GetAccountPasswordPolicy",
    ]
    resources = [
      "*",
    ]
  }

  statement {
    actions = [
      "iam:ChangePassword",
    ]
    resources = [
      "arn:aws:iam::${var.account_id}:user/&{aws:username}"
    ]
  }

  statement {
    actions = [
      "iam:DeactivateMFADevice",
      "iam:EnableMFADevice",
      "iam:ListMFADevices",
      "iam:ResyncMFADevice",
    ]

    resources = [
      "arn:aws:iam::${var.account_id}:user/&{aws:username}",
    ]
  }

  statement {
    actions = [
      "iam:ListVirtualMFADevices",
    ]
    resources = [
      "arn:aws:iam::${var.account_id}:mfa/*"
    ]
  }

  statement {
    actions = [
      "iam:ListUsers"
    ]
    resources = [
      "arn:aws:iam::${var.account_id}:user/*",
    ]
  }
}

# Force user to use MFA for security issue
data "aws_iam_policy_document" "force_mfa" {
  statement {
    effect = "Deny"
    not_actions = [
      "iam:*",
    ]
    resources = [
      "*",
    ]
    condition {
      test     = "Null"
      variable = "aws:MultiFactorAuthAge"
      values   = ["true"]
    }
  }
}

data "aws_iam_policy_document" "assume_role" {
  statement {
    actions = [
      "sts:AssumeRole",
    ]
    resources = [
      "arn:aws:iam::${var.account_id}:role/*",
    ]
  }
}

# iam group for developers
resource "aws_iam_group" "developer" {
  name = "developer"
}

resource "aws_iam_user" "developer" {
  for_each = toset(var.developer)
  name     = each.key
}

resource "aws_iam_group_membership" "developer" {
  name  = aws_iam_group.developer.name
  users = [for k, v in aws_iam_user.developer : v.name]
  group = aws_iam_group.developer.name
}

resource "aws_iam_policy" "rotate_keys" {
  name   = "rotate_keys"
  policy = data.aws_iam_policy_document.rotate_keys.json
}

resource "aws_iam_policy" "self_manage_mfa" {
  name   = "self_manage_mfa"
  policy = data.aws_iam_policy_document.self_manage_mfa.json
}

resource "aws_iam_policy" "force_mfa" {
  name   = "force_mfa"
  policy = data.aws_iam_policy_document.force_mfa.json
}

resource "aws_iam_policy" "assume_role" {
  name   = "assume_role"
  policy = data.aws_iam_policy_document.assume_role.json
}

resource "aws_iam_group_policy_attachment" "rotate_keys" {
  group      = aws_iam_group.developer.name
  policy_arn = aws_iam_policy.rotate_keys.arn
}

resource "aws_iam_group_policy_attachment" "self_manage_mfa" {
  group      = aws_iam_group.developer.name
  policy_arn = aws_iam_policy.self_manage_mfa.arn
}

resource "aws_iam_group_policy_attachment" "force_mfa" {
  group      = aws_iam_group.developer.name
  policy_arn = aws_iam_policy.force_mfa.arn
}

resource "aws_iam_group_policy_attachment" "assume_role" {
  group      = aws_iam_group.developer.name
  policy_arn = aws_iam_policy.assume_role.arn
}

resource "aws_iam_role" "developer" {
  name               = "developer"
  assume_role_policy = data.aws_iam_policy_document.developer.json
}

resource "aws_iam_role_policy_attachment" "devloper" {
  role       = aws_iam_role.developer.name
  policy_arn = "arn:aws:iam::aws:policy/AdministratorAccess"
}
